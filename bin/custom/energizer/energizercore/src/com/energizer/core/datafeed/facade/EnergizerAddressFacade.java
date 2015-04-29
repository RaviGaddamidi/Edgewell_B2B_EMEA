/**
 * 
 */
package com.energizer.core.datafeed.facade;

import de.hybris.platform.core.model.user.AddressModel;

import java.util.List;


/**
 * @author M1023278
 * 
 */
public interface EnergizerAddressFacade
{
	public List<AddressModel> fetchAddress(final String erpAddressId);
}
