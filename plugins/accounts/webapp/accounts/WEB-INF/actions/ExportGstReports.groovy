import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityUtil
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.UtilMisc
import org.apache.ofbiz.accounting.invoice.InvoiceWorker
import java.sql.Timestamp

	organizationPartyId = request.getParameter("organizationPartyId")
    startDate = parameters.startDate
    endDate = parameters.endDate
    organizationPartyId = parameters.organizationPartyId
    reportType = parameters.reportType
    //print "############organizationPartyId:::"+organizationPartyId + "####reportType::"+reportType
	
    invoiceGstExportCond = []
    if (organizationPartyId) {
        invoiceGstExportCond.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, organizationPartyId))
    }
    if (startDate) {
        invoiceGstExportCond.add(EntityCondition.makeCondition("invoiceDate", EntityOperator.GREATER_THAN_EQUAL_TO, Timestamp.valueOf(startDate)))
    }
    if (endDate) {
        invoiceGstExportCond.add(EntityCondition.makeCondition("invoiceDate", EntityOperator.LESS_THAN_EQUAL_TO, Timestamp.valueOf(endDate)))
    }
	invoiceGstExportCond.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "SALES_INVOICE"))
	
    if (reportType == "b2b") {
		println "#####reportType b2b::"+reportType;
    	invoiceGstExportCond.add(EntityCondition.makeCondition("partyGstn", EntityOperator.NOT_EQUAL, null))
    }else if(reportType == "b2cl") {
		/*
		 * Invoices for Taxable outward supplies to consumers where 
		 * a)The place of supply is outside the state where the supplier is registered and 
		 * b)The total invoice value is more that Rs 2,50,000
		 */
		
		println "#####reportType b2cl::"+reportType;
		invoiceGstExportCond.add(EntityCondition.makeCondition("taxAuth", EntityOperator.EQUALS, "IND_IGST")) // IGST means placeofsupply outside.
	}else if(reportType == "b2cs") {
		/*
		 * Supplies made to consumers and unregistered persons of the following nature
		 * a) Intra-State: any value b) Inter-State: Invoice value Rs 2.5 lakh or less
		 */
		
		println "#####reportType b2cs::"+reportType;
		invoiceGstExportCond.add(EntityCondition.makeCondition("taxAuth", EntityOperator.EQUALS, "IND_IGST")) // IGST means placeofsupply outside.
	}
    
    invoiceGstExportCond.add(EntityCondition.makeCondition("taxAuthRate", EntityOperator.NOT_EQUAL, null))
    
	allReportWithHeader = [];
    invoiceGstExportList = [];
    invoiceGstExportList = from("InvoiceGstExport").where(invoiceGstExportCond).queryList()
    gstInvoiceRecords = [];
	gstReportHeader = [];
    
    if (invoiceGstExportList) {
    	gstTaxRateMap = [:];
		invoiceTotalMap = [:]; invoiceTotalVal = 0;
		invoiceAllRecipients = [:]; invoiceTotalRecpt = 0;
		invoiceCount = 0;totalTaxableVal = 0;
		for (GenericValue invoiceGstRecord : invoiceGstExportList) {
    	//invoiceGstExportList.each { invoiceGstRecord ->
    			gstReportMap = [:]
    			invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceGstRecord.invoiceId).queryOne();
    			//println "########::Total InvoiceValue with Tax::"+InvoiceWorker.getInvoiceTotal(delegator, invoiceGstRecord.invoiceId);
    			//println "########::Total InvoiceValue without Tax::"+InvoiceWorker.getInvoiceNoTaxTotal(invoice);
    			//println "########::getShippingAddress::"+InvoiceWorker.getShippingAddress(invoice);
				gstReportMap.partyGstn = invoiceGstRecord.partyGstn
				gstReportMap.partyName = invoiceGstRecord.partyName
			    gstReportMap.invoiceId = invoiceGstRecord.invoiceId
			    gstReportMap.invoiceDate = UtilDateTime.timeStampToString(invoiceGstRecord.invoiceDate, "dd-MMMM-yyyy", timeZone, locale)
			    gstReportMap.invoiceType = "Regular"//invoiceGstRecord.invoiceTypeId
			    gstReportMap.Description = invoiceGstRecord.description
			    gstReportMap.partyIdFrom = invoiceGstRecord.partyIdFrom
			    gstReportMap.reverseCharges = "N"
			    gstReportMap.ecomGSTN = ""
			    gstReportMap.cessAmount = 0
				gstReportMap.type = "OE"
			    
				shipToGeoId = InvoiceWorker.getShippingAddress(invoice);
				isDuplicateInvoice = false;
				if(!UtilValidate.isEmpty(invoice.getString("referenceNumber")) && invoice.getString("referenceNumber").contains("Invoice id:")) {
					String originalInvoiceId = invoice.getString("referenceNumber").substring(invoice.getString("referenceNumber").lastIndexOf(' ') + 1);
					//println originalInvoiceId+"####newInvoiceId"+invoice.getString("invoiceId");
					if(!invoice.getString("invoiceId").equals(originalInvoiceId)) {
						isDuplicateInvoice = true;
						println reportType+"####add ::isDuplicateInvoice: true"+invoice.getString("invoiceId");
						GenericValue orgInvoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", originalInvoiceId).queryOne();
						if(orgInvoice != null) {
							gstReportMap.orgInvoiceId = orgInvoice.invoiceId;
							gstReportMap.orgInvoiceDate = UtilDateTime.timeStampToString(orgInvoice.invoiceDate, "dd-MMMM-yyyy", timeZone, locale);
							if(reportType == "b2cla") {
								String orgShipToGeoId = InvoiceWorker.getShippingAddress(orgInvoice);
								if(orgShipToGeoId != null) {
									orgShipToGeoId = InvoiceWorker.getBillToAddress(invoice).get("stateProvinceGeoId")
									orgInvoiceGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipToGeoId).queryOne();
									gstReportMap.orgShipToGeoId = orgInvoiceGeo.geoName;
								}
							}
							if(reportType == "gstr1b2csa") {
								gstReportMap.orgFinancialYear =UtilDateTime.timeStampToString(orgInvoice.invoiceDate, "yyyy", timeZone, locale);
								gstReportMap.orgMonth = UtilDateTime.timeStampToString(orgInvoice.invoiceDate, "MMM", timeZone, locale);
							}
							
						}
					}else {
						isDuplicateInvoice = false;
					}
				}
				
				if(UtilValidate.isEmpty(shipToGeoId )) {
					shipToGeoId = InvoiceWorker.getBillToAddress(invoice).get("stateProvinceGeoId")
				}
				invoiceGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipToGeoId).queryOne();
				
				gstReportMap.shipToGeoId = invoiceGeo.geoName
			    totInvoiceValue = InvoiceWorker.getInvoiceTotal(delegator, invoiceGstRecord.invoiceId);
				if(isDuplicateInvoice && reportType == "b2cla" && totInvoiceValue.compareTo(new BigDecimal("250000")) == -1) {
					continue;
				}else if(isDuplicateInvoice && reportType == "b2csa" && (invoiceGstRecord.taxAuth == "IND_IGST" && totInvoiceValue.compareTo(new BigDecimal("250000"))) == 1) {
					continue;
				}else if(!isDuplicateInvoice && reportType == "b2ba") {
					continue;
					println reportType+"####add amendment::totInvoiceValue:"+totInvoiceValue;
				}else if(!isDuplicateInvoice && reportType == "b2cl" && totInvoiceValue.compareTo(new BigDecimal("250000")) == -1) {
					continue;
				}else if(!isDuplicateInvoice && reportType == "b2cs" && (invoiceGstRecord.taxAuth == "IND_IGST" && totInvoiceValue.compareTo(new BigDecimal("250000"))) == 1) {
					continue;
				}else if(isDuplicateInvoice && reportType == "b2b") {
					continue;
					//println reportType+"####add ::totInvoiceValue:"+totInvoiceValue;
				}

				
				
				
				
			    gstReportMap.invoiceAmount = totInvoiceValue
				invoiceTaxRateKey = invoiceGstRecord.invoiceId +invoiceGstRecord.taxAuthRate;
			    if(invoiceGstRecord.invoiceItemTypeId == "ITM_SALES_TAX"){
			    	if(!gstTaxRateMap.containsKey(invoiceTaxRateKey)){
			    		gstTaxRateMap.put(invoiceTaxRateKey,invoiceGstRecord.amount)
						gstReportMap.taxAmount = InvoiceWorker.getInvoiceTaxableAmtForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate)//InvoiceWorker.getInvoiceTaxTotalForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate);
						//println invoiceGstRecord.invoiceId+"####if####gstReportMap.taxAmount::"+gstReportMap.taxAmount + "::taxRate"+invoiceGstRecord.taxAuthRate;
						gstReportMap.taxRate = invoiceGstRecord.taxAuthRate.replace("TAX_SLAB_", "")
			    		gstInvoiceRecords.add(gstReportMap)
						totalTaxableVal = totalTaxableVal+gstReportMap.taxAmount;
						
						invoiceAndAmtKey = invoiceGstRecord.invoiceId;
						if(!invoiceTotalMap.containsKey(invoiceAndAmtKey)){
							invoiceTotalMap.put(invoiceAndAmtKey,totInvoiceValue)
							if(invoiceCount == 0) {
								invoiceTotalVal = totInvoiceValue
								invoiceCount = 1;
							}else {
								invoiceTotalVal = invoiceTotalVal +totInvoiceValue
								invoiceCount = invoiceCount +1;
							}
						}
						if(!invoiceAllRecipients.containsKey(invoiceGstRecord.partyId)){
							invoiceAllRecipients.put(invoiceGstRecord.partyId,invoiceGstRecord.partyName)
							if(invoiceTotalRecpt == 0) {
								invoiceTotalRecpt = 1
							}else {
								invoiceTotalRecpt = invoiceTotalRecpt + 1;
							}
						}
						
			    	}
			    }
			    
    	}
		//println "::::invoiceCount:::"+invoiceCount;
		//println "::::invoiceTotalVal:::"+invoiceTotalVal;
		//println "::::invoiceTotalRecpt:::"+invoiceTotalRecpt;
		gstReportHeader.add(UtilMisc.toMap("partyGstn","Summary For B2B(4)","partyName","","invoiceId","","invoiceDate","","invoiceAmount","","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","","cessAmount",""))
		gstReportHeader.add(UtilMisc.toMap("partyGstn","No. of Recipients","partyName","","invoiceId","No. of Invoices","invoiceDate","","invoiceAmount","Total Invoice Value","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","Total Taxable Value","cessAmount","Total Cess"))
		gstReportHeader.add(UtilMisc.toMap("partyGstn",invoiceTotalRecpt,"partyName","","invoiceId",invoiceCount,"invoiceDate","","invoiceAmount",invoiceTotalVal,"shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount",totalTaxableVal,"cessAmount","0"))
		//gstReportHeader.add(UtilMisc.toMap("partyGstn","GSTIN/UIN of Recipient","partyName","Receiver Name","invoiceId","Invoice Number","invoiceDate","Invoice date","invoiceAmount","invoice Value","shipToGeoId","Place Of Supply","reverseCharges","Reverse Charge","appTaxRatePer","Applicable % of Tax Rate","invoiceType","Invoice Type","ecomGSTN","E-Commerce GSTIN","taxRate","Rate","taxAmount","Taxable Value","cessAmount","Cess Amount"))
		allReportWithHeader.addAll(gstReportHeader);
		allReportWithHeader.addAll(gstInvoiceRecords);
    	context.allReportWithHeader = allReportWithHeader
    }
	
