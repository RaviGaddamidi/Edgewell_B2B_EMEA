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

import org.apache.log4j.Logger;

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

	private Converter<AddressModel, AddressData> energizerAddressConverter;

	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerAddressFacade.class);

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

	@Override
	public List<AddressModel> fetchAddressOnSHCustID(final String shcustid)
	{
		// YTODO Auto-generated method stub

		List<AddressModel> energizercustSHAddList = null;
		energizercustSHAddList = defaultEnergizerAddressService.fetchAddressOnSHCustID(shcustid);
		List<AddressModel> energizersoldtoidlist = null;
		energizersoldtoidlist = fetchAddress(shcustid);//fetching add in case sh and sp are same then fetch on basis of sp.



		if (energizersoldtoidlist != null && energizersoldtoidlist.size() == 1)
		{
			LOG.info("its sold to as well shipto found, just send for update");
			return energizersoldtoidlist;
		}



		LOG.info("its shipto  found, just send for update");
		return energizercustSHAddList;

	}
}
