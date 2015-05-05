<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="breadcrumb"
	tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>


<template:page pageTitle="${pageTitle}">
	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}" />
	</div>
	<div id="globalMessages">
		<common:globalMessages />
	</div>
	<nav:accountNav selected="fileupload" />
	
		<div>
		<c:if test="${not empty cartData and not empty cartData.shippingPoint}">
		<p><spring:theme code="text.account.quickorder.currentShippingPointInCart"/>${cartData.shippingPoint}</p>
		
		</c:if>
		</div>
		
		
	
	<form:form  name="testform" action="/my-cart/addtocart" id="excelupload" method="post" modelAttribute = "excelUploadForm">
		<table  cellspacing="10" cellpadding="10" border="1">
			<thead>
				<tr>
						<th bordercolor="solid black" id="header1">
						<spring:theme
						code="text.account.excelUpload.orderShipmentPoint" text="Shipping Point" />
						</th>
						
						<%-- <th id="header2"><spring:theme
						code="text.account.excelUpload.productData" text="Product Data" />
						 --%>
									
										<th id="header3" >
										<spring:theme
										code="text.account.excelUpload.materialId" text="Material Id" />
										</th>
								
										<th id="header4">
										<spring:theme
										code="text.account.excelUpload.customerMaterialId" text="Customer Material Id" />
										</th>
						
										<th id="header5">
										<spring:theme
										code="text.account.excelUpload.uom" text="UOM" />
										</th>
						
										<th id="header6">
										<spring:theme
										code="text.account.excelUpload.quantity" text="Quantity" />
										</th>

					</tr>
			
			</thead>
			
			<tbody>
				
				<c:forEach items="${shipmentData}" var="shipmentDetails">

					<tr>
						<td headers="header1" ><ycommerce:testId
								code="orderHistory_orderNumber_link">
								<input type="radio" id='buttonStatus' name="shippingPoint" class="shippingPointRadioButton" value="${shipmentDetails.key}">
								<c:out value="${shipmentDetails.key}" default="Not Available"
									escapeXml="false"></c:out>	
									</ycommerce:testId>
						</td>
								<c:forEach items="${shipmentDetails.value}" var="uploadDataList">
							<tr>
								<td headers="header2" > </td>
								<td headers="header3"><ycommerce:testId
								code="orderHistory_orderNumber_link">
								<c:out value="${uploadDataList.materialId}" default="Not Available"
									escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header4"><ycommerce:testId
								code="orderHistory_orderStatus_label">
								<c:out value="${uploadDataList.customerMaterialId}"
									default="Not Available" escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header5" ><ycommerce:testId
								code="orderHistory_orderDate_label">
								<c:out value="${uploadDataList.uom}" default="Not Available"
									escapeXml="false"></c:out>
								</ycommerce:testId></td>
								<td headers="header6" cellspacing="10" colspan="1"><ycommerce:testId
								code="orderHistory_orderDate_label">
								<%-- <c:out value="${uploadDataList.quantity}" default="Not Available"
									escapeXml="false"></c:out> --%>
									
								<input type="hidden" name="productCode" value="${uploadDataList.materialId}"/>
								<input type="text" name="qty" size="4" class="excelOrderModifiedQuantity" value="${uploadDataList.quantity}"  />
								<input type="hidden" name="moq" class="minorderQuantity" value="${uploadDataList.orderingUnit}" />	
								</ycommerce:testId></td>
						</tr>
							</c:forEach>
					</tr>
				</c:forEach>
				
			</tbody>
			
		</table>
		
		<c:if test="${not empty shipmentData}">
		<div id="addToCartform_button_panel" class="button positive">
				<button class="excelOrderSubmitButton button" id='continueButton' disabled="disabled"> <spring:theme code="text.addToCart" text="Add To cart"/></button>
			</div>		
		</c:if>
			

		
	</form:form>		
	
</template:page>

