<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="order" required="true"
	type="de.hybris.platform.commercefacades.order.data.AbstractOrderData"%>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>


<div class="orderList">
	<div class="headline"><spring:theme code="text.account.orderHistory.loadingDetails" /></div>
	<table class="orderListTable orderListApprovTbl-cls">
		
		<tbody>
		   
		    <tr>
		        <td>
		            <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerType"/>&nbsp;:&nbsp;
		               <c:if test="${not empty order.containerHeight }">
		                 ${order.containerHeight}
		               </c:if>
		            </p>
		        </td>
		        <td>
		            <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.fullPallets"/>&nbsp;:&nbsp;
		                <c:if test="${not empty order.totalPalletCount }">
		                  ${order.totalPalletCount}
		                </c:if>
		            </p>
		        </td>
		    </tr> 
		    
		     <tr>
		        <td>
		             <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerPackingMaterial"/>&nbsp;:&nbsp;
		                 <c:if test="${not empty order.containerPackingType }">
		                    ${order.containerPackingType}
		                 </c:if>
		             </p>
		        </td>
		        <td>
		              <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.partialPallets"/>&nbsp;:&nbsp;
		                   <c:if test="${not empty order.partialPalletCount }">
		                     ${order.partialPalletCount}
		                   </c:if>
		              </p>
		        </td>
		    </tr>   
		
		
		</tbody>
	</table>
	<br/>
	<br/>
	<div align="left" style="font-size:11px;color: blue; ">
        <spring:theme code="text.account.orderHistory.loadingDetails.disclaimer"/>
    </div>  
</div>

   