/**
 *
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderHistoryData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.services.BaseStoreService;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.facades.order.EnergizerB2BOrderHistoryFacade;
import com.energizer.services.order.impl.DefaultEnergizerB2BOrderHistoryService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M1028886
 *
 */
public class DefaultEnergizerB2BOrderHistoryFacade implements EnergizerB2BOrderHistoryFacade
{
	@Resource(name = "energizerOrderHistoryConverter")
	private Converter<OrderModel, OrderHistoryData> energizerOrderHistoryConverter;

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "customerAccountService")
	private CustomerAccountService customerAccountService;

	@Resource(name = "baseStoreService")
	private BaseStoreService baseStoreService;

	@Resource(name = "orderConverter")
	private Converter<OrderModel, OrderData> orderConverter;

	@Resource(name = "defaultEnergizerB2BOrderHistoryService")
	private DefaultEnergizerB2BOrderHistoryService b2bOrderHistoryService;

	@Resource(name = "energizerProductService")
	private EnergizerProductService energizerProductService;

	@Resource(name = "cartService")
	private CartService cartService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getOrdersForB2BUnit(de.hybris.platform.b2b.jalo.B2BUnit
	 * )
	 */
	public SearchPageData<OrderHistoryData> getOrdersForB2BUnit(final B2BUnitModel unitId, final PageableData pageableData,
			final OrderStatus[] orderStatuses)
	{
		final SearchPageData<OrderModel> ordersHistoryList = b2bOrderHistoryService.getOrdersForB2BUnit(unitId, pageableData,
				orderStatuses);
		return convertPageData(ordersHistoryList, energizerOrderHistoryConverter);
	}

	protected <S, T> SearchPageData<T> convertPageData(final SearchPageData<S> source, final Converter<S, T> converter)
	{
		final SearchPageData<T> result = new SearchPageData<T>();
		result.setPagination(source.getPagination());
		result.setSorts(source.getSorts());
		result.setResults(Converters.convertAll(source.getResults(), converter));
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getEnergizerCMIR(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId, final String b2bUnitId)
	{
		final EnergizerCMIRModel energizerCMIR = energizerProductService.getEnergizerCMIR(erpMaterialId, b2bUnitId);
		return energizerCMIR;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getParentUnitForCustomer(java.lang.String)
	 */
	@Override
	public String getProductCodeForCustomer()
	{
		final String productCode = cartService.getSessionCart().getEntries().get(0).getProduct().getCode();
		return productCode;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getParentUnitForCustomer(java.lang.String)
	 */
	@Override
	public EnergizerB2BUnitModel getParentUnitForCustomer(final String userId)
	{
		final EnergizerB2BUnitModel b2bUnitModel = b2bCommerceUserService.getParentUnitForCustomer(userId);
		return b2bUnitModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getCurrentUser()
	 */
	@Override
	public String getCurrentUser()
	{
		final String userId = userService.getCurrentUser().getUid();
		return userId;
	}

}
