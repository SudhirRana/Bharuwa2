import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.util.EntityUtil
import org.apache.ofbiz.base.util.UtilDateTime
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.base.util.UtilMisc
import org.apache.ofbiz.accounting.invoice.InvoiceWorker

import java.math.BigDecimal
import java.sql.Timestamp
import src.main.java.org.patanjali.CommonWorker;

	organizationPartyId = request.getParameter("organizationPartyId")
    startDate = parameters.startDate
    endDate = parameters.endDate
    organizationPartyId = parameters.organizationPartyId
    reportType = parameters.reportType
    println "#####>>>#######organizationPartyId:::"+organizationPartyId + "####reportType::"+reportType
	
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
	invoiceGstExportCond.add(EntityCondition.makeCondition("invoiceTypeId", EntityOperator.EQUALS, "PURCHASE_INVOICE"))
	
   /* if (reportType == "b2b") {
		println "#####reportType b2b::"+reportType;
    	invoiceGstExportCond.add(EntityCondition.makeCondition("partyGstn", EntityOperator.NOT_EQUAL, null))
    }
  */  
    invoiceGstExportCond.add(EntityCondition.makeCondition("taxAuthRate", EntityOperator.NOT_EQUAL, null))
    
	allReportWithHeader = [];
    invoiceGstExportList = [];
    invoiceGstExportList = from("InvoiceGstExport").where(invoiceGstExportCond).queryList()
    gstInvoiceRecords = [];
	gstReportHeader = [];
    
    if (invoiceGstExportList) {
    	gstTaxRateMap = [:];
		invoiceTotalMap = [:]; invoiceTotalVal = 0;
		invoiceAllRecipients = [:]; invoiceTotalRecpt = 0
		invoiceCount = 0;totalTaxableVal = 0;
		for (GenericValue invoiceGstRecord : invoiceGstExportList) {
    	//invoiceGstExportList.each { invoiceGstRecord ->
    			gstReportMap = [:]
    			invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceGstRecord.invoiceId).queryOne();
    			//println "########::Total InvoiceValue with Tax::"+InvoiceWorker.getInvoiceTotal(delegator, invoiceGstRecord.invoiceId);
    			//println "########::Total InvoiceValue without Tax::"+InvoiceWorker.getInvoiceNoTaxTotal(invoice);
    			//println "########::getShippingAddress::"+InvoiceWorker.getShippingAddress(invoice);
				supplierGstn = CommonWorker.getPartyGstn(delegator, invoiceGstRecord.partyIdFrom);
				supplierName = CommonWorker.getPartyName(delegator, invoiceGstRecord.partyIdFrom);
				 
				if(reportType == "b2b" && supplierGstn == "") {
					println invoiceGstRecord.invoiceId+"########::supplierGstn::"+supplierGstn;
					continue;
				} 
				if(reportType == "b2bur" && supplierGstn != ""){
					println invoiceGstRecord.invoiceId+"########::supplierGstn::"+supplierGstn;
					continue;
				}
				gstReportMap.supplierName = supplierName
				gstReportMap.partyGstn = supplierGstn
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
				
				gstReportMap.integratedTaxPaid = ""
				gstReportMap.centralTaxPaid = ""
				gstReportMap.stateUTTax = ""
				gstReportMap.eligibilityItc = ""
				
				
				//TODO : Need to check what values to get.
				gstReportMap.availItcIntegratedTax = ""
				gstReportMap.availItcCentralTax = ""
				gstReportMap.availItcStateTax = ""
				gstReportMap.availItcCess = ""
				
				//New fields
				
							    
				shipToGeoId = InvoiceWorker.getShippingAddress(invoice);
				if(UtilValidate.isEmpty(shipToGeoId )) {
					shipToGeoId = InvoiceWorker.getBillToAddress(invoice).get("stateProvinceGeoId")
				}
				invoiceGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipToGeoId).queryOne();
				
				gstReportMap.shipToGeoId = invoiceGeo.geoName
			    totInvoiceValue = InvoiceWorker.getInvoiceTotal(delegator, invoiceGstRecord.invoiceId);
				
			    gstReportMap.invoiceAmount = totInvoiceValue
				invoiceTaxRateKey = invoiceGstRecord.invoiceId +invoiceGstRecord.taxAuthRate;
				invoiceTaxAuthKey = invoiceGstRecord.invoiceId +invoiceGstRecord.taxAuth;
			    if(invoiceGstRecord.invoiceItemTypeId == "PITM_SALES_TAX"){
			    	if(!gstTaxRateMap.containsKey(invoiceTaxRateKey)){
			    		gstTaxRateMap.put(invoiceTaxRateKey,invoiceGstRecord.amount)
						gstReportMap.taxAmount = InvoiceWorker.getInvoiceTaxableAmtForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate)//InvoiceWorker.getInvoiceTaxTotalForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate);
						totalTaxValue= InvoiceWorker.getInvoiceTaxTotalForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate)//InvoiceWorker.getInvoiceTaxTotalForTaxAuthRate(invoice,invoiceGstRecord.taxAuthRate);
						//println invoiceGstRecord.invoiceId+"####if####gstReportMap.taxAmount::"+gstReportMap.taxAmount + "::taxRate"+invoiceGstRecord.taxAuthRate;
						//println invoiceGstRecord.taxAuth+">>>>>>>>>>>>getInvoiceTaxTotalForTaxAuthRate>>"+InvoiceWorker.getInvoiceTaxTotalForTaxAuthRate(invoice, invoiceGstRecord.taxAuthRate);
						if(!gstTaxRateMap.containsKey(invoiceTaxAuthKey)){
							
							gstTaxRateMap.put(invoiceTaxAuthKey,totalTaxValue)
							if("IND_IGST" == invoiceGstRecord.taxAuth) {
								println invoiceGstRecord.taxAuth+">>"+invoiceGstRecord.invoiceId+">>>>>>>>111>>>>>totalTaxValue::"+totalTaxValue;
								gstReportMap.integratedTaxPaid = totalTaxValue;
								gstReportMap.supplyType = "Inter State";
							}
							if("IND_CGST" == invoiceGstRecord.taxAuth || "IND_SGST"== invoiceGstRecord.taxAuth ) {
								println invoiceGstRecord.taxAuth+">>"+invoiceGstRecord.invoiceId+">>>>>>>>222>>>>>>totalTaxValue::"+totalTaxValue;
								gstReportMap.centralTaxPaid = totalTaxValue.divide(new BigDecimal("2"), 3,2 );
								gstReportMap.stateUTTax = totalTaxValue.divide(new BigDecimal("2"), 3,2 )
								gstReportMap.supplyType = "Intra State";
							}
						}
						
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
		
    }
	gstReportHeader.add(UtilMisc.toMap("partyGstn","Summary For B2B(4)","partyName","","invoiceId","","invoiceDate","","invoiceAmount","","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","","cessAmount",""))
	gstReportHeader.add(UtilMisc.toMap("partyGstn","No. of Recipients","partyName","","invoiceId","No. of Invoices","invoiceDate","","invoiceAmount","Total Invoice Value","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","Total Taxable Value","cessAmount","Total Cess"))
	gstReportHeader.add(UtilMisc.toMap("partyGstn",invoiceTotalRecpt,"partyName","","invoiceId",invoiceCount,"invoiceDate","","invoiceAmount",invoiceTotalVal,"shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount",totalTaxableVal,"cessAmount","0"))
	//gstReportHeader.add(UtilMisc.toMap("partyGstn","GSTIN/UIN of Recipient","partyName","Receiver Name","invoiceId","Invoice Number","invoiceDate","Invoice date","invoiceAmount","invoice Value","shipToGeoId","Place Of Supply","reverseCharges","Reverse Charge","appTaxRatePer","Applicable % of Tax Rate","invoiceType","Invoice Type","ecomGSTN","E-Commerce GSTIN","taxRate","Rate","taxAmount","Taxable Value","cessAmount","Cess Amount"))
	allReportWithHeader.addAll(gstReportHeader);
	allReportWithHeader.addAll(gstInvoiceRecords);
	
    context.allReportWithHeader = allReportWithHeader
