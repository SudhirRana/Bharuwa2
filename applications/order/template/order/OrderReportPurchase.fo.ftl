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
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Verdana, Arial, sans-serif, Ubuntu">
	  <fo:layout-master-set>
	    <fo:simple-page-master master-name="OrderStockPdf"
	              page-width="8.27in" page-height="11.69in"
	              margin-top="0" margin-bottom="0"
	              margin-left="0.1in" margin-right="0.1in">
	            <fo:region-body margin-top="0" margin-bottom="0"/>
	            <fo:region-before extent="0.2in"/>
	            <fo:region-after extent="2in" />
	    </fo:simple-page-master>
	  </fo:layout-master-set>
	  <fo:page-sequence master-reference="OrderStockPdf">
	    <fo:flow flow-name="xsl-region-body">
	        <fo:block font-size="10pt" >
	          ${screens.render(CompanyHeaderPurchase)}
	        </fo:block>
    		<fo:block font-size="8pt">
		 	 	${screens.render(OrderReportBodyPurchase)}
			</fo:block> 
	 		<fo:block id="theEnd"/>
	    </fo:flow>
	  </fo:page-sequence>
	</fo:root>     
</#escape>