/**
 * 
 */
package com.energizer.core.datafeed.service.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.datafeed.service.EnergizerCustomerLeadTimeService;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;


/**
 * @author M1023278
 * 
 */
public class DefaultEnergizerCustomerLeadTimeService implements EnergizerCustomerLeadTimeService
{


	@Resource
	protected FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.core.datafeed.facade.EnergizerCustomerLeadTimeFacade#fecthEnergizerB2BUnitLeadTime(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public List<EnergizerB2BUnitLeadTimeModel> fecthEnergizerB2BUnitLeadTime(final String pk_EnergizerB2BUnit,
			final String shippingPointNo, final String shipTo)
	{
		final String fsq = "SELECT{" + EnergizerB2BUnitLeadTimeModel.PK + "} FROM {" + EnergizerB2BUnitLeadTimeModel._TYPECODE
				+ "} WHERE {" + EnergizerB2BUnitLeadTimeModel.B2BUNITID + "}=?b2bUnitId AND {"
				+ EnergizerB2BUnitLeadTimeModel.SHIPPINGPOINTID + "} =?shippingId AND {"
				+ EnergizerB2BUnitLeadTimeModel.SOLDTOADDRESSID + "} =?soldToId";
		final HashMap params = new HashMap();
		params.put("b2bUnitId", pk_EnergizerB2BUnit);
		params.put("shippingId", shippingPointNo);
		params.put("soldToId", shipTo);
		final FlexibleSearchQuery fsquery = new FlexibleSearchQuery(fsq, params);
		final SearchResult<EnergizerB2BUnitLeadTimeModel> result = flexibleSearchService.search(fsquery);
		final List<EnergizerB2BUnitLeadTimeModel> energizerB2BUnitLeadTimes = result.getResult();
		return energizerB2BUnitLeadTimes;
	}

}
