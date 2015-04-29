package com.energizer.services.order.dao.impl;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.order.dao.EnergizerOrderDAO;


/**
 * @author ROMIL JAISHWAL M1028411
 */
public class DefaultEnergizerOrderDAO implements EnergizerOrderDAO
{
	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Override
	public OrderModel findExistingOrder(final String sapOrderNo)
	{
		OrderModel energizerOrderModel = null;
		final String fsq = "SELECT{" + OrderModel.PK + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.ERPORDERNUMBER
				+ "}=?sapOrderId";
		final FlexibleSearchQuery fsquery = new FlexibleSearchQuery(fsq, Collections.singletonMap("sapOrderId", sapOrderNo));
		final SearchResult<OrderModel> result = flexibleSearchService.search(fsquery);
		final List<OrderModel> energizerOrderModels = result.getResult();
		if (!(energizerOrderModels.isEmpty()))
		{
			energizerOrderModel = energizerOrderModels.get(0);
		}
		return energizerOrderModel;
	}

	@Override
	public OrderModel findExistingOrder(final String sapOrderNo, final String hybrisOrderNo)
	{
		OrderModel energizerOrderModel = null;
		//Retrieve EnergizerOrder
		final String fsq = "SELECT{" + OrderModel.PK + "} from {" + OrderModel._TYPECODE + "} WHERE {" + OrderModel.CODE
				+ "}=?hybrisOrderId AND {" + OrderModel.ERPORDERNUMBER + "} =?sapOrderId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("hybrisOrderId", hybrisOrderNo);
		params.put("sapOrderId", sapOrderNo);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderModel> result = flexibleSearchService.search(query);
		final List<OrderModel> energizerOrderModels = result.getResult();
		if (!energizerOrderModels.isEmpty())
		{
			energizerOrderModel = energizerOrderModels.get(0);
		}
		return energizerOrderModel;
	}

	@Override
	public OrderEntryModel findExistingOrderItem(final OrderModel energizerOrderModel,
			final EnergizerProductModel energizerProductModel)
	{
		OrderEntryModel energizerOrderEntryModel = null;
		//Retrieve energizerOrderEntryModel
		final String fsq = "SELECT{" + OrderEntryModel.PK + "} FROM {" + OrderEntryModel._TYPECODE + "} WHERE {"
				+ OrderEntryModel.ORDER + "}=?orderId AND {" + OrderEntryModel.PRODUCT + "} =?productId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", energizerOrderModel);
		params.put("productId", energizerProductModel);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderEntryModel> result = flexibleSearchService.search(query);
		final List<OrderEntryModel> energizerOrderEntryModels = result.getResult();
		if (!energizerOrderEntryModels.isEmpty())
		{
			energizerOrderEntryModel = energizerOrderEntryModels.get(0);
		}
		return energizerOrderEntryModel;
	}

	@Override
	public List<OrderEntryModel> findExistingOrderItems(final OrderModel energizerOrderModel)
	{
		List<OrderEntryModel> energizerOrderEntryModels = null;
		//Retrieve energizerOrderEntryModel
		final String fsq = "SELECT{" + OrderEntryModel.PK + "} FROM {" + OrderEntryModel._TYPECODE + "} WHERE {"
				+ OrderEntryModel.ORDER + "}=?orderId";
		final HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("orderId", energizerOrderModel);
		final FlexibleSearchQuery query = new FlexibleSearchQuery(fsq, params);
		final SearchResult<OrderEntryModel> result = flexibleSearchService.search(query);

		if (!result.getResult().isEmpty())
		{
			energizerOrderEntryModels = result.getResult();
		}
		return energizerOrderEntryModels;
	}

}
