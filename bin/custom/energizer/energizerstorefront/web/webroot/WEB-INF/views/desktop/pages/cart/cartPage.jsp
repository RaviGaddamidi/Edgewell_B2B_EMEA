<%@ page trimDirectiveWhitespaces="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="cart" tagdir="/WEB-INF/tags/desktop/cart" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement" %>

<spring:theme text="Your Shopping Cart" var="title" code="cart.page.title"/>
<c:url value="/checkout/single" var="checkoutUrl"/>
<c:url var="containerUrl" value="/cart" />

<template:page pageTitle="${pageTitle}">



	<spring:theme code="basket.add.to.cart" var="basketAddToCart"/>
	<spring:theme code="cart.page.checkout" var="checkoutText"/>
	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	
	<div id="businesRuleErrors">
	</div>
	
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	
	<cart:cartRestoration/>
	<cart:cartValidation/>
	
	<input name="isOrderBlocked" type="hidden" id="isOrderBlocked" value="${cartData.isOrderBlocked }"><br/>	

	
	<cms:pageSlot position="TopContent" var="feature" element="div" class="span-24">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
	
	<!-- displaying products that cannot be added to the cart -->
		     <c:if test="${not empty productList}">
		     
		       <div style="width:100%;margin-bottom: 30px;">
		       	      
		       <br/><br/>
		       <table style="width:100%;" cellspacing="10" cellpadding="10" border="1">
			<thead>
				<tr>
										<th style="border-bottom:1px dotted #a5a5a5;height:30px;line-height:30px;text-transform:uppercase;font-weight:bold;font-size:1em;text-align:center;">
										&nbsp;&nbsp;&nbsp;&nbsp;
										<spring:theme
										code=" ErpMaterialterialId " />
										&nbsp;&nbsp;&nbsp;&nbsp;
										</th> 
										
										<th style="border-bottom:1px dotted #a5a5a5;height:30px;line-height:30px;text-transform:uppercase;font-weight:bold;font-size:1em;text-align:center;">
										&nbsp;&nbsp;&nbsp;&nbsp;
										<spring:theme
										code=" QUANTITY" />
										&nbsp;&nbsp;&nbsp;&nbsp;
										</th> 
																
					</tr>
			
			</thead>
			
			<tbody>
			<c:forEach items="${productList}" var="productList">
			<tr style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;">
			
			<td headers="header3" style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;width:80px;text-align:center;">
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			
			  <c:out value="${productList.key}"  escapeXml="false"></c:out>
			 
			  &nbsp;&nbsp;&nbsp;&nbsp;
			
			</td>
			
			<td headers="header3" style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;width:80px;text-align:center;">
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			
			  <c:out value="${productList.value}"  escapeXml="false"></c:out>
			 
			  &nbsp;&nbsp;&nbsp;&nbsp;
			
			</td>
			
			 
			</tr>
			</c:forEach>
			</tbody>
			
			</table>
		       
		       </div>
		       </c:if>
		
	
	<c:if test="${not empty cartData.entries}">
		<spring:url value="${continueUrl}" var="continueShoppingUrl" htmlEscape="true"/>

			<!--   Start Code changes for order flag check -->
			<c:choose>
				<c:when test="${cartData.isOrderBlocked }">
					<button id ="checkoutButton_top" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>
				</c:when>
				<c:when test="${errorMessages}">
					<button id ="checkoutButton_tops" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>			
				</c:when>
				<c:otherwise>
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
				</c:otherwise>
			</c:choose>
			
			
			<!--   Start Code changes for order flag check -->			
			<cart:cartItems cartData="${cartData}"/>
			
			<div >
			<form:form  name="containerform" action="cart" id="containeroptimization" method="post" commandName ="containerUtilizationForm" >
		 
			  <label ><spring:theme code="container.height" /></label> 
			   <form:select path="containerHeight">
   
                   <form:options items="${containerHeightList}" />
               </form:select>
					
					
				 &nbsp;&nbsp;<label ><spring:theme code="packing.type" /></label>
				 <form:select path="packingType">
   
                   <form:options items="${packingOptionList}" />
               </form:select>
				 
				
					
					<button id ="container_Optamization"  style="position:absolute;left:900px;" class="positive"  type="submit"  ><spring:theme code="basket.your.shopping.container.optimization" /></button> 
					
					<!--  <div class="form-actions"">
					<ycommerce:testId code="update_button" >
						<button class="positive" type="submit"  >
							<spring:theme code="basket.your.shopping.container.optimization" />
						</button>
					</ycommerce:testId>
				</div>-->
					
					</form:form>
					
					</div>
					
					
				<div class="clearfix fixthis_row_cls">
				<br><br><br><br>
					<div class="span-16">
					
						<div class="cntutil_wrapper_cls">
				
							 
							 
									<div class="cnt_utl_cls"><spring:theme code="basket.your.shopping.container.utilization"/></div>
				                      	<div id="volume_cont">
				                        <div class="divider_20"><span class="span_cls">40ftHC</span></div>
				                      	<div class="divider_40"><span class="span_cls">20ft</span></div>
				                    	<div class="cnt_utlvolfill_cls"><span id="utl_vol">${cartData.totalProductVolumeInPercent}</span>%</div>
				                    	<div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.volume"/></div>
				                        <div style="height: 69px;" id="volume_utilization"></div>                                                                           
				                    </div>                                                   
							
								
							
									<div id="weight_cont">
				                       <div class="cnt_utlwilfill_cls"><span id="utl_wt">${cartData.totalProductWeightInPercent}</span>%</div>
				                       <div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.weight"/></div>                                                                             
				       				   <div style="height: 135px;" id="weight_utilization"></div>
				                    </div>
							
						</div>
						
						<div class="clearfix"><!-- --></div>
						<div>
							<cart:cartPromotions cartData="${cartData}"/>
							
							<cart:cartPotentialPromotions cartData="${cartData}"/>
						&nbsp;
					</div>
					</div>
					<div class="span-8 last">
						<cart:ajaxCartTotals/>
						<cart:cartTotals cartData="${cartData}" showTaxEstimate="true"/> 
						
					</div>
				</div>

	<!-- displaying product  that cannot be double stacked-->	
	
	<c:if test="${not empty productsNotDoubleStacked}">
		     
		       <div style="width:100%;margin-bottom: 30px;">
		       
		       <spring:theme code="product.notdoublestacked" />
		       <br/><br/>
		       <table  style="width:100%;" cellspacing="10" cellpadding="10" border="1">
			<thead>
				<tr>
					
										
										<th style="border-bottom:1px dotted #a5a5a5;height:30px;line-height:30px;text-transform:uppercase;font-weight:bold;font-size:1em;text-align:center;">
										&nbsp;&nbsp;&nbsp;&nbsp;
										<spring:theme
										code=" ERPMATERIALID " />
										&nbsp;&nbsp;&nbsp;&nbsp;
										</th> 
										
										<th style="border-bottom:1px dotted #a5a5a5;height:30px;line-height:30px;text-transform:uppercase;font-weight:bold;font-size:1em;text-align:center;">
										&nbsp;&nbsp;&nbsp;&nbsp;
										<spring:theme
										code=" Matching Products " />
										&nbsp;&nbsp;&nbsp;&nbsp;
										</th> 
						
					</tr>
				</thead>
			
			<tbody >
			<c:forEach items="${productsNotDoubleStacked}" var="productList">
			<tr style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;">
			
			        <td headers="header3" style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;width:80px;text-align:center;">
			         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			           <c:out value="${productList.key}"  escapeXml="false"></c:out>
			            &nbsp;&nbsp;&nbsp;&nbsp;
			         </td>
			
			         
                     <td style="vertical-align:top;border-bottom:1px dotted #a5a5a5;padding:20px 0 0 0;width:80px;text-align:center;">
			         &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			           <c:out value="${productList.value}"  escapeXml="false"></c:out>
			            &nbsp;&nbsp;&nbsp;&nbsp;
			         </td>
	
			 
			</tr>
			</c:forEach>
			</tbody>
			
			</table>
		       
		       </div>
		       </c:if>
	       
		       
			<!--   Start Code changes for order flag check  for continueShop button -->
			<c:choose>
				<c:when test="${cartData.isOrderBlocked }">
					<!-- a class="button continueShop-button" disabled="disabled" style="height: 30px;padding-top: 8px;font-size: 140%;margin-top: 20px;border-color: #169e08;" href="${continueShoppingUrl}"><spring:theme text="Continue Shopping" code="cart.page.continue"/></a-->
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:when>
				<c:when test="${errorMessages}">
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:when>
				<c:otherwise>
					<!-- a class="button continueShop-button"  style="height: 30px;padding-top: 8px;font-size: 140%;margin-top: 20px;border-color: #169e08;" href="${continueShoppingUrl}"><spring:theme text="Continue Shopping" code="cart.page.continue"/></a>-->
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" ><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:otherwise>
			</c:choose>
			<!--   End Code changes for order flag check  for continueShop button -->	
				
				
			<!--   Start Code changes for order flag check -->
			<c:choose>
				<c:when test="${cartData.isOrderBlocked }">
					<button id ="checkoutButton_bottom" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>			
				</c:when>
				<c:when test="${errorMessages}">
					<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>			
				</c:when>
				<c:otherwise>
					<button id ="checkoutButton_bottom" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
				</c:otherwise>
			</c:choose>
			<!--   Start Code changes for order flag check -->	
				
		
	</c:if>
	
	<c:if test="${empty cartData.entries}">
		<div class="span-24">
			<div class="span-24 wide-content-slot cms_disp-img_slot">
				<cms:pageSlot position="MiddleContent" var="feature" element="div">
					<cms:component component="${feature}"/>
				</cms:pageSlot>

				<cms:pageSlot position="BottomContent" var="feature" element="div">
					<cms:component component="${feature}"/>
				</cms:pageSlot>
			</div>
		</div>
	</c:if>

	<c:if test="${not empty cartData.entries}" >
		<cms:pageSlot position="Suggestions" var="feature" element="div" class="span-24">
			<cms:component component="${feature}"/>
		</cms:pageSlot>
	</c:if>
	
	<cms:pageSlot position="BottomContent" var="feature" element="div" class="span-24">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
</template:page>
