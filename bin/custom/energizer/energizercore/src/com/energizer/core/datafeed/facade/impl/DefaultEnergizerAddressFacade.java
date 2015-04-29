/**
 * 
 */
package com.energizer.core.datafeed.facade.impl;

import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.facade.EnergizerAddressFacade;
import com.energizer.core.datafeed.service.impl.DefaultEnergizerAddressService;


/**
 * @author M1023278
 * 
 */
public class DefaultEnergizerAddressFacade implements EnergizerAddressFacade
{

	@Resource
	DefaultEnergizerAddressService defaultEnergizerAddressService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.facade.EnergizerAddressFacade#fetchAddress(java.lang.String)
	 */
	@Override
	public List<AddressModel> fetchAddress(final String erpAddressId)
	{
		final List<AddressModel> energizerB2BUnitModelList = defaultEnergizerAddressService.fetchAddress(erpAddressId);
		return energizerB2BUnitModelList;
	}
}
