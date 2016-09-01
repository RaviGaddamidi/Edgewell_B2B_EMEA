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

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;


/**
 * Controller for home page.
 */
@Controller
@Scope("tenant")
@RequestMapping("/")
public class HomePageController extends AbstractPageController
{
	@Resource
	protected SessionService sessionService;

	/**
	 * @return the sessionService
	 */
	@Override
	public SessionService getSessionService()
	{
		return sessionService;
	}

	/**
	 * @param sessionService
	 *           the sessionService to set
	 */
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}


	@Resource(name = "energizerCompanyB2BCommerceFacade")
	protected EnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade;

	@RequestMapping(method = RequestMethod.GET)
	public String home(@RequestParam(value = "logout", defaultValue = "false") final boolean logout, final Model model,
			final RedirectAttributes redirectModel, final HttpSession hs) throws CMSItemNotFoundException
	{
		if (logout)
		{
			GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.INFO_MESSAGES_HOLDER, "account.confirmation.signout.title");
			return REDIRECT_PREFIX + ROOT;
		}
		if (null != sessionService.getAttribute("passwordAlert"))
		{

			GlobalMessages.addMessage(model, "accErrorMsgs", (String) sessionService.getAttribute("passwordAlert"), new Object[]
			{ sessionService.getAttribute("dayCount") });
			sessionService.removeAttribute("passwordAlert");
			sessionService.removeAttribute("dayCount");
		}
		if (null != sessionService.getAttribute("quesAnsAlert"))
		{
			GlobalMessages.addBusinessRuleMessage(model, (String) sessionService.getAttribute("quesAnsAlert"));
			sessionService.removeAttribute("quesAnsAlert");
		}



		final EnergizerB2BUnitModel b2bUnit = energizerCompanyB2BCommerceFacade.getEnergizerB2BUnitModelForLoggedInUser();

		final List catalogManagementList = b2bUnit.getEnergizerNACustomerCatalogs();

		if (null == sessionService.getAttribute("selectedCatalog"))
		{
			sessionService.setAttribute("selectedCatalog", b2bUnit.getMainCatalog());
			hs.setAttribute("currentCatalog", b2bUnit.getMainCatalog());
		}


		final String siteURL = getSiteURL(b2bUnit.getSite());

		if (siteURL.equals(WebConstants.PERSONAL_CARE_NA))
		{
			hs.setAttribute(WebConstants.PERSONAL_CARE_NA, Boolean.TRUE);
		}

		model.addAttribute("catalogManagementList", catalogManagementList);
		storeCmsPageInModel(model, getContentPageForLabelOrId(null));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(null));
		updatePageTitle(model, getContentPageForLabelOrId(null));
		return getViewForPage(model);
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

	protected void updatePageTitle(final Model model, final AbstractPageModel cmsPage)
	{
		storeContentPageTitleInModel(model, getPageTitleResolver().resolveHomePageTitle(cmsPage.getTitle()));
	}
}