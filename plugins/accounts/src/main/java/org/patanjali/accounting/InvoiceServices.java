package org.patanjali.accounting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.ofbiz.accounting.invoice.InvoiceWorker;
import org.apache.ofbiz.accounting.payment.PaymentGatewayServices;
import org.apache.ofbiz.accounting.payment.PaymentWorker;
import org.apache.ofbiz.accounting.util.UtilAccounting;
import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilDateTime;
import org.apache.ofbiz.base.util.UtilFormatOut;
import org.apache.ofbiz.base.util.UtilGenerics;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilNumber;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.condition.EntityExpr;
import org.apache.ofbiz.entity.condition.EntityOperator;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.entity.util.EntityTypeUtil;
import org.apache.ofbiz.entity.util.EntityUtil;
import org.apache.ofbiz.entity.util.EntityUtilProperties;
import org.apache.ofbiz.order.order.OrderReadHelper;
import org.apache.ofbiz.product.product.ProductWorker;
import org.apache.ofbiz.service.DispatchContext;
import org.apache.ofbiz.service.GenericServiceException;
import org.apache.ofbiz.service.LocalDispatcher;
import org.apache.ofbiz.service.ServiceUtil;

public class InvoiceServices {

    public static final String module = InvoiceServices.class.getName();

    // set some BigDecimal properties
    private static final int DECIMALS = UtilNumber.getBigDecimalScale("invoice.decimals");
    private static final RoundingMode ROUNDING = UtilNumber.getRoundingMode("invoice.rounding");
    private static final int TAX_DECIMALS = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    private static final RoundingMode TAX_ROUNDING = UtilNumber.getRoundingMode("salestax.rounding");
    private static final int INVOICE_ITEM_SEQUENCE_ID_DIGITS = 5; // this is the number of digits used for invoiceItemSeqId: 00001, 00002...

    public static final String resource = "AccountingUiLabels";

    public static Map<String, Object> roundingOffInvoice(DispatchContext ctx, Map<String, Object> context) {
    	
    	Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingAritmeticPropertiesNotConfigured", locale));
        }

        String invoiceId = (String) context.get("invoiceId");
        GenericValue invoice = null ;
        try {
            invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceId).queryOne();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Invoice for Invoice ID" + invoiceId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingInvoiceNotFound", UtilMisc.toMap("invoiceId", invoiceId), locale));
        }    	
    	
        GenericValue roundOffInvItmAdj = null ;
        try {
        	roundOffInvItmAdj = EntityQuery.use(delegator).from("InvoiceItem").where("invoiceId", invoiceId, "invoiceItemSeqId","roundoff").queryOne();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Invoice for Invoice ID" + invoiceId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingInvoiceNotFound", UtilMisc.toMap("invoiceId", invoiceId), locale));
        }  
        
        BigDecimal totInvoiceValue = InvoiceWorker.getInvoiceTotal(delegator, invoiceId);
        BigDecimal roundOffAdjAmt = totInvoiceValue.subtract(new BigDecimal(totInvoiceValue.toBigInteger()));
        
        if(roundOffAdjAmt.compareTo(new BigDecimal("0.50")) == -1 ) {
	    	roundOffAdjAmt = roundOffAdjAmt.negate();
		}else {
			roundOffAdjAmt = BigDecimal.ONE.subtract(roundOffAdjAmt);
		}
        
        if (roundOffAdjAmt.compareTo(BigDecimal.ZERO) != 0 && !UtilValidate.isEmpty(roundOffInvItmAdj)) {
        	 roundOffInvItmAdj.put("amount", roundOffAdjAmt);
        	 try {
        		 delegator.createOrStore(roundOffInvItmAdj);
             } catch (GenericEntityException e) {
            	 e.printStackTrace();
             }
         }else if(roundOffAdjAmt.compareTo(BigDecimal.ZERO) == 0 && !UtilValidate.isEmpty(roundOffInvItmAdj)) {
        	 try {
        		 delegator.removeValue(roundOffInvItmAdj);
             } catch (GenericEntityException e) {
            	 e.printStackTrace();
             }
         }else if(roundOffAdjAmt.compareTo(BigDecimal.ZERO) != 0) {
             Map<String, Object> createInvoiceItemContext = new HashMap<>();
             createInvoiceItemContext.put("invoiceId", invoiceId);
             createInvoiceItemContext.put("invoiceItemSeqId", "roundoff");
             createInvoiceItemContext.put("invoiceItemTypeId", "SROUNDED_OFF");
             createInvoiceItemContext.put("description", "rounding off");
             createInvoiceItemContext.put("amount", roundOffAdjAmt);
             createInvoiceItemContext.put("taxableFlag", "N");
             //createInvoiceItemContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
             createInvoiceItemContext.put("userLogin", userLogin);
             Map<String, Object> createInvoiceItemResult = null;
             try {
                 createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
             } catch (GenericServiceException e) {
                 Debug.logError(e, "Service/other problem creating InvoiceItem from order header adjustment", module);
                 return ServiceUtil.returnError("Problem creating InvoiceItem roundoff adjustment");
             }
             if (ServiceUtil.isError(createInvoiceItemResult)) {
            	 return ServiceUtil.returnError("Problem creating InvoiceItem roundoff adjustment");
             }
         }
    	
    	return ServiceUtil.returnSuccess();
    }
    
    
    public static Map<String, Object> addTcsForInvoice(DispatchContext ctx, Map<String, Object> context) {
    	
    	Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");

        if (DECIMALS == -1 || ROUNDING == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingAritmeticPropertiesNotConfigured", locale));
        }

        String invoiceId = (String) context.get("invoiceId");
        GenericValue invoice = null ;
        try {
            invoice = EntityQuery.use(delegator).from("Invoice").where("invoiceId", invoiceId).queryOne();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Invoice for Invoice ID" + invoiceId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingInvoiceNotFound", UtilMisc.toMap("invoiceId", invoiceId), locale));
        }    	
    	
        GenericValue tcsInvItmAdj = null ;
        try {
        	tcsInvItmAdj = EntityQuery.use(delegator).from("InvoiceItem").where("invoiceId", invoiceId, "invoiceItemSeqId","tcs").queryOne();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Invoice for Invoice ID" + invoiceId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "AccountingInvoiceNotFound", UtilMisc.toMap("invoiceId", invoiceId), locale));
        }  
        
        BigDecimal invoiceNoTaxTotal = InvoiceWorker.getInvoiceNoTaxTotal(invoice);
        BigDecimal tcsAdjAmt = invoiceNoTaxTotal.multiply(new BigDecimal("0.01")).setScale(0,BigDecimal.ROUND_DOWN);
        Debug.logInfo(":::invoiceNoTaxTotal:::"+invoiceNoTaxTotal + " :::tcsAdjAmt:: "+tcsAdjAmt, module);
        
         if (tcsAdjAmt.compareTo(BigDecimal.ZERO) != 0 && !UtilValidate.isEmpty(tcsInvItmAdj)) {
        	 tcsInvItmAdj.put("amount", tcsAdjAmt);
        	 try {
        		 delegator.createOrStore(tcsInvItmAdj);
             } catch (GenericEntityException e) {
            	 e.printStackTrace();
             }
         }else if(tcsAdjAmt.compareTo(BigDecimal.ZERO) == 0 && !UtilValidate.isEmpty(tcsInvItmAdj)) {
        	 try {
        		 delegator.removeValue(tcsInvItmAdj);
             } catch (GenericEntityException e) {
            	 e.printStackTrace();
             }
         }else if(tcsAdjAmt.compareTo(BigDecimal.ZERO) != 0) {
             Map<String, Object> createInvoiceItemContext = new HashMap<>();
             createInvoiceItemContext.put("invoiceId", invoiceId);
             createInvoiceItemContext.put("invoiceItemSeqId", "tcs");
             createInvoiceItemContext.put("invoiceItemTypeId", "STAX_COLLECT_SRC");
             createInvoiceItemContext.put("description", "TCS 1%");
             createInvoiceItemContext.put("taxableFlag", "N");
             createInvoiceItemContext.put("amount", tcsAdjAmt);
             //createInvoiceItemContext.put("overrideGlAccountId", adj.get("overrideGlAccountId"));
             createInvoiceItemContext.put("userLogin", userLogin);
             Map<String, Object> createInvoiceItemResult = null;
             
             try {
                 createInvoiceItemResult = dispatcher.runSync("createInvoiceItem", createInvoiceItemContext);
             } catch (GenericServiceException e) {
                 Debug.logError(e, "Service/other problem creating InvoiceItem from order header adjustment", module);
                 return ServiceUtil.returnError("Problem creating InvoiceItem TCS adjustment");
             }
             if (ServiceUtil.isError(createInvoiceItemResult)) {
            	 return ServiceUtil.returnError("Problem creating InvoiceItem roundoff adjustment");
             }
         }
    	
    	return ServiceUtil.returnSuccess();
    }
}