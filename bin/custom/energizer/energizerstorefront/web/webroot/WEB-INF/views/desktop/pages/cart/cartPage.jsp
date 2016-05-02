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

<spring:theme text="Your Shopping Cart" var="title" code="cart.page.title"/>
<c:url value="/checkout/single" var="checkoutUrl"/>
<c:url value="/cart/clearCart" var="clearCartUrl"/>

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
	
 	<c:if test="${not empty cartData.productsNotAddedToCart}">
	<div id="productsNotAddedToCart" class="alert negative"><spring:theme code="product.notadded.container"/></div>
	<div >
	<table style="padding: 0px; border-collapse: collapse;" class="productsNotAdded alert negative" border="1">
		<thead class="alert negative">
			<tr>
			<th class="alert negative" id="header1" colspan="2"><spring:theme code="errorMessages.erpmaterialid"/></th>

				<th id="header2" colspan="2"> <spring:theme code="errorMessages.Quantity"/></th>
			</tr>
			</thead>
			<tbody class="alert negative">
				<c:forEach items="${cartData.productsNotAddedToCart}" var="entry">
				<tr><td class="alert negative prod_det" colspan="2" headers="header1">${entry.key}</td>
				 <td headers="header2" colspan="2">&nbsp;&nbsp;&nbsp;${entry.value}</td></tr>
			 </c:forEach>
			</tbody>
			</table>
	
	</div>	 
	</c:if> 
	
	<cart:cartRestoration/>
	<cart:cartValidation/>
	
	<input name="isOrderBlocked" type="hidden" id="isOrderBlocked" value="${cartData.isOrderBlocked }"><br/>	

	
	<cms:pageSlot position="TopContent" var="feature" element="div" class="span-24">
		<cms:component component="${feature}"/>
	</cms:pageSlot>
	
	<c:if test="${not empty cartData.entries}">
		<spring:url value="${continueUrl}" var="continueShoppingUrl" htmlEscape="true"/>
		
		 <!--  Adding container optimizationbutton and 2 radio buttons -->
            
               
			
				<br>
				
				<c:if test="${enableForB2BUnit}">
				<form id="form" name="form" action="cart" method="post"  >
				  <c:choose>
				<c:when test="${enableButton}">  
				
				<input type="radio" name="choice"  value="Yes" onclick="submitForm()"  checked="checked"><spring:theme code="enable.containerOptimization"/><br>
				
				<input type="radio" name="choice" value="No"   onclick="submitForm()" ><spring:theme code="disable.containerOptimization"/> 
			
                 
                 </c:when>
                 <c:otherwise>
                 
                 
				<input type="radio" name="choice"  value="Yes" onclick="submitForm()"  ><spring:theme code="enable.containerOptimization"/><br>
				
				<input type="radio" name="choice" value="No"  checked="checked" onclick="submitForm()" ><spring:theme code="disable.containerOptimization"/> 
                 
                 </c:otherwise>
                 
                 </c:choose>
                 </form>
                 </c:if>
                 <br><br>
                 <div >
                 <c:if test="${enableButton}">
			<form:form  name="containerform" action="cart" id="containeroptimization" method="post" commandName ="containerUtilizationForm" >
		 
			  <label ><spring:theme code="container.height"/></label> 
			   <form:select id="containerHght" name="containerHght" path="containerHeight" onChange="getPackingOptionChange(this);">
                      <form:options items="${containerHeightList}" selected='' />
                </form:select>
					
				 &nbsp;&nbsp;<label ><spring:theme code="packing.type" /></label>
				 <form:select id="packingTypeForm"  path="packingType">
				        <form:options id="packingOptionsId" items="${packingOptionList}" />
   			     </form:select>
				
			  <!-- <button id ="container_Optamization"  style="position:absolute;left:900px;" class="positive"  type="submit"  ><spring:theme code="basket.your.shopping.container.optimization" /></button> --> 
					<br>
					<div class="form-actions">
					<ycommerce:testId code="update_button" >
						<button class="positive" type="submit" style="margin-top:20px;font-size: 130%;"  >
							<spring:theme code="basket.your.shopping.container.optimization" />
						</button>
					</ycommerce:testId>
				      </div>
					</form:form>
				</c:if>
				</div>	
				
				
		<div> 		
          
			<!--   Start Code changes for order flag check -->
			<c:choose>
				<c:when test="${cartData.isOrderBlocked} ">
					<button id ="checkoutButton_top" class="checkoutButtonRed positive right" type="button" data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.blocked.order" /></button>
				</c:when>
				<c:when test="${ not empty cartData.productsNotAddedToCart}">
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
				</c:when>
				<c:when test="${cartData.isFloorSpaceFull}">
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
				</c:when>
				<c:otherwise>
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button" data-checkout-url="${checkoutUrl}"><spring:theme code="checkout.checkout" /></button>
				</c:otherwise>
			</c:choose>
			
			<form:form action="/cart/clearCart"  method="get">
         		 <button id="clearCartButton" class="checkoutButton positive right" style="font-size: 140%;" type="submit" ><spring:theme code="basket.your.shopping.clearCart" /></button>
			</form:form>
		</div>	
				<!--   Start Code changes for order flag check -->	
				
					<cart:cartItems cartData="${cartData}" />
			
			<div class="clearfix fixthis_row_cls">
					<div class="span-16">
					
						<div class="cntutil_wrapper_cls">
							 
							
				                      
				                  	<div id="volume_cont">
									
									   		<div class="divider_20"><span id="containerHeightLine" class="span_cls">${cartData.containerHeight}</span></div> 
									   		
				                       	<div class="cnt_utlvolfill_cls"><span id="utl_vol">${cartData.totalProductVolumeInPercent}</span>%</div>
				                    	<div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.volume"/></div>
				                        <div style="height: 1px;" id="volume_utilization"></div>                                                                           
				                    </div>                                                   
														
							
									<div id="weight_cont">
				                       <div class="cnt_utlwilfill_cls"><span id="utl_wt">${cartData.totalProductWeightInPercent}</span>%</div>
				                       <div class="cnt_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.weight"/></div>                                                                             
				       				   <div style="height: 1px;" id="weight_utilization"></div>
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
			

				<div class="clearfix fixthis_row_cls">
					<div class="span-16">
					
						<div class="cntutil_wrapper_cls">
							         <c:if test="${cartData.enableFloorSpaceGraphics}"> 	
				                      <div id="floorSpace_cont">
										<div class="divider_40"><span id="containerHeightLine" class="span_cls">${cartData.containerHeight}</span></div>
				                	   	<div class="cnt_utlvolfill_cls" style="display:none"><span id="utl_vol">${cartData.floorSpaceCount}</span></div>
				                	   	<div id="cnt_floorSpaceProducts" style="display:none"><span id="utl_vol">${cartData.floorSpaceProductsMap}</span></div>
				                    	<div id="floorSpaceFull" style="display:none"><span id="floor_Space_Full">${cartData.isFloorSpaceFull}</span></div>
				                    	<div class="cnt_fs_utllbl_cls"><spring:theme code="basket.your.shopping.container.utilization.floorSpace"/></div>
				                      </div>   
							 	 </c:if>
									   							
						</div>
						

					</div>
	
				</div>
				<div class="clearfix fixthis_row_cls">
					<c:if test="${not cartData.enableFloorSpaceGraphics}"> 	
						<div style="font-weight: bold;font-size: 1.2em;">
							<span style="padding-left:127px"><spring:theme code="basket.your.shopping.container.utilization"/> </span>
								<div align="left" style="font-size:11px;color: blue; ">
									<spring:theme code="basket.your.shopping.container.utilization1"/>
								</div>
						</div>
					</c:if>
					<c:if test="${cartData.enableFloorSpaceGraphics}"> 	
						<div style="padding-top:20px;font-weight: bold;font-size: 1.2em;">
							<span style="padding-left:248px"><spring:theme code="basket.your.shopping.container.utilization"/> </span>
								<div align="left" style="font-size:11px;color: blue; ">
									<spring:theme code="basket.your.shopping.container.utilization1"/>
								</div>
						</div>
					</c:if>
				</div>
				
				
		
			<!--   Start Code changes for order flag check  for continueShop button -->
			<c:choose>
				<c:when test="${cartData.isOrderBlocked }">
					<!-- a class="button continueShop-button" disabled="disabled" style="height: 30px;padding-top: 8px;font-size: 140%;margin-top: 20px;border-color: #169e08;" href="${continueShoppingUrl}"><spring:theme text="Continue Shopping" code="cart.page.continue"/></a-->
					<button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
				</c:when>
				<c:when test="${cartData.isFloorSpaceFull}">
				     <button id ="continueButton_bottom" class="checkoutButton positive left" type="button" data-checkout-url="${continueShoppingUrl}" disabled="disabled"><spring:theme text="Continue Shopping" code="cart.page.continue"/></button>
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
				<c:when test="${ not empty cartData.productsNotAddedToCart}">
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
				</c:when>
				<c:when test="${cartData.isFloorSpaceFull}">
					<button id ="checkoutButton_top" class="checkoutButton positive right" type="button"  data-checkout-url="${checkoutUrl}" disabled="disabled"><spring:theme code="checkout.checkout" /></button>
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
