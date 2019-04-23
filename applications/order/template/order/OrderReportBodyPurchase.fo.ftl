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
    				<fo:table-cell>
						<fo:table>      
    						<fo:table-body>
    							<fo:table-row color="black" font-weight="normal" border-bottom-color="black">
    								<fo:table-cell border="1pt solid black" text-align="center" width="20.5cm" border-bottom-style="solid" border-start-style="solid" border-before-style="solid">
										<fo:table>
											<fo:table-body>
												<fo:table-row font-weight="bold" >
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															Item
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															Material
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															Description
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															HSN
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															${uiLabelMap.OrderQuantity}
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															UOM
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															Unit Price(INR)
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm">
														<fo:block margin-top="1mm">
															Dis%
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.27cm" >
														<fo:block margin-top="1mm" margin-right="4mm">
															Net Amount(INR)
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										
										<#if orderItems??>
											<#list orderItemList as orderItem>
												<#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
							                    <#assign productId = orderItem.productId!>
							                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
							                    <#assign itemAdjustment = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, false, false)>
							                    <#assign internalImageUrl = Static["org.apache.ofbiz.product.imagemanagement.ImageManagementHelper"].getInternalImageUrl(request, productId!)!>
							                 	<fo:table>
													<fo:table-body>
														<fo:table-row font-weight="normal">
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	${orderItem_index+1}
											                    </fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	<#if orderItem.supplierProductId?has_content>
									                                    ${orderItem.supplierProductId}
									                                <#elseif productId??>
									                                    ${orderItem.productId?default("N/A")}
									                                <#elseif orderItemType??>
									                                    ${orderItemType.get("description",locale)}
									                                </#if><#-- 23869 -->
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	${orderItem.itemDescription!}
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	${remainingQuantity}
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	PC
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	<@ofbizCurrency amount=orderItem.unitPrice isoCode=currencyUomId/>
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2.27cm">
																<fo:block margin-top="1mm">
																	<#if orderItem.statusId != "ITEM_CANCELLED">
									                                    <@ofbizCurrency amount=Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments) isoCode=currencyUomId/>
									                                <#else>
									                                    <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
									                                </#if>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</fo:table-body>
												</fo:table>
												<fo:table>
													<fo:table-body>
														<fo:table-row>
															<fo:table-cell width="20.5cm" font-weight="normal" margin-left="30mm" text-align="left">
																<fo:block margin-top="2mm">
																	Taxes: CGST and SGST: 18% - Input
											                    </fo:block>
											                    <#if orderItem.estimatedShipDate??>
								                             		<fo:block>
								                                    	${uiLabelMap.OrderEstimatedShipDate} : ${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDate(orderItem.estimatedShipDate, "dd.MM.yyyy", locale, timeZone)!}
									                             	</fo:block>
										                        </#if>
										                        <#if orderItem.estimatedDeliveryDate??>
									                            	<fo:block>
									                                    ${uiLabelMap.OrderOrderQuoteEstimatedDeliveryDate} : ${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDate(orderItem.estimatedDeliveryDate, "dd.MM.yyyy", locale, timeZone)!}
									                               	</fo:block>
										                        </#if>
																<#assign orderItemShipGroupAssocs = orderItem.getRelated("OrderItemShipGroupAssoc", null, null, false)!>
										                        <#if orderItemShipGroupAssocs?has_content>
										                            <#list orderItemShipGroupAssocs as shipGroupAssoc>
										                                <#assign shipGroup = shipGroupAssoc.getRelatedOne("OrderItemShipGroup", false)>
										                                <#assign shipGroupAddress = shipGroup.getRelatedOne("PostalAddress", false)!>
										                                <#assign stateGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipGroupAddress.stateProvinceGeoId).queryOne() />
										                                <#assign countryGeo = EntityQuery.use(delegator).from("Geo").where("geoId", shipGroupAddress.countryGeoId).queryOne() />
									                                	<fo:block> Unloading Point :
								                                        	${shipGroupAddress.address1?default("${uiLabelMap.OrderNotShipped}")}
									                                 	</fo:block>
									                                 	<fo:block>
																			<#if shipGroupAddress.address2?exists>${shipGroupAddress.address2},</#if>
																			${shipGroupAddress.city!} (${stateGeo.geoName!}) - ${shipGroupAddress.postalCode!}(${countryGeo.geoName!})
													                    </fo:block>
										                            </#list>
										                        </#if>
															</fo:table-cell>
														</fo:table-row>
													</fo:table-body>
												</fo:table>
											</#list>
										</#if>
										
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="12cm">
														<fo:block margin-top="2mm">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="3cm" font-weight="bold" text-align="left">
														<fo:block margin-top="2mm">
															Net Value
									                    </fo:block>
									                    <fo:block>
															Freight
									                    </fo:block>
									                    <fo:block>
															SGST/UTGST
									                    </fo:block>
									                    <fo:block>
															CGST
									                    </fo:block>
									                    <fo:block>
															IGST
									                    </fo:block>
									                    <fo:block>
															Insurance %
									                    </fo:block>
									                    <fo:block>
															Pack. and For.
									                    </fo:block>
									                    <fo:block>
															Gross Value
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="1cm" font-weight="bold" text-align="center">
														<fo:block margin-top="2mm">
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
									                    <fo:block>
															-
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="4cm" font-weight="bold" text-align="right">
														<fo:block margin-top="2mm">
															<@ofbizCurrency amount=orderSubTotal isoCode=currencyUomId/>
									                    </fo:block>
									                    <fo:block >
															<@ofbizCurrency amount=shippingAmount isoCode=currencyUomId/>
									                    </fo:block>
									                    <fo:block>
															1620.00
									                    </fo:block>
									                    <fo:block>
															1620.00
									                    </fo:block>
									                    <fo:block>
															0.00
									                    </fo:block>
									                    <fo:block>
															0.00
									                    </fo:block>
									                    <fo:block>
															0.00
									                    </fo:block>
									                    <fo:block>
															<@ofbizCurrency amount=grandTotal isoCode=currencyUomId/>
									                    </fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										
    								</fo:table-cell>
    							</fo:table-row>
    						</fo:table-body>
						</fo:table>
						<fo:block font-weight="bold" text-align="left" margin-top="1mm">
							Total Amount in words : Twenty one thousand two hundred forty rupees only
						</fo:block>
						<fo:block font-weight="bold" text-align="left" margin-top="1mm">
							Payment Terms : 30 days from the date of invoice
						</fo:block>
						<fo:block font-weight="bold" text-align="left" margin-top="1mm">
							Delivery Terms : UN FREIGHT EXTRA
						</fo:block>
						<fo:block font-weight="bold" text-align="right" margin-top="20mm" margin-right="5mm">
							For ${companyName!}
						</fo:block>
						<fo:block font-weight="bold" text-align="right" margin-top="13mm" margin-right="5mm">
							Shri Muniraj Singh Pundir
						</fo:block>
						<fo:block font-weight="bold" text-align="right" margin-top="1mm" margin-right="5mm">
							Business Head-Biscuit Division
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</fo:block>
</#escape>