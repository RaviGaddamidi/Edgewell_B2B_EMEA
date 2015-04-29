<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
 <jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" /> 


<div class="orderBox address">
	<div class="headline"><spring:theme code="text.account.orderHistory.shippingDetaiils" text="Shipping Details"/></div>
	
		<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.shippingDetaiils"/>&nbsp;: </p>	
		<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerNumber"/>&nbsp;:  </p>
		<p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.carrierName"/>&nbsp;:  </p>
		<p class="reduce_space_cls"><spring:theme code="product.product.details.future.date" />&nbsp;:
			<c:if test="${not empty order.requestedDeliveryDate }">
				 ${DateTimeUtil.displayDate(order.requestedDeliveryDate)} 
			</c:if>
		</p>
</div>
