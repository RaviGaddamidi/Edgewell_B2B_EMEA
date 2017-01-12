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


import de.hybris.platform.acceleratorcms.model.components.MiniCartComponentModel;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.servicelayer.services.CMSComponentService;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.storefront.controllers.AbstractController;
import com.energizer.storefront.controllers.ControllerConstants;


/**
 * Controller for MiniCart functionality which is not specific to a page.
 */
@Controller
@Scope("tenant")
public class MiniCartController extends AbstractController
{
	protected static final Logger LOG = Logger.getLogger(MiniCartController.class);
	/**
	 * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
	 * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
	 * the issue and future resolution.
	 */
	private static final String TOTAL_DISPLAY_PATH_VARIABLE_PATTERN = "{totalDisplay:.*}";
	private static final String COMPONENT_UID_PATH_VARIABLE_PATTERN = "{componentUid:.*}";

	@Deprecated
	@Resource(name = "cartFacade")
	private CartFacade cartFacade;

	@Resource(name = "cmsComponentService")
	private CMSComponentService cmsComponentService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource
	EnergizerCartService energizerCartService;

	@RequestMapping(value = "/cart/miniCart/" + TOTAL_DISPLAY_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
	public String getMiniCart(@PathVariable final String totalDisplay, final Model model)
	{
		final CartData cartData = cartFacade.getMiniCart();
		int numberOfEntries = 0;
		model.addAttribute("totalPrice", cartData.getTotalPrice());
		model.addAttribute("subTotal", cartData.getSubTotal());
		if (cartData.getDeliveryCost() != null)
		{
			final PriceData withoutDelivery = cartData.getDeliveryCost();
			withoutDelivery.setValue(cartData.getTotalPrice().getValue().subtract(cartData.getDeliveryCost().getValue()));
			model.addAttribute("totalNoDelivery", withoutDelivery);
		}
		else
		{
			model.addAttribute("totalNoDelivery", cartData.getTotalPrice());
		}
		if (cartFacade.getSessionCart().getEntries() == null)
		{
			numberOfEntries = 0;
		}
		else
		{
			numberOfEntries = cartFacade.getSessionCart().getEntries().size();
		}
		model.addAttribute("totalItems", numberOfEntries);
		model.addAttribute("totalDisplay", totalDisplay);
		return ControllerConstants.Views.Fragments.Cart.MiniCartPanel;
	}


	@RequestMapping(value = "/cart/rollover/" + COMPONENT_UID_PATH_VARIABLE_PATTERN, method =
	{ RequestMethod.GET, RequestMethod.POST })
	public String rolloverMiniCartPopup(@PathVariable final String componentUid, final Model model, final HttpSession session)
			throws CMSItemNotFoundException
	{
		final CartData cartData = cartFacade.getSessionCart();
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

		CartData cartModificationData = null;
		final Boolean enableButton = b2bUnit.getEnableContainerOptimization();
		String height = (String) session.getAttribute("containerHeight");
		if (height == null)
		{
			height = "40FT";
		}
		if (height != null && height.contains("40"))
		{
			LOG.info("for 40 ft");
			cartModificationData = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart(), "40FT",
					"1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

		}
		else if (height != null && height.contains("20"))
		{
			LOG.info("for 20 ft");
			cartModificationData = energizerCartService.calCartContainerUtilization(cartFacade.getSessionCart(), "20FT",
					"1 SLIP SHEET AND 1 WOODEN BASE", enableButton);

		}
		if (cartModificationData.getTotalPalletCount() == null && cartModificationData.getPartialPalletCount() == null)
		{
			model.addAttribute("FullPallet", null);
			model.addAttribute("MixedPallet", null);
		}
		else
		{
			model.addAttribute("FullPallet", cartModificationData.getTotalPalletCount());
			model.addAttribute("MixedPallet", cartModificationData.getPartialPalletCount());
		}

		model.addAttribute("cartData", cartData);

		final MiniCartComponentModel component = (MiniCartComponentModel) cmsComponentService.getSimpleCMSComponent(componentUid);

		final List entries = cartData.getEntries();
		if (entries != null)
		{
			Collections.reverse(entries);
			model.addAttribute("entries", entries);

			model.addAttribute("numberItemsInCart", Integer.valueOf(entries.size()));
			if (entries.size() < component.getShownProductCount())
			{
				model.addAttribute("numberShowing", Integer.valueOf(entries.size()));
			}
			else
			{
				model.addAttribute("numberShowing", Integer.valueOf(component.getShownProductCount()));
			}
		}
		model.addAttribute("lightboxBannerComponent", component.getLightboxBannerComponent());

		return ControllerConstants.Views.Fragments.Cart.CartPopup;
	}
}
