/**
 * 
 */
package com.energizer.services.order;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.model.order.OrderModel;



/**
 * @author M1028886
 * 
 */
public interface EnergizerB2BOrderHistoryService
{

	public SearchPageData<OrderModel> getOrdersForB2BUnit(B2BUnitModel unitId, PageableData pageableData);

}
