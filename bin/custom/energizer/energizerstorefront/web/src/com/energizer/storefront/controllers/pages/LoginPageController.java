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

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.energizer.core.datafeed.processor.customer.EnergizerPasswordExpiryJob;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Login Controller. Handles login and register for the account flow.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/login")
public class LoginPageController extends AbstractLoginPageController
{

	private static final Logger LOG = Logger.getLogger(EnergizerPasswordExpiryJob.class);
	@Resource(name = "httpSessionRequestCache")
	private HttpSessionRequestCache httpSessionRequestCache;

	@Resource
	private UserService userService;

	private final static String FAILED_MAX_ATTEMPTS_TO_LOGIN = "FAILED_MAX_ATTEMPTS_TO_LOGIN";

	private final static String ACCOUNT_IS_BLOCKED = "login.maxattempts.failed";


	protected static final String SPRING_SECURITY_LAST_USERNAME = "SPRING_SECURITY_LAST_USERNAME";

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	public void setHttpSessionRequestCache(final HttpSessionRequestCache accHttpSessionRequestCache)
	{
		this.httpSessionRequestCache = accHttpSessionRequestCache;
	}


	@RequestMapping(method = RequestMethod.GET)
	public String doLogin(@RequestHeader(value = "referer", required = false) final String referer,
			@RequestParam(value = "error", defaultValue = "false") final boolean loginError, final Model model,
			final HttpServletRequest request, final HttpServletResponse response, final HttpSession session)
			throws CMSItemNotFoundException, IOException
	{

		final UserModel user = userService.getCurrentUser();
		final boolean isUserAnonymous = user == null || userService.isAnonymousUser(user);
		List<String> notificationMessages = null;

		if (!isUserAnonymous)
		{
			return REDIRECT_PREFIX + ROOT;
		}

		final String userName = (String) session.getAttribute(SPRING_SECURITY_LAST_USERNAME);
		if (null != userName)
		{
			notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(userName);
			if (null != notificationMessages && notificationMessages.size() > 0 && notificationMessages.get(0).equalsIgnoreCase("1"))
			{
				GlobalMessages.addErrorMessage(model, notificationMessages.get(1));

			}

			try
			{
				final EnergizerB2BCustomerModel b2bcustomer = (EnergizerB2BCustomerModel) userService.getUserForUID(userName
						.toLowerCase());

				final EnergizerB2BUnitModel unit = (EnergizerB2BUnitModel) b2bUnitService.getParent(b2bcustomer);

				final CMSSiteModel site = cmsSiteService.getCurrentSite();

				if (null != unit)
				{
					//final CountryModel userCountry = unit.getCountry();

					final String siteid = site.getUid();

					/*
					 * if (null == userCountry) { GlobalMessages.addErrorMessage(model, "login.error.incorrect.country"); }
					 */
					if ((siteid.equals(WebConstants.PERSONAL_CARE_NA))
							&& !(WebConstants.PERSONAL_CARE_NA).equals(getSiteURL(unit.getSite()))) //(siteid.equals(WebConstants.PERSONAL_CARE_NA) && !siteid.equals(getSiteURL(unit.getSite())))//!USER_COUNTRY.contains(userCountry.getIsocode())
					{
						GlobalMessages.addErrorMessage(model, "login.error.incorrect.site.us");
					}
					else if ((siteid.equals(WebConstants.PERSONAL_CARE))
							&& !(WebConstants.PERSONAL_CARE.equals(getSiteURL(unit.getSite())))) //(siteid.equals(WebConstants.PERSONAL_CARE) && !siteid.equals(getSiteURL(unit.getSite())))//USER_COUNTRY.contains(userCountry.getIsocode())
					{
						GlobalMessages.addErrorMessage(model, "login.error.incorrect.site");
					}
					else if (!b2bcustomer.getActive())
					{
						GlobalMessages.addErrorMessage(model, "login.error.account.disabled");
					}
					else
					{
						GlobalMessages.addErrorMessage(model, "login.error.account.incorrect");
					}
				}


			}
			catch (final UnknownIdentifierException e)
			{
				GlobalMessages.addErrorMessage(model, "login.error.user.not.exist");
			}


		}

		if (!loginError)
		{
			storeReferer(referer, request, response);
		}
		else
		{
			if (getSessionService().getAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN) != null
					&& getSessionService().getAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN).equals(FAILED_MAX_ATTEMPTS_TO_LOGIN))
			{
				GlobalMessages.addErrorMessage(model, ACCOUNT_IS_BLOCKED);
				model.addAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN, FAILED_MAX_ATTEMPTS_TO_LOGIN);
				getSessionService().removeAttribute(FAILED_MAX_ATTEMPTS_TO_LOGIN);
			}
		}

		return getDefaultLoginPage(loginError, session, model);
	}

	protected String getSiteURL(final int siteId)
	{
		String siteMatchURL = null;
		List<String> siteList = null;
		final List<String> siteURLsList = Arrays.asList(Config.getParameter("website.personalCare.site").split(
				new Character(',').toString()));

		for (final Iterator iterator = siteURLsList.iterator(); iterator.hasNext();)
		{
			final String tempSiteURL = (String) iterator.next();

			siteList = Arrays.asList(tempSiteURL.split(new Character(':').toString()));

			if (Integer.parseInt(siteList.get(0)) == siteId)
			{
				siteMatchURL = siteList.get(1);
			}
		}
		return siteMatchURL;
	}



	@Override
	protected String getLoginView()
	{
		return ControllerConstants.Views.Pages.Account.AccountLoginPage;
	}

	@Override
	protected String getSuccessRedirect(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (httpSessionRequestCache.getRequest(request, response) != null)
		{
			return httpSessionRequestCache.getRequest(request, response).getRedirectUrl();
		}

		return "/my-account";
	}

	@Override
	protected AbstractPageModel getLoginCmsPage() throws CMSItemNotFoundException
	{
		return getContentPageForLabelOrId("login");
	}

	protected void storeReferer(final String referer, final HttpServletRequest request, final HttpServletResponse response)
	{
		if (StringUtils.isNotBlank(referer))
		{
			httpSessionRequestCache.saveRequest(request, response);
		}
	}
}
