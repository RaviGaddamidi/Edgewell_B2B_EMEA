/**
 * 
 */
package com.energizer.services.order.dao.impl;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.flexiblesearch.data.SortQueryData;
import de.hybris.platform.commerceservices.search.flexiblesearch.impl.DefaultPagedFlexibleSearchService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.energizer.services.order.dao.EnergizerB2BOrderHistoryDAO;


/**
 * @author M1028886
 * 
 */
public class DefaultEnergizerB2BOrderHistoryDAO implements EnergizerB2BOrderHistoryDAO
{

	@Resource(name = "pagedFlexibleSearchService")
	DefaultPagedFlexibleSearchService pagedFlexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.order.dao.EnergizerB2BOrderHistoryDAO#getOrdersForB2BUnit(de.hybris.platform.b2b.model.
	 * B2BUnitModel)
	 */
	@Override
	public SearchPageData<OrderModel> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData)
	{
		final Map queryParameters = new HashMap();
		final List sortedResults;
		queryParameters.put("name", unitId);
		final String query = "SELECT {o.pk}, {o.creationtime}, {o.code} FROM { Order AS o JOIN B2BUnit AS u ON {o.Unit}= {u.pk}}";
		sortedResults = Arrays.asList(new SortQueryData[]
		{ getSortedResultData("byDate", query), });
		final SearchPageData<OrderModel> results = pagedFlexibleSearchService.search(sortedResults, "byDate", queryParameters,
				pageableData);
		return results;
	}

	protected SortQueryData getSortedResultData(final String sortCode, final String query)
	{
		final SortQueryData result = new SortQueryData();
		result.setSortCode(sortCode);
		result.setQuery(query);
		return result;
	}
}
