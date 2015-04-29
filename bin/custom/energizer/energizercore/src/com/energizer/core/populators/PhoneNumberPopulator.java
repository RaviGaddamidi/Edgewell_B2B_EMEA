/**
 * 
 */
package com.energizer.core.populators;

import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.mobileservices.model.text.PhoneNumberModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import com.energizer.core.data.PhoneNumberData;


/**
 * @author M1023097
 * 
 */
public class PhoneNumberPopulator implements Populator<PhoneNumberModel, PhoneNumberData>
{

	private Converter<CountryModel, CountryData> countryConverter;
	private Converter<PhoneNumberModel, PhoneNumberData> phoneNumberConverter;


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final PhoneNumberModel source, final PhoneNumberData target) throws ConversionException
	{
		target.setFormat(source.getFormat());
		target.setNumber(Integer.parseInt(source.getNumber().trim()));
		target.setNormalizedNumber(source.getNormalizedNumber());
		target.setCountry(countryConverter.convert(source.getCountry()));

	}

	/**
	 * @return the countryConverter
	 */
	public Converter<CountryModel, CountryData> getCountryConverter()
	{
		return countryConverter;
	}

	/**
	 * @param countryConverter
	 *           the countryConverter to set
	 */
	public void setCountryConverter(final Converter<CountryModel, CountryData> countryConverter)
	{
		this.countryConverter = countryConverter;
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



}
