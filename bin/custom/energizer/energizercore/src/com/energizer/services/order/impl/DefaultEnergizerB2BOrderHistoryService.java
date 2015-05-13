/**
 * 
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.order.OrderModel;

import javax.annotation.Resource;

import com.energizer.services.order.EnergizerB2BOrderHistoryService;
import com.energizer.services.order.dao.impl.DefaultEnergizerB2BOrderHistoryDAO;



/**
 * @author M1028886
 * 
 */
public class DefaultEnergizerB2BOrderHistoryService implements EnergizerB2BOrderHistoryService
{

	@Resource(name = "defaultEnergizerB2BOrderHistoryDAO")
	private DefaultEnergizerB2BOrderHistoryDAO orderDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.order.EnergizerB2BOrderHistoryService#getOrdersForB2BUnit(de.hybris.platform.b2b.model.
	 * B2BUnitModel)
	 */
	@Override
	public SearchPageData<OrderModel> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData)
	{

		final SearchPageData<OrderModel> ordersHistoryList = orderDAO.getOrdersForB2BUnit(unitId, pageableData);

		return ordersHistoryList;

	}

}
