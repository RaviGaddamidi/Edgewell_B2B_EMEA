<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="allowAddToCart" required="true" type="java.lang.Boolean" %>
<%@ attribute name="isMain" required="true" type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<div class="qty">
	<c:if test="${product.purchasable}">
		<label for="qtyInput"><spring:theme code="basket.page.quantity"/></label>
		<input type="text" maxlength="5" size="3" id="qtyInput" name="qtyInput" class="qty" value="${fn:escapeXml(product.moq)}">
	</c:if>

	<c:if test="${product.stock.stockLevel gt 0}">
		<c:set var="productStockLevel">${product.stock.stockLevel}&nbsp;<spring:theme code="product.variants.in.stock"/></c:set>
	</c:if>
	<c:if test="${product.stock.stockLevelStatus.code eq 'inStock' and empty product.stock.stockLevel}">
		<c:set var="productStockLevel"><spring:theme code="product.variants.available"/></c:set>
	</c:if>

	<ycommerce:testId code="productDetails_productInStock_label">
		<p class="stock_message">${productStockLevel}</p>
	</ycommerce:testId>
</div>

<product:productFutureAvailability product="${product}" futureStockEnabled="${futureStockEnabled}" />

<div class="productAddToCartPanel clearfix">
	<div class="obsolete_error">
            	   <c:if test="${product.obsolete}">            	   		
            	   		<spring:theme code="basket.page.obsolete.error"/>
	               </c:if>            	
     </div>
     <div class="moq_error">          	
            		<c:if test="${(product.moq == 0) || (empty product.moq)}">
	               		<spring:theme code="basket.page.moq.notexists"/>
	               	</c:if>
	 </div>    	
	           
	           
	<c:if test="${multiDimensionalProduct}" >
					<sec:authorize ifAnyGranted="ROLE_CUSTOMERGROUP">
						<c:url value="${product.url}/orderForm" var="productOrderFormUrl"/>
						<a href="${productOrderFormUrl}" class="button negative" id="productOrderButton" ><spring:theme code="order.form" /></a>
					</sec:authorize>
				</c:if>
	<form id="addToCartForm" class="add_to_cart_form" action_data="<c:url value="/cart/add"/>" method="post">
	<c:if test="${product.purchasable}">
		<input type="hidden" maxlength="3" size="1" id="qty" name="qty" class="qty" value="${fn:escapeXml(product.moq)}">
	</c:if>
	<input type="hidden" name="productCodePost" value="${product.code}"/>

	<c:if test="${allowAddToCart}">
		<c:set var="buttonType">button</c:set>

		<c:if test="${product.purchasable and product.stock.stockLevelStatus.code ne 'outOfStock' }">
			<c:set var="buttonType">submit</c:set>
		</c:if>

		<c:choose>
			<c:when test="${fn:contains(buttonType, 'button')}">
				<button type="${buttonType}" class="addToCartButton outOfStock" disabled="disabled">
					<spring:theme code="product.variants.out.of.stock"/>
				</button>
			</c:when>
			<c:when test="${product.obsolete eq 'true'}">
				<button id="addToCartButton"  type="button"  disabled="disabled" >
					<spring:theme code="product.variants.obsolete"/>
				</button>			
			</c:when>	
			<c:when test="${product.moq eq '0'}">
				<button id="addToCartButton"  type="button"  disabled="disabled" >
					<spring:theme code="basket.add.to.basket"/>
				</button>			
			</c:when>
			<c:when test="${empty product.uom }">
				<button id="addToCartButton"  type="button"  disabled="disabled" >
					<spring:theme code="product.variants.out.of.stock"/>
				</button>			
			</c:when>					
			<c:otherwise>
				<button id="addToCartButton" type="${buttonType}" class="addToCartButton display_none">
					<spring:theme code="basket.add.to.basket"/>
				</button>
			</c:otherwise>
		</c:choose>
	</c:if>
	</form>

</div>
