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
package com.energizer.storefront.security;

import de.hybris.platform.acceleratorservices.uiexperience.UiExperienceService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commerceservices.enums.UiExperienceLevel;
import de.hybris.platform.commerceservices.order.CommerceCartRestorationException;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.energizer.core.datafeed.facade.impl.DefaultEnergizerPasswordExpiryFacade;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.pages.AbstractPageController;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Success handler initializing user settings and ensuring the cart is handled correctly
 */
public class StorefrontAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler
{
	private CustomerFacade customerFacade;
	private UiExperienceService uiExperienceService;
	private CartFacade cartFacade;
	private SessionService sessionService;
	private BruteForceAttackCounter bruteForceAttackCounter;

	private Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel;

	private final static String HOMEPAGE = "/";

	@Resource
	protected DefaultEnergizerPasswordExpiryFacade defaultEnergizerPasswordExpiryFacade;

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;

	public UiExperienceService getUiExperienceService()
	{
		return uiExperienceService;
	}

	@Required
	public void setUiExperienceService(final UiExperienceService uiExperienceService)
	{
		this.uiExperienceService = uiExperienceService;
	}

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		getCustomerFacade().loginSuccess();
		List<String> notificationMessages = null;
		if (null != authentication.getName())
		{
			notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(authentication.getName());
			if (null != notificationMessages && notificationMessages.size() > 0 && notificationMessages.get(0).equalsIgnoreCase("0"))
			{
				sessionService.setAttribute("passwordAlert", notificationMessages.get(1));
				sessionService.setAttribute("dayCount", notificationMessages.get(2));

			}
		}
		isPasswordQuestionAnswerSet(authentication.getName());



		if (!getCartFacade().hasSessionCart() || getCartFacade().getSessionCart().getEntries().isEmpty())
		{
			try
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION, getCartFacade().restoreSavedCart(null));
			}
			catch (final CommerceCartRestorationException e)
			{
				getSessionService().setAttribute(WebConstants.CART_RESTORATION, "basket.restoration.errorMsg");
			}
		}

		getBruteForceAttackCounter().resetUserCounter(getCustomerFacade().getCurrentCustomer().getUid());

		//redirect the user to homepage if the user has just updated the password
		if (sessionService.getAttribute(AbstractPageController.JUST_UPDATED_PWD) != null
				&& sessionService.getAttribute(AbstractPageController.JUST_UPDATED_PWD).equals(
						AbstractPageController.JUST_UPDATED_PWD))
		{
			sessionService.removeAttribute(AbstractPageController.JUST_UPDATED_PWD);

			response.sendRedirect(HOMEPAGE);

		}
		else
		{


			super.onAuthenticationSuccess(request, response, authentication);
		}
	}

	/**
	 * @param name
	 */
	private void isPasswordQuestionAnswerSet(final String userName)
	{

		final EnergizerB2BCustomerModel energizerB2BCustomerModel = defaultEnergizerPasswordExpiryFacade.getCustomerByUID(userName);

		if (!energizerB2BCustomerModel.getIsPasswordQuestionSet())
		{
			sessionService.setAttribute("quesAnsAlert", "account.password.isQuestionAnsSet");
		}

	}

	protected CartFacade getCartFacade()
	{
		return cartFacade;
	}

	@Required
	public void setCartFacade(final CartFacade cartFacade)
	{
		this.cartFacade = cartFacade;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	@Required
	public void setCustomerFacade(final CustomerFacade customerFacade)
	{
		this.customerFacade = customerFacade;
	}

	/*
	 * @see org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler#
	 * isAlwaysUseDefaultTargetUrl()
	 */
	@Override
	protected boolean isAlwaysUseDefaultTargetUrl()
	{
		final UiExperienceLevel uiExperienceLevel = getUiExperienceService().getUiExperienceLevel();
		if (getForceDefaultTargetForUiExperienceLevel().containsKey(uiExperienceLevel))
		{
			return Boolean.TRUE.equals(getForceDefaultTargetForUiExperienceLevel().get(uiExperienceLevel));
		}
		else
		{
			return false;
		}
	}

	protected Map<UiExperienceLevel, Boolean> getForceDefaultTargetForUiExperienceLevel()
	{
		return forceDefaultTargetForUiExperienceLevel;
	}

	@Required
	public void setForceDefaultTargetForUiExperienceLevel(
			final Map<UiExperienceLevel, Boolean> forceDefaultTargetForUiExperienceLevel)
	{
		this.forceDefaultTargetForUiExperienceLevel = forceDefaultTargetForUiExperienceLevel;
	}


	protected BruteForceAttackCounter getBruteForceAttackCounter()
	{
		return bruteForceAttackCounter;
	}

	@Required
	public void setBruteForceAttackCounter(final BruteForceAttackCounter bruteForceAttackCounter)
	{
		this.bruteForceAttackCounter = bruteForceAttackCounter;
	}
}
