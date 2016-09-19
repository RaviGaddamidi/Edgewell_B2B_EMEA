package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.user.converters.populator.AddressPopulator;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.ArrayList;
import java.util.List;


/**
 * @author M1023097
 *
 */
public class EnergizerAddressPopulator extends AddressPopulator
{
	private Converter<AddressModel, AddressData> energizerAddressConverter;

	@Override
	public void populate(final AddressModel source, final AddressData target)
	{
		super.populate(source, target);
		if (source.getErpAddressId() != null)
		{
			target.setErpAddressId(source.getErpAddressId());
		}
		if (source.getSalesPersonEmailId() != null)
		{
			target.setSalesPersonEmailId(source.getSalesPersonEmailId());
		}
		if (source.getDisplayName() != null)
		{
			target.setDisplayName(source.getDisplayName());
		}
		if (source.getActive() != null)
		{
			target.setActive(source.getActive());
		}

		target.setSoldTo(source.getSoldTo() != null ? source.getSoldTo() : false);

		if (source.getShipToPartyList() != null)
		{
			List<AddressModel> shipToAddress = new ArrayList<AddressModel>();
			final List<AddressData> shipToPartyListData = new ArrayList<AddressData>();
			shipToAddress = source.getShipToPartyList();
			for (final AddressModel address : shipToAddress)
			{
				shipToPartyListData.add(getEnergizerAddressConverter().convert(address));
			}
			if (shipToPartyListData != null)
			{
				target.setShipToPartyList(shipToPartyListData);
			}
		}
	}

	/**
	 * @return the energizerAddressConverter
	 */
	public Converter<AddressModel, AddressData> getEnergizerAddressConverter()
	{
		return energizerAddressConverter;
	}

	/**
	 * @param energizerAddressConverter
	 *           the energizerAddressConverter to set
	 */
	public void setEnergizerAddressConverter(final Converter<AddressModel, AddressData> energizerAddressConverter)
	{
		this.energizerAddressConverter = energizerAddressConverter;
	}

}
