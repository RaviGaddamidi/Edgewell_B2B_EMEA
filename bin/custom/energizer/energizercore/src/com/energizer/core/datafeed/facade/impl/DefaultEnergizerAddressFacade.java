/**
 *
 */
package com.energizer.core.datafeed.facade.impl;

import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.facade.EnergizerAddressFacade;
import com.energizer.core.datafeed.service.impl.DefaultEnergizerAddressService;
import com.energizer.core.model.EnergizerUnitContactsModel;


/**
 * @author M1023278
 *
 */
public class DefaultEnergizerAddressFacade implements EnergizerAddressFacade
{

	@Resource
	DefaultEnergizerAddressService defaultEnergizerAddressService;

	private Converter<AddressModel, AddressData> energizerAddressConverter;

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

	@Override
	public List<AddressData> fetchAddressForB2BUnit(final String b2bUnitUId)
	{
		final List<AddressData> deliveryAddresses = new ArrayList<AddressData>();
		final List<AddressModel> energizerB2BUnitModelList = defaultEnergizerAddressService.fetchAddressForB2BUnit(b2bUnitUId);

		for (final AddressModel model : energizerB2BUnitModelList)
		{
			final AddressData addressData = energizerAddressConverter.convert(model);
			//LOG.info("no exception");
			deliveryAddresses.add(addressData);
		}
		return deliveryAddresses;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.facade.EnergizerAddressFacade#getEnergizerUnitContact(java.lang.String)
	 */
	@Override
	public EnergizerUnitContactsModel getEnergizerUnitContact(final String customerId)
	{
		return defaultEnergizerAddressService.getEnergizerUnitContact(customerId);
	}
}
