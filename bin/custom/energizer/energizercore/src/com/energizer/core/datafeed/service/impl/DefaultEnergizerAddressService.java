/**
 *
 */
package com.energizer.core.datafeed.service.impl;

import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.service.EnergizerAddressService;
import com.energizer.core.model.EnergizerUnitContactsModel;




/**
 * @author M1023278
 *
 */
public class DefaultEnergizerAddressService implements EnergizerAddressService
{
	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Override
	public List<AddressModel> fetchAddress(final String erpAddressId)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery("SELECT {pk} FROM {Address} where {"
				+ AddressModel.ERPADDRESSID + "}=?erpAddressId");
		retreiveQuery.addQueryParameter("erpAddressId", erpAddressId);

		final SearchResult<AddressModel> result = flexibleSearchService.search(retreiveQuery);
		final List<AddressModel> energizerB2BUnitModelList = result.getResult();
		return energizerB2BUnitModelList;

	}

	@Override
	public List<AddressModel> fetchAddressForB2BUnit(final String b2bUnitUId)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
				"SELECT {pk} FROM {Address as p join energizerb2bunit as b on {p.OWNER}={b.PK}} where {b:uid}=?b2bUnitUId");
		retreiveQuery.addQueryParameter("b2bUnitUId", b2bUnitUId);

		final SearchResult<AddressModel> result = flexibleSearchService.search(retreiveQuery);
		final List<AddressModel> energizerB2BUnitModelList = result.getResult();
		return energizerB2BUnitModelList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.datafeed.service.EnergizerAddressService#getEnergizerUnitContact(java.lang.String)
	 */
	@Override
	public EnergizerUnitContactsModel getEnergizerUnitContact(final String customerId)
	{
		final FlexibleSearchQuery retquery = new FlexibleSearchQuery(
				"select {pk} from {EnergizerUnitContacts} where {customerId}=?customerId");

		retquery.addQueryParameter("customerId", customerId);
		final SearchResult<EnergizerUnitContactsModel> result = flexibleSearchService.search(retquery);
		if (result.getResult() != null && result.getResult().size() > 0)
		{
			return result.getResult().get(0);
		}
		else
		{
			return null;
		}

	}

}
