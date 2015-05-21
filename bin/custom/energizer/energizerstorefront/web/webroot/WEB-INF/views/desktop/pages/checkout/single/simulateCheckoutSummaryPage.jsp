<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart"%>
<%@ taglib prefix="user" tagdir="/WEB-INF/tags/desktop/user"%>
<%@ taglib prefix="formElement"
	tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="checkout" tagdir="/WEB-INF/tags/desktop/checkout"%>
<%@ taglib prefix="single-checkout"
	tagdir="/WEB-INF/tags/desktop/checkout/single"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="breadcrumb"
	tagdir="/WEB-INF/tags/desktop/nav/breadcrumb"%>

<spring:url value="/checkout/single/placeOrder" var="placeOrderUrl" />
<spring:url value="/checkout/single/termsAndConditions"
	var="getTermsAndConditionsUrl" />

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb"></div>

	<div id="globalMessages">
		<common:globalMessages />
	</div>

	<single-checkout:simulateSummaryFlow />

	<div id="placeOrder" class="clearfix">
		<form:form action="${placeOrderUrl}" id="placeOrderForm1" commandName="placeOrderForm">
			<formElement:formCheckbox idKey="Terms1PlaceOrder" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
			<button type="submit" id="simulatePlaceOrderId1" class="positive right placeOrderButton">
				<spring:theme code="simulateCheckout.summary.placeOrder" />
			</button>
		</form:form>
	</div>
	<div id="checkoutOrderDetails">
		<checkout:cartItems cartData="${cartData1}" />
		<div class="span-16 ">
			<cart:cartPromotions cartData="${cartData1}" />
			&nbsp;
		</div>
		<div class="span-8 last ">
			<checkout:cartTotals cartData="${cartData1}" showTaxEstimate="true"/> 
		</div>
	</div>

	<div class="span-24">
		<form:form action="${placeOrderUrl}" id="placeOrderForm2" commandName="placeOrderForm">
			<formElement:formCheckbox idKey="Terms2PlaceOrder" labelKey="checkout.summary.placeOrder.readTermsAndConditions" inputCSS="checkbox-input" labelCSS="checkbox-label" path="termsCheck" mandatory="true" />
			<button type="submit" id="simulatePlaceOrderId2" class="positive right placeOrderButton" >
				<spring:theme code="simulateCheckout.summary.placeOrder" />
			</button>
		</form:form>
	</div>

</template:page>
