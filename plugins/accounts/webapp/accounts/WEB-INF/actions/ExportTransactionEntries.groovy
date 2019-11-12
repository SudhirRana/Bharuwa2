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
	
    transEntriesExportCond = []
    if (organizationPartyId) {
        transEntriesExportCond.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, organizationPartyId))
    }
    if (startDate) {
        transEntriesExportCond.add(EntityCondition.makeCondition("transactionDate", EntityOperator.GREATER_THAN_EQUAL_TO, Timestamp.valueOf(startDate)))
    }
    if (endDate) {
        transEntriesExportCond.add(EntityCondition.makeCondition("transactionDate", EntityOperator.LESS_THAN_EQUAL_TO, Timestamp.valueOf(endDate)))
    }
    if (reportType == "cdnr") {
		println "#####reportType cdnr::"+reportType;
    	transEntriesExportCond.add(EntityCondition.makeCondition("partyGstn", EntityOperator.NOT_EQUAL, null))
		transEntriesExportCond.add(EntityCondition.makeCondition("acctgTransTypeId", EntityOperator.EQUALS, "SALES_INVOICE"))
    }
    
	allReportWithHeader = [];
    transEntriesList = [];
    transEntriesList = from("TransactionEntriesExport").where(transEntriesExportCond).queryList()
    transEntryRecords = [];
	gstReportHeader = [];
    
    if (transEntriesList) {
    	gstTaxRateMap = [:];
		invoiceTotalMap = [:]; invoiceTotalVal = 0;
		invoiceAllRecipients = [:]; invoiceTotalRecpt = 0
		invoiceCount = 0;totalTaxableVal = 0;
		for (GenericValue invoiceGstRecord : transEntriesList) {
    	//transEntriesList.each { invoiceGstRecord ->
    			gstReportMap = [:]
    			invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceGstRecord.invoiceId).queryOne();
    			//println "########::Total InvoiceValue with Tax::"+InvoiceWorker.getInvoiceTotal(delegator, invoiceGstRecord.invoiceId);
    			//println "########::Total InvoiceValue without Tax::"+InvoiceWorker.getInvoiceNoTaxTotal(invoice);
    			//println "########::getShippingAddress::"+InvoiceWorker.getShippingAddress(invoice);
				gstReportMap.acctgTransId = invoiceGstRecord.acctgTransId
				gstReportMap.partyGstn = invoiceGstRecord.partyGstn
				gstReportMap.partyName = invoiceGstRecord.partyName
			    gstReportMap.receiptId = invoiceGstRecord.invoiceId
			    gstReportMap.receiptDate = UtilDateTime.timeStampToString(invoiceGstRecord.transactionDate, "dd-MMMM-yyyy", timeZone, locale)
				gstReportMap.voucherNo = invoiceGstRecord.invoiceId
				gstReportMap.voucherDate =""
				gstReportMap.refundAmount = 0
				gstReportMap.appTaxRatePer = ""
				gstReportMap.refundAmount = 0
			    gstReportMap.invoiceType = "Regular"//invoiceGstRecord.invoiceTypeId
			    gstReportMap.preGST = ""
			    gstReportMap.cessAmount = 0
				
				//gstReportMap.type = "OE"
				//gstReportMap.Description = invoiceGstRecord.description
				//gstReportMap.partyIdFrom = invoiceGstRecord.partyIdFrom
			    
				shipToGeoId = InvoiceWorker.getShippingAddress(invoice);
				totalTaxAmount = BigDecimal.ZERO;
				transactionEntriesList = EntityQuery.use(delegator).from("AcctgTransEntry").where("acctgTransId", invoiceGstRecord.acctgTransId).queryList();
				for (GenericValue transEntries : transactionEntriesList) {
					if(transEntries.roleTypeId == "TAX_AUTHORITY") {
						totalTaxAmount = totalTaxAmount +transEntries.amount;
					}
				}
				
				for (GenericValue transEntries : transactionEntriesList) {
					documentType = transEntries.debitCreditFlag;
					if(transEntries.roleTypeId != "TAX_AUTHORITY") {
						if(transEntries.productId != null) {
							//Todo : need to see from which entity we have to get the voucher entries.
						}
					}
				}
				
				if(UtilValidate.isEmpty(shipToGeoId )) {
					shipToGeoId = InvoiceWorker.getBillToAddress(invoice).get("stateProvinceGeoId")
				}
				invoiceGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipToGeoId).queryOne();
				gstReportMap.shipToGeoId = invoiceGeo.geoName
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

    	}
		//println "::::invoiceCount:::"+invoiceCount;
		//println "::::invoiceTotalVal:::"+invoiceTotalVal;
		//println "::::invoiceTotalRecpt:::"+invoiceTotalRecpt;
		
    }
	gstReportHeader.add(UtilMisc.toMap("partyGstn","Summary For B2B(4)","partyName","","invoiceId","","invoiceDate","","invoiceAmount","","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","","cessAmount",""))
	gstReportHeader.add(UtilMisc.toMap("partyGstn","No. of Recipients","partyName","","invoiceId","No. of Invoices","invoiceDate","","invoiceAmount","Total Invoice Value","shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount","Total Taxable Value","cessAmount","Total Cess"))
	gstReportHeader.add(UtilMisc.toMap("partyGstn",invoiceTotalRecpt,"partyName","","invoiceId",invoiceCount,"invoiceDate","","invoiceAmount",invoiceTotalVal,"shipToGeoId","","reverseCharges","","appTaxRatePer","","invoiceType","","ecomGSTN","","taxRate","","taxAmount",totalTaxableVal,"cessAmount","0"))
	//allReportWithHeader.addAll(gstReportHeader);
	allReportWithHeader.addAll(transEntryRecords);
	
    context.allReportWithHeader = allReportWithHeader
