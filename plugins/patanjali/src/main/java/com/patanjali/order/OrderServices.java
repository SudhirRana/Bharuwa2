package com.patanjali.order;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.GeneralException;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityConditionList;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityTypeUtil;
import org.apache.ofbiz.party.contact.ContactMechWorker;
import org.apache.ofbiz.party.party.PartyWorker;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class OrderServices {
	
	public static String module = OrderServices.class.getName();
	public static final String resource = "AccountingUiLabels";
	public static final String SAMPLE_XLSX_FILE_PATH = System.getProperty("ofbiz.home")+"/plugins/patanjali/dtd/xlsfiles/Po.xlsx";
	public static final String STOCK_XLSX_FILE_PATH = System.getProperty("ofbiz.home")+"/plugins/patanjali/dtd/xlsfiles/Stock.xlsx";
	public static final String JOURNAL_XLSX_FILE_PATH = System.getProperty("ofbiz.home")+"/plugins/patanjali/dtd/xlsfiles/Journal.xlsx";
    public static Map<String, Object> readFileAndConvertToMap(){
    	System.out.println("=======SAMPLE_XLSX_FILE_PATH===="+SAMPLE_XLSX_FILE_PATH);
        Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File(SAMPLE_XLSX_FILE_PATH));
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        Map<String, Object> orderMap = new HashMap<String, Object>();
        List<Map<String, Object>> orderList = new ArrayList<Map<String, Object>>();
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(row.getRowNum() == 0)continue;
            if(sheet.getRow(row.getRowNum() -1).getCell(1).getStringCellValue() != sheet.getRow(row.getRowNum()).getCell(1).getStringCellValue()) {
            	orderList = new ArrayList<Map<String, Object>>();
            }
            Map<String, Object> orderItems = new HashMap<String, Object>();
            orderItems.put("productId", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(4)));
            orderItems.put("productName", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(5)));
            orderItems.put("materialCenter", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(6)));
            orderItems.put("quantity", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(7)));
            orderItems.put("unit", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(8)));
            orderItems.put("price", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(9)));
            orderItems.put("amount", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(10)));
            orderList.add(orderItems);
        	String poorderId = sheet.getRow(row.getRowNum()).getCell(1).getStringCellValue();
        	Map<String, Object> orderDetailMap = new HashMap<String, Object>();
        	orderDetailMap.put("orderDate", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(0)));
        	orderDetailMap.put("orderId", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(1)));
        	orderDetailMap.put("supplierId", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(2)));
        	orderDetailMap.put("supplierName", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(3)));
        	orderDetailMap.put("orderItems", orderList);
        	orderMap.put(poorderId, orderDetailMap);
        }
        try {
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        Debug.logInfo("===========Number Of orders in this file=========="+orderMap.size(), module);
        return orderMap;
    }
    
    @SuppressWarnings("unchecked")
	public static Map<String, Object> importOrderFromXLS(DispatchContext ctx, Map<String, ? extends Object> context){
    	Delegator delegator = ctx.getDelegator();
    	LocalDispatcher dispatcher = ctx.getDispatcher();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
		Map<String, Object> orderMap = readFileAndConvertToMap();
        for (Map.Entry<String,Object> entry : orderMap.entrySet()) {
        	String orderId = entry.getKey();
        	GenericValue orderHeader;
			try {
				orderHeader = EntityQuery.use(delegator).from("OrderHeader").where("orderId", orderId.trim()).queryOne();
			} catch (GenericEntityException e1) {
				e1.printStackTrace();
				return ServiceUtil.returnError(e1.getMessage());
			}
        	if(orderHeader != null) continue;
        	Map<String, Object> orderDetail = (Map<String, Object>) entry.getValue();
        	Map <String, Object> inMap = new HashMap<>();
        	inMap.put("partyId", "patanjali");
        	inMap.put("orderId", orderId);
        	inMap.put("orderTypeId", "PURCHASE_ORDER");
        	inMap.put("currencyUom", "INR");
        	inMap.put("productStoreId","PATANJALI_STORE");
        	inMap.put("orderDate", UtilDateTime.toDate((String)orderDetail.get("orderDate"), "00:00:00"));

        	List<Map<String, Object>> items = (List<Map<String, Object>>) orderDetail.get("orderItems");
        	int totalItems = items.size();
        	Map<String, Object> serviceContext = makeTaxContext(items, delegator, (String)orderDetail.get("supplierId"));
        	List<List<? extends Object>> taxReturn = null;
			try {
				taxReturn = getTaxAdjustments(dispatcher, "calcTax", serviceContext);
			} catch (GeneralException e1) {
				e1.printStackTrace();
			}

            if (Debug.verboseOn()) {
                Debug.logVerbose("ReturnList: " + taxReturn, module);
            }
            //List<GenericValue> orderAdj = UtilGenerics.checkList(taxReturn.get(0));
            List<List<GenericValue>> itemAdj = UtilGenerics.checkList(taxReturn.get(1));
            
            // add all of the item adjustments to this list too
            int orderAdjustmentSeqId = 1;
            for (int i = 0; i < totalItems; i++) {
                Collection<GenericValue> adjs = itemAdj.get(i);
                if (adjs != null) {
                    for (GenericValue orderAdjustment: adjs) {
                        orderAdjustment.set("orderItemSeqId", "0000"+String.valueOf(orderAdjustmentSeqId));
                    }
                }
                orderAdjustmentSeqId ++;
            }
            List <GenericValue> orderAdjustments = new LinkedList<>();
            for(List<GenericValue> itemAdjList : itemAdj) {
            	for(GenericValue orderAdjustment : itemAdjList) {
            		orderAdjustments.add(orderAdjustment);
            	}
            }
        	List <GenericValue> orderItems = new LinkedList<>();
        	int orderItemSeqId = 1;
        	for(Map<String, Object> item : items) {
        		GenericValue orderItem = delegator.makeValue("OrderItem", UtilMisc.toMap("orderItemSeqId", "0000"+String.valueOf(orderItemSeqId), "orderItemTypeId", "PRODUCT_ORDER_ITEM", "productId", item.get("productId"), "quantity", new BigDecimal(((String)item.get("quantity")).replaceAll(",", "")), "isPromo", "N"));
                orderItem.set("unitPrice", new BigDecimal(((String)item.get("price")).replaceAll(",", "")));
                orderItem.set("unitListPrice", BigDecimal.ZERO);
                orderItem.set("isModifiedPrice", "N");
                orderItem.set("statusId", "ITEM_CREATED");
                orderItems.add(orderItem);
                orderItemSeqId ++;
        	}
        	
        	inMap.put("orderItems", orderItems);
        	
        	GenericValue partyContactMech = PartyWorker.findPartyLatestContactMech((String)orderDetail.get("supplierId"), "POSTAL_ADDRESS", delegator);
        	if(partyContactMech != null) {
        		GenericValue orderContactMech = delegator.makeValue("OrderContactMech", UtilMisc.toMap("contactMechPurposeTypeId", "SHIPPING_LOCATION", "contactMechId", partyContactMech.get("contactMechId")));
                List <GenericValue> orderContactMechs = new LinkedList<>();
                orderContactMechs.add(orderContactMech);
                inMap.put("orderContactMechs", orderContactMechs);
                
//                GenericValue orderItemContactMech = delegator.makeValue("OrderItemContactMech", UtilMisc.toMap("contactMechPurposeTypeId", "SHIPPING_LOCATION", "contactMechId", partyContactMech.get("contactMechId"), "orderItemSeqId", "00001"));
//                List <GenericValue> orderItemContactMechs = new LinkedList<>();
//                orderItemContactMechs.add(orderItemContactMech);
//                inMap.put("orderItemContactMechs", orderItemContactMechs);
        	}

            GenericValue orderItemShipGroup = delegator.makeValue("OrderItemShipGroup", UtilMisc.toMap("carrierPartyId", "patanjali", "contactMechId", "9000", "isGift", "N", "maySplit", "N", "shipGroupSeqId", "00001", "shipmentMethodTypeId", "LOCAL_DELIVERY"));
            orderItemShipGroup.set("carrierRoleTypeId","CARRIER");
            List <GenericValue> orderItemShipGroupInfo = new LinkedList<>();
            orderItemShipGroupInfo.add(orderItemShipGroup);
            inMap.put("orderItemShipGroupInfo", orderItemShipGroupInfo);

            List <GenericValue> orderTerms = new LinkedList<>();
            inMap.put("orderTerms", orderTerms);

            inMap.put("orderAdjustments", orderAdjustments);

            inMap.put("billToCustomerPartyId", "patanjali");
            inMap.put("billFromVendorPartyId", orderDetail.get("supplierId"));
            inMap.put("shipFromVendorPartyId", "patanjali");
            inMap.put("supplierAgentPartyId", orderDetail.get("supplierId"));
            inMap.put("userLogin", userLogin);
            
            Map<String, Object> resp;
    		try {
    			resp = dispatcher.runSync("storeOrder", inMap);
    		} catch (GenericServiceException e) {
    			e.printStackTrace();
    			return ServiceUtil.returnError(e.getMessage());
    		}
            if (ServiceUtil.isError(resp)) {
                Debug.logError(ServiceUtil.getErrorMessage(resp), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
            }
            
//            try {
//                OrderChangeHelper.orderStatusChanges(dispatcher, userLogin, orderId, "ORDER_APPROVED", null, "ITEM_APPROVED", null);
//            } catch (GenericServiceException e) {
//                Debug.logError(e, "Service invocation error, status changes were not updated for order #" + orderId, module);
//                return ServiceUtil.returnError(e.getMessage());
//            }
        }
    	return ServiceUtil.returnSuccess();
    }
    
    private static Map<String, Object> makeTaxContext(List<Map<String, Object>> items, Delegator delegator, String vendorPartyId){
    	int totalItems = items.size();
    	List<GenericValue> product = new ArrayList<>(totalItems);
        List<BigDecimal> amount = new ArrayList<>(totalItems);
        List<BigDecimal> price = new ArrayList<>(totalItems);
        List<BigDecimal> quantity = new ArrayList<>(totalItems);
        List<BigDecimal> shipAmt = new ArrayList<>(totalItems);
        for (int i = 0; i < totalItems; i++) {
    		try {
				product.add(i, EntityQuery.use(delegator).from("Product").where("productId", items.get(i).get("productId")).queryOne());
			} catch (GenericEntityException e1) {
				e1.printStackTrace();
			}
            amount.add(i, (new BigDecimal(((String)items.get(i).get("price")).replaceAll(",",""))).multiply(new BigDecimal(((String)items.get(i).get("quantity")).replaceAll(",",""))));
            price.add(i, new BigDecimal(((String)items.get(i).get("price")).replaceAll(",","")));
            quantity.add(i, new BigDecimal(((String)items.get(i).get("quantity")).replaceAll(",","")));
            shipAmt.add(i, BigDecimal.ZERO); // no per item shipping yet
    	}
        
        GenericValue shipAddress = null;
        GenericValue facilityContactMech = ContactMechWorker.getFacilityContactMechByPurpose(delegator, "PATANJALI_WAREHOUSE", UtilMisc.toList("SHIP_ORIG_LOCATION", "PRIMARY_LOCATION"));
        if (facilityContactMech != null) {
            try {
                shipAddress = EntityQuery.use(delegator).from("PostalAddress").where("contactMechId", facilityContactMech.getString("contactMechId")).queryOne();
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }

        // if shippingAddress is still null then don't calculate tax; it may be an situation where no tax is applicable, or the data is bad and we don't have a way to find an address to check tax for
        if (shipAddress == null) {
            Debug.logWarning("Not calculating tax for new order because there is no shipping address, no billing address, and no address on the origin facility [PATANJALI_WAREHOUSE]", module);
        }
        
    	Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("productStoreId", "PATANJALI_STORE");
        serviceContext.put("payToPartyId", vendorPartyId);
        serviceContext.put("billToPartyId", "patanjali");
        serviceContext.put("itemProductList", product);
        serviceContext.put("itemAmountList", amount);
        serviceContext.put("itemPriceList", price);
        serviceContext.put("itemQuantityList", quantity);
        serviceContext.put("itemShippingList", shipAmt);
        serviceContext.put("orderShippingAmount", BigDecimal.ZERO);
        serviceContext.put("shippingAddress", shipAddress);

        return serviceContext;
    }
    
 // Calc the tax adjustments.
    private static List<List<? extends Object>> getTaxAdjustments(LocalDispatcher dispatcher, String taxService, Map<String, Object> serviceContext) throws GeneralException {
        Map<String, Object> serviceResult = null;

        try {
            serviceResult = dispatcher.runSync(taxService, serviceContext);
            if (ServiceUtil.isError(serviceResult)) {
                String errorMessage = ServiceUtil.getErrorMessage(serviceResult);
                Debug.logError(errorMessage, module);
                throw new GeneralException(errorMessage);
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            throw new GeneralException("Problem occurred in tax service (" + e.getMessage() + ")", e);
        }
        // the adjustments (returned in order) from taxware.
        List<GenericValue> orderAdj = UtilGenerics.checkList(serviceResult.get("orderAdjustments"));
        List<List<GenericValue>> itemAdj = UtilGenerics.checkList(serviceResult.get("itemAdjustments"));

        return UtilMisc.toList(orderAdj, itemAdj);
    }
    
    static String ch1 = "";
    private static String pw(int n,String ch, int start)
	  {
    	//23.45
    	//23 ruppe 1
    	String ch11="";
    	//23 ruppe and
	    String  one[]={" "," One"," Two"," Three"," Four"," Five"," Six"," Seven"," Eight"," Nine"," Ten"," Eleven"," Twelve"," Thirteen"," Fourteen","Fifteen"," Sixteen"," Seventeen"," Eighteen"," Nineteen"};
	 
	    String ten[]={" "," "," Twenty"," Thirty"," Forty"," Fifty"," Sixty"," Seventy"," Eighty"," Ninety"};
	    if(n > 19) {
	    	System.out.print(ten[n/10]+" "+one[n%10]);
	    	
	    	if(start ==1){
	    		ch1 = ch1+ten[n/10]+" "+one[n%10];
	    	}else{
	    		ch1 = ch1+ten[n/10]+" "+one[n%10];
	    	}
	    	System.out.print("--------19---------ch11--------"+ch11);
    	} else { 
    		System.out.print(one[n]);
    		if(start ==1){
	    		ch1 = ch1+one[n];
	    	}else{
	    		ch1 = ch1+one[n];
	    	}
		}
	    if(n > 0){
	    	System.out.print(ch);
	    	if(start ==1){
	    		ch1 = ch1+ch;
	    	}else{
	    		ch11 = ch1+ch;
	    		ch1 = "";
	    	}
	    }
	    System.out.print("-----------------ch11--------"+ch11);
	    return ch11;
	  }

	public static String convertNumberToWords(BigDecimal number) {
		String word = "";
		int n = 0;
		int s = 0;
		String[] values = String.valueOf(number).split("\\.");
		if (values.length > 1) {
			n = Integer.valueOf(values[0]);
			if ( !values[1].startsWith("0") && !values[1].startsWith("00") ) {
				s = Integer.valueOf(values[1]);
			}
		} else {
			n = Integer.valueOf(values[0]);
		}
		if (n <= 0) {
			System.out.println("Enter numbers greater than 0");
		} else {
			word = pw((n / 1000000000), " Hundred",1);
			word = pw((n / 10000000) % 100, " Crore",1);
			word =  pw(((n / 100000) % 100), " Lakh", 1);
			word =  pw(((n / 1000) % 100), " Thousand",1);
			word =  pw(((n / 100) % 10), " Hundred",1);
			if (values.length > 1 && s > 0) {
				word =  pw((n % 100), " Rupees and",1);
			} else {
				word =  pw((n % 100), " Rupees",0);
			}
			if (values.length > 1 && s > 0) {
				word =  pw((s % 100), " Paise",0);
			}
		}
		return word;
	}
	
	public static Map<String, Object> importStockFromXLS(DispatchContext ctx, Map<String, ? extends Object> context){
    	LocalDispatcher dispatcher = ctx.getDispatcher();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
		List<Map<String, Object>> itemList = readFileAndConvertToMapForStock();
		for(Map<String, Object> item : itemList) {
			Map<String, Object> resp;
    		try {
    			item.put("userLogin", userLogin);
    			resp = dispatcher.runSync("receiveInventoryProduct", item);
    		} catch (GenericServiceException e) {
    			e.printStackTrace();
    			return ServiceUtil.returnError(e.getMessage());
    		}
            if (ServiceUtil.isError(resp)) {
                Debug.logError(ServiceUtil.getErrorMessage(resp), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
            }
    	}
    	return ServiceUtil.returnSuccess();
    }
	
	public static List<Map<String, Object>> readFileAndConvertToMapForStock(){
    	System.out.println("=======SAMPLE_XLSX_FILE_PATH===="+STOCK_XLSX_FILE_PATH);
        Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File(STOCK_XLSX_FILE_PATH));
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
        Iterator<Row> rowIterator = sheet.iterator();
        itemList = new ArrayList<Map<String, Object>>();
        while (rowIterator.hasNext()) {
        	Map<String, Object> items = new HashMap<String, Object>();
            Row row = rowIterator.next();
            if(row.getRowNum() == 0)continue;
            items.put("productId", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(0)));
            items.put("quantityAccepted", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(1)));
            items.put("unitCost", dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(2)));
            items.put("facilityId", "PATANJALI_WAREHOUSE");
            items.put("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
            items.put("datetimeReceived", UtilDateTime.nowTimestamp());
            items.put("quantityRejected", BigDecimal.ZERO);
            items.put("ownerPartyId", "patanjali");
            System.out.println("----------items---------"+items);
            itemList.add(items);
        }
        try {
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return itemList;
    }
	
	
    public static Map<String, Object> importInvoice(DispatchContext dctx, Map<String, Object> context) {
        Locale locale = (Locale) context.get("locale");
       Delegator delegator = dctx.getDelegator();
       LocalDispatcher dispatcher = dctx.getDispatcher();
       GenericValue userLogin = (GenericValue) context.get("userLogin");
       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
       
       if (fileBytes == null) {
           return ServiceUtil.returnError(UtilProperties.getMessage(resource, "AccountingUploadedFileDataNotFound", locale));
       }
       
       Debug.logInfo(":::Import Ptanjali invoice:::", module);
       String organizationPartyId = (String) context.get("organizationPartyId");
       String encoding = System.getProperty("file.encoding");
       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
       List<String> errMsgs = new LinkedList<>();
       List<String> newErrMsgs;
       String lastInvoiceId = null;
       String currentInvoiceId = null;
       String newInvoiceId = null;
       int invoicesCreated = 0;
       String partyIdFrom = "patanjali";
       String partyId = "patanjali";
       List<String> createdInvoiceIds = new LinkedList<>();
       try {
           for (final CSVRecord rec : fmt.parse(csvReader)) {
               currentInvoiceId =  rec.get("invoiceId");
               String referenceNumber = "";
               String invoiceMessage = "";
               if(!rec.get("invoiceTypeId").equals("SALES_INVOICE")) { 
            	   partyId = "patanjali";
            	   partyIdFrom = rec.get("partyId"); 
            	   referenceNumber = "PO Ref. " + rec.get("PO Ref."); 
            	   invoiceMessage = "Vch/Bill No "+ rec.get("Vch/Bill No"); 
            	   currentInvoiceId = "PI" +currentInvoiceId;
               }else{
            	   partyId = rec.get("partyId");
            	   partyIdFrom = "patanjali"; 
            	   referenceNumber = "orginal Invoice id: " + currentInvoiceId; 
               }

               if (lastInvoiceId == null || !currentInvoiceId.equals(lastInvoiceId)) {
                   newInvoiceId = null;
                   
                   Map<String, Object> invoice = UtilMisc.toMap(
                		   "invoiceId", currentInvoiceId,
                           "invoiceTypeId", rec.get("invoiceTypeId"),
                           "partyIdFrom", partyIdFrom,
                           "partyId", partyId,
                           "invoiceDate", rec.get("invoiceDate"),
                           "currencyUomId", "INR",
                           "description", rec.get("partyDetails"),
                           "referenceNumber",referenceNumber,
                           "invoiceMessage", invoiceMessage,
                           "userLogin", userLogin
                           );

                  // invoice validation
                   newErrMsgs = new LinkedList<>();
                   try {
                       /*if (UtilValidate.isEmpty(invoice.get("partyIdFrom"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory Party Id From and Party Id From Trans missing for invoice: " + currentInvoiceId);
                       } else if (EntityQuery.use(delegator).from("Party").where("partyId", invoice.get("partyIdFrom")).queryOne() == null) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": partyIdFrom: " + invoice.get("partyIdFrom") + " not found for invoice: " + currentInvoiceId);
                       }*/
                       if (UtilValidate.isEmpty(invoice.get("partyId"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory Party Id and Party Id Trans missing for invoice: " + currentInvoiceId);
                       } else if (EntityQuery.use(delegator).from("Party").where("partyId", invoice.get("partyId")).queryOne() == null) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": partyId: " + invoice.get("partyId") + " not found for invoice: " + currentInvoiceId);
                       }
                       if (UtilValidate.isEmpty(invoice.get("invoiceTypeId"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory Invoice Type missing for invoice: " + currentInvoiceId);
                       } else if (EntityQuery.use(delegator).from("InvoiceType").where("invoiceTypeId", invoice.get("invoiceTypeId")).queryOne() == null) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": InvoiceItem type id: " + invoice.get("invoiceTypeId") + " not found for invoice: " + currentInvoiceId);
                       }

                       Boolean isPurchaseInvoice = EntityTypeUtil.hasParentType(delegator, "InvoiceType", "invoiceTypeId", (String) invoice.get("invoiceTypeId"), "parentTypeId", "PURCHASE_INVOICE");
                       Boolean isSalesInvoice = EntityTypeUtil.hasParentType(delegator, "InvoiceType", "invoiceTypeId", (String) invoice.get("invoiceTypeId"), "parentTypeId", "SALES_INVOICE");
                       if (isPurchaseInvoice && !invoice.get("partyId").equals(organizationPartyId)) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": A purchase type invoice should have the partyId 'To' being the organizationPartyId(=" + organizationPartyId + ")! however is " + invoice.get("partyId") +"! invoice: " + currentInvoiceId);
                       }
                       if (isSalesInvoice && !invoice.get("partyIdFrom").equals(organizationPartyId)) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": A sales type invoice should have the partyId 'from' being the organizationPartyId(=" + organizationPartyId + ")! however is " + invoice.get("partyIdFrom") +"! invoice: " + currentInvoiceId);
                       }


                   } catch (GenericEntityException e) {
                       Debug.logError("Valication checking problem against database. due to " + e.getMessage(), module);
                   }

                   if (newErrMsgs.size() > 0) {
                       errMsgs.addAll(newErrMsgs);
                   } else {
                       Map<String, Object> invoiceResult = null;
                       try {
                           invoiceResult = dispatcher.runSync("createInvoice", invoice);
                           if (ServiceUtil.isError(invoiceResult)) {
                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(invoiceResult));
                           }
                       } catch (GenericServiceException e) {
                           csvReader.close();
                           Debug.logError(e, module);
                           return ServiceUtil.returnError(e.getMessage());
                       }
                       newInvoiceId = (String) invoiceResult.get("invoiceId");
                       createdInvoiceIds.add(newInvoiceId);
                       invoicesCreated++;
                   }
                   lastInvoiceId = currentInvoiceId;
               }


               if (newInvoiceId != null) {
            	   //DecimalFormat df = new DecimalFormat("0000");
                   Map<String, Object> invoiceItem = UtilMisc.toMap(
                           "invoiceId", newInvoiceId,
                           "invoiceItemSeqId",rec.get("invoiceItemSeqId"),
                           "invoiceItemTypeId", "INV_PROD_ITEM",
                           "productId", rec.get("productId"),
                           "description", rec.get("itemDescription"),
                           "amount", rec.get("price"),
                           "quantity", rec.get("quantity"),
                           "userLogin", userLogin
                           );
                   Debug.logInfo(":::::::##########:::::productId::"+ rec.get("productId"), module);
                   // invoice item validation
                   newErrMsgs = new LinkedList<>();
                   try {
                       if (UtilValidate.isEmpty(invoiceItem.get("invoiceItemSeqId"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory item sequence Id missing for invoice: " + currentInvoiceId);
                       }
                       if (UtilValidate.isEmpty(invoiceItem.get("invoiceItemTypeId"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory invoice item type missing for invoice: " + currentInvoiceId);
                       } else if (EntityQuery.use(delegator).from("InvoiceItemType").where("invoiceItemTypeId", invoiceItem.get("invoiceItemTypeId")).queryOne() == null) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": InvoiceItem Item type id: " + invoiceItem.get("invoiceItemTypeId") + " not found for invoice: " + currentInvoiceId + " Item seqId:" + invoiceItem.get("invoiceItemSeqId"));
                       }
                       if (UtilValidate.isEmpty(invoiceItem.get("productId")) && UtilValidate.isEmpty(invoiceItem.get("description"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": no Product Id given, no description given");
                       }
                       if (UtilValidate.isNotEmpty(invoiceItem.get("productId")) && EntityQuery.use(delegator).from("Product").where("productId", invoiceItem.get("productId")).queryOne() == null) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Product Id: " + invoiceItem.get("productId") + " not found for invoice: " + currentInvoiceId + " Item seqId:" + invoiceItem.get("invoiceItemSeqId"));
                       }
                       if (UtilValidate.isEmpty(invoiceItem.get("amount")) && UtilValidate.isEmpty(invoiceItem.get("quantity"))) {
                           newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Either or both quantity and amount is required for invoice: " + currentInvoiceId + " Item seqId:" + invoiceItem.get("invoiceItemSeqId"));
                       }
                   } catch (GenericEntityException e) {
                       Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
                   }

                   if (newErrMsgs.size() > 0) {
                       errMsgs.addAll(newErrMsgs);
                   } else {
                       try {
                           Map<String, Object> result = dispatcher.runSync("createInvoiceItem", invoiceItem);
                           Debug.logInfo(":::::::##########:::::invoiceItem:::"+invoiceItem.get("productId"), module);
                           if (ServiceUtil.isError(result)) {
                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                           }
                       } catch (GenericServiceException e) {
                           csvReader.close();
                           Debug.logError(e, module);
                           return ServiceUtil.returnError(e.getMessage());
                       }
                   }
               }
           }

       } catch (IOException e) {
           Debug.logError(e, module);
           return ServiceUtil.returnError(e.getMessage());
       }

       if (errMsgs.size() > 0) {
           return ServiceUtil.returnError(errMsgs);
       }

       Map<String, Object> result = ServiceUtil.returnSuccess(UtilProperties.getMessage(resource, "AccountingNewInvoicesCreated", UtilMisc.toMap("invoicesCreated", invoicesCreated), locale));
       result.put("organizationPartyId", organizationPartyId);
       Debug.logInfo(":::Invoice created ::"+invoicesCreated, module);
       Debug.logInfo(":::Invoices created ::"+createdInvoiceIds, module);
       for(String invoiceId :createdInvoiceIds) {
    	   try {
               Map<String, Object> addTaxResult = dispatcher.runSync("addtax", UtilMisc.toMap("invoiceId", invoiceId,"userLogin", userLogin));
               if (ServiceUtil.isError(addTaxResult)) {
                  return ServiceUtil.returnError(ServiceUtil.getErrorMessage(addTaxResult));
               }
           } catch (GenericServiceException e) {
               Debug.logError(e, module);
               return ServiceUtil.returnError(e.getMessage());
           }
    	   Debug.logInfo(":::Tax added for invoice ::"+invoiceId, module);
       }
       return result;
   }	
	
    
    public static Map<String, Object> importPaymentVouchers(DispatchContext dctx, Map<String, Object> context) {
        Locale locale = (Locale) context.get("locale");
       Delegator delegator = dctx.getDelegator();
       LocalDispatcher dispatcher = dctx.getDispatcher();
       GenericValue userLogin = (GenericValue) context.get("userLogin");
       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
       
       if (fileBytes == null) {
           return ServiceUtil.returnError(UtilProperties.getMessage(resource, "AccountingUploadedFileDataNotFound", locale));
       }
       
       Debug.logInfo(":::importPaymentVouchers:::", module);
       String organizationPartyId = (String) context.get("organizationPartyId");
       String encoding = System.getProperty("file.encoding");
       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
       List<String> errMsgs = new LinkedList<>();
       List<String> newErrMsgs;
       String lastTransactionDate = null;
       String transactionDate = null;
       String debitGlAccountId = null;
       String creditGlAccountId = null;
       String debitCreditFlag = "";
       int transCreated = 0;
       try {
           for (final CSVRecord rec : fmt.parse(csvReader)) {
        	   transactionDate =  rec.get("transactionDate");
        	   creditGlAccountId = rec.get("creditGlAccountId");
        	   debitGlAccountId = rec.get("debitGlAccountId");
        	   /*if(rec.get("transactionType").equals("PERIOD_CLOSING") && creditGlAccountId != null){
        		   debitCreditFlag = "C";
        	   }
        	   if(rec.get("transactionType").equals("PERIOD_CLOSING") && debitGlAccountId != null){
        		   debitCreditFlag = "D";
        	   }*/
               if (transactionDate != null && !UtilValidate.isEmpty(transactionDate.trim())) {
            	   Map<String, Object> acctingTransAndEntriesMap = UtilMisc.toMap(
                           "acctgTransTypeId", rec.get("transactionType"),
                           "transactionDate", rec.get("transactionDate"),
                           "voucherRef", rec.get("Vch/Bill No"),
                           "currencyUomId", "INR",
                           "organizationPartyId", "patanjali",
                           "glFiscalTypeId", "ACTUAL",
                           "amount", rec.get("amount"),
                           "userLogin", userLogin);
            	   
        		   if(debitGlAccountId != null){
        			   acctingTransAndEntriesMap.put("debitGlAccountId",debitGlAccountId);
        		   }
        		   if(creditGlAccountId != null){
        			   acctingTransAndEntriesMap.put("creditGlAccountId",creditGlAccountId);
        		   }

                   newErrMsgs = new LinkedList<>();
                   Debug.logInfo("##################acctingTransAndEntriesMap::"+acctingTransAndEntriesMap, module);
                   Debug.logInfo(":::transactionDate:::"+transactionDate+":::debitGlAccountId:::"+rec.get("debitGlAccountId")+":::creditGlAccountId:::"+rec.get("creditGlAccountId")+":::amount:::"+rec.get("amount"), module);
                   try {
                	   if(!rec.get("transactionType").equals("PERIOD_CLOSING")){
                		   if (UtilValidate.isEmpty(acctingTransAndEntriesMap.get("debitGlAccountId"))) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory debitGlAccountId missing for transaction: " + transactionDate);
		                   }else if (EntityQuery.use(delegator).from("GlAccount").where("glAccountId", acctingTransAndEntriesMap.get("debitGlAccountId")).queryOne() == null) {
		                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":debit GlAccount id: " + acctingTransAndEntriesMap.get("debitGlAccountId") + " not found for glAccount: ");
		                   }
		                   if (UtilValidate.isEmpty(acctingTransAndEntriesMap.get("creditGlAccountId"))) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory creditGlAccountId missing for transaction: " + transactionDate);
		                   } else if (EntityQuery.use(delegator).from("GlAccount").where("glAccountId", acctingTransAndEntriesMap.get("creditGlAccountId")).queryOne() == null) {
		                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":credit GlAccount id: " + acctingTransAndEntriesMap.get("creditGlAccountId") + " not found for glAccount: ");
		                   }
                	   }
	                   if (UtilValidate.isEmpty(acctingTransAndEntriesMap.get("amount")) || acctingTransAndEntriesMap.get("amount").toString().contains("-")) {
	                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory amount missing for transaction: " + transactionDate);
	                   }
	               } catch (GenericEntityException e) {
	                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
	               }
                   if (newErrMsgs.size() > 0) {
                       errMsgs.addAll(newErrMsgs);
                   } else {
                       Map<String, Object> transactionResult = null;
                       try {
                    	   transactionResult = dispatcher.runSync("quickCreateAcctgTransAndEntries", acctingTransAndEntriesMap);
                    	                      	   
                           if (ServiceUtil.isError(transactionResult)) {
                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
                           }
                           
                           String acctgTransId = (String) transactionResult.get("acctgTransId");
                           if(rec.get("transactionType").equals("PERIOD_CLOSING")){
                        	   dispatcher.runSync("updateAcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId, "isPosted","Y", "postedDate",transactionDate, "userLogin", userLogin));
                           }else{
                        	   dispatcher.runSync("postAcctgTrans", UtilMisc.toMap("acctgTransId",acctgTransId, "userLogin", userLogin));
                           }
                           
                       } catch (GenericServiceException e) {
                           csvReader.close();
                           Debug.logError(e, module);
                           return ServiceUtil.returnError(e.getMessage());
                       }
                       transCreated++;
                   }
                   lastTransactionDate = transactionDate;
               }
           }
           Debug.logInfo(":::Transactions created ::"+transCreated, module); 
       } catch (IOException e) {
           Debug.logError(e, module);
           return ServiceUtil.returnError(e.getMessage());
       }

       if (errMsgs.size() > 0) {
           return ServiceUtil.returnError(errMsgs);
       }

       Map<String, Object> result = ServiceUtil.returnSuccess();
       result.put("organizationPartyId", organizationPartyId);
       return result;
   }    
    
    public static Map<String, Object> ImportSupplierProducts(DispatchContext dctx, Map<String, Object> context) {
        Locale locale = (Locale) context.get("locale");
       Delegator delegator = dctx.getDelegator();
       LocalDispatcher dispatcher = dctx.getDispatcher();
       GenericValue userLogin = (GenericValue) context.get("userLogin");
       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
       
       if (fileBytes == null) {
           return ServiceUtil.returnError(UtilProperties.getMessage(resource, "AccountingUploadedFileDataNotFound", locale));
       }
       
       Debug.logInfo(":::ImportSupplierProducts:::", module);
       String encoding = System.getProperty("file.encoding");
       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
       List<String> errMsgs = new LinkedList<>();
       List<String> newErrMsgs;
       String productId = null;
       int transCreated = 0;
       try {
           for (final CSVRecord rec : fmt.parse(csvReader)) {
        	   productId =  rec.get("OfbizProductCode");
               if (productId != null && !UtilValidate.isEmpty(productId.trim())) {
                   Map<String, Object> supplierProductMap = UtilMisc.toMap(
                           "productId", productId,
                           "partyId", rec.get("OfbizPartyCode"),
                           "availableFromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
                           "minimumOrderQuantity", UtilValidate.isNotEmpty(rec.get("minimumOrderQuantity"))?rec.get("minimumOrderQuantity"):BigDecimal.ZERO,
                           "currencyUomId", "INR",
                           "supplierProductId", UtilValidate.isNotEmpty(rec.get("supplierProductId"))?rec.get("supplierProductId"):productId,
                           "lastPrice", UtilValidate.isNotEmpty(rec.get("unitPrice"))?rec.get("unitPrice"):BigDecimal.ONE,
                           "userLogin", userLogin
                           );

                   newErrMsgs = new LinkedList<>();
                   Debug.logInfo("##################supplierProductMap::"+supplierProductMap, module);
                   try {
                	   
	                   if (UtilValidate.isEmpty(supplierProductMap.get("productId"))) {
	                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory productId missing for entry");
	                   }else if (EntityQuery.use(delegator).from("Product").where("productId", supplierProductMap.get("productId")).queryOne() == null) {
	                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":product id: " + supplierProductMap.get("productId") + " not found for prodcut: ");
	                   }
	                   if (UtilValidate.isEmpty(supplierProductMap.get("partyId"))) {
	                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory supplier Id missing for entry");
	                   } else if (EntityQuery.use(delegator).from("Party").where("partyId", supplierProductMap.get("partyId")).queryOne() == null) {
	                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":supplier id: " + supplierProductMap.get("partyId") + " not found for supplier: ");
	                   }
	               } catch (GenericEntityException e) {
	                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
	               }
                   if (newErrMsgs.size() > 0) {
                       errMsgs.addAll(newErrMsgs);
                   } else {
                       Map<String, Object> transactionResult = null;
                       try {
                    	   transactionResult = dispatcher.runSync("createSupplierProduct", supplierProductMap);
                    	                      	   
                           if (ServiceUtil.isError(transactionResult)) {
                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
                           }
                           
                       } catch (GenericServiceException e) {
                           csvReader.close();
                           Debug.logError(e, module);
                           return ServiceUtil.returnError(e.getMessage());
                       }
                       transCreated++;
                   }
               }
           }
           Debug.logInfo(":::entery created ::"+transCreated, module); 
       } catch (IOException e) {
           Debug.logError(e, module);
           return ServiceUtil.returnError(e.getMessage());
       }

       if (errMsgs.size() > 0) {
           return ServiceUtil.returnError(errMsgs);
       }

       return ServiceUtil.returnSuccess();
   }  

	public static Map<String, Object> ImportBOMData(DispatchContext dctx, Map<String, Object> context) {
       Delegator delegator = dctx.getDelegator();
       LocalDispatcher dispatcher = dctx.getDispatcher();
       GenericValue userLogin = (GenericValue) context.get("userLogin");
       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
       
       if (fileBytes == null) {
           return ServiceUtil.returnError("Bom Uploaded File Data Not Found.");
       }
       
       Debug.logInfo(":::ImportBOMData:::", module);
       String encoding = System.getProperty("file.encoding");
       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
       List<String> errMsgs = new LinkedList<>();
       List<String> newErrMsgs;
       String currentProductId = null;
       String lastProductId = null;
       int transCreated = 0;
       try {
           for (final CSVRecord rec : fmt.parse(csvReader)) {
        	   if(UtilValidate.isNotEmpty(rec.get("Qty"))){
        		   currentProductId =  rec.get("OfbizCode").trim();
        		   lastProductId = null;
        	   }
        	   
        	   System.out.println(currentProductId+"------------------currentProductId----------------------------"+lastProductId);
        	   String productIdTo = rec.get("Type");
        	   if ((lastProductId == null || currentProductId.equals(lastProductId))) {
                   Map<String, Object> bomMap = UtilMisc.toMap(
                           "productId", currentProductId,
                           "productIdTo", productIdTo,
                           "fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
                           "productAssocTypeId", "MANUF_COMPONENT",
                           "quantity", UtilValidate.isNotEmpty(rec.get("itemQty"))?rec.get("itemQty"):BigDecimal.ONE,
                           "userLogin", userLogin
                           );
                   GenericValue checkBomExists = null;
                   try {
                	   checkBomExists = EntityQuery.use(delegator).from("ProductAssoc").where("productId", currentProductId,"productIdTo", productIdTo).queryFirst();
                   } catch (GenericEntityException e1) {
                	   e1.printStackTrace();
                   }
                   if(UtilValidate.isNotEmpty(checkBomExists))continue;
                   newErrMsgs = new LinkedList<>();
                   Debug.logInfo("##################bomMap::"+bomMap, module);
                   try {
                	   
	                   if (UtilValidate.isEmpty(bomMap.get("productId"))) {
	                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory productId missing for entry");
	                   }else if (EntityQuery.use(delegator).from("Product").where("productId", bomMap.get("productId")).queryOne() == null) {
	                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":product id: " + bomMap.get("productId") + " not found for prodcut: ");
	                   }
	                   if (UtilValidate.isEmpty(bomMap.get("productIdTo"))) {
	                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory Bom item missing for entry");
	                   }else if (EntityQuery.use(delegator).from("Product").where("productId", bomMap.get("productIdTo")).queryOne() == null) {
	                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":Bom product id: " + bomMap.get("productIdTo") + " not found for prodcut: ");
	                   }
	               } catch (GenericEntityException e) {
	                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
	               }
                   if (newErrMsgs.size() > 0) {
                       errMsgs.addAll(newErrMsgs);
                   } else {
                       Map<String, Object> transactionResult = null;
                       try {
                    	   transactionResult = dispatcher.runSync("createBOMAssoc", bomMap);
                    	                      	   
                           if (ServiceUtil.isError(transactionResult)) {
                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
                           }
                           
                       } catch (GenericServiceException e) {
                           csvReader.close();
                           Debug.logError(e, module);
                           return ServiceUtil.returnError(e.getMessage());
                       }
                       transCreated++;
                   }
                   lastProductId = currentProductId;
               }
           }
           Debug.logInfo(":::entery created ::"+transCreated, module); 
       } catch (IOException e) {
           Debug.logError(e, module);
           return ServiceUtil.returnError(e.getMessage());
       }

       if (errMsgs.size() > 0) {
           return ServiceUtil.returnError(errMsgs);
       }

       return ServiceUtil.returnSuccess();
	}
	
	public static Map<String, Object> uploadEmployeesTimeSheet(DispatchContext dctx, Map<String, Object> context) {
	       Delegator delegator = dctx.getDelegator();
	       LocalDispatcher dispatcher = dctx.getDispatcher();
	       GenericValue userLogin = (GenericValue) context.get("userLogin");
	       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
	       
	       if (fileBytes == null) {
	           return ServiceUtil.returnError("Employees Timesheet File Data Not Found.");
	       }
	       
	       Debug.logInfo(":::EmployeesTimeSheet:::", module);
	       String encoding = System.getProperty("file.encoding");
	       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
	       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
	       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
	       List<String> errMsgs = new LinkedList<>();
	       List<String> newErrMsgs;
	       String currentPartyId = null;
	       String lastPartyId = null;
	       int transCreated = 0;
	       
	       try {
	           for (final CSVRecord rec : fmt.parse(csvReader)) {
	        	   currentPartyId = rec.get("OfbizPartyCode");
	        	   BigDecimal workingDays = new BigDecimal((String)rec.get("W.Days"));
	        	   BigDecimal hours = new BigDecimal((String)rec.get("W.Days"));
	        	   if(UtilValidate.isNotEmpty(hours))hours = hours.multiply(new BigDecimal(8));
	        	   BigDecimal overTimeHours = new BigDecimal((String)rec.get("OvertimeW.Hours"));
	        	   if ((lastPartyId == null || !currentPartyId.equals(lastPartyId))) {
	                   Map<String, Object> timeSheetMap = UtilMisc.toMap(
	                           "partyId", currentPartyId,
	                           "statusId", "TIMESHEET_IN_PROCESS",
	                           "fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
	                           "comments", UtilValidate.isNotEmpty(rec.get("Comments"))?rec.get("Comments"):"",
	                           "userLogin", userLogin
	                           );
	                   
	                   newErrMsgs = new LinkedList<>();
	                   Debug.logInfo("##################timeSheetMap::"+timeSheetMap, module);
	                   try {
		                   if (UtilValidate.isEmpty(timeSheetMap.get("partyId"))) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory partyId missing for entry");
		                   }else if (EntityQuery.use(delegator).from("Party").where("partyId", timeSheetMap.get("partyId")).queryOne() == null) {
		                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":party id: " + timeSheetMap.get("partyId") + " not found for party: ");
		                   }
		                   if (UtilValidate.isEmpty(hours)) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory working days missing for entry");
		                   }
		               } catch (GenericEntityException e) {
		                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
		               }
	                   if (newErrMsgs.size() > 0) {
	                       errMsgs.addAll(newErrMsgs);
	                   } else {
	                       Map<String, Object> transactionResult = null;
	                       try {
	                    	   transactionResult = dispatcher.runSync("createTimesheet", timeSheetMap);
	                    	                      	   
	                           if (ServiceUtil.isError(transactionResult)) {
	                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
	                           }
	                       } catch (GenericServiceException e) {
	                           csvReader.close();
	                           Debug.logError(e, module);
	                           return ServiceUtil.returnError(e.getMessage());
	                       }
	                       Map<String, Object> transactionResult1 = null;
	                       String timesheetId = (String) transactionResult.get("timesheetId");
	                       try {
	                    	   transactionResult1 = dispatcher.runSync("createTimeEntry", UtilMisc.toMap("timesheetId",timesheetId, "hours",hours, 
                    			   					"rateTypeId","STANDARD", "partyId",currentPartyId,
	                    			   				"fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
                			   						"comments", UtilValidate.isNotEmpty(rec.get("Comments"))?rec.get("Comments"):"","userLogin", userLogin));
	                    	   if (ServiceUtil.isError(transactionResult1)) {
	                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
	                           }
	                       } catch (GenericServiceException e) {
	                    	   csvReader.close();
	                           Debug.logError(e, module);
	                           return ServiceUtil.returnError(e.getMessage());
	                       }
	                       
	                       	String timeEntryId = (String) transactionResult1.get("timeEntryId");
	                       	Map<String, Object> createPayrollInvoiceReqult = createPayrollInvoice(delegator, dispatcher, currentPartyId, "patanjali", workingDays, overTimeHours, userLogin);// Create Payroll Invoice if basic Salary define.
                    	   	
	                       	String invoiceId = (String) createPayrollInvoiceReqult.get("invoiceId");
		                    //update timeEntry with generated invoice
	           				try {
		           				dispatcher.runSync("updateTimeEntry",UtilMisc.toMap("invoiceId",invoiceId,"timeEntryId",timeEntryId,"userLogin", userLogin));
		           			} catch (GenericServiceException e) {
		           				e.printStackTrace();
		           			}
	           				if(UtilValidate.isNotEmpty(overTimeHours)){
                    		   Map<String, Object> transactionResult2 = null;
                    		   try {
									transactionResult2 = dispatcher.runSync("createTimeEntry", UtilMisc.toMap("timesheetId",timesheetId, "hours",overTimeHours, 
											"rateTypeId","OVERTIME", "partyId",currentPartyId,
											"fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
											"comments", UtilValidate.isNotEmpty(rec.get("Comments"))?rec.get("Comments"):"","userLogin", userLogin));
									
									if (ServiceUtil.isError(transactionResult2)) {
				               		   return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult2));
				               	   	}
								} catch (GenericServiceException e) {
									e.printStackTrace();
								}
                    		   	String timeEntryId1 = (String) transactionResult2.get("timeEntryId");
                		   		try {
                		   			dispatcher.runSync("updateTimeEntry",UtilMisc.toMap("invoiceId",invoiceId,"timeEntryId",timeEntryId1,"userLogin", userLogin));
   		           				} catch (GenericServiceException e) {
   		           					e.printStackTrace();
   		           				}
                    	   }
	                       transCreated++;
	                   }
	                   lastPartyId = currentPartyId;
	               }
	           }
	           Debug.logInfo(":::entery created ::"+transCreated, module); 
	       } catch (IOException e) {
	           Debug.logError(e, module);
	           return ServiceUtil.returnError(e.getMessage());
	       }

	       if (errMsgs.size() > 0) {
	           return ServiceUtil.returnError(errMsgs);
	       }

       return ServiceUtil.returnSuccess();
   }

	private static Map<String, Object> createPayrollInvoice(Delegator delegator, LocalDispatcher dispatcher, String partyIdFrom, String organizationPartyId, BigDecimal workingDays, BigDecimal overTimeHours, GenericValue userLogin) {
		// need to do to stop unwanted benefit and payroll
		GenericValue basicSalary = null;
		List<EntityCondition> exprList = new ArrayList<EntityCondition>();
        exprList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdFrom));
        exprList.add(EntityCondition.makeCondition("rateTypeId", EntityOperator.EQUALS, "STANDARD"));
        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
        exprList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
        EntityConditionList<EntityCondition> condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
        Map<String, Object> resultMap = ServiceUtil.returnSuccess();

        try {
			basicSalary = EntityQuery.use(delegator).from("RateAmount").where(condition).queryFirst();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if(UtilValidate.isNotEmpty(basicSalary)){
			Map<String, Object> createInvoiceItem = new HashMap<String, Object>();
			Map<String, Object> transactionResult = null;
			try {
				transactionResult = dispatcher.runSync("createInvoice", UtilMisc.toMap("partyIdFrom",partyIdFrom, "invoiceTypeId","PAYROL_INVOICE","statusId","INVOICE_IN_PROCESS",
						"currencyUomId","INR","partyId",organizationPartyId,"userLogin",userLogin));
				if (ServiceUtil.isError(transactionResult)) {
	                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
	             }
			} catch (GenericServiceException e) {
				e.printStackTrace();
			}
			String invoiceId = (String) transactionResult.get("invoiceId");
			//Get value from RateAmount, PartyBenefit, PayrollPreferences
		
			Map<String, Object> transactionResult1 = null;
			BigDecimal fixedBasicSalaryAmount = (BigDecimal) basicSalary.get("rateAmount");
			BigDecimal basicSalaryAmount = (fixedBasicSalaryAmount.multiply(workingDays)).divide(new BigDecimal(30), RoundingMode.HALF_UP);
			
			if(UtilValidate.isNotEmpty(basicSalaryAmount)){
				createInvoiceItem.put("amount", basicSalaryAmount);
				createInvoiceItem.put("invoiceId", invoiceId);
				createInvoiceItem.put("invoiceItemTypeId", "PAYROL_SALARY");
				createInvoiceItem.put("quantity", BigDecimal.ONE);
				createInvoiceItem.put("description", "Earnings and Hours : Salary");
				createInvoiceItem.put("userLogin", userLogin);
				try {
					transactionResult1 = dispatcher.runSync("createInvoiceItem",createInvoiceItem);
					if (ServiceUtil.isError(transactionResult1)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
					}
				} catch (GenericServiceException e) {
					e.printStackTrace();
				}
			}
			
			// need to create overtime pay as invoice item
			//if overTimeWorkingHour available and (overTime rateAmount exists or calculate overtime/hour rate from fixed basicSalary rateAmount ( fixedBasicSalaryAmount/(30*8))
			if(UtilValidate.isNotEmpty(overTimeHours)){
				exprList.remove(EntityCondition.makeCondition("rateTypeId", EntityOperator.EQUALS, "STANDARD"));
				exprList.remove(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
				
				exprList.add(EntityCondition.makeCondition("rateTypeId", EntityOperator.EQUALS, "OVERTIME"));
		        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_HOUR"));
		        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        GenericValue overTimeRate = null;
				try {
					overTimeRate = EntityQuery.use(delegator).from("RateAmount").where(condition).queryFirst();
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
				BigDecimal overtimeAmount = fixedBasicSalaryAmount.multiply(overTimeHours);
				overtimeAmount = overtimeAmount.divide(new BigDecimal(240), RoundingMode.HALF_UP);
				if(UtilValidate.isNotEmpty(overTimeRate)){
					overtimeAmount = overTimeRate.getBigDecimal("rateAmount").multiply(overTimeHours);
				}
				
				createInvoiceItem.put("amount", overtimeAmount);
				createInvoiceItem.put("invoiceId", invoiceId);
				createInvoiceItem.put("invoiceItemTypeId", "PAYROL_OVERTIME_PAY");
				createInvoiceItem.put("quantity", BigDecimal.ONE);
				createInvoiceItem.put("description", "Earnings and Hours : Overtime Pay");
				createInvoiceItem.put("userLogin", userLogin);
				try {
					transactionResult1 = dispatcher.runSync("createInvoiceItem",createInvoiceItem);
					if (ServiceUtil.isError(transactionResult1)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
					}
				} catch (GenericServiceException e) {
					e.printStackTrace();
				}
				
			}
			exprList.clear();
			List<GenericValue> partyBenefit = new ArrayList<>(); 
			exprList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, partyIdFrom));
	        exprList.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "EMPLOYEE"));
	        exprList.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, organizationPartyId));
	        exprList.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "INTERNAL_ORGANIZATIO"));
	        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
	        exprList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
			try {
				partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryList();
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			
			for(GenericValue benefit : partyBenefit){
				GenericValue benefitDesc = null;
				try {
					benefitDesc = benefit.getRelatedOne("BenefitType", true);
				} catch (GenericEntityException e1) {
					e1.printStackTrace();
				}
				BigDecimal cost = benefit.getBigDecimal("cost");
				
				if(UtilValidate.isNotEmpty(cost)){
					cost = (cost.multiply(workingDays)).divide(new BigDecimal(30), RoundingMode.HALF_UP);
				}
				
				if(UtilValidate.isEmpty(cost)  && UtilValidate.isNotEmpty(benefit.getBigDecimal("actualEmployerPaidPercent"))){
					cost = (basicSalaryAmount.multiply(benefit.getBigDecimal("actualEmployerPaidPercent"))).divide(new BigDecimal(100), RoundingMode.HALF_UP);
				}
				
				if(UtilValidate.isNotEmpty(cost)){
					createInvoiceItem.put("amount", cost);
					createInvoiceItem.put("invoiceId", invoiceId);
					createInvoiceItem.put("invoiceItemTypeId", "PAYROL_"+benefit.get("benefitTypeId"));
					createInvoiceItem.put("quantity", BigDecimal.ONE);
					createInvoiceItem.put("description", "Earnings and Hours : "+benefitDesc.get("description"));
					createInvoiceItem.put("userLogin", userLogin);
					try {
						transactionResult1 = dispatcher.runSync("createInvoiceItem",createInvoiceItem);
						if (ServiceUtil.isError(transactionResult1)) {
	                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
						}
					} catch (GenericServiceException e) {
						e.printStackTrace();
					}
				}
			}
			exprList.clear();
			
			List<GenericValue> payrollPreferences = new ArrayList<>(); 
			exprList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyIdFrom));
	        exprList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "EMPLOYEE"));
	        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
	        exprList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
			try {
				payrollPreferences = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryList();
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
			for(GenericValue payroll : payrollPreferences){
				GenericValue payrollDesc = null;
				try {
					payrollDesc = payroll.getRelatedOne("DeductionType", true);
				} catch (GenericEntityException e1) {
					e1.printStackTrace();
				}
				BigDecimal amount = payroll.getBigDecimal("flatAmount");
				if(UtilValidate.isNotEmpty(amount)){
					amount = (amount.multiply(workingDays)).divide(new BigDecimal(30),RoundingMode.HALF_UP);
				}
				if(UtilValidate.isEmpty(amount)  && UtilValidate.isNotEmpty(payroll.getBigDecimal("percentage"))){
					amount = (basicSalaryAmount.multiply(payroll.getBigDecimal("percentage"))).divide(new BigDecimal(100), RoundingMode.HALF_UP);
				}
				if(UtilValidate.isNotEmpty(amount)){
					createInvoiceItem.put("amount", amount.multiply(new BigDecimal(-1)));
					createInvoiceItem.put("invoiceId", invoiceId);
					if(payroll.get("deductionTypeId").equals("TDS")){
						createInvoiceItem.put("invoiceItemTypeId", "PAYROL_"+payroll.get("deductionTypeId"));
					}else{
						createInvoiceItem.put("invoiceItemTypeId", "PAYROL_DD_"+payroll.get("deductionTypeId"));
					}
					createInvoiceItem.put("quantity", BigDecimal.ONE);
					createInvoiceItem.put("description", "Deductions from Gross : "+payrollDesc.get("description"));
					createInvoiceItem.put("userLogin", userLogin);
					try {
						transactionResult1 = dispatcher.runSync("createInvoiceItem",createInvoiceItem);
						if (ServiceUtil.isError(transactionResult1)) {
	                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
						}
					} catch (GenericServiceException e) {
						e.printStackTrace();
					}
				}
			}
			
			resultMap.put("invoiceId", invoiceId);
		}
		
		return resultMap;
	}
	
	@SuppressWarnings("resource")
	public static Map<String, Object> uploadEmployeeFixedSalaryStructure(DispatchContext dctx, Map<String, Object> context) {
	       Delegator delegator = dctx.getDelegator();
	       LocalDispatcher dispatcher = dctx.getDispatcher();
	       GenericValue userLogin = (GenericValue) context.get("userLogin");
	       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
	       
	       if (fileBytes == null) {
	           return ServiceUtil.returnError("Employees Timesheet File Data Not Found.");
	       }
	       
	       Debug.logInfo(":::EmployeesTimeSheet:::", module);
	       String encoding = System.getProperty("file.encoding");
	       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
	       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
	       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
	       List<String> errMsgs = new LinkedList<>();
	       List<String> newErrMsgs;
	       String currentPartyId = null;
	       String lastPartyId = null;
	       int transCreated = 0;
	       
	       try {
	           for (final CSVRecord rec : fmt.parse(csvReader)) {
	        	   currentPartyId = rec.get("OfbizPartyCode");
	        	   if(UtilValidate.isNotEmpty(rec.get("FromDate"))){
	        		   	List<EntityCondition> exprListCheck = new ArrayList<EntityCondition>();
	        	       	exprListCheck.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, currentPartyId));
	        	       	exprListCheck.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "EMPLOYEE"));
	        	       	exprListCheck.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
	        	       	exprListCheck.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.EQUALS, "EFT_ACCOUNT"));
	        	       	exprListCheck.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
	   	        		EntityConditionList<EntityCondition> conditionCheck = EntityCondition.makeCondition(exprListCheck, EntityOperator.AND);
		   	        	GenericValue payrollPreferenceCheck = null;
		   	        	try {
		   	        		payrollPreferenceCheck = EntityQuery.use(delegator).from("PayrollPreference").where(conditionCheck).queryFirst();
		   	        	} catch (GenericEntityException e1) {
		   					e1.printStackTrace();
		   	        	}
		   	        	if(UtilValidate.isNotEmpty(payrollPreferenceCheck)){
			   	        	Timestamp fd = Timestamp.valueOf(rec.get("FromDate"));
			   	        	if(payrollPreferenceCheck.getTimestamp("fromDate").compareTo(fd) == 0){
			   	        		return ServiceUtil.returnError("FromDate should not remain same as before, old date is :"+payrollPreferenceCheck.getTimestamp("fromDate"));
			   	        	}
		   	        	}
   	        	   }
	        	   
	        	   BigDecimal basicSalary = new BigDecimal(rec.get("BasicSalary"));
	        	   BigDecimal overTimeRatePerHour = new BigDecimal(rec.get("OvertimeRatePerHour"));
	        	   if ((lastPartyId == null || !currentPartyId.equals(lastPartyId))) {
	        		   if(UtilValidate.isNotEmpty(basicSalary)){
		        		   //rateAmount 
	        			   	Map <String, Object> inMapRateAmount = new HashMap<>();
	        				Map<String, Object> respRateAmount;
	        				inMapRateAmount.put("partyId", currentPartyId);
	        				inMapRateAmount.put("rateTypeId", "STANDARD");
	        				inMapRateAmount.put("periodTypeId","RATE_MONTH");
	        				inMapRateAmount.put("rateCurrencyUomId","INR");
	        				inMapRateAmount.put("workEffortId","_NA_");
	        				inMapRateAmount.put("emplPositionTypeId","_NA_");
	        				inMapRateAmount.put("rateAmount",basicSalary);
	        				inMapRateAmount.put("fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp());
	        				inMapRateAmount.put("userLogin",userLogin);
	        				
        		    		try {
        		    			respRateAmount = dispatcher.runSync("updateRateAmount", inMapRateAmount);
        		    		} catch (GenericServiceException e) {
        		    			csvReader.close();
        		    			e.printStackTrace();
        		    			return ServiceUtil.returnError(e.getMessage());
        		    		}
        		            if (ServiceUtil.isError(respRateAmount)) {
        		            	if(!ServiceUtil.getErrorMessage(respRateAmount).equals("The Rate amount already exist")){
        		            		csvReader.close();
            		                Debug.logError(ServiceUtil.getErrorMessage(respRateAmount), module);
            		                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(respRateAmount));
        		            	}
        		            }
		        	        
        		            // overTimeRatePerHour added
        		            if(UtilValidate.isNotEmpty(overTimeRatePerHour)){
        		            	inMapRateAmount.put("rateTypeId", "OVERTIME");
    	        				inMapRateAmount.put("periodTypeId","RATE_HOUR");
    	        				inMapRateAmount.put("rateAmount",overTimeRatePerHour);
        		            	try {
            		    			respRateAmount = dispatcher.runSync("updateRateAmount", inMapRateAmount);
            		    		} catch (GenericServiceException e) {
            		    			csvReader.close();
            		    			e.printStackTrace();
            		    			return ServiceUtil.returnError(e.getMessage());
            		    		}
            		            if (ServiceUtil.isError(respRateAmount)) {
            		            	if(!ServiceUtil.getErrorMessage(respRateAmount).equals("The Rate amount already exist")){
            		            		csvReader.close();
                		                Debug.logError(ServiceUtil.getErrorMessage(respRateAmount), module);
                		                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(respRateAmount));
            		            	}
            		            }
        		            }
		        	        List<EntityCondition> exprList = new ArrayList<EntityCondition>();
		        	        exprList.add(EntityCondition.makeCondition("partyIdTo", EntityOperator.EQUALS, currentPartyId));
		        	        exprList.add(EntityCondition.makeCondition("roleTypeIdTo", EntityOperator.EQUALS, "EMPLOYEE"));
		        	        exprList.add(EntityCondition.makeCondition("partyIdFrom", EntityOperator.EQUALS, "patanjali"));
		        	        exprList.add(EntityCondition.makeCondition("roleTypeIdFrom", EntityOperator.EQUALS, "INTERNAL_ORGANIZATIO"));
		        	        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
		        	        exprList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
		        	        EntityConditionList<EntityCondition> condition = null;
		        	        
		        	        GenericValue partyBenefit = null; 
	        	        	
		        	        if(UtilValidate.isNotEmpty(rec.get("DA_percentage"))){
		        	        	BigDecimal DA = new BigDecimal((String)rec.get("DA_percentage"));
		        	        	exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "DA"));
		        	            condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
		        	    			e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(DA, partyBenefit, currentPartyId, rec, "per", "DA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "DA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("HRA_percentage"))){
		        	        	BigDecimal HRA = new BigDecimal((String)rec.get("HRA_percentage"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "HRA"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(HRA, partyBenefit, currentPartyId, rec, "per", "HRA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "HRA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("CCA_percentage"))){
		        	        	BigDecimal CCA = new BigDecimal((String)rec.get("CCA_percentage"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "CCA"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(CCA, partyBenefit, currentPartyId, rec, "per", "CCA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "CCA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("CA_flat_amount"))){
		        	        	BigDecimal CA = new BigDecimal((String)rec.get("CA_flat_amount"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "CA"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(CA, partyBenefit, currentPartyId, rec, "amount", "CA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "CA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("MA_flat_amount"))){
		        	        	BigDecimal MA = new BigDecimal((String)rec.get("MA_flat_amount"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "MA"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(MA, partyBenefit, currentPartyId, rec, "amount", "MA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "MA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("SA_flat_amount"))){
		        	        	BigDecimal SA = new BigDecimal((String)rec.get("SA_flat_amount"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "SA"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(SA, partyBenefit, currentPartyId, rec, "amount", "SA", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "SA"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("BONUS_flat_amount"))){
		        	        	BigDecimal BONUS = new BigDecimal((String)rec.get("BONUS_flat_amount"));
			        	        exprList.add(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "BONUS"));
			        	        condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		partyBenefit = EntityQuery.use(delegator).from("PartyBenefit").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePartyBenefit(BONUS, partyBenefit, currentPartyId, rec, "amount", "BONUS", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("benefitTypeId", EntityOperator.EQUALS, "BONUS"));
		        	        }
		        	        
	        	        	
		        	        // payroll prefernces
		        	        GenericValue payrollPreference = null;
		        	        exprList.clear();
		        	        exprList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, currentPartyId));
		        	        exprList.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "EMPLOYEE"));
		        	        exprList.add(EntityCondition.makeCondition("periodTypeId", EntityOperator.EQUALS, "RATE_MONTH"));
		        	        exprList.add(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null));
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("PF_flat_amount"))){
		        	        	BigDecimal PF = new BigDecimal((String)rec.get("PF_flat_amount"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "PF"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, PF, payrollPreference, currentPartyId, rec, "amount", "PF", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "PF"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("PF_percentage"))){
		        	        	BigDecimal PF = new BigDecimal((String)rec.get("PF_percentage"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "PF"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, PF, payrollPreference, currentPartyId, rec, "per", "PF", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "PF"));
		        	        }
	    	        	   	
		        	        if(UtilValidate.isNotEmpty(rec.get("MESS_flat_amount"))){
		        	        	BigDecimal MESS = new BigDecimal((String)rec.get("MESS_flat_amount"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "MESS"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, MESS, payrollPreference, currentPartyId, rec, "amount", "MESS", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "MESS"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("ESI_flat_amount"))){
		        	        	BigDecimal ESI = new BigDecimal((String)rec.get("ESI_flat_amount"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ESI"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, ESI, payrollPreference, currentPartyId, rec, "amount", "ESI", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ESI"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("ESI_percentage"))){
		        	        	BigDecimal ESI = new BigDecimal((String)rec.get("ESI_percentage"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ESI"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, ESI, payrollPreference, currentPartyId, rec, "per", "ESI", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ESI"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("ADV_DED_flat_amount"))){
		        	        	BigDecimal ADV_DED = new BigDecimal((String)rec.get("ADV_DED_flat_amount"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ADV_DED"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, ADV_DED, payrollPreference, currentPartyId, rec, "amount", "ADV_DED", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "ADV_DED"));
		        	        }
		        	        
		        	        if(UtilValidate.isNotEmpty(rec.get("TDS_flat_amount"))){
		        	        	BigDecimal TDS = new BigDecimal((String)rec.get("TDS_flat_amount"));
		        	        	exprList.add(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "TDS"));
		        	        	condition = EntityCondition.makeCondition(exprList, EntityOperator.AND);
		        	        	try {
		        	        		payrollPreference = EntityQuery.use(delegator).from("PayrollPreference").where(condition).queryFirst();
		        	        	} catch (GenericEntityException e1) {
									e1.printStackTrace();
		        	        	}
		        	        	createUpdatePayrollPreference(delegator, TDS, payrollPreference, currentPartyId, rec, "amount", "TDS", dispatcher, userLogin);
		        	        	exprList.remove(EntityCondition.makeCondition("deductionTypeId", EntityOperator.EQUALS, "TDS"));
		        	        }
		    	        	   
	        		   }
	                   newErrMsgs = new LinkedList<>();
	                   try {
		                   if (UtilValidate.isEmpty(currentPartyId)) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory partyId missing for entry");
		                   }else if (EntityQuery.use(delegator).from("Party").where("partyId", currentPartyId).queryOne() == null) {
		                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":party id: " + currentPartyId + " not found for party: ");
		                   }
		                   if (UtilValidate.isEmpty(basicSalary)) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory basic salary missing for entry");
		                   }
		               } catch (GenericEntityException e) {
		                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
		               }
	                   if (newErrMsgs.size() > 0) {
	                       errMsgs.addAll(newErrMsgs);
	                   } else {
	                       transCreated++;
	                   }
	                   lastPartyId = currentPartyId;
	        	   }
	           }
	           Debug.logInfo(":::entery created ::"+transCreated, module); 
	       } catch (IOException e) {
	           Debug.logError(e, module);
	           return ServiceUtil.returnError(e.getMessage());
	       }

	       if (errMsgs.size() > 0) {
	           return ServiceUtil.returnError(errMsgs);
	       }

	       return ServiceUtil.returnSuccess();
	}

	private static Map<String, Object> createUpdatePayrollPreference(Delegator delegator, BigDecimal val, GenericValue payrollPreference,	String currentPartyId, CSVRecord rec, String type, String deductionType, LocalDispatcher dispatcher, GenericValue userLogin) {
		Map <String, Object> inMap = new HashMap<>();
		Map<String, Object> resp;
		inMap.put("partyId", currentPartyId);
		inMap.put("roleTypeId", "EMPLOYEE");
		inMap.put("periodTypeId","RATE_MONTH");
		inMap.put("paymentMethodTypeId","EFT_ACCOUNT");
		inMap.put("deductionTypeId",deductionType);
		inMap.put("userLogin",userLogin);
		
		if(UtilValidate.isNotEmpty(payrollPreference)){
			//inMap.put("fromDate",payrollPreference.getTimestamp("fromDate"));
			inMap.put("payrollPreferenceSeqId",payrollPreference.getString("payrollPreferenceSeqId"));
			inMap.put("thruDate", UtilDateTime.nowTimestamp());
    		try {
    			resp = dispatcher.runSync("updatePayrollPreference", inMap);
    		} catch (GenericServiceException e) {
    			e.printStackTrace();
    			return ServiceUtil.returnError(e.getMessage());
    		}
            if (ServiceUtil.isError(resp)) {
                Debug.logError(ServiceUtil.getErrorMessage(resp), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
            }
            inMap.remove("payrollPreferenceSeqId");
            inMap.remove("thruDate");
    	}
		String payrollPreferenceSeqId = delegator.getNextSeqId("PayrollPreference");
		inMap.put("payrollPreferenceSeqId",payrollPreferenceSeqId);
		inMap.put("fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp());
		if(type.equals("per")){
			inMap.put("percentage", val);
		}else{
			inMap.put("flatAmount", val);
		}
		
		try {
			resp = dispatcher.runSync("createPayrollPreference", inMap);
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
        if (ServiceUtil.isError(resp)) {
            Debug.logError(ServiceUtil.getErrorMessage(resp), module);
            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
        }
    	
    	return ServiceUtil.returnSuccess();
	}

	private static Map<String, Object> createUpdatePartyBenefit(BigDecimal val, GenericValue partyBenefit, String partyIdTo, CSVRecord rec, String type, String benefitType, LocalDispatcher dispatcher, GenericValue userLogin) {
		Map <String, Object> inMap = new HashMap<>();
		Map<String, Object> resp;
		inMap.put("partyIdTo", partyIdTo);
		inMap.put("roleTypeIdTo", "EMPLOYEE");
		inMap.put("periodTypeId","RATE_MONTH");
		inMap.put("partyIdFrom","patanjali");
		inMap.put("benefitTypeId",benefitType);
		inMap.put("roleTypeIdFrom","INTERNAL_ORGANIZATIO");
		inMap.put("userLogin",userLogin);
		System.out.println(benefitType+"------------partyBenefit-------");
		if(UtilValidate.isNotEmpty(partyBenefit)){
			System.out.println(benefitType+"------------partyBenefit-------"+partyBenefit.get("benefitTypeId"));
			inMap.put("fromDate",partyBenefit.getTimestamp("fromDate"));
			inMap.put("thruDate", UtilDateTime.nowTimestamp());
    		try {
    			resp = dispatcher.runSync("updatePartyBenefit", inMap);
    		} catch (GenericServiceException e) {
    			e.printStackTrace();
    			return ServiceUtil.returnError(e.getMessage());
    		}
            if (ServiceUtil.isError(resp)) {
                Debug.logError(ServiceUtil.getErrorMessage(resp), module);
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
            }
            inMap.remove("thruDate");
    	}
    	
		inMap.put("fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp());
		if(type.equals("per")){
			inMap.put("actualEmployerPaidPercent", val);
		}else{
			inMap.put("cost", val);
		}
		try {
			resp = dispatcher.runSync("createPartyBenefit", inMap);
		} catch (GenericServiceException e) {
			e.printStackTrace();
			return ServiceUtil.returnError(e.getMessage());
		}
        if (ServiceUtil.isError(resp)) {
            Debug.logError(ServiceUtil.getErrorMessage(resp), module);
            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
        }
    	return ServiceUtil.returnSuccess();
	} 
	
	// Issues with attendance data:-
	//1. Same date comes many time, it should be only two times occur
	//2. How we assume in and out time.(time should be 24 hours format)
	//3. Some dates comes only once(it's in or out date)
	//4. Some where time sequence not in ordered. (xxx 6:38, 6:42, 5:32), it should be in an order.
	//5. Also date contains some special character.
	
	public static Map<String, Object> calculateEmployeesTimeFromCSV(DispatchContext dctx, Map<String, Object> context) {
	       Delegator delegator = dctx.getDelegator();
	       LocalDispatcher dispatcher = dctx.getDispatcher();
	       GenericValue userLogin = (GenericValue) context.get("userLogin");
	       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
	       
	       if (fileBytes == null) {
	           return ServiceUtil.returnError("Employees attendance File Data Not Found.");
	       }
	       
	       Debug.logInfo(":::EmployeesAttendanceSheet:::", module);
	       String encoding = System.getProperty("file.encoding");
	       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
	       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
	       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
	       List<String> errMsgs = new LinkedList<>();
	       List<String> newErrMsgs;
	       String currentPartyId = null;
	       String lastPartyId = null;
	       int transCreated = 0;
	       String lastCheckDate = "";
	       String checkDate = "";
	       String checkTime = "";
	       String lastTime = "";
	       BigDecimal workingHours = BigDecimal.ZERO;
	       try {
	           for (final CSVRecord rec : fmt.parse(csvReader)) {
	        	   currentPartyId = rec.get("USERID");
	        	   if ((lastPartyId == null || currentPartyId.equals(lastPartyId))) {
	        		   //Timestamp checkDate = Timestamp.valueOf(rec.get("CHECKTIME"));
	        		   //String sDate5 = "Wednesday 29 April 2015 08:50:12";
	        		   String sDate5 = returnTimestamp(rec.get("CHECKTIME"));
	                   String dateArry[] = sDate5.split(" ");
	        		   Debug.logInfo(":::sDate5 ::"+sDate5, module);
	        		   checkDate = dateArry[0];
	        		   checkTime = dateArry[1];
	        		   if(lastCheckDate == null || checkDate.equals(lastCheckDate)){
	        			   Debug.logInfo(dateArry[0]+":::sDate5 splited ::"+dateArry[1], module);
	        			   
	        			   //check hours difference is greater than or equal to 8 hours.
	        			   //if(lastTime > checkTime){
	        				   workingHours =  workingHours.add(new BigDecimal(8));
	        			   //}
	        		   }else{
	        			   // first need to check month changed than 
	        			   //need to store working hours in timeEntry when next date is different
	        		   }
	        		   lastTime = checkTime;
	        		   lastCheckDate = checkDate;
	        		   lastPartyId = currentPartyId;
	               }else{
	            	   // store working hours of last value for previous party
	            	   // start adding hours for new party
	               }
	           }
	           Debug.logInfo(":::entery created ::"+transCreated, module); 
	       } catch (IOException e) {
	           Debug.logError(e, module);
	           return ServiceUtil.returnError(e.getMessage());
	       }

	       if (errMsgs.size() > 0) {
	           return ServiceUtil.returnError(errMsgs);
	       }

	       return ServiceUtil.returnSuccess();
	   }

	private static String returnTimestamp(String sDate5) {
		
         SimpleDateFormat formatter5=new SimpleDateFormat("E dd MMM yyyy HH:mm");
         DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
         Date date5 = null;
         try{
 				date5=formatter5.parse(sDate5); 
 				//System.out.println(sDate5+"\t"+date5); 
 				//System.out.println("Date in dd/MM/yyyy format is: "+df.format(date5));
         }catch (Exception e ){
        	 Debug.logError(e, module);
         }
		
		return df.format(date5);
	}
	
	public static Map<String, Object> uploadEmployeesOverTimeData(DispatchContext dctx, Map<String, Object> context) {
	       Delegator delegator = dctx.getDelegator();
	       LocalDispatcher dispatcher = dctx.getDispatcher();
	       GenericValue userLogin = (GenericValue) context.get("userLogin");
	       ByteBuffer fileBytes = (ByteBuffer) context.get("uploadedFile");
	       
	       if (fileBytes == null) {
	           return ServiceUtil.returnError("Employees overtime data File Data Not Found.");
	       }
	       
	       Debug.logInfo(":::EmployeesTimeSheet:::", module);
	       String encoding = System.getProperty("file.encoding");
	       String csvString = Charset.forName(encoding).decode(fileBytes).toString();
	       final BufferedReader csvReader = new BufferedReader(new StringReader(csvString));
	       CSVFormat fmt = CSVFormat.DEFAULT.withHeader();
	       List<String> errMsgs = new LinkedList<>();
	       List<String> newErrMsgs;
	       String currentPartyId = null;
	       String lastPartyId = null;
	       int transCreated = 0;
	       try {
	           for (final CSVRecord rec : fmt.parse(csvReader)) {
	        	   currentPartyId = rec.get("OfbizPartyCode");
	        	   BigDecimal overTimeHours = new BigDecimal((String)rec.get("OvertimeW.Hours"));
	        	   if ((lastPartyId == null || !currentPartyId.equals(lastPartyId))) {
	                   Map<String, Object> timeSheetMap = UtilMisc.toMap(
	                           "partyId", currentPartyId,
	                           "statusId", "TIMESHEET_IN_PROCESS",
	                           "fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
	                           "comments", UtilValidate.isNotEmpty(rec.get("Ccomments"))?rec.get("Comments"):"",
	                           "userLogin", userLogin
	                           );
	                   
	                   newErrMsgs = new LinkedList<>();
	                   Debug.logInfo("##################timeSheetMap::"+timeSheetMap, module);
	                   try {
		                   if (UtilValidate.isEmpty(timeSheetMap.get("partyId"))) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory partyId missing for entry");
		                   }else if (EntityQuery.use(delegator).from("Party").where("partyId", timeSheetMap.get("partyId")).queryOne() == null) {
		                	   newErrMsgs.add("Line number " + rec.getRecordNumber() + ":party id: " + timeSheetMap.get("partyId") + " not found for party: ");
		                   }
		                   if (UtilValidate.isEmpty(overTimeHours)) {
		                       newErrMsgs.add("Line number " + rec.getRecordNumber() + ": Mandatory working days missing for entry");
		                   }
		               } catch (GenericEntityException e) {
		                   Debug.logError("Validation checking problem against database. due to " + e.getMessage(), module);
		               }
	                   if (newErrMsgs.size() > 0) {
	                       errMsgs.addAll(newErrMsgs);
	                   } else {
	                       Map<String, Object> transactionResult = null;
	                       try {
	                    	   transactionResult = dispatcher.runSync("createTimesheet", timeSheetMap);
	                    	                      	   
	                           if (ServiceUtil.isError(transactionResult)) {
	                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult));
	                           }
	                       } catch (GenericServiceException e) {
	                           csvReader.close();
	                           Debug.logError(e, module);
	                           return ServiceUtil.returnError(e.getMessage());
	                       }
	                       Map<String, Object> transactionResult1 = null;
	                       String timesheetId = (String) transactionResult.get("timesheetId");
	                       try {
	                    	   transactionResult1 = dispatcher.runSync("createTimeEntry", UtilMisc.toMap("timesheetId",timesheetId, "hours",overTimeHours, 
                 			   					"rateTypeId","OVERTIME", "partyId",currentPartyId,
	                    			   				"fromDate", UtilValidate.isNotEmpty(rec.get("FromDate"))?rec.get("FromDate"):UtilDateTime.nowTimestamp(),
             			   						"comments", UtilValidate.isNotEmpty(rec.get("Comments"))?rec.get("Comments"):"","userLogin", userLogin));
	                    	   if (ServiceUtil.isError(transactionResult1)) {
	                              return ServiceUtil.returnError(ServiceUtil.getErrorMessage(transactionResult1));
	                           }
	                    	   String timeEntryId = (String) transactionResult1.get("timeEntryId");
	                    	   // need to discuss about overTime payroll invoice generate separately or merge in employee salary Invoice
	                    	   //createPayrollInvoice(delegator, dispatcher, currentPartyId, "patanjali", timeEntryId, overTimeHours, userLogin);// Create Payroll Invoice if basic Salary define.
	                    	   
	                       } catch (GenericServiceException e) {
	                    	   csvReader.close();
	                           Debug.logError(e, module);
	                           return ServiceUtil.returnError(e.getMessage());
	                       }
                        
	                       transCreated++;
	                   }
	                   lastPartyId = currentPartyId;
	               }
	           }
	           Debug.logInfo(":::entery created ::"+transCreated, module); 
	       } catch (IOException e) {
	           Debug.logError(e, module);
	           return ServiceUtil.returnError(e.getMessage());
	       }

	       if (errMsgs.size() > 0) {
	           return ServiceUtil.returnError(errMsgs);
	       }

    return ServiceUtil.returnSuccess();
}
	
	public static Map<String, Object> importJournalTransaction(DispatchContext ctx, Map<String, ? extends Object> context){
    	LocalDispatcher dispatcher = ctx.getDispatcher();
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
    	Workbook workbook = null;
		try {
			workbook = WorkbookFactory.create(new File(JOURNAL_XLSX_FILE_PATH));
		} catch (EncryptedDocumentException | IOException e) {
			e.printStackTrace();
		}
        Sheet sheet = workbook.getSheetAt(0);
        DataFormatter dataFormatter = new DataFormatter();
        Iterator<Row> rowIterator = sheet.iterator();
        String billNumber = "0";
        String acctgTransId = null;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            if(row.getRowNum() == 0)continue;
            String date = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(0));
            String newBillNumber = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(1));
            String accountName = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(2));
            if(newBillNumber != billNumber) {
            	Map<String, Object> acctgTransContext = UtilMisc.toMap("acctgTransTypeId","INTERNAL_ACCTG_TRANS","isPosted", "Y",
                		"glFiscalTypeId", "ACTUAL", "voucherRef",newBillNumber, "transactionDate", UtilDateTime.toDate(date, "00:00:00"),
                		"userLogin", userLogin, "description", accountName);
                Map<String, Object> acctgTransResult;
                try {
                	acctgTransResult = dispatcher.runSync("createAcctgTrans", acctgTransContext);
    			} catch (GenericServiceException e) {
    				e.printStackTrace();
    				return ServiceUtil.returnError(e.getMessage());
    			}
                
                acctgTransId = (String) acctgTransResult.get("acctgTransId");
            }
            billNumber = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(1));
            
            Map<String, Object> acctgTransEntryContext = UtilMisc.toMap("userLogin", userLogin,"acctgTransId",acctgTransId, "acctgTransEntryTypeId","_NA_",
            		"voucherRef",newBillNumber,"currency","INR","organizationPartyId","patanjali","reconcileStatusId","AES_NOT_RECONCILED");
            String creditAmount = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(3));
            if(UtilValidate.isNotEmpty(creditAmount)) {
            	String creditGl = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(4));
            	acctgTransEntryContext.put("glAccountId", creditGl);
            	acctgTransEntryContext.put("amount", new BigDecimal(creditAmount.replaceAll(",", "")));
            	acctgTransEntryContext.put("debitCreditFlag", "C");
            	try {
                	dispatcher.runSync("createAcctgTransEntry", acctgTransEntryContext);
    			} catch (GenericServiceException e) {
    				e.printStackTrace();
    			}
            }
            String debitAmount = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(6));
            if(UtilValidate.isNotEmpty(debitAmount)) {
            	String debitGl = dataFormatter.formatCellValue(sheet.getRow(row.getRowNum()).getCell(7));
            	acctgTransEntryContext.put("glAccountId", debitGl);
            	acctgTransEntryContext.put("amount", new BigDecimal(debitAmount.replaceAll(",", "")));
            	acctgTransEntryContext.put("debitCreditFlag", "D");
            	try {
                	dispatcher.runSync("createAcctgTransEntry", acctgTransEntryContext);
    			} catch (GenericServiceException e) {
    				e.printStackTrace();
    			}
            }
        }
    	return ServiceUtil.returnSuccess();
	}
}
