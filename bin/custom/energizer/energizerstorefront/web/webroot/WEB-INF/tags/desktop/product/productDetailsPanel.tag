<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="product" required="true" type="de.hybris.platform.commercefacades.product.data.ProductData" %>
<%@ attribute name="galleryImages" required="true" type="java.util.List" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>

<spring:theme code="text.addToCart" var="addToCartText"/>

<div class="productDetailsPanel">

	<product:productImagePanel product="${product}" galleryImages="${galleryImages}"/>

	<div class="span-10 productDescription last">
		<c:choose>
			<c:when test="${ empty product.customerProductPrice }">
				<ycommerce:testId code="productDetails_productNamePrice_label_${product.erpMaterialID}">
					<product:productPricePanel product="${product}"  table="false" />
				</ycommerce:testId>
			</c:when>
			<c:otherwise>
				 <%--   Energizer customer Price --%> 
				 <p class="big-price right">
					${product.customerPriceCurrency} ${product.customerProductPrice} 
				</p>
			 </c:otherwise>
		</c:choose>
		
		<ycommerce:testId code="productDetails_productNamePrice_label_${product.code}">
			<h1>
					${fn:escapeXml(product.name)}
			</h1>
		</ycommerce:testId>

		<%-- product:productReviewSummary product="${product}"/--%>


		<div class="summary">
			${fn:escapeXml(product.summary)}<br>
			<spring:theme code="basket.page.MaterialId"/>  : ${fn:escapeXml(product.code)}<br>
			<spring:theme code="basket.page.customerMaterialId"/>  : ${fn:escapeXml(product.customerMaterialId)}<br>
			<spring:theme code="basket.page.customerProductName"/>  : ${fn:escapeXml(product.customerProductName)}<br>
			<spring:theme code="basket.page.shipFrom"/> : ${fn:escapeXml( product.shippingPoint)}  <br>
			<spring:theme code="basket.page.moq"/> : ${fn:escapeXml(product.moq)}<br>
			<spring:theme code="basket.page.uom"/> : ${fn:escapeXml(product.uom)}<br>
			<spring:theme code="basket.page.baseuom.convertion"/> : ${fn:escapeXml(product.baseUOM)}<br>
			<spring:theme code="basket.page.segmentName"/> : ${fn:escapeXml(product.segmentName)}<br>			
			<spring:theme code="basket.page.familyName"/> : ${fn:escapeXml(product.familyName)}<br>
			<spring:theme code="basket.page.groupName"/> : ${fn:escapeXml(product.groupName)}<br>				
			<%-- 
			spring:theme code="basket.page.Weight"/> : ${fn:escapeXml(product.weight)}<br>
			<spring:theme code="basket.page.weightUom"/> : ${fn:escapeXml(product.weightUom)}<br>
			<spring:theme code="basket.page.volume"/> : ${fn:escapeXml(product.volume)}<br>
			<spring:theme code="basket.page.volumeUom"/> : ${fn:escapeXml(product.volumeUom)}<br>
			--%>
			<%-- obsolete : ${product.obsolete}<br>	  --%>		
			
		</div>
		
		<product:productPromotionSection product="${product}"/>

		<cms:pageSlot position="VariantSelector" var="component" element="div">
			<cms:component component="${component}"/>
		</cms:pageSlot>

		<cms:pageSlot position="AddToCart" var="component" element="div">
			<cms:component component="${component}"/>
		</cms:pageSlot>

		<%-- <product:productShareTag/> --%>
	</div>

	<cms:pageSlot position="Section2" var="feature" element="div" class="span-8 section2 cms_disp-img_slot last">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
</div>
