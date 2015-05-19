/**
 * 
 */
package com.energizer.facades.order;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;


/**
 * @author M1028886
 * 
 */
public interface EnergizerB2BOrderHistoryFacade
{
	public SearchPageData<OrderHistoryData> getOrdersForB2BUnit(B2BUnitModel unitId, PageableData pageableData,
			OrderStatus[] orderStatuses);

	public EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId, String b2bUnitId);

	public String getProductCodeForCustomer();

	public EnergizerB2BUnitModel getParentUnitForCustomer(String userId);

	public String getCurrentUser();
}
