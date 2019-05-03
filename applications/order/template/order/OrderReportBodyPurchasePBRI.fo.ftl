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
	<#assign state = "IN-UT" />
	<#if supplierGeneralContactMechValueMap??>
	 	<#assign supplierAddress = supplierGeneralContactMechValueMap.postalAddress>
		<#if supplierAddress?has_content>
			<#assign supplierAddressStation = supplierAddress>
			<#assign state = supplierAddressStation.stateProvinceGeoId?default("IN-UT") />
		</#if>
	</#if>
	<fo:block font-size="10pt">
		<fo:table>
			<fo:table-body>
				<fo:table-row>
    				<fo:table-cell>
						<fo:table>      
    						<fo:table-body>
    							<fo:table-row color="black" font-weight="normal">
    								<fo:table-cell text-align="center" width="20.5cm" border-style="solid" border-width="0.5pt" border-start-style="solid" border-before-style="solid">
										<fo:table>
											<fo:table-body>
												<fo:table-row font-weight="bold" border-bottom-style="solid" border-bottom-width="0.5pt" font-size="9pt">
													<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															S.N.
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="3cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Description of Goods
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															HSN/SAC Code
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2cm" text-align="right" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm" margin-right="1mm">
															${uiLabelMap.OrderQuantity}
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Unit
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Price
														</fo:block>
													</fo:table-cell>
													<#if state == "IN-UT">
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																CGST Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																CGST Amount
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																SGST Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																SGST Amount
															</fo:block>
														</fo:table-cell>
													<#else>
														<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																IGST Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																IGST Amount
															</fo:block>
														</fo:table-cell>
													</#if>
													<fo:table-cell width="2.5cm" text-align="right">
														<fo:block margin-top="1mm" margin-right="1mm">
															Amount (INR)
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<#assign totalQty = 0/>
										<#if orderItems??>
											<#list orderItemList as orderItem>
												<#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
							                    <#assign productId = orderItem.productId!>
							                    <#assign productDetail = orderItem.getRelatedOne("Product", false)!>
							                    <#assign uomDetail = EntityQuery.use(delegator).from("Uom").where("uomId",productDetail.quantityUomId!).cache().queryOne()?if_exists />
							                    <#assign productHsn = EntityQuery.use(delegator).from("GoodIdentification").where("goodIdentificationTypeId","HS_CODE","productId",productId).cache().queryFirst()?if_exists />
							                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
							                    <#assign totalQty = totalQty + remainingQuantity />
												<#assign gstDetail = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFirst(orderItem.getRelated("OrderAdjustment", null,null, false)!)!/>
							                    <#assign itemTax = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, true, false)>
							                    <#assign internalImageUrl = Static["org.apache.ofbiz.product.imagemanagement.ImageManagementHelper"].getInternalImageUrl(request, productId!)!>
							                 	<fo:table border-bottom-style="solid" border-bottom-width="0.5pt">
													<fo:table-body>
														<fo:table-row font-weight="normal" >
															<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm">
																	${orderItem_index+1}.
											                    </fo:block>
															</fo:table-cell>
															<fo:table-cell <#if state == "IN-UT">width="3cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm">
																	${orderItem.itemDescription!}
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm">
																<#if productHsn??>${productHsn.idValue!}</#if>
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2cm" text-align="right" border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm" margin-right="1mm">
																	${remainingQuantity}
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm">
																	${uomDetail.abbreviation!}
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																<fo:block margin-top="1mm">
																	<@ofbizCurrency amount=orderItem.unitPrice isoCode=currencyUomId/>
																</fo:block>
															</fo:table-cell>
															<#if state == "IN-UT">
																<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??>${gstDetail.sourcePercentage!} %</#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??>${gstDetail.sourcePercentage!}  %</#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
															<#else>
																<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??>${gstDetail.sourcePercentage!}  %</#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
															</#if>
															<fo:table-cell width="2.5cm" padding-right="1mm">
																<fo:block text-align="right" margin-top="1mm">
																	<#if orderItem.statusId != "ITEM_CANCELLED">
																		<#assign itemTotal = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments) />
																		<#assign itemTotal = itemTotal + itemTax?default(0.00) />
									                                    <@ofbizCurrency amount=itemTotal isoCode=currencyUomId/>
									                                <#else>
									                                    <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
									                                </#if>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</fo:table-body>
												</fo:table>
											</#list>
										</#if>
										<#list orderHeaderAdjustments as orderHeaderAdjustment>
						                    <#assign adjustmentType = orderHeaderAdjustment.getRelatedOne("OrderAdjustmentType", false)>
						                    <#assign adjustmentAmount = Static["org.apache.ofbiz.order.order.OrderReadHelper"].calcOrderAdjustment(orderHeaderAdjustment, orderSubTotal)>
						                    <#if adjustmentAmount != 0>
						                        <fo:table>
													<fo:table-body>
														<fo:table-row border-bottom-style="solid" border-bottom-width="0.5pt" font-size="9pt">
								                            <fo:table-cell width="18cm" border-right-style="solid" border-right-width="0.5pt" padding="4mm">
								                                <fo:block>
								                                    <#if orderHeaderAdjustment.get("description")?has_content>
								                                        ${orderHeaderAdjustment.get("description")!}
								                                    </#if>
								                                </fo:block>
								                            </fo:table-cell>
								                            <fo:table-cell width="2.5cm" padding="4mm 1mm">
								                            	<fo:block text-align="right"><@ofbizCurrency amount=orderSubTotal+taxAmount isoCode=currencyUomId/></fo:block>
								                                <fo:block text-align="right"><@ofbizCurrency amount=adjustmentAmount isoCode=currencyUomId/></fo:block>
								                            </fo:table-cell>
						                        		</fo:table-row>
					                        		</fo:table-body>
												</fo:table>
						                    </#if>
						                </#list>
										<fo:table>
											<fo:table-body>
												<fo:table-row font-size="10pt">
													<fo:table-cell text-align="right" <#if state == "IN-UT">width="6cm"<#else>width="9cm"</#if> padding="4mm"  >
														<fo:block margin-top="1mm">
															Grand Total
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="2cm" border-bottom-style="solid"  border-bottom-width="0.9pt" padding="4mm 0">
														<fo:block text-align="right" margin-right="1mm">
															 ${totalQty!}
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="10cm"<#else>width="7cm"</#if>>
														<fo:block>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.5cm" padding="4mm 1mm" border-left-style="solid"  border-left-width="0.5pt" border-bottom-style="solid"  border-bottom-width="0.5pt">
														<fo:block text-align="right">
															<@ofbizCurrency amount=Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderGrandTotal(orderItems, orderAdjustments)?if_exists isoCode=currencyUomId/>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<#if orderItems??>
											<fo:table font-size="7pt" margin-top="2mm">
												<fo:table-body>
													<fo:table-row font-weight="normal" border-bottom-style="solid" border-bottom-width="0.5pt">
														<fo:table-cell width="1cm">
															<fo:block margin="1mm 0">
																S.N.
										                    </fo:block>
														</fo:table-cell>
														<fo:table-cell width="2cm">
															<fo:block margin="1mm 0">
															HSN/SAC
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm">
															<fo:block margin="1mm 0">
																Tax Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="2cm">
															<fo:block margin="1mm 0">
																Taxable Amt
															</fo:block>
														</fo:table-cell>
														<#if state == "IN-UT">
															<fo:table-cell width="1.5cm">
																<fo:block margin="1mm 0">
																	 CGST Amt.
																</fo:block>
															</fo:table-cell>
															<fo:table-cell width="1.5cm">
																<fo:block margin="1mm 0">
																	 SGST Amt.
																</fo:block>
															</fo:table-cell>
														<#else>
															<fo:table-cell width="3cm">
																<fo:block margin="1mm 0">
																	 IGST Amt.
																</fo:block>
															</fo:table-cell>
														</#if>
														<fo:table-cell width="1.5cm">
															<fo:block margin="1mm 0">
																Total Tax
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
													<#list orderItemList as orderItem>
														<#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
									                    <#assign productId = orderItem.productId!>
									                    <#assign productDetail = orderItem.getRelatedOne("Product", false)!>
									                    <#assign uomDetail = EntityQuery.use(delegator).from("Uom").where("uomId",productDetail.quantityUomId!).cache().queryOne()?if_exists />
									                    <#assign productHsn = EntityQuery.use(delegator).from("GoodIdentification").where("goodIdentificationTypeId","HS_CODE","productId",productId).cache().queryFirst()?if_exists />
									                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
									                    <#assign gstDetail = Static["org.apache.ofbiz.entity.util.EntityUtil"].getFirst(orderItem.getRelated("OrderAdjustment", null,null, false)!)!/>
									                    <#assign itemTax = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, true, false)>
									                    <#assign internalImageUrl = Static["org.apache.ofbiz.product.imagemanagement.ImageManagementHelper"].getInternalImageUrl(request, productId!)!>
							                 	
														<fo:table-row font-weight="normal" font-size="8pt">
															<fo:table-cell width="1cm">
																<fo:block margin="1mm 0">
																	${orderItem_index+1}.
											                    </fo:block>
															</fo:table-cell>
															<fo:table-cell width="2cm">
																<fo:block margin="1mm 0">
																<#if productHsn??>${productHsn.idValue!}</#if>
																</fo:block>
															</fo:table-cell>
															<#if state == "IN-UT">
																<fo:table-cell width="1.5cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??>${gstDetail.sourcePercentage*2}  %</#if>
																	</fo:block>
																</fo:table-cell>
															<#else>
																<fo:table-cell width="1.5cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??>${gstDetail.sourcePercentage}  %</#if>
																	</fo:block>
																</fo:table-cell>
															</#if>
															<fo:table-cell width="2cm">
																<fo:block margin="1mm 0">
																	<#if orderItem.statusId != "ITEM_CANCELLED">
									                                    <@ofbizCurrency amount=Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments) isoCode=currencyUomId/>
									                                <#else>
									                                    <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
									                                </#if>
																</fo:block>
															</fo:table-cell>
															<#if state == "IN-UT">
																<fo:table-cell width="1.5cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="1.5cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
															<#else>
																<fo:table-cell width="3cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
															</#if>
															<fo:table-cell width="1.5cm">
																<fo:block margin="1mm 0">
																	<#if orderItem.statusId != "ITEM_CANCELLED">
									                                    <@ofbizCurrency amount=itemTax?default(0.00) isoCode=currencyUomId/>
									                                <#else>
									                                    <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
									                                </#if>
																</fo:block>
															</fo:table-cell>
														</fo:table-row>
													</#list>
												</#if>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="20.5cm" text-align="left" padding="2mm">
														<fo:block>
															<#assign toatlAmount = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderGrandTotal(orderItems, orderAdjustments)?if_exists>
															<#assign word =Static["com.patanjali.order.OrderServices"].convertNumberToWords(toatlAmount)?if_exists/>
															${word!} Only
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="20.5cm" text-align="center" padding="2mm" border-top-width="0.5pt" border-top-style="solid">
														<fo:block font-weight="bold" text-decoration="underline">
															Delivery Terms
														</fo:block>
														<fo:block> 
															Supply of material is on time is essential part of PO. Delay in supply will attract a penalty@1% per week to max of 5% of PO value. In case of abnornal delay we may procure the matertial from alternate source at your risk or cancel the order. 
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="10cm" border-style="solid" border-width="0.5pt" font-weight="normal" font-size="9pt">
														<fo:block margin-top="2mm"  margin-bottom="2mm" margin-left="1mm">
															<fo:table>
																<fo:table-body>
																	<fo:table-row>
																		<fo:table-cell text-align="left">
																			<fo:block><fo:inline border-bottom-style="solid" border-bottom-width="0.5pt" font-size="7pt">Terms &amp; Conditions</fo:inline></fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="left">
																			<fo:block>1. Payment:100% within 30 days (OR after QC Report).</fo:block>
																			<fo:block>2. Delivery period: Within a day from the date of PO.</fo:block>
																			<fo:block>3. Delivery: FOR / Actual at PBRI, Haridwar.</fo:block>
																			<fo:block>4. The Supplier will be responsible for any damages to any good/materials for want or lack of proper packing </fo:block>
																			<fo:block>5. All dispute subject to Haridwar Jurisdiction.</fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																</fo:table-body>
															</fo:table>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="10.5cm" border-style="solid" border-width="0.5pt">
														<fo:block margin="2mm 0">
															<fo:table>
																<fo:table-body>
																	<fo:table-row>
																		<fo:table-cell text-align="left" border-bottom-style="solid" border-bottom-width="0.5pt" padding="1mm 1mm 5mm">
																			<fo:block font-size="7pt">Receiver&apos;s Signature :</fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="right" padding="2mm 1mm 6mm">
																			<fo:block>For ${companyName!} </fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="right" padding-right="2mm">
																			<fo:block>Authorised Signatory</fo:block>
																		</fo:table-cell>
																	</fo:table-row>
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
	</fo:block>
</#escape>