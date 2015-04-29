<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ attribute name="entry" required="true" type="de.hybris.platform.commercefacades.order.data.OrderEntryData" %>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>

<c:url value="${entry.product.url}" var="productUrl"/>
<tr class="item item_order_cls">
	<td headers="header2" class="thumb">
		<a href="${productUrl}">
			<product:productPrimaryImage product="${entry.product}" format="thumbnail"/>
		</a>
	</td>
	<td headers="header2" class="details">				
		<ycommerce:testId code="orderDetails_productName_link">
			<div class="itemName"><a href="${entry.product.purchasable ? productUrl : ''}">${entry.product.name}</a></div>
		</ycommerce:testId>
						
		<c:forEach items="${entry.product.baseOptions}" var="option">
			<c:if test="${not empty option.selected and option.selected.url eq entry.product.url}">
				<c:forEach items="${option.selected.variantOptionQualifiers}" var="selectedOption">
					<dl>
						<dt>${selectedOption.name}:</dt>
						<dd>${selectedOption.value}</dd>
					</dl>
				</c:forEach>
			</c:if>
		</c:forEach>
  
		<c:if test="${not empty order.appliedProductPromotions}">
			<ul>
				<c:forEach items="${order.appliedProductPromotions}" var="promotion">
					<c:set var="displayed" value="false"/>
					<c:forEach items="${promotion.consumedEntries}" var="consumedEntry">
						<c:if test="${not displayed && consumedEntry.orderEntryNumber == entry.entryNumber}">
							<c:set var="displayed" value="true"/>
							<li><span>${promotion.description}</span></li>
						</c:if>
					</c:forEach>
				</c:forEach>
			</ul>
		</c:if> 
	</td>
	<td headers="header4" class="quantity">
		<ycommerce:testId code="orderDetails_productQuantity_label">${entry.quantity}</ycommerce:testId>
	</td>
	
	<td headers="header5">
		<ycommerce:testId code="orderDetails_productItemPrice_label">
			<c:choose>
				<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value)}">
					<format:price priceData="${entry.basePrice}" displayFreeForZero="true"/>
				</c:when>
				<c:otherwise>
					<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="true"/>
					-
					<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="true"/>
				</c:otherwise>
			</c:choose>
		</ycommerce:testId>
	</td>
	<td headers="header6" class="total">
		<ycommerce:testId code="orderDetails_productTotalPrice_label"><format:price priceData="${entry.totalPrice}" displayFreeForZero="true"/></ycommerce:testId>
	</td>
	<td headers="header7" class="adjustedquantity">
		<ycommerce:testId code="orderDetails_productQuantity_label">${entry.adjustedQty}</ycommerce:testId>
	</td>
	<td headers="header8" class="adjustedPrice">
		<ycommerce:testId code="orderDetails_productItemPrice_label">
			<c:choose>
				<c:when test="${not entry.product.multidimensional or (entry.product.priceRange.minPrice.value eq entry.product.priceRange.maxPrice.value)}">
					<format:price priceData="${entry.basePrice}" displayFreeForZero="true"/>
				</c:when>
				<c:otherwise>
					<format:price priceData="${entry.product.priceRange.minPrice}" displayFreeForZero="true"/>
					
					<format:price priceData="${entry.product.priceRange.maxPrice}" displayFreeForZero="true"/>
				</c:otherwise>
			</c:choose>
		</ycommerce:testId>
	</td>
	<td headers="header9" class="adjustedTotalPrice">
		<ycommerce:testId code="orderDetails_productQuantity_label" ><fmt:formatNumber value="${entry.adjustedLinePrice}" maxFractionDigits="0"/></ycommerce:testId>
	</td>
	<td headers="header10" class="rejected">
		<ycommerce:testId code="orderDetails_productQuantity_label">${entry.rejectedStatus}</ycommerce:testId>
	</td>
	<td headers="header14" class="rejected">
		<ycommerce:testId code="orderDetails_MaterialId_label">${entry.product.code}</ycommerce:testId>
	</td>
	<td headers="header15" class="rejected">
		<ycommerce:testId code="orderDetails_CMIRId_label">${entry.product.customerMaterialId}</ycommerce:testId>
	</td>
<!-- 	<td headers="header16" class="rejected">
		<ycommerce:testId code="orderDetails_custProductName_label">${entry.product.customerProductName}</ycommerce:testId>
	</td>
	<td headers="header17" class="rejected">
		<ycommerce:testId code="orderDetails_CustSpecificPrice_label">${entry.product.customerProductPrice}</ycommerce:testId>
	</td>
	<td headers="header18" class="rejected">
		<ycommerce:testId code="orderDetails_ShipFrom_label">${entry.product.shippingPoint}</ycommerce:testId>
	</td>
	<td headers="header19" class="rejected">
		<ycommerce:testId code="orderDetails_MOU_label">${entry.product.moq}</ycommerce:testId>
	</td>
	<td headers="header20" class="rejected">
		<ycommerce:testId code="orderDetails_UOM_label">${entry.product.uom}</ycommerce:testId>
	</td>
	 -->
</tr>
			