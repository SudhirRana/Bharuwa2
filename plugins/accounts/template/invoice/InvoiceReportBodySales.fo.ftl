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
	<#if billingAddress?has_content>
		<#assign billingAddressStation = billingAddress>
		<#assign state = billingAddressStation.stateProvinceGeoId?default("IN-UT") />
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
													<fo:table-cell width="1.6cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															HSN Code
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="3cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Product Name
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Batch
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Mfg
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm">
															Exp
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
														<fo:block margin-top="1mm" text-align="right" margin-right="1mm">
															${uiLabelMap.CommonQty}
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
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																CGST Amount
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																SGST Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																SGST Amount
															</fo:block>
														</fo:table-cell>
													<#else>
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																IGST Rate
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
															<fo:block margin-top="1mm">
																IGST Amount
															</fo:block>
														</fo:table-cell>
													</#if>
													<fo:table-cell width="2cm" text-align="right">
														<fo:block margin-top="1mm" margin-right="1mm">
															Amount (INR)
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<#assign totalQty = 0/>
										<#if invoiceItems??>
											<#assign invoiceItemIndex = 0 />	
											<#list invoiceItems as invoiceItem>
									            <#assign itemType = invoiceItem.getRelatedOne("InvoiceItemType", false)>
									            <#assign isItemAdjustment = Static["org.apache.ofbiz.entity.util.EntityTypeUtil"].hasParentType(delegator, "InvoiceItemType", "invoiceItemTypeId", itemType.getString("invoiceItemTypeId"), "parentTypeId", "INVOICE_ADJ")/>
									            <#assign description = Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemDescription(dispatcher, invoiceItem, locale)>
												<#if !isItemAdjustment>		
													<#assign invoiceItemIndex = invoiceItemIndex +1 />				
								                    <#assign productId = invoiceItem.productId!>
								                    <#assign productDetail = invoiceItem.getRelatedOne("Product", false)!>
								                    <#assign uomDetail = EntityQuery.use(delegator).from("Uom").where("uomId",productDetail.quantityUomId!).cache().queryOne()?if_exists />
								                    <#assign productHsn = EntityQuery.use(delegator).from("GoodIdentification").where("goodIdentificationTypeId","HS_CODE","productId",productId).cache().queryFirst()?if_exists />
								                    <#assign itemQty = invoiceItem.quantity?default(0)>
								                    <#assign totalQty = totalQty + itemQty />
								                    <#assign gstDetail = EntityQuery.use(delegator).from("InvoiceItem").where("invoiceId", invoiceItem.invoiceId, "parentInvoiceItemSeqId",invoiceItem.invoiceItemSeqId!,"productId",productId).queryFirst()?if_exists />
								                    <#assign gstPer = "" />
								                    <#if gstDetail??><#assign gstPer = gstDetail.description?if_exists?replace('SGST[','')?replace('CGST[','')?replace('IGST[','')?replace(']','')/></#if>
													<#assign itemTax = 0>
								                 	<fo:table border-bottom-style="solid" border-bottom-width="0.5pt">
														<fo:table-body>
															<fo:table-row font-weight="normal" >
																<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		${invoiceItemIndex}.
												                    </fo:block>
																</fo:table-cell>
																<fo:table-cell width="1.6cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																	<#if productHsn??>${productHsn.idValue!}</#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell <#if state == "IN-UT">width="3cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		${description!}
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell <#if state == "IN-UT">width="1cm"<#else>width="6cm"</#if> border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm" text-align="right" margin-right="1mm">
																		${itemQty}
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="1cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		${uomDetail.abbreviation!}
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="2cm" border-right-style="solid" border-right-width="0.5pt">
																	<fo:block margin-top="1mm">
																		<@ofbizCurrency amount=invoiceItem.amount isoCode=invoice.currencyUomId/>
																	</fo:block>
																</fo:table-cell>
																<#if state == "IN-UT">
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstPer??>${gstPer}</#if>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstDetail??>
																				<#assign itemTax = (gstDetail.amount?default(0) * 2) />
																				<@ofbizCurrency amount=gstDetail.amount isoCode=invoice.currencyUomId/>
																			</#if>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstPer??>${gstPer}</#if>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstDetail??><@ofbizCurrency amount=gstDetail.amount isoCode=invoice.currencyUomId/></#if>
																		</fo:block>
																	</fo:table-cell>
																<#else>
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstPer??>${gstPer}</#if>
																		</fo:block>
																	</fo:table-cell>
																	<fo:table-cell width="1.5cm" border-right-style="solid" border-right-width="0.5pt">
																		<fo:block margin-top="1mm">
																			<#if gstDetail??>
																				<#assign itemTax = gstDetail.amount?default(0) />
																				<@ofbizCurrency amount=gstDetail.amount isoCode=invoice.currencyUomId/>
																			</#if>
																		</fo:block>
																	</fo:table-cell>
																</#if>
																<fo:table-cell width="2cm" padding-right="1mm">
																	<fo:block text-align="right" margin-top="1mm">
																		<@ofbizCurrency amount=((Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemTotal(invoiceItem)) + itemTax) isoCode=invoice.currencyUomId!/>
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
														</fo:table-body>
													</fo:table>
												</#if>
											</#list>
										</#if>
										<#-- list orderHeaderAdjustments as orderHeaderAdjustment>
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
						                </#list -->
										
										<fo:table>
											<fo:table-body>
												<fo:table-row border-bottom-style="solid" border-bottom-width="0.5pt" font-size="9pt">
													<fo:table-cell text-align="right" <#if state == "IN-UT">width="6cm"<#else>width="9cm"</#if> padding="4mm"  >
														<fo:block margin-top="1mm">
															Less :Rounded Off(-)
									                    </fo:block>
													</fo:table-cell>
													<fo:table-cell width="2cm" border-bottom-style="solid"  border-bottom-width="0.9pt" padding="4mm 0">
														<fo:block text-align="right" margin-right="1mm">
														</fo:block>
													</fo:table-cell>
													<fo:table-cell <#if state == "IN-UT">width="10cm"<#else>width="7cm"</#if>>
														<fo:block>
														</fo:block>
													</fo:table-cell>
													<fo:table-cell width="2.5cm" padding="4mm 1mm" border-left-style="solid"  border-left-width="0.5pt" border-bottom-style="solid"  border-bottom-width="0.5pt">
														<fo:block text-align="right">
															<@ofbizCurrency amount=invoiceTotal isoCode=invoice.currencyUomId!/>
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
						                            <fo:table-cell font-weight="bold" width="18cm" border-right-style="solid" border-right-width="0.5pt" padding-top="5mm" padding-left="15cm">
						                                <fo:block>
						                                	Grand Total
						                                </fo:block>
						                            </fo:table-cell>
						                            <fo:table-cell width="2.5cm" border-bottom-style="solid"  border-bottom-width="0.9pt" padding="4mm 0">
						                            	<fo:block text-align="right"><@ofbizCurrency amount=invoiceNoTaxTotal isoCode=invoice.currencyUomId!/></fo:block>
						                            </fo:table-cell>
				                        		</fo:table-row>
			                        		</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="20.5cm" text-align="left" padding="2mm">
														<fo:block>
															<#assign word =Static["com.patanjali.order.OrderServices"].convertNumberToWords(invoiceTotal)?if_exists/>
															${word!} Only
														</fo:block>
													</fo:table-cell>
												</fo:table-row>
											</fo:table-body>
										</fo:table>
										<#if invoiceItems??>
											<fo:table font-size="7pt" margin-top="2mm">
												<fo:table-body>
													<fo:table-row font-weight="bold" border-bottom-style="solid" border-bottom-width="0.5pt">
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
														<fo:table-cell width="3cm">
															<fo:block margin="1mm 0">
																 IGST Amt.
															</fo:block>
														</fo:table-cell>
														<fo:table-cell width="1.5cm">
															<fo:block margin="1mm 0">
																Total Tax
															</fo:block>
														</fo:table-cell>
													</fo:table-row>
													<#assign index = 0/>
													<#list invoiceItems as invoiceItem>
														<#assign itemType = invoiceItem.getRelatedOne("InvoiceItemType", false)>
											            <#assign isItemAdjustment = Static["org.apache.ofbiz.entity.util.EntityTypeUtil"].hasParentType(delegator, "InvoiceItemType", "invoiceItemTypeId", itemType.getString("invoiceItemTypeId"), "parentTypeId", "INVOICE_ADJ")/>
											            <#assign description = Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemDescription(dispatcher, invoiceItem, locale)>
														<#if !isItemAdjustment>	
															<#assign index = index +1 />					
										                    <#assign productId = invoiceItem.productId!>
										                    <#assign productDetail = invoiceItem.getRelatedOne("Product", false)!>
										                    <#assign uomDetail = EntityQuery.use(delegator).from("Uom").where("uomId",productDetail.quantityUomId!).cache().queryOne()?if_exists />
										                    <#assign productHsn = EntityQuery.use(delegator).from("GoodIdentification").where("goodIdentificationTypeId","HS_CODE","productId",productId).cache().queryFirst()?if_exists />
										                    <#assign itemQty = invoiceItem.quantity?default(0)>
										                    <#assign totalQty = totalQty + itemQty />
										                    <#assign gstDetail = EntityQuery.use(delegator).from("InvoiceItem").where("invoiceId", invoiceItem.invoiceId, "parentInvoiceItemSeqId",invoiceItem.invoiceItemSeqId!,"productId",productId).cache().queryFirst()?if_exists />
										                    <#assign gstPer = "" />
										                    <#if gstDetail??><#assign gstPer = gstDetail.description?if_exists?replace('SGST[','')?replace('CGST[','')?replace('IGST[','')?replace(']','')?replace('%','')/></#if>
										                    <#assign itemTax = 0>
										                 	
															<fo:table-row font-weight="normal" font-size="8pt">
																<fo:table-cell width="2cm">
																	<fo:block margin="1mm 0">
																	<#if productHsn??>${productHsn.idValue!}</#if>
																	</fo:block>
																</fo:table-cell>
																<#if state == "IN-UT">
																	<fo:table-cell width="1.5cm">
																		<fo:block margin="1mm 0">
																			<#if gstPer?? && gstPer !="" >${(gstPer?number * 2)}  %</#if>
																		</fo:block>
																	</fo:table-cell>
																<#else>
																	<fo:table-cell width="1.5cm">
																		<fo:block margin="1mm 0">
																			<#if gstDetail??>${gstPer}  %</#if>
																		</fo:block>
																	</fo:table-cell>
																</#if>
																<fo:table-cell width="2cm">
																	<fo:block margin="1mm 0">
																		<@ofbizCurrency amount=(Static["org.apache.ofbiz.accounting.invoice.InvoiceWorker"].getInvoiceItemTotal(invoiceItem)) isoCode=invoice.currencyUomId!/>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="3cm">
																	<fo:block margin="1mm 0">
																		<#if gstDetail??><#assign itemTax = gstDetail.amount?default(0) /><@ofbizCurrency amount=gstDetail.amount?default(0) isoCode=invoice.currencyUomId/></#if>
																	</fo:block>
																</fo:table-cell>
																<fo:table-cell width="1.5cm">
																	<fo:block margin="1mm 0">
									                                    <@ofbizCurrency amount=itemTax?default(0.00) isoCode=invoice.currencyUomId/>
																	</fo:block>
																</fo:table-cell>
															</fo:table-row>
														</#if>
													</#list>
												</#if>
											</fo:table-body>
										</fo:table>
										<fo:table>
											<fo:table-body>
												<fo:table-row>
													<fo:table-cell width="20.5cm" text-align="left" padding="2mm" border-top-width="0.5pt" border-top-style="solid">
														<fo:block>
															Bank Details : 
															<#if eftAccount??>Bank Name: ${eftAccount.bankName!}, A/C. No. ${eftAccount.accountNumber!}, IFSC: ${eftAccount.routingNumber!}<#else>----------------------</#if>
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
																			<fo:block>E.&amp; O.E. </fo:block>
																		</fo:table-cell>
																	</fo:table-row>
																	<fo:table-row>
																		<fo:table-cell text-align="left">
																			<fo:block>1. Goods once sold will not be taken back. </fo:block>
																			<fo:block>2. Interest @ 18% p.a. will be charged if the payment is not made with in the stipulated time.</fo:block>
																			<fo:block>3. Subject to 'HARIDWAR' Jurisdiction only. </fo:block>
																			<fo:block>4. Please submit your Sales Tax Declaration witin 15 days. </fo:block>
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