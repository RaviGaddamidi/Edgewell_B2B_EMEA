<!-- Content may get change When actual View Invoice functionality will Work -->
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="orderData" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<spring:url value="/my-account/invoice/${orderData.invoiceNumber}?inline=true" var="viewinvoiceUrl" />

<form:form action="${viewinvoiceUrl}" id="viewinvoiceForm" commandName="reorderForm">
	<form:input type="hidden" name="orderCode" path="orderCode" value="${orderData.code}" />
	<button type="submit" class="positive right pad_right re-order" id="viewinvoiceButton">
		<spring:theme code="text.order.viewInvoice" text="viewInvoice"/>
	</button>
</form:form>


