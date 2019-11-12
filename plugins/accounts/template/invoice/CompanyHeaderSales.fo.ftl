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
	<#macro displayAddress address type>
	    <fo:table-row>
			<fo:table-cell width="3.5cm" text-align="left">
	    		<fo:block font-weight="bold">
						${type} to <fo:inline padding-left="12mm">:</fo:inline>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell width="3.5cm" text-align="left">
				<fo:block font-weight="bold">
	           		${address.address1!}<#if address.address2?has_content>${address.address2!}</#if>
	                <#assign stateGeo = (delegator.findOne("Geo", {"geoId", address.stateProvinceGeoId!}, false))! />
	                ${address.city!}<#if stateGeo?has_content>, ${stateGeo.geoName!}</#if>${address.postalCode!}
	                <#assign countryGeo = (delegator.findOne("Geo", {"geoId", address.countryGeoId!}, false))! />
	                <#if countryGeo?has_content>${countryGeo.geoName!}</#if>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell width="2cm" text-align="left"><fo:block><#if address.toName??>${address.toName!}</#if></fo:block></fo:table-cell>
		</fo:table-row>
	</#macro>
	
	<#macro displayEmailContactMech emailContactMechList>
		<#list emailContactMechList as emailContactMech>
			<fo:table-row margin-top="2mm">
				<fo:table-cell width="2cm" text-align="left">
					<fo:block>Party Email Id <fo:inline padding-left="5mm">:</fo:inline></fo:block>
				</fo:table-cell>
				<fo:table-cell width="2cm" text-align="left">
					<fo:block>${emailContactMech.infoString!}</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</#list>
	</#macro>
	<#macro displayTelecomContactMech telecomContactMechList>
		<#list telecomContactMechList as telecomContactMech>
			<#assign telephone = (delegator.findOne("TelecomNumber", {"contactMechId", telecomContactMech.contactMechId!}, false))! />
			<fo:table-row margin-top="2mm">
				<fo:table-cell width="2cm" text-align="left">
					<fo:block>Party Mohali No <fo:inline padding-left="5mm">:</fo:inline></fo:block>
				</fo:table-cell>
				<fo:table-cell width="2cm" text-align="left">
					<fo:block><#if telephone.countryCode??>${telephone.countryCode}-</#if><#if telephone.areaCode??>${telephone.areaCode}-</#if>${telephone.contactNumber!}</fo:block>
				</fo:table-cell>
			</fo:table-row>
		</#list>
	</#macro>
	
	<#macro displayPartyIdentification partyIdentificationList>
	    <#list partyIdentificationList as partyIdentification>
	    	<fo:table-row margin-top="2mm">
	    		<fo:table-cell width="2cm" text-align="left">
					<fo:block><#if partyIdentification.partyIdentificationTypeId == "GST">G.S.T. IN<#else>${partyIdentification.partyIdentificationTypeId!}</#if><fo:inline padding-left="16mm">:</fo:inline></fo:block>
				</fo:table-cell>
				<fo:table-cell width="1cm" text-align="left">
					<fo:block>${partyIdentification.idValue!}</fo:block>
				</fo:table-cell>
			</fo:table-row>
	    </#list>
	</#macro>
	<fo:block font-size="10pt">
		<fo:table>
			<fo:table-body>
				<fo:table-row>
    				<fo:table-cell padding-top="1.5mm">
						<fo:table>      
    						<fo:table-body>
    							<fo:table-row color="black" font-weight="normal">
    								<fo:table-cell width="20.5cm" border-style="solid" border-width="0.5pt" border-start-style="solid" border-before-style="solid">
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="6.5cm" padding="1mm" border-style="solid" border-width="0.5pt" border-start-style="solid" border-before-style="solid">
														<fo:block margin-top="4mm" text-decoration="underline" text-align="left" text-transform="uppercase">
															<fo:external-graphic src=""  width="100%" content-height="scale-to-fit" />
															<#if objectInfo?has_content>
																<fo:external-graphic src="<@ofbizContentUrl>${objectInfo}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" />
															<#elseif logoImageUrl?has_content>
																<fo:external-graphic src="<@ofbizContentUrl>${logoImageUrl!}</@ofbizContentUrl>"  width="100%" content-height="scale-to-fit" />
															<#else>
																<fo:external-graphic src="<@ofbizContentUrl>/images/patanjali.png</@ofbizContentUrl>"  width="10%" content-height="scale-to-fit" />
															</#if>
														</fo:block>
														<fo:block font-size="9pt" font-weight="bold" text-transform="uppercase">
															${companyName!}
														</fo:block>
														<fo:block font-size="7.5pt" margin-top="1mm">
															${postalAddress.address1!},<#if postalAddress.address2?exists>${postalAddress.address2},</#if>
															<#-- Khasra No. 450,451,452, Vill Lodhiwala,pargana Bhagwanpur, Tehsil Roorkee,-->
														</fo:block>
														<fo:block font-size="7.5pt" margin-top="1mm" text-transform="uppercase">
															${postalAddress.city!} (${stateProvinceName!}) - ${postalAddress.postalCode!}(${countryName!})
														</fo:block>
														<fo:block font-size="7.5pt" margin-top="1mm">
															<#if phone??>
																<fo:inline>Phone : <#if phone.countryCode??>${phone.countryCode}-</#if><#if phone.areaCode??>${phone.areaCode}-</#if>${phone.contactNumber!}</fo:inline>
															</#if>
														</fo:block>
														<fo:block font-size="7.5pt" margin-top="1mm">
																<fo:inline>Website : www.patanjalibio.com</fo:inline>
														</fo:block>
														<#-- fo:block margin-top="1mm">
															G.S.T. IN : 
															<fo:inline>
																<#if sendingPartyTaxId??>${sendingPartyTaxId}</#if>
															</fo:inline>
														</fo:block -->
														<fo:block font-size="7.5pt" margin-top="1mm">
															<#if email??>
																<fo:inline> Email : ${email.infoString!}</fo:inline>
															</#if>
														</fo:block>
														<fo:block font-size="7.5pt" margin-top="1mm">
																<fo:inline> State Code/state Name : 05/UTTARAKHAND</fo:inline>
														</fo:block>
														<#if companyPartyIdentificationList??>
											                <#list companyPartyIdentificationList?sort_by("partyIdentificationTypeId") as partyIdentification>
											                	<fo:block font-size="7.5pt" margin-top="1mm">
																	<#if partyIdentification.partyIdentificationTypeId == "GST">G.S.T. IN<#else>${partyIdentification.partyIdentificationTypeId!}</#if>
																	<fo:inline>
																	 : ${partyIdentification.idValue!} 
																	</fo:inline>
																</fo:block>
											                </#list>
														</#if>
													</fo:table-cell>
													<fo:table-cell>
														<fo:table>
															<fo:table-body>
																<fo:table-row>
																	<fo:table-cell width="7cm" font-weight="normal">
																		<fo:block margin-top="2mm" margin-bottom="2mm" margin-left="1mm">
																			<fo:inline padding-left="4cm" font-weight="bold" margin-bottom="2mm">TAX INVOICE</fo:inline>
																			<fo:table>
																				<fo:table-body>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Invoice No.</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>${invoice.invoiceId}</fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Date of Invoice</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>${invoice.invoiceDate?string("dd-MM-yyyy")}</fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Place of Supply</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Uttarakhand (05)</fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																							<fo:table-cell width="2cm" text-align="left">
																								<fo:block>Reverse Charge</fo:block>
																							</fo:table-cell>
																							<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																							<fo:table-cell width="2cm" text-align="left">
																								<fo:block>N</fo:block>
																							</fo:table-cell>
																						</fo:table-row>
																				</fo:table-body>
																			</fo:table>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="7cm" font-weight="normal">
																		<fo:block margin-top="7mm" margin-bottom="2mm" margin-left="1mm">
																			<fo:table>
																				<fo:table-body>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Transport</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>N .A.</fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Vehicle No</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block>Station</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="3cm" text-align="left">
																							<fo:block wrap-option="no-wrap">
																							<#if shippingAddress?has_content>
																				        		<#assign shippingAddressStation = shippingAddress>
																				        		<#assign stateGeo = (delegator.findOne("Geo", {"geoId", shippingAddressStation.stateProvinceGeoId!}, false))! />
																			                    ${shippingAddressStation.city!}<#if stateGeo?has_content>, ${stateGeo.geoName!}</#if>
																			        		</#if>
																							</fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<fo:table-row>
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block>E-Way Bill No</fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																				</fo:table-body>
																			</fo:table>
																		</fo:block>
																	</fo:table-cell>
																</fo:table-row>
															</fo:table-body>
														</fo:table>
														<fo:table>
															<fo:table-body>
																<fo:table-row>
																	<fo:table-cell width="7cm"  font-weight="normal" border-width="0.5pt" border-style="solid">
																		<fo:block margin-top="2mm" margin-bottom="2mm" margin-left="1mm" >
																			<fo:table>
																				<fo:table-body>
																				    <#if billingAddress??>
																				        <#assign billAddress =  billingAddress>
																				        <#if billAddress?has_content>
																				        	<@displayAddress address=billAddress type="Billed"/>
																						</#if>
																				    </#if>
																				    <fo:table-row margin-top="2mm">
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block>Party PAN<fo:inline padding-left="14.5mm">:</fo:inline></fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="5cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<#if emailContactMechList?has_content>
																						<@displayEmailContactMech emailContactMechList=emailContactMechList/>
																					<#else>
																						<fo:table-row margin-top="2mm">
																							<fo:table-cell width="2cm" text-align="left">
																								<fo:block>Party Email Id <fo:inline padding-left="8mm">:</fo:inline></fo:block>
																							</fo:table-cell>
																							<fo:table-cell width="5cm" text-align="left">
																								<fo:block></fo:block>
																							</fo:table-cell>
																						</fo:table-row>
																					</#if>
																					<#if telecomContactMechList??>
																						<@displayTelecomContactMech telecomContactMechList=telecomContactMechList/>
																					<#else>
																						<fo:table-row margin-top="2mm">
																							<fo:table-cell width="4cm" text-align="left">
																								<fo:block>Party Mobile No.</fo:block>
																							</fo:table-cell>
																							<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																							<fo:table-cell width="5cm" text-align="left">
																								<fo:block></fo:block>
																							</fo:table-cell>
																						</fo:table-row>
																					</#if>
																					<fo:table-row margin-top="2mm">
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block>State<fo:inline padding-left="22.5mm">:</fo:inline></fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="5cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<#if partyIdentificationList??>
																						<@displayPartyIdentification partyIdentificationList=partyIdentificationList/>
																					</#if>
																				</fo:table-body>
																			</fo:table>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="7cm" font-weight="normal" border-width="0.5pt" border-style="solid">
																		<fo:block margin-top="2mm" margin-bottom="2mm" margin-left="1mm">
																			<fo:table>
																				<fo:table-body>
																				    <#if shippingAddress??>
																				        <#assign shipAddress = shippingAddress>
																				        <#if shipAddress?has_content>
																				        	<@displayAddress address=shipAddress type="Shipped"/>
																						</#if>
																					<#else>
																						<#if billingAddress??>
																					        <#assign shipAddress = billingAddress>
																					        <#if shipAddress?has_content>
																					        	<@displayAddress address=shipAddress type="Shipped"/>
																							</#if>
																					    </#if>
																				    </#if>
																				    <fo:table-row margin-top="2mm">
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block>Party PAN<fo:inline padding-left="14.5mm">:</fo:inline></fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="5cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<#if emailContactMechList?has_content>
																                		<@displayEmailContactMech emailContactMechList=emailContactMechList/>
																					<#else>
																						<fo:table-row margin-top="2mm">
																							<fo:table-cell width="4cm" text-align="left">
																								<fo:block>Party Email Id <fo:inline padding-left="8mm">:</fo:inline></fo:block>
																							</fo:table-cell>
																							<fo:table-cell width="5cm" text-align="left">
																								<fo:block></fo:block>
																							</fo:table-cell>
																						</fo:table-row>
																					</#if>
																					<#if telecomContactMechList??>
																                		<@displayTelecomContactMech telecomContactMechList=telecomContactMechList/>
																					<#else>
																						<fo:table-row margin-top="2mm">
																							<fo:table-cell width="4cm" text-align="left">
																								<fo:block>Party Mobile No.</fo:block>
																							</fo:table-cell>
																							<fo:table-cell width="1cm" text-align="left"><fo:block>:</fo:block></fo:table-cell>
																							<fo:table-cell width="5cm" text-align="left">
																								<fo:block></fo:block>
																							</fo:table-cell>
																						</fo:table-row>
																					</#if>
																					<fo:table-row margin-top="2mm">
																						<fo:table-cell width="2cm" text-align="left">
																							<fo:block>State<fo:inline padding-left="22.5mm">:</fo:inline></fo:block>
																						</fo:table-cell>
																						<fo:table-cell width="5cm" text-align="left">
																							<fo:block></fo:block>
																						</fo:table-cell>
																					</fo:table-row>
																					<#if partyIdentificationList??>
																		                <@displayPartyIdentification partyIdentificationList=partyIdentificationList/>
																					</#if>
																				</fo:table-body>
																			</fo:table>
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
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</fo:block>
</#escape>