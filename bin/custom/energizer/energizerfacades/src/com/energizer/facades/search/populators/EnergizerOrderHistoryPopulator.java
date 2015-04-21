/**
 * 
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.order.converters.populator.OrderHistoryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.core.model.order.OrderModel;


/**
 * @author M1023097
 * 
 */
public class EnergizerOrderHistoryPopulator extends OrderHistoryPopulator
{
	@Override
	public void populate(final OrderModel source, final OrderHistoryData target)
	{
		super.populate(source, target);
		target.setReferenceNumber(source.getErpOrderNumber());
		target.setPurchaseOrderNumber(source.getPurchaseOrderNumber());
		target.setErpOrderCreator(source.getErpOrderCreator());
	}
}
