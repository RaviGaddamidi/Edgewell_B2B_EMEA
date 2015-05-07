/**
 * 
 */
package com.energizer.facades.accounts.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.model.B2BUserGroupModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.security.PrincipalGroupModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;
import com.energizer.facades.accounts.populators.ContactNumberPopulator;
import com.energizer.facades.accounts.populators.EnergizerB2BCustomerReversePopulator;


/**
 * @author m9005673
 * 
 */
public class DefaultEnergizerCompanyB2BCommerceFacade extends DefaultCustomerFacade implements EnergizerCompanyB2BCommerceFacade
{
	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bUserService;

	@Resource(name = "customerAccountService")
	private CustomerAccountService customerAccountService;

	@SuppressWarnings("rawtypes")
	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "energizerCustomerReversePopulator")
	private EnergizerB2BCustomerReversePopulator energizerCustomerReversePopulator;

	@Resource(name = "companyB2BCommerceService")
	private CompanyB2BCommerceService companyB2BCommerceService;

	@Resource(name = "contactNumberPopulator")
	private ContactNumberPopulator contactNumberPopulator;

	@Resource(name = "energizerGroupsLookUpStrategy")
	private DefaultEnergizerGroupsLookUpStrategy energizerGroupsLookUpStrategy;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade#getContactNumber(java.lang.String)
	 */
	@Override
	public String getContactNumber(final String uuid, final CustomerData customerData)
	{
		final EnergizerB2BCustomerModel model = userService.getUserForUID(uuid, EnergizerB2BCustomerModel.class);
		contactNumberPopulator.populate(model, customerData);
		return customerData.getContactNumber();
	}

	/**
	 * This method retrieves the EnergizerB2BUnitModel of the currently logged in user.
	 * 
	 * @see EnergizerB2BUnitModel
	 * @return EnergizerB2BUnitModel
	 */
	private EnergizerB2BUnitModel getB2BUnitForLoggedInUser()
	{
		return b2bUserService.getParentUnitForCustomer(userService.getCurrentUser().getUid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade#validateUserCount()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean validateUserCount(final EnergizerB2BUnitModel energizerModel)
	{
		boolean validateFlag = false;
		if (null != energizerModel.getMaxUserLimit())
		{
			validateFlag = (defaultB2BUnitService.getB2BCustomers(energizerModel).size() == energizerModel.getMaxUserLimit()) ? true
					: false;
		}
		return validateFlag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#getEnergizerB2BUnitModelForLoggedInUser()
	 */
	@Override
	public EnergizerB2BUnitModel getEnergizerB2BUnitModelForLoggedInUser()
	{
		return getB2BUnitForLoggedInUser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#updateProfile(de.hybris.platform.commercefacades
	 * .user.data.CustomerData)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void updateProfile(final CustomerData customerData)
	{
		try
		{
			validateDataBeforeUpdate(customerData);
			final String name = getCustomerNameStrategy().getName(customerData.getFirstName(), customerData.getLastName());
			final EnergizerB2BCustomerModel customer = (EnergizerB2BCustomerModel) getCurrentSessionCustomer();
			customer.setOriginalUid(customerData.getDisplayUid());
			customer.setContactNumber(customerData.getContactNumber());
			customerAccountService.updateProfile(customer, customerData.getTitleCode(), name, customerData.getUid());
			getModelService().save(customer);
		}
		catch (final DuplicateUidException e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#findB2BAdministratorForCustomer(com.energizer
	 * .core.model.EnergizerB2BUnitModel.EnergizerB2BUnitModel)
	 */

	public B2BCustomerModel findB2BAdministratorForCustomer(final EnergizerB2BUnitModel b2bModel)
	{
		@SuppressWarnings("unchecked")
		final List<B2BCustomerModel> b2bAdminGroupUsers = new ArrayList<B2BCustomerModel>(
				defaultB2BUnitService.getUsersOfUserGroup(b2bModel, B2BConstants.B2BADMINGROUP, true));
		return (CollectionUtils.isNotEmpty(b2bAdminGroupUsers) ? b2bAdminGroupUsers.get(0) : null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade#updateCustomer(de.hybris.platform.commercefacades
	 * .user.data.CustomerData)
	 */

	@Override
	public void updateCustomer(final CustomerData customerData) throws DuplicateUidException
	{
		validateParameterNotNullStandardMessage("customerData", customerData);
		final EnergizerB2BCustomerModel energizerB2BCustomerModel;
		if (StringUtils.isEmpty(customerData.getUid()))
		{
			energizerB2BCustomerModel = this.getModelService().create(EnergizerB2BCustomerModel.class);
			energizerB2BCustomerModel.setRegistrationEmailFlag(Boolean.TRUE);
		}
		else
		{
			energizerB2BCustomerModel = (EnergizerB2BCustomerModel) userService.getUserForUID(customerData.getUid());
		}
		energizerCustomerReversePopulator.populate(customerData, energizerB2BCustomerModel);
		companyB2BCommerceService.saveModel(energizerB2BCustomerModel);
	}

	@Override
	public List getUserGroups()
	{
		return energizerGroupsLookUpStrategy.getGroups();
	}

	/**
	 * As per enhancement added new group b2bviewergroup
	 */
	public void populateRolesByCustomer(final String uuid, final CustomerData target)
	{
		final List<String> roles = new ArrayList<String>();
		final EnergizerB2BCustomerModel model = userService.getUserForUID(uuid, EnergizerB2BCustomerModel.class);
		final Set<PrincipalGroupModel> roleModels = new HashSet<PrincipalGroupModel>(model.getGroups());
		CollectionUtils.filter(roleModels, PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(B2BUnitModel.class)));
		CollectionUtils
				.filter(roleModels, PredicateUtils.notPredicate(PredicateUtils.instanceofPredicate(B2BUserGroupModel.class)));
		for (final PrincipalGroupModel role : roleModels)
		{
			// only display allowed usergroups
			if (energizerGroupsLookUpStrategy.getUserGroups().contains(role.getUid()))
			{
				roles.add(role.getUid());
			}
		}
		target.setRoles(roles);
	}

	/**
	 * As per enhancement added new group b2bviewergroup
	 */
	@Override
	public void populateUserRoles(final SearchPageData<CustomerData> b2bCustomer)
	{
		List<String> roles = null;
		for (int i = 0; i < b2bCustomer.getResults().size(); i++)
		{
			roles = new ArrayList<String>();
			final EnergizerB2BCustomerModel customerModel = userService.getUserForUID(b2bCustomer.getResults().get(i).getUid(),
					EnergizerB2BCustomerModel.class);
			final Set<PrincipalGroupModel> roleModels = new HashSet<PrincipalGroupModel>(customerModel.getGroups());
			for (final PrincipalGroupModel role : roleModels)
			{
				// only display allowed usergroups
				if (energizerGroupsLookUpStrategy.getUserGroups().contains(role.getUid()))
				{
					roles.add(role.getUid());
				}
			}
			b2bCustomer.getResults().get(i).setRoles(roles);
		}
	}
}