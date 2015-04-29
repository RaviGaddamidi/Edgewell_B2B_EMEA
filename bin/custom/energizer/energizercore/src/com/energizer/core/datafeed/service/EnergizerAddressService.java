/**
 * 
 */
package com.energizer.core.datafeed.service;

import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;


/**
 * @author M1023278
 * 
 */
public interface EnergizerAddressService
{
	public List<AddressModel> fetchAddress(final String erpAddressId);
}
