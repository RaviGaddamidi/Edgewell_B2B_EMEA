/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *
 */
package com.energizer.storefront.controllers.misc;

import de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade;
import de.hybris.platform.b2bacceleratorfacades.product.data.CartEntryData;
import de.hybris.platform.b2b.company.B2BCommerceUserService;

import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.controllers.AbstractController;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.AddToCartForm;
import com.energizer.storefront.forms.AddToCartOrderForm;
import com.google.common.collect.Lists;


/**
 * Controller for Add to Cart functionality which is not specific to a certain page.
 */
@Controller
@Scope("tenant")
public class AddToCartController extends AbstractController
{
	private static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";
	private static final String ERROR_MSG_TYPE = "errorMsg";
	private static final String QUANTITY_INVALID_BINDING_MESSAGE_KEY = "basket.error.quantity.invalid.binding";

	protected static final Logger LOG = Logger.getLogger(AddToCartController.class);
	private static final Long MINIMUM_SINGLE_SKU_ADD_CART = 0L;
	private static final String SHOWN_PRODUCT_COUNT = "storefront.minicart.shownProductCount";
	public static final String SUCCESSFUL_MODIFICATION_CODE = "success";


	@Resource(name = "b2bCartFacade")
	private CartFacade cartFacade;

	@Deprecated
	@Resource(name = "cartFacade")
	private de.hybris.platform.commercefacades.order.CartFacade cartCommerceFacade;

	@Resource
	private CartService cartService;

	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@InitBinder
	public void initBinder(final WebDataBinder binder)
	{
		binder.setAutoGrowCollectionLimit(Integer.MAX_VALUE);
	}

	@RequestMapping(value = "/cart/add", method = RequestMethod.POST, produces = "application/json")
	public String addToCart(@RequestParam("productCodePost") final String code, final Model model,
			@Valid final AddToCartForm form, final BindingResult bindingErrors, final HttpSession session)
	{
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final OrderEntryData orderEntryData = getOrderEntryData(form.getQty(), code, null);
		getOrderEntryShippingPoints(orderEntryData);
		orderEntryBusinessRulesService.clearErrors();

		orderEntryBusinessRulesService.validateBusinessRules(orderEntryData);
		if (orderEntryBusinessRulesService.hasErrors())
		{
			final List<BusinessRuleError> errors = orderEntryBusinessRulesService.getErrors();
			for (final BusinessRuleError error : errors)
			{
				LOG.info("The error message is " + error.getMessage());
				GlobalMessages.addBusinessRuleMessage(model, error.getMessage());
			}
			return ControllerConstants.Views.Fragments.Product.ProductLister;
		}

		if (bindingErrors.hasErrors())
		{
			return getViewWithBindingErrorMessages(model, bindingErrors);
		}


		final CartModificationData modification = cartFacade.addOrderEntry(orderEntryData);
		CartData cartModificationData = null;
		String height = (String) session.getAttribute("containerHeight");
		final Boolean enableButton = b2bUnit.getEnableContainerOptimization();
		if (height == null)
		{
			height = "40FT";
		}
		if (height != null && height.contains("40"))
		{
			LOG.info("for 40 ft");
			cartModificationData = energizerCartService.calCartContainerUtilization(cartCommerceFacade.getSessionCart(), "40FT",
					"1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

		}
		else if (height != null && height.contains("20"))
		{
			LOG.info("for 20 ft");
			cartModificationData = energizerCartService.calCartContainerUtilization(cartCommerceFacade.getSessionCart(), "20FT",
					"1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

		}
		if (cartModificationData == null && cartModificationData.getTotalPalletCount() == null
				&& cartModificationData.getPartialPalletCount() == null)
		{
			model.addAttribute("FullPallet", null);
			model.addAttribute("MixedPallet", null);
		}
		else
		{
			model.addAttribute("FullPallet", cartModificationData.getTotalPalletCount());
			model.addAttribute("MixedPallet", cartModificationData.getPartialPalletCount());
		}



		model.addAttribute("numberShowing", Config.getInt(SHOWN_PRODUCT_COUNT, 3));
		model.addAttribute("modifications", (modification != null ? Lists.newArrayList(modification) : Collections.emptyList()));

		addStatusMessages(model, modification);

		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected void addStatusMessages(final Model model, final CartModificationData modification)
	{
		final boolean hasMessage = StringUtils.isNotEmpty(modification.getStatusMessage());
		if (hasMessage)
		{
			if (SUCCESSFUL_MODIFICATION_CODE.equals(modification.getStatusCode()))
			{
				GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, modification.getStatusMessage(), null);
			}
			else if (!model.containsAttribute(ERROR_MSG_TYPE))
			{
				GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, modification.getStatusMessage(), null);
			}
		}
	}


	@RequestMapping(value = "/cart/addGrid", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public final String addGridToCart(@RequestBody final AddToCartOrderForm form, final Model model)
	{
		final List<OrderEntryData> orderEntries = getOrderEntryData(form.getCartEntries());
		final List<CartModificationData> modifications = cartFacade.addOrderEntryList(orderEntries);

		model.addAttribute("modifications", modifications);
		model.addAttribute("numberShowing", Config.getInt(SHOWN_PRODUCT_COUNT, 3));

		for (final CartModificationData modification : modifications)
		{
			addStatusMessages(model, modification);
		}

		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected String getViewWithBindingErrorMessages(final Model model, final BindingResult bindingErrors)
	{
		for (final ObjectError error : bindingErrors.getAllErrors())
		{
			if (error.getCode().equals(TYPE_MISMATCH_ERROR_CODE))
			{
				model.addAttribute(ERROR_MSG_TYPE, QUANTITY_INVALID_BINDING_MESSAGE_KEY);
			}
			else
			{
				model.addAttribute(ERROR_MSG_TYPE, error.getDefaultMessage());
			}
		}
		return ControllerConstants.Views.Fragments.Cart.AddToCartPopup;
	}

	protected OrderEntryData getOrderEntryData(final long quantity, final String productCode, final Integer entryNumber)
	{

		final OrderEntryData orderEntry = new OrderEntryData();
		orderEntry.setQuantity(quantity);
		orderEntry.setProduct(new ProductData());
		orderEntry.getProduct().setCode(productCode);
		orderEntry.setEntryNumber(entryNumber);

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		orderEntry.getProduct().setUom(energizerCMIR.getUom());

		return orderEntry;
	}

	protected List<OrderEntryData> getOrderEntryData(final List<CartEntryData> cartEntries)
	{
		final List<OrderEntryData> orderEntries = new ArrayList<>();

		for (final CartEntryData entry : cartEntries)
		{
			final Integer entryNumber = entry.getEntryNumber() != null ? entry.getEntryNumber().intValue() : null;
			orderEntries.add(getOrderEntryData(entry.getQuantity(), entry.getSku(), entryNumber));
		}
		return orderEntries;
	}


	/**
	 *
	 * @param orderEntryData
	 */
	protected void getOrderEntryShippingPoints(final OrderEntryData orderEntryData)
	{
		final String productCode = orderEntryData.getProduct().getCode();
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		final String shippingPointNo = energizerCMIR.getShippingPoint();

		orderEntryData.setShippingPoint(shippingPointNo);
		/*
		 * if (cartService.hasSessionCart()) {
		 */
		final CartModel cartModel = cartService.getSessionCart();
		if (cartModel.getEntries() != null && !cartModel.getEntries().isEmpty())
		{
			final AbstractOrderEntryModel orderEntry = cartModel.getEntries().get(0);

			final String cartProductCode = orderEntry.getProduct().getCode();

			final EnergizerCMIRModel cartenergizerCMIR = energizerProductService.getEnergizerCMIR(cartProductCode, b2bUnit.getUid());

			final String cartshippingPoint = cartenergizerCMIR.getShippingPoint();
			LOG.info("The shippingPointNo of product in the cart : " + cartshippingPoint);
			orderEntryData.setReferenceShippingPoint(cartshippingPoint);

		}
		else
		{
			energizerCMIR = energizerProductService.getEnergizerCMIR(orderEntryData.getProduct().getCode(), b2bUnit.getUid());
			final String shippingPoint = energizerCMIR.getShippingPoint();
			orderEntryData.setShippingPoint(shippingPoint);
			orderEntryData.setReferenceShippingPoint(shippingPoint);
		}
		//}
	}
}
