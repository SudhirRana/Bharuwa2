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
    								<fo:table-cell border="1pt solid black" width="20.5cm" border-bottom-style="solid" border-start-style="solid" border-before-style="solid">
										<#if partyIdentificationList??>
							                <#list partyIdentificationList as partyIdentification>
												<fo:block padding="2mm" margin-left="1mm" font-weight="bold">
													<#if partyIdentification.partyIdentificationTypeId == "GST">
														GSTIN :
													<#else>
														${partyIdentification.partyIdentificationTypeId!}
													</#if>
													<fo:inline>${partyIdentification.idValue!} </fo:inline>
												</fo:block>
							                </#list>
										</#if>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													
													<fo:table-cell width="20.5cm" padding="1mm" text-align="center">
														<fo:block margin-top="4mm" text-decoration="underline" text-align="left" text-transform="uppercase">
															<#if objectInfo?has_content>
																<fo:external-graphic src="<@ofbizContentUrl>${objectInfo}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" />
															<#elseif logoImageUrl?has_content>
																<fo:external-graphic src="<@ofbizContentUrl>${logoImageUrl!}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" />
															<#else>
																<fo:external-graphic src="<@ofbizContentUrl>/images/patanjali.png</@ofbizContentUrl>"  width="10%" content-height="scale-to-fit" />
															</#if>
															<fo:inline padding-left="2.7in">Purchase Order</fo:inline>
														</fo:block>
														<fo:block font-size="17pt" font-weight="bold" text-transform="uppercase">
															${companyName!}
														</fo:block>
														<fo:block margin-top="1mm" text-transform="uppercase">
															${postalAddress.address1!}, <#if postalAddress.address2?exists>${postalAddress.address2},</#if>
															<#-- Khasra No. 450,451,452, Vill Lodhiwala,pargana Bhagwanpur, Tehsil Roorkee,-->
														</fo:block>
														<fo:block margin-top="1mm" text-transform="uppercase">
															${postalAddress.city!} (${stateProvinceName!}) - ${postalAddress.postalCode!}(${countryName!})
														</fo:block>
														<#if companyPartyIdentificationList??>
											                <#list companyPartyIdentificationList?sort_by("partyIdentificationTypeId") as partyIdentification>
											                	<fo:block margin-top="1mm">
																	<#if partyIdentification.partyIdentificationTypeId == "GST">G.S.T. IN<#else>${partyIdentification.partyIdentificationTypeId!}</#if>
																	<fo:inline>
																	 : ${partyIdentification.idValue!} 
																	</fo:inline>
																</fo:block>
											                </#list>
														</#if>
														<#-- fo:block margin-top="1mm">
															G.S.T. IN : 
															<fo:inline>
																<#if sendingPartyTaxId??>${sendingPartyTaxId}</#if>
															</fo:inline>
														</fo:block -->
														<fo:block font-size="7pt" font-weight="bold">
															<#if phone??>
																<fo:inline>Tel. : <#if phone.countryCode??>${phone.countryCode}-</#if><#if phone.areaCode??>${phone.areaCode}-</#if>${phone.contactNumber!}</fo:inline>
															</#if>
															<#if email??>
																<fo:inline> Email : ${email.infoString!}</fo:inline>
															</#if>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell text-align="left" width="10cm" border-style="solid" border-width="0.5pt" font-weight="normal">
														<fo:block margin-top="2mm"  margin-bottom="2mm" margin-left="1mm">
															Invoice :
														</fo:block>
														<fo:block text-align="left" margin-top="2mm"  margin-bottom="2mm" margin-left="1mm">
															Date of Invoice :
														</fo:block>
													</fo:table-cell>
													<fo:table-cell text-align="left" width="10.5cm" border-style="solid" border-width="0.5pt" font-weight="normal">
														<fo:block margin-top="2mm"  margin-bottom="2mm" margin-left="1mm">
														</fo:block>
													</fo:table-cell>	
												</fo:table-row>
											</fo:table-body>
										</fo:table>		
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="10cm" border-style="solid" border-width="0.5pt" font-weight="normal">
														<fo:block margin-top="2mm"  margin-bottom="2mm" margin-left="1mm">
															<fo:table>
																<fo:table-body>
																	<fo:table-row>
																		<fo:table-cell width="4cm" text-align="left">
															        		<fo:block font-weight="bold">
																				Billed to :
																			</fo:block>
														        		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="left" width="9cm" padding-top="2mm" padding-bottom="4mm">
																			<fo:block wrap-option="no-wrap">
																                <#if supplierGeneralContactMechValueMap??>
																				 	<#assign supplierAddress = supplierGeneralContactMechValueMap.postalAddress>
																					<#if supplierAddress?has_content>
																					 	<#if supplierAddress.toName?has_content><fo:block>${supplierAddress.toName!}</fo:block></#if>
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
																				<#else>
																					<#assign vendorParty = orderReadHelper.getBillFromParty()>
																			        <fo:block>
																			            <fo:inline font-weight="bold">Party Name :</fo:inline> ${Static['org.apache.ofbiz.party.party.PartyHelper'].getPartyName(vendorParty)}
																			        </fo:block>
																				</#if>
																			</fo:block>
																		</fo:table-cell>
																		<fo:table-cell width="0.5cm" text-align="left" padding-top="2mm" padding-bottom="4mm"><fo:block></fo:block></fo:table-cell>
																		<fo:table-cell width="0.5cm" text-align="left" padding-top="2mm" padding-bottom="4mm"><fo:block></fo:block></fo:table-cell>
																	</fo:table-row>
																	<#if partyIdentificationList??>
														                <#list partyIdentificationList as partyIdentification>
														                	<fo:table-row padding-top="1mm">
														                		<fo:table-cell width="4cm" text-align="left">
																					<fo:block><#if partyIdentification.partyIdentificationTypeId == "GST">GSTIN / UIN<#else>${partyIdentification.partyIdentificationTypeId!}</#if></fo:block>
																				</fo:table-cell>
																				<fo:table-cell width="4cm" text-align="left">
																					<fo:block>:<fo:inline padding-left="2mm">${partyIdentification.idValue!}</fo:inline></fo:block>
																				</fo:table-cell>
																			</fo:table-row>
														                </#list>
																	</#if>
																</fo:table-body>
															</fo:table>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="10.5cm"  font-weight="normal" border-style="solid" border-width="0.5pt">
														<fo:block margin-top="2mm" margin-bottom="2mm" margin-left="1mm">
															<fo:table>
																<fo:table-body>
																	<fo:table-row>
																		<fo:table-cell width="4cm" text-align="left" font-weight="bold">
																			<fo:block>Shipped To :</fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="left" width="9cm" padding-top="2mm" padding-bottom="4mm">
																			<fo:block wrap-option="no-wrap">
																                <#if supplierGeneralContactMechValueMap??>
																				 	<#assign supplierAddress = supplierGeneralContactMechValueMap.postalAddress>
																					<#if supplierAddress?has_content>
																					 	<#if supplierAddress.toName?has_content><fo:block>${supplierAddress.toName!}</fo:block></#if>
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
																				<#else>
																					<#assign vendorParty = orderReadHelper.getBillFromParty()>
																			        <fo:block>
																			            <fo:inline font-weight="bold">Party Name :</fo:inline> ${Static['org.apache.ofbiz.party.party.PartyHelper'].getPartyName(vendorParty)}
																			        </fo:block>
																				</#if>
																			</fo:block>
																		</fo:table-cell>
																		<fo:table-cell width="0.5cm" text-align="left" padding-top="2mm" padding-bottom="4mm"><fo:block></fo:block></fo:table-cell>
																		<fo:table-cell width="0.5cm" text-align="left" padding-top="2mm" padding-bottom="4mm"><fo:block></fo:block></fo:table-cell>
																	</fo:table-row>
																	<#if partyIdentificationList??>
														                <#list partyIdentificationList as partyIdentification>
														                	<fo:table-row padding-top="1mm">
														                		<fo:table-cell width="4cm" text-align="left">
																					<fo:block><#if partyIdentification.partyIdentificationTypeId == "GST">GSTIN / UIN<#else>${partyIdentification.partyIdentificationTypeId!}</#if></fo:block>
																				</fo:table-cell>
																				<fo:table-cell width="4cm" text-align="left">
																					<fo:block>:<fo:inline padding-left="2mm">${partyIdentification.idValue!}</fo:inline></fo:block>
																				</fo:table-cell>
																			</fo:table-row>
														                </#list>
																	</#if>
																</fo:table-body>
															</fo:table>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
												<fo:table-row>
													<fo:table-cell text-align="left" padding="2mm">
														<fo:block>
															Order No : 
															<fo:inline>${orderId!}</fo:inline>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
    								</fo:table-cell>
    							</fo:table-row>
    						</fo:table-body>
						</fo:table>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</fo:block>
</#escape>