/**
 * 
 */
package com.energizer.facades.accounts.populators;

import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import com.energizer.core.model.EnergizerB2BCustomerModel;


/**
 * @author M1023097
 * 
 */
public class ContactNumberPopulator implements Populator<CustomerModel, CustomerData>
{

	private Converter<EnergizerB2BCustomerModel, CustomerData> ContactNumberConverter;


	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.converters.Populator#populate(java.lang.Object, java.lang.Object)
	 */
	@Override
	public void populate(final CustomerModel source, final CustomerData target) throws ConversionException
	{
		if (source instanceof EnergizerB2BCustomerModel)
		{
			target.setContactNumber(((EnergizerB2BCustomerModel) source).getContactNumber());
		}
	}

	/**
	 * @return the ContactNumberConverter
	 */
	public Converter<EnergizerB2BCustomerModel, CustomerData> getContactNumberConverter()
	{
		return ContactNumberConverter;
	}

	/**
	 * @param ContactNumberConverter
	 *           the ContactNumberConverter to set
	 */
	public void setContactNumberConverter(final Converter<EnergizerB2BCustomerModel, CustomerData> ContactNumberConverter)
	{
		this.ContactNumberConverter = ContactNumberConverter;
	}



}
