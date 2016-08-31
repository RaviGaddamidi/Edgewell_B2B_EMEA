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

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.Constants;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.spring.security.CoreAuthenticationProvider;
import de.hybris.platform.util.Config;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.util.EnergizerPasswordNotificationUtil;


/**
 * Derived authentication provider supporting additional authentication checks. See
 * {@link de.hybris.platform.spring.security.RejectUserPreAuthenticationChecks}.
 * 
 * <ul>
 * <li>prevent login without password for users created via CSCockpit</li>
 * <li>prevent login as user in group admingroup</li>
 * <li>prevent login as user if not authorised for B2B</li>
 * <li>prevent login as user if not authorised for B2B</li>
 * </ul>
 * 
 * any login as admin disables SearchRestrictions and therefore no page can be viewed correctly
 */
public class AcceleratorAuthenticationProvider extends CoreAuthenticationProvider
{
	private static final Logger LOG = Logger.getLogger(AcceleratorAuthenticationProvider.class);
	private static final String ROLE_ADMIN_GROUP = "ROLE_" + Constants.USER.ADMIN_USERGROUP.toUpperCase();

	private BruteForceAttackCounter bruteForceAttackCounter;
	private UserService userService;
	private ModelService modelService;
	private GrantedAuthority adminAuthority = new SimpleGrantedAuthority(ROLE_ADMIN_GROUP);
	private CartService cartService;

	private B2BUserGroupProvider b2bUserGroupProvider;

	@Resource(name = "cmsSiteService")
	private CMSSiteService cmsSiteService;

	@Resource(name = "b2bUnitService")
	private B2BUnitService<B2BUnitModel, B2BCustomerModel> b2bUnitService;

	@Resource
	protected EnergizerPasswordNotificationUtil energizerPasswordNotificationUtil;


	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException
	{
		final String username = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();

		// check if the user of the cart matches the current user and if the
		// user is not anonymous. If otherwise, remove delete the session cart as it might
		// be stolen / from another user
		final String sessionCartUserId = getCartService().getSessionCart().getUser().getUid();

		if (!username.equals(sessionCartUserId) && !sessionCartUserId.equals(userService.getAnonymousUser().getUid()))
		{
			getCartService().setSessionCart(null);
		}
		return super.authenticate(authentication);
	}

	/**
	 * @see de.hybris.platform.spring.security.CoreAuthenticationProvider#additionalAuthenticationChecks(org.springframework.security.core.userdetails.UserDetails,
	 *      org.springframework.security.authentication.AbstractAuthenticationToken)
	 */
	@Override
	protected void additionalAuthenticationChecks(final UserDetails details, final AbstractAuthenticationToken authentication)
			throws AuthenticationException
	{
		super.additionalAuthenticationChecks(details, authentication);
		List<String> notificationMessages = null;
		// Check if user has supplied no password
		if (StringUtils.isEmpty((String) authentication.getCredentials()))
		{
			throw new BadCredentialsException("Login without password");
		}

		// Check if the user is in role admingroup
		if (getAdminAuthority() != null && details.getAuthorities().contains(getAdminAuthority()))
		{
			throw new LockedException("Login attempt as " + Constants.USER.ADMIN_USERGROUP + " is rejected");
		}

		// Check if the customer is B2B type
		if (!getB2bUserGroupProvider().isUserAuthorized(details.getUsername()))
		{
			throw new InsufficientAuthenticationException(messages.getMessage("checkout.error.invalid.accountType",
					"You are not allowed to login"));
		}

		if (!getB2bUserGroupProvider().isUserEnabled(details.getUsername()))
		{
			throw new DisabledException("User " + details.getUsername() + " is disabled... "
					+ messages.getMessage("text.company.manage.units.disabled"));
		}


		if (null != authentication.getName())
		{
			notificationMessages = energizerPasswordNotificationUtil.checkPasswordExpiryStatus(authentication.getName());
			if (null != notificationMessages && notificationMessages.size() > 0 && notificationMessages.get(0).equalsIgnoreCase("1"))
			{
				throw new LockedException("Your Password Has Been expired.......Please reset your password");

			}
		}

		if (null != authentication.getName())
		{

			final EnergizerB2BCustomerModel b2bcustomer = (EnergizerB2BCustomerModel) userService.getUserForUID(authentication
					.getName());

			final EnergizerB2BUnitModel unit = (EnergizerB2BUnitModel) b2bUnitService.getParent(b2bcustomer);

			final CMSSiteModel site = cmsSiteService.getCurrentSite();

			if (null != unit && site != null)
			{
				final String siteid = site.getUid();

				if (siteid.equals(WebConstants.PERSONAL_CARE_NA)
						&& !(WebConstants.PERSONAL_CARE_NA).equals(getSiteURL(unit.getSite()))) //(siteid.equals(WebConstants.PERSONAL_CARE_NA) && !(siteid.equals(getSiteURL(unit.getSite()))))//!USER_COUNTRY.contains(userCountry.getIsocode())
				{
					throw new InsufficientAuthenticationException(messages.getMessage("login.error.incorrect.site",
							"You are not allowed to login"));
				}
				else if (siteid.equals(WebConstants.PERSONAL_CARE)
						&& (!(WebConstants.PERSONAL_CARE).equals(getSiteURL(unit.getSite())))) //(siteid.equals(WebConstants.PERSONAL_CARE) && !(siteid.equals(getSiteURL(unit.getSite()))))//USER_COUNTRY.contains(userCountry.getIsocode())
				{
					throw new InsufficientAuthenticationException(messages.getMessage("login.error.incorrect.site",
							"You are not allowed to login"));
				}
				else if (!b2bcustomer.getActive())
				{
					throw new InsufficientAuthenticationException(messages.getMessage("login.error.incorrect.site",
							"You are not allowed to login"));
				}
			}
		}
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

	/**
	 * @return the b2bUserGroupProvider
	 */
	protected B2BUserGroupProvider getB2bUserGroupProvider()
	{
		return b2bUserGroupProvider;
	}

	/**
	 * @param b2bUserGroupProvider
	 *           the b2bUserGroupProvider to set
	 */
	public void setB2bUserGroupProvider(final B2BUserGroupProvider b2bUserGroupProvider)
	{
		this.b2bUserGroupProvider = b2bUserGroupProvider;
	}

	/**
	 * @param adminGroup
	 *           the adminGroup to set
	 */
	public void setAdminGroup(final String adminGroup)
	{
		if (StringUtils.isBlank(adminGroup))
		{
			adminAuthority = null;
		}
		else
		{
			adminAuthority = new SimpleGrantedAuthority(adminGroup);
		}
	}

	protected GrantedAuthority getAdminAuthority()
	{
		return adminAuthority;
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

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
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

	public CartService getCartService()
	{
		return cartService;
	}

	public void setCartService(final CartService cartService)
	{
		this.cartService = cartService;
	}
}
