//package src.main.java.org.patanjali;
package org.patanjali;


import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.util.EntityQuery;
import org.apache.ofbiz.party.party.PartyWorker;

public class CommonWorker {
	 public static final String module = CommonWorker.class.getName();

	 public static String  getPartyGstn(Delegator delegator, String partyId) {
		 String partyGstn = "";
		 try {
			GenericValue partyIdentification = EntityQuery.use(delegator).from("PartyIdentification").where("partyId", partyId,"partyIdentificationTypeId","GST").cache().queryOne();
			if(!UtilValidate.isEmpty(partyIdentification)) {
				partyGstn = partyIdentification.getString("idValue");
			}
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 
		 return partyGstn;
	 }
	 
	 public static String  getPartyName(Delegator delegator, String partyId) {
		 String supplierName = "";
		 try {
			GenericValue partyGroup = EntityQuery.use(delegator).from("PartyGroup").where("partyId", partyId).cache().queryOne();
			if(!UtilValidate.isEmpty(partyGroup)) {
				supplierName = partyGroup.getString("groupName");
			}
		} catch (GenericEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return supplierName;
	 }
	 
}
	    
	    
