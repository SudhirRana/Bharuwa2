<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#escape x as x?xml>
	<fo:block font-size="10pt">
		<fo:table>
			<fo:table-body>
				<fo:table-row>
    				<fo:table-cell padding-top="1.5mm">
						<fo:table>      
    						<fo:table-body>
    							<fo:table-row color="black" font-weight="normal" border-bottom-color="black">
    								<fo:table-cell border="1pt solid black" text-align="center" width="20.5cm" border-bottom-style="solid" border-start-style="solid" border-before-style="solid">
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="5cm">
														<fo:block>
															<#if objectInfo?has_content>
																<fo:external-graphic src="<@ofbizContentUrl>${objectInfo}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" content-width="scale-to-fit"/>
															<#else>
																<fo:external-graphic src="<@ofbizContentUrl>${logoImageUrl!}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" content-width="scale-to-fit"/>
															</#if>
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="15.5cm" font-weight="bold">
														<fo:block font-size="16pt" margin-top="4mm" margin-left="5mm">
															${companyName!}
														</fo:block>
														<fo:block margin-top="1mm" font-size="9pt">
															${postalAddress.address1!}, <#if postalAddress.address2?exists>${postalAddress.address2},</#if>
															<#-- Khasra No. 450,451,452, Vill Lodhiwala,pargana Bhagwanpur, Tehsil Roorkee,-->
														</fo:block>
														<fo:block margin-top="1mm" font-size="9pt">
															${postalAddress.city!} (${stateProvinceName!}) - ${postalAddress.postalCode!}(${countryName!})
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell>
														<fo:block border-bottom-width="0.5pt" border-bottom-style="solid" margin-top="2mm"></fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="15.5cm">
														<fo:block margin-top="2mm" margin-bottom="2mm" font-weight="bold" font-size="15pt" text-align="right">
															CAPITAL PURCHASE ORDER
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="5cm">
														<fo:block margin-top="2mm" margin-bottom="2mm" font-weight="normal" font-size="13pt" text-align="right" margin-right="8mm">
															Page <fo:page-number/> of <fo:page-number-citation ref-id="theEnd"/>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell>
														<fo:block border-bottom-width="0.5pt" border-bottom-style="solid"></fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="10cm">
														<fo:block margin-top="1mm" margin-left="2mm" font-weight="bold" text-align="left">
															PO No. : 
															<fo:inline>
																${orderId}
															</fo:inline>
														</fo:block>
														<fo:block margin-top="1mm" margin-left="2mm" font-weight="normal" text-align="left">
															G.S.T. IN : 
															<fo:inline>
																<#--05AAICP7520K1Z9-->
																<#if sendingPartyTaxId??>${sendingPartyTaxId}</#if>
															</fo:inline>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="10.5cm">
														<fo:block margin-top="1mm" font-weight="bold" text-align="right">
															Release Date:
															<fo:inline padding-right="2mm">
																<#--11.09.2018-->
																${orderHeader.get("orderDate")?string("dd.MM.yyyy")}
															</fo:inline>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell>
														<fo:block border-bottom-width="0.5pt" border-bottom-style="solid" margin-top="1mm"></fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="10cm" border-right="1pt solid black" margin-left="2mm" font-weight="normal" text-align="left">
														<fo:block margin-top="1mm" margin-bottom="1mm" font-weight="bold" background-color="silver">
															Contact Details
														</fo:block>
														<fo:block>
															Contact Person. ${postalAddress.toName!}
														</fo:block>
														<#if email??>
															<fo:block>
																Email id. <fo:inline>${email.infoString!}</fo:inline>
															</fo:block>
														</#if>
														<#if phone??>
															<fo:block>
																Phone No/Extn. <fo:inline><#if phone.countryCode??>${phone.countryCode}-</#if><#if phone.areaCode??>${phone.areaCode}-</#if>${phone.contactNumber!}</fo:inline>
															</fo:block>
														</#if>
														<#if mobile??>
															<fo:block>
																Mobile. 
																<fo:inline><#if mobile.countryCode??>${mobile.countryCode}-</#if>${mobile.contactNumber!}</fo:inline>
															</fo:block>
														</#if>
													</fo:table-cell>
													<fo:table-cell width="10.4cm" margin-left="2mm" font-weight="normal" text-align="left">
														<fo:block margin-top="1mm" margin-bottom="1mm" font-weight="bold" background-color="silver">
															Vendor Information
														</fo:block>
														<fo:block>
															Vendor No:
															<fo:inline>
																${partyId!} 
															</fo:inline>
														</fo:block>
														<#if partyIdentificationList??>
											                <#list partyIdentificationList as partyIdentification>
											                	<fo:block>
																	<#if partyIdentification.partyIdentificationTypeId == "GST">G.S.T. IN<#else>${partyIdentification.partyIdentificationTypeId!}</#if>
																	<fo:inline>
																	 : ${partyIdentification.idValue!} 
																	</fo:inline>
																</fo:block>
											                </#list>
														</#if>
														<#if supplierGeneralContactMechValueMap??>
														 	<#assign supplierAddress = supplierGeneralContactMechValueMap.postalAddress>
															<#if supplierAddress?has_content>
																<fo:block>
																	Vendor Address :
																</fo:block>
															 	<#if supplierAddress.toName?has_content><fo:block>${supplierAddress.toName}</fo:block></#if>
																<#if supplierAddress.attnName?has_content><fo:block>${supplierAddress.attnName!}</fo:block></#if>
																<fo:block>${supplierAddress.address1!}</fo:block>
	                											<#if supplierAddress.address2?has_content><fo:block>${supplierAddress.address2!}</fo:block></#if>
																<fo:block>
												                    <#assign stateGeo = (delegator.findOne("Geo", {"geoId", supplierAddress.stateProvinceGeoId!}, false))! />
												                    ${supplierAddress.city}<#if stateGeo?has_content>, ${stateGeo.geoName!}</#if>, ${supplierAddress.postalCode!}
												                </fo:block>
												                <fo:block>
												                    <#assign countryGeo = (delegator.findOne("Geo", {"geoId", supplierAddress.countryGeoId!}, false))! />
												                    <#if countryGeo?has_content>${countryGeo.geoName!}</#if>
												                </fo:block>
											                </#if>
											                <#if telecomContactMechList??>
												                <#list telecomContactMechList as telecomContactMech>
												                	<#assign telephone = (delegator.findOne("TelecomNumber", {"contactMechId", telecomContactMech.contactMechId!}, false))! />
																	<fo:block>
																		Ph- Mob- <fo:inline><#if telephone.countryCode??>${telephone.countryCode}-</#if><#if telephone.areaCode??>${telephone.areaCode}-</#if>${telephone.contactNumber!}</fo:inline>
																	</fo:block>
																</#list>
															</#if>
															<#if emailContactMechList??>
												                <#list emailContactMechList as emailContactMech>
																	<fo:block>
																		Email- <fo:inline>${emailContactMech.infoString!}</fo:inline>
																	</fo:block>
																</#list>
															</#if>
														<#else>
															<#assign vendorParty = orderReadHelper.getBillFromParty()>
													        <fo:block>
													            <fo:inline font-weight="bold">Vendor Name :</fo:inline> ${Static['org.apache.ofbiz.party.party.PartyHelper'].getPartyName(vendorParty)}
													        </fo:block>
														</#if>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
    								</fo:table-cell>
    							</fo:table-row>
    						</fo:table-body>
						</fo:table>
						<fo:block font-weight="bold" text-align="center" margin-top="1mm">
							Please Supply the following material as per the terms and Conditions mentioned below !!
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</fo:block>
</#escape>