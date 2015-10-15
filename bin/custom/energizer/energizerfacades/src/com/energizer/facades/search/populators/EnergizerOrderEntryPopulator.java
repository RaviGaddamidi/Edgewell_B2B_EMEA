/**
 * 
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.commercefacades.order.converters.populator.OrderEntryPopulator;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;


/**
 * @author M1023097
 * 
 */
public class EnergizerOrderEntryPopulator extends OrderEntryPopulator
{
	@Override
	public void populate(final AbstractOrderEntryModel source, final OrderEntryData target)
	{
		super.populate(source, target);
		target.setAdjustedQty(source.getAdjustedQty());
		addTotals(source, target);
		//target.setAdjustedLinePrice(source.getAdjustedLinePrice());
		target.setRejectedStatus(source.getRejectedStatus());
		if (source.getCustomerMaterialId() != null)
		{
			target.setCustomerMaterialId(source.getCustomerMaterialId());

		}
		if (null != source.getIsNewEntry())
		{
			target.setIsNewEntry(source.getIsNewEntry());
		}
		else
		{
			target.setIsNewEntry("N");
		}

	}

	@Override
	protected void addTotals(final AbstractOrderEntryModel orderEntry, final OrderEntryData entry)
	{
		if (orderEntry.getBasePrice() != null)
		{
			entry.setBasePrice(createPrice(orderEntry, orderEntry.getBasePrice()));
		}
		if (orderEntry.getAdjustedItemPrice() != null)
		{
			entry.setAdjustedItemPrice(createPrice(orderEntry, orderEntry.getAdjustedItemPrice().doubleValue()));
		}
		if (orderEntry.getTotalPrice() != null)
		{
			entry.setTotalPrice(createPrice(orderEntry, orderEntry.getTotalPrice()));
		}
		if (orderEntry.getAdjustedLinePrice() != null)
		{
			entry.setAdjustedLinePrice(createPrice(orderEntry, orderEntry.getAdjustedLinePrice().doubleValue()));
		}
	}
}