/**
 *
 */
package com.energizer.core.populators;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.storesession.data.CurrencyData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.mobileservices.model.text.PhoneNumberModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.data.PhoneNumberData;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1023097
 *
 */
public class EnergizerB2BUnitPopulator implements Populator<EnergizerB2BUnitModel, EnergizerB2BUnitData>
{
	private Converter<CurrencyModel, CurrencyData> currencyConverter;
	private Converter<PhoneNumberModel, PhoneNumberData> phoneNumberConverter;

	private Converter<B2BUnitModel, B2BUnitData> b2bUnitConverter;



	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final EnergizerB2BUnitModel source, final EnergizerB2BUnitData target) throws ConversionException
	{
		getB2bUnitConverter().convert(source, target);
		target.setSalesOrganisation(source.getSalesOrganisation());
		target.setDistributionChannel(source.getDistributionChannel());
		target.setCustomerAccountName(source.getCustomerAccountName());
		target.setOrderBlock(source.getOrderBlock());
		target.setCurrencyPreference(currencyConverter.convert(source.getCurrencyPreference()));
		target.setMinimumOrderValue(source.getMinimumOrderValue());
		target.setMaxUserLimit(source.getMaxUserLimit());
		target.setSalesPersonEmailId(source.getSalesPersonEmailId());
		target.setLanguagePreference(source.getLanguagePreference());
		target.setDivision(source.getDivision());
		//target.setContactNumber(phoneNumberConverter.convert(source.getContactNumber()));
		target.setErpOrderingType(source.getErpOrderingType());
	}

	protected Converter<CurrencyModel, CurrencyData> getCurrencyConverter()
	{
		return currencyConverter;
	}

	@Required
	public void setCurrencyConverter(final Converter<CurrencyModel, CurrencyData> currencyConverter)
	{
		this.currencyConverter = currencyConverter;
	}

	/**
	 * @return the phoneNumberConverter
	 */
	public Converter<PhoneNumberModel, PhoneNumberData> getPhoneNumberConverter()
	{
		return phoneNumberConverter;
	}

	/**
	 * @param phoneNumberConverter
	 *           the phoneNumberConverter to set
	 */
	public void setPhoneNumberConverter(final Converter<PhoneNumberModel, PhoneNumberData> phoneNumberConverter)
	{
		this.phoneNumberConverter = phoneNumberConverter;
	}


	/**
	 * @return the b2bUnitConverter
	 */
	public Converter<B2BUnitModel, B2BUnitData> getB2bUnitConverter()
	{
		return b2bUnitConverter;
	}

	/**
	 * @param b2bUnitConverter
	 *           the b2bUnitConverter to set
	 */
	public void setB2bUnitConverter(final Converter<B2BUnitModel, B2BUnitData> b2bUnitConverter)
	{
		this.b2bUnitConverter = b2bUnitConverter;
	}


}
