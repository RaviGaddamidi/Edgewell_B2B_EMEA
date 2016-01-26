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
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorservices.customer.CustomerLocationService;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.CartModificationData;
import de.hybris.platform.commercefacades.order.data.CartRestorationData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.search.data.SearchStateData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.commerceservices.search.facetdata.ProductSearchPageData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.PaginationData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.facades.flow.impl.SessionOverrideB2BCheckoutFlowFacade;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.breadcrumb.impl.SearchBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.ControllerConstants.Views;
import com.energizer.storefront.controllers.pages.AbstractSearchPageController.ShowMode;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ContainerUtilizationForm;
import com.energizer.storefront.forms.UpdateProfileForm;
import com.energizer.storefront.forms.UpdateQuantityForm;
import com.energizer.storefront.variants.VariantSortStrategy;


/**
 * Controller for cart page
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/cart")
public class CartPageController extends AbstractPageController
{
	private static final String TYPE_MISMATCH_ERROR_CODE = "typeMismatch";
	private static final String ERROR_MSG_TYPE = "errorMsg";
	private static final String QUANTITY_INVALID_BINDING_MESSAGE_KEY = "basket.error.quantity.invalid.binding";
	private static final String ORDER_EXCEEDED = "container.business.rule.orderExceeded";
	private static final String ORDER_BLOCKED = "container.business.rule.orderblocked";

	protected static final Logger LOG = Logger.getLogger(CartPageController.class);

	private static final String CART_CMS_PAGE = "cartPage";
	private static final String REDIRECT_TO_CART_PAGE = REDIRECT_PREFIX + "/cart";

	private static final String CONTINUE_URL = "continueUrl";
	public static final String SUCCESSFUL_MODIFICATION_CODE = "success";

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "userFacade")
	protected UserFacade userFacade;

	@Resource(name = "cartFacade")
	private de.hybris.platform.b2bacceleratorfacades.api.cart.CartFacade b2bCartFacade;

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "variantSortStrategy")
	private VariantSortStrategy variantSortStrategy;

	@Resource(name = "productService")
	private ProductService productService;

	@Resource(name = "b2bProductFacade")
	private ProductFacade productFacade;

	@Resource(name = "customerLocationService")
	private CustomerLocationService customerLocationService;

	@Resource
	EnergizerCartService energizerCartService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource
	private EnergizerOrderEntryBusinessRuleValidationService cartEntryBusinessRulesService;

	@Resource
	private EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;


	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	private CartService cartService;

	@Resource(name = "cmsPageService")
	private CMSPageService cmsPageService;

	ContainerUtilizationForm contUtilForm = new ContainerUtilizationForm();

	String containerHeight, packingOption;

	@Resource(name = "productConverter")
	private Converter<ProductModel, ProductData> productConverter;

	@Resource(name = "searchBreadcrumbBuilder")
	private SearchBreadcrumbBuilder searchBreadcrumbBuilder;



	@RequestMapping(method = RequestMethod.GET)
	public String showCart(final Model model) throws CMSItemNotFoundException
	{
		prepareDataForPage(model);
		return Views.Pages.Cart.CartPage;
	}

	@RequestMapping(method = RequestMethod.POST)
	@RequireHardLogIn
	public String updateContainerUtil(@Valid final ContainerUtilizationForm containerUtilizationForm, final Model model,
			final BindingResult bindingErrors, final RedirectAttributes redirectAttributes)
			throws CMSItemNotFoundException
	{

		if (bindingErrors.hasErrors())
		{
			getViewWithBindingErrorMessages(model, bindingErrors);
		}
		
		cartEntryBusinessRulesService.clearErrors();

		LOG.info(" Inside updateContainerUtil method");
		LOG.info("Container Height: " + containerUtilizationForm.getContainerHeight());
		LOG.info("Packing Option: " + containerUtilizationForm.getPackingType());
		contUtilForm.setContainerHeight(containerUtilizationForm.getContainerHeight());
		contUtilForm.setPackingType(containerUtilizationForm.getPackingType());

		prepareDataForPage(model);
		return Views.Pages.Cart.CartPage;
	}

	@RequestMapping(value = "/checkout", method = RequestMethod.GET)
	@RequireHardLogIn
	public String cartCheck(final Model model, final RedirectAttributes redirectModel) throws CommerceCartModificationException
	{
		SessionOverrideB2BCheckoutFlowFacade.resetSessionOverrides();
		if (!cartFacade.hasSessionCart() || cartFacade.getSessionCart().getEntries().isEmpty())
		{
			LOG.info("Missing or empty cart");

			// No session cart or empty session cart. Bounce back to the cart page.
			return REDIRECT_PREFIX + "/cart";
		}


		orderBusinessRulesService.validateBusinessRules(cartFacade.getSessionCart());
		if (orderBusinessRulesService.hasErrors())
		{
			final List<BusinessRuleError> errors = orderBusinessRulesService.getErrors();
			for (final BusinessRuleError error : errors)
			{
				LOG.info("The error message is " + error.getMessage());
				redirectModel.addFlashAttribute("businessErrors", error.getMessage());
			}
			return REDIRECT_PREFIX + "/cart";
			//return Views.Pages.Cart.CartPage;
		}

		if (validateCart(redirectModel))
		{
			return REDIRECT_PREFIX + "/cart";
		}

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final String productCode = cartService.getSessionCart().getEntries().get(0).getProduct().getCode();
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());
		cartFacade.getSessionCart().setShippingPoint(energizerCMIR.getShippingPoint());

		// Redirect to the start of the checkout flow to begin the checkout process
		// We just redirect to the generic '/checkout' page which will actually select the checkout flow
		// to use. The customer is not necessarily logged in on this request, but will be forced to login
		// when they arrive on the '/checkout' page.
		return REDIRECT_PREFIX + "/checkout";

	}

	@RequestMapping(value = "/getProductVariantMatrix", method = RequestMethod.GET)
	@RequireHardLogIn
	public String getProductVariantMatrix(@RequestParam("productCode") final String productCode, final Model model)
	{
		final ProductModel productModel = productService.getProductForCode(productCode);

		final ProductData productData = productFacade.getProductForOptions(productModel, Arrays.asList(ProductOption.BASIC,
				ProductOption.CATEGORIES, ProductOption.VARIANT_MATRIX_BASE, ProductOption.VARIANT_MATRIX_PRICE,
				ProductOption.VARIANT_MATRIX_MEDIA, ProductOption.VARIANT_MATRIX_STOCK));

		model.addAttribute("product", productData);

		return ControllerConstants.Views.Fragments.Cart.ExpandGridInCart;
	}

	protected boolean validateCart(final RedirectAttributes redirectModel) throws CommerceCartModificationException
	{
		// Validate the cart
		final List<CartModificationData> modifications = cartFacade.validateCartData();
		if (!modifications.isEmpty())
		{
			redirectModel.addFlashAttribute("validationData", modifications);

			// Invalid cart. Bounce back to the cart page.
			return true;
		}
		return false;
	}




	@ResponseBody
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@RequireHardLogIn
	public CartData updateCartQuantities(@RequestParam("entryNumber") final Integer entryNumber,
			@RequestParam("productCode") final String productCode, final Model model, @Valid final UpdateQuantityForm form,
			final BindingResult bindingErrors) throws CMSItemNotFoundException
	{
		final boolean errorMessages = false;
		if (bindingErrors.hasErrors())
		{
			getViewWithBindingErrorMessages(model, bindingErrors);
		}

		cartEntryBusinessRulesService.clearErrors();
		final List<String> businessRuleErrors = new ArrayList<String>();
		if (form.getQuantity() > 0)
		{
			cartEntryBusinessRulesService.validateBusinessRules(getOrderEntryData(form.getQuantity(), productCode, entryNumber));
		}

		if (cartEntryBusinessRulesService.hasErrors())
		{
			final List<BusinessRuleError> businessValidationRuleErrors = cartEntryBusinessRulesService.getErrors();

			BusinessRuleError validationErrors[] = new BusinessRuleError[businessValidationRuleErrors.size()];
			validationErrors = businessValidationRuleErrors.toArray(validationErrors);
			for (int errorCount = 0; errorCount < validationErrors.length; errorCount++)
			{
				LOG.info("The error message is " + validationErrors[errorCount]);
				businessRuleErrors.add(validationErrors[errorCount].getMessage());
			}
			//remove duplicate error
			final HashSet<String> set = new HashSet<String>(businessRuleErrors);
			businessRuleErrors.clear();
			businessRuleErrors.addAll(set);
		}
		else
		{
			final CartModificationData cartModification = b2bCartFacade.updateOrderEntry(getOrderEntryData(form.getQuantity(),
					productCode, entryNumber));

			if (cartModification.getStatusCode().equals(SUCCESSFUL_MODIFICATION_CODE))
			{
				GlobalMessages.addMessage(model, GlobalMessages.CONF_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
			}
			else if (!model.containsAttribute(ERROR_MSG_TYPE))
			{
				GlobalMessages.addMessage(model, GlobalMessages.ERROR_MESSAGES_HOLDER, cartModification.getStatusMessage(), null);
			}
		}
		if (bindingErrors.hasErrors())
		{
			getViewWithBindingErrorMessages(model, bindingErrors);
		}

		/** Energizer Container Utilization service */

		//LOG.info(" Container Height: " + containerUtilizationForm.getContainerHeight());
		//LOG.info(" Packing Type: " + containerUtilizationForm.getPackingType());

		//final CartData cartData = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart());

		/** Energizer Container Utilization service */
		//contUtilForm.setContainerHeight(Config.getParameter("energizer.default.containerHeight"));
		//contUtilForm.setPackingType(Config.getParameter("energizer.default.packingOption"));
		if (contUtilForm.getContainerHeight() != null || contUtilForm.getPackingType() != null)
		{
			containerHeight = contUtilForm.getContainerHeight();
			packingOption = contUtilForm.getPackingType();
		}
		else
		{
			containerHeight = Config.getParameter("energizer.default.containerHeight");

			packingOption = Config.getParameter("energizer.default.packingOption");
		}


		LOG.info(" Container Height: " + containerHeight);
		LOG.info(" Packing Type: " + packingOption);

		final CartData cartData = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart(), containerHeight,
				packingOption);


		if (cartData.isIsContainerFull())
		{

			businessRuleErrors.add(Localization.getLocalizedString(ORDER_EXCEEDED));
		}

		if (cartData.isIsOrderBlocked())
		{

			businessRuleErrors.add(Localization.getLocalizedString(ORDER_BLOCKED));
		}

		cartData.setBusinesRuleErrors(businessRuleErrors);

		return cartData;

	}

	protected void createProductList(final Model model) throws CMSItemNotFoundException
	{
		CartData cartData = cartFacade.getSessionCart();
		boolean errorMessages = false;

		reverseCartProductsOrder(cartData.getEntries());
		if (cartData.getEntries() != null && !cartData.getEntries().isEmpty())
		{

			boolean flag = false;
			String productWithCmirInActive = "";
			for (final OrderEntryData entry : cartData.getEntries())
			{
				final UpdateQuantityForm uqf = new UpdateQuantityForm();
				uqf.setQuantity(entry.getQuantity());
				model.addAttribute("updateQuantityForm" + entry.getEntryNumber(), uqf);
				if (entry.getProduct().isIsActive() == false)
				{
					productWithCmirInActive += entry.getProduct().getErpMaterialID() + "  ";
					flag = true;

				}
			}
			if (flag == true)
			{
				GlobalMessages.addMessage(model, "accErrorMsgs", "cart.cmirinactive", new Object[]
				{ productWithCmirInActive });
				//return FORWARD_PREFIX + "/cart";
			}
		}

		/** Energizer Container Utilization service */
		//	contUtilForm.setContainerHeight(Config.getParameter("energizer.default.containerHeight"));
		//	contUtilForm.setPackingType(Config.getParameter("energizer.default.packingOption"));

		if (contUtilForm.getContainerHeight() != null || contUtilForm.getPackingType() != null)
		{
			containerHeight = contUtilForm.getContainerHeight();
			packingOption = contUtilForm.getPackingType();
		}
		else
		{
			containerHeight = Config.getParameter("energizer.default.containerHeight");

			packingOption = Config.getParameter("energizer.default.packingOption");
		}

		cartData = energizerCartService.calCartContainerUtilization(cartData, containerHeight, packingOption);
		final List<String> message = energizerCartService.messages();
		if (message.size() != 0)
		{
			for (final String messages : message)
			{

				GlobalMessages.addErrorMessage(model, messages);
			}
			errorMessages = true;
		}

		final HashMap productList = energizerCartService.productNotAddedToCart();


		final HashMap productsNotDoubleStacked = energizerCartService.productsNotDoublestacked123();


		final List<String> containerHeightList = Arrays.asList(Config.getParameter("possibleContainerHeights").split(
				new Character(',').toString()));


		final List<String> packingOptionsList = Arrays.asList(Config.getParameter("possiblePackingOptions").split(
				new Character(',').toString()));


		model.addAttribute("containerHeightList", containerHeightList);
		model.addAttribute("packingOptionList", packingOptionsList);
		model.addAttribute("errorMessages", errorMessages);

		model.addAttribute("containerUtilizationForm", contUtilForm);
		model.addAttribute("productList", productList);
		model.addAttribute("productsNotDoubleStacked", productsNotDoubleStacked);
		//model.addAttribute("data", data);

		model.addAttribute("cartData", cartData);

		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CART_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
		model.addAttribute("Link", "/my-account");
	}

	@RequestMapping(value = "/updateprofile", method = RequestMethod.POST)
	@RequireHardLogIn
	public void updateProfile(@Valid final UpdateProfileForm updateProfileForm, final BindingResult bindingResult,
			final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{
		LOG.info("for container utilization");
	}


	protected void reverseCartProductsOrder(final List<OrderEntryData> entries)
	{
		if (entries != null)
		{
			Collections.reverse(entries);
		}
	}

	protected void prepareDataForPage(final Model model) throws CMSItemNotFoundException
	{
		final String continueUrl = (String) getSessionService().getAttribute(WebConstants.CONTINUE_URL);
		model.addAttribute(CONTINUE_URL, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);

		if (sessionService.getAttribute(WebConstants.CART_RESTORATION) instanceof CartRestorationData)
		{
			final CartRestorationData restorationData = (CartRestorationData) sessionService
					.getAttribute(WebConstants.CART_RESTORATION);
			model.addAttribute("restorationData", restorationData);

		}

		createProductList(model);
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("breadcrumb.cart"));
		model.addAttribute("pageType", PageType.CART.name());
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
		return Views.Fragments.Cart.AddToCartPopup;
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

}
