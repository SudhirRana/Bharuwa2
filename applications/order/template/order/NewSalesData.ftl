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

<#if security.hasEntityPermission("ORDERMGR", "_VIEW", session)>
<div class="screenlet">
    <div class="screenlet-title-bar">
      <h3>${uiLabelMap.OrderOrderStatisticsPage}</h3>
    </div>
    <div class="screenlet-body">
        <table class="basic-table" cellspacing='0'>
          <tr>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td align="right">${uiLabelMap.CommonToday}</td>
            <td align="right">${uiLabelMap.OrderWTD}</td>
            <td align="right">${uiLabelMap.OrderMTD}</td>
            <td align="right">${uiLabelMap.OrderYTD}</td>
          </tr>
          <tr>
          <td colspan="2">&nbsp;</td>
          <td colspan="4"><hr /></td></tr>
          <tr>
            <td class="label">${uiLabelMap.OrderOrdersTotals}</td>
            <td colspan="5">&nbsp;</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td>${uiLabelMap.OrderGrossDollarAmountsIncludesAdjustmentsAndPendingOrders}</td>
            <td align="right">${dayItemTotal}</td>
            <td align="right">${weekItemTotal}</td>
            <td align="right">${monthItemTotal}</td>
            <td align="right">${yearItemTotal}</td>
          </tr>
          <tr class="alternate-row">
            <td>&nbsp;</td>
            <td>${uiLabelMap.OrderPaidDollarAmountsIncludesAdjustments}</td>
            <td align="right">${dayItemTotalPaid}</td>
            <td align="right">${weekItemTotalPaid}</td>
            <td align="right">${monthItemTotalPaid}</td>
            <td align="right">${yearItemTotalPaid}</td>
          </tr>
          <tr>
            <td>&nbsp;</td>
            <td>${uiLabelMap.OrderPendingPaymentDollarAmountsIncludesAdjustments}</td>
            <td align="right">${dayItemTotalPending}</td>
            <td align="right">${weekItemTotalPending}</td>
            <td align="right">${monthItemTotalPending}</td>
            <td align="right">${yearItemTotalPending}</td>
          </tr>
          
        </table>
    </div>
</div>
<#else>
  <h3>${uiLabelMap.OrderViewPermissionError}</h3>
</#if>
