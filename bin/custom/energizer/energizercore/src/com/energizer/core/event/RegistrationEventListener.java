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
package com.energizer.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.enums.SiteChannel;
import de.hybris.platform.commerceservices.event.AbstractSiteEventListener;
import de.hybris.platform.commerceservices.event.RegisterEvent;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.site.BaseSiteService;

import java.util.Collection;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;


/**
 * Event listener for registration functionality.
 */
public class RegistrationEventListener extends AbstractSiteEventListener<RegisterEvent>
{
	private ModelService modelService;
	private BusinessProcessService businessProcessService;
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	protected BusinessProcessService getBusinessProcessService()
	{
		return businessProcessService;
	}

	@Required
	public void setBusinessProcessService(final BusinessProcessService businessProcessService)
	{
		this.businessProcessService = businessProcessService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	@Override
	protected void onSiteEvent(final RegisterEvent registerEvent)
	{
		final StoreFrontCustomerProcessModel storeFrontCustomerProcessModel = (StoreFrontCustomerProcessModel) getBusinessProcessService()
				.createProcess(
						"b2bCustomerRegistrationEmailProcess" + "-" + registerEvent.getCustomer().getUid() + "-"
								+ System.currentTimeMillis(), "b2bCustomerRegistrationEmailProcess");
		storeFrontCustomerProcessModel.setSite(registerEvent.getSite());
		storeFrontCustomerProcessModel.setCustomer(registerEvent.getCustomer());
		storeFrontCustomerProcessModel.setLanguage(registerEvent.getLanguage());
		storeFrontCustomerProcessModel.setCurrency(registerEvent.getCurrency());
		storeFrontCustomerProcessModel.setStore(registerEvent.getBaseStore());
		getModelService().save(storeFrontCustomerProcessModel);
		getBusinessProcessService().startProcess(storeFrontCustomerProcessModel);
	}

	@Override
	protected boolean shouldHandleEvent(final RegisterEvent event)
	{
		/*
		 * final BaseSiteModel site = baseSiteService.getBaseSiteForUID("personalCare"); site.setChannel(SiteChannel.B2B);
		 * event.setSite(site); ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", site); return
		 * SiteChannel.B2B.equals(site.getChannel());
		 */


		//		final BaseSiteModel site = baseSiteService.getBaseSiteForUID("personalCare");
		//		final BaseSiteModel site = baseSiteService.getCurrentBaseSite();
		boolean siteFlag = false;
		final Collection<BaseSiteModel> allSite = baseSiteService.getAllBaseSites();
		for (final BaseSiteModel baseSiteModel : allSite)
		{
			baseSiteModel.setChannel(SiteChannel.B2B);
			event.setSite(baseSiteModel);
			ServicesUtil.validateParameterNotNullStandardMessage("event.order.site", baseSiteModel);
			siteFlag = SiteChannel.B2B.equals(baseSiteModel.getChannel());
		}
		return siteFlag;
	}
}
