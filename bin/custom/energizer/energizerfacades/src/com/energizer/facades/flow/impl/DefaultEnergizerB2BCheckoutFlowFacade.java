/**
 * 
 */
package com.energizer.facades.flow.impl;

import de.hybris.platform.b2b.services.B2BOrderService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.model.ModelService;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.business.service.EnergizerOrderBusinessRuleValidationService;
import com.energizer.core.business.service.EnergizerOrderEntryBusinessRuleValidationService;
import com.energizer.core.data.EnergizerB2BUnitData;
import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade;
import com.energizer.services.order.EnergizerB2BOrderService;


/**
 * @author M1023097
 * 
 */
public class DefaultEnergizerB2BCheckoutFlowFacade extends DefaultB2BCheckoutFlowFacade implements EnergizerB2BCheckoutFlowFacade
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerB2BCheckoutFlowFacade.class);

	@Resource(name = "orderEntryBusinessRulesService")
	EnergizerOrderEntryBusinessRuleValidationService orderEntryBusinessRulesService;

	@Resource(name = "orderBusinessRulesService")
	EnergizerOrderBusinessRuleValidationService orderBusinessRulesService;

	@Resource(name = "energizerB2BOrderService")
	EnergizerB2BOrderService energizerB2BOrderService;

	@Resource(name = "energizerSolrQueryManipulationService")
	EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	@Resource(name = "companyB2BCommerceService")
	CompanyB2BCommerceService companyB2BCommerceService;

	@Resource(name = "modelService")
	ModelService modelService;

	@Resource(name = "cartService")
	CartService cartService;

	@Resource(name = "orderService")
	OrderService orderService;

	@Resource(name = "productService")
	ProductService productService;

	@Resource(name = "priceService")
	PriceService priceService;

	@Resource
	private B2BOrderService b2bOrderService;

	private Converter<OrderModel, OrderData> energizerOrderConverter;
	private Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
	private Converter<AddressModel, AddressData> energizerAddressConverter;
	private Converter<CartModel, CartData> energizerCartConverter;
	private Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter;

	EnergizerB2BUnitModel b2bUnitModel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getEnergizerDeliveryAddresses()
	 */
	@Override
	public List<AddressData> getEnergizerDeliveryAddresses()
	{
		final List<AddressData> deliveryAddresses = new ArrayList<AddressData>();
		List<AddressModel> addressModels = new ArrayList<AddressModel>();
		final CartModel cartModel = getCart();
		if (cartModel != null)
		{
			addressModels = getDeliveryService().getSupportedDeliveryAddressesForOrder(cartModel, true);
		}
		for (final AddressModel model : addressModels)
		{
			final AddressData addressData = getEnergizerAddressConverter().convert(model);
			deliveryAddresses.add(addressData);
		}
		return deliveryAddresses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade#getCheckoutCart()
	 */
	@Override
	public CartData getCheckoutCart()
	{
		final CartModel cartModel = getCart();
		final CartData cartData = getEnergizerCartConverter().convert(cartModel);
		return cartData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getsoldToAddressIds()
	 */
	@Override
	public List<String> getsoldToAddressIds()
	{
		b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final List<String> shippingIds = energizerB2BOrderService.getsoldToAddressIds(b2bUnitModel);
		return shippingIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderValidation(de.hybris.platform.core.model.order
	 * .AbstractOrderEntryModel)
	 */
	@Override
	public List<BusinessRuleError> getOrderValidation(final AbstractOrderModel orderModel)
	{
		final OrderData orderData = new OrderData();
		/*
		 * final ProductModel product = orderEntryModel.getProduct(); final List<PriceInformation> prices =
		 * priceService.getPriceInformationsForProduct(product); final ProductData productData = new ProductData();
		 * productData.setCode(product.getCode()); productData.setDescription(product.getDescription());
		 * productData.setName(product.getName());
		 * 
		 * if (!prices.isEmpty()) { final PriceInformation price = prices.iterator().next(); final PriceData priceData =
		 * new PriceData(); priceData.setCurrencyIso(price.getPriceValue().getCurrencyIso());
		 * priceData.setValue(BigDecimal.valueOf(price.getPriceValue().getValue())); productData.setPrice(priceData); }
		 */

		final PriceData priceData = new PriceData();
		priceData.setCurrencyIso(orderModel.getCurrency().getIsocode());
		priceData.setValue(BigDecimal.valueOf(orderModel.getTotalPrice()));


		orderData.setTotalPrice(priceData);
		final List<BusinessRuleError> OrderDataError = new ArrayList<BusinessRuleError>();

		orderBusinessRulesService.validateBusinessRules(orderData);
		if (orderBusinessRulesService.hasErrors())
		{
			OrderDataError.addAll(orderBusinessRulesService.getErrors());
		}
		return OrderDataError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderShippingValidation(de.hybris.platform.core.model
	 * .order.AbstractOrderEntryModel)
	 */
	@Override
	public List<BusinessRuleError> getOrderShippingValidation(final AbstractOrderEntryModel orderEntryModel)
	{
		final OrderEntryData entrydata = getOrderEntryConverter().convert(orderEntryModel);
		final String shippingPoint = entrydata.getProduct().getShippingPoint();
		if (shippingPoint != null)
		{
			entrydata.setShippingPoint(shippingPoint);
		}
		final List<BusinessRuleError> OrderDataError = new ArrayList<BusinessRuleError>();
		orderEntryBusinessRulesService.validateBusinessRules(entrydata);
		if (orderEntryBusinessRulesService.hasErrors())
		{
			OrderDataError.addAll(orderEntryBusinessRulesService.getErrors());
		}
		return OrderDataError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getLeadTimeData(java.lang.String, java.lang.String)
	 */
	public int getLeadTimeData(final String shippingPointId, final String soldToAddressId)
	{
		int leadTIme = 0;
		final EnergizerB2BUnitModel b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final List<EnergizerB2BUnitLeadTimeModel> models = energizerB2BOrderService.getLeadTimeData(b2bUnitModel, shippingPointId,
				soldToAddressId);
		if (models.size() > 0)
		{
			for (final EnergizerB2BUnitLeadTimeModel leadTimeModel : models)
			{
				if (leadTimeModel.getB2bUnitId().getUid().equalsIgnoreCase(b2bUnitModel.getUid()))
				{
					leadTIme = leadTimeModel.getLeadTime();
				}
			}
		}
		return leadTIme;
	}

	/**
	 * @param deliveryDate
	 * @param orderCode
	 * @return <T extends AbstractOrderData>
	 */
	public <T extends AbstractOrderData> T setDeliveryDate(final String deliveryDate, final String orderCode)
	{
		final String[] splitString = deliveryDate.split("-");
		String deliveryDateFinal = "";
		deliveryDateFinal = splitString[1] + "/" + splitString[0] + "/" + splitString[2];
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = new Date();
		try
		{
			date = dateFormat.parse(deliveryDateFinal);
		}
		catch (final ParseException e)
		{
			e.printStackTrace();
		}
		final CartModel cartModel = cartService.getSessionCart();
		cartModel.setRequestedDeliveryDate(date);
		modelService.save(cartModel);
		return (T) getCheckoutCart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#getOrderData()
	 */
	@Override
	public AbstractOrderData getOrderData()
	{
		final CartModel cartModel = getCart();
		final AbstractOrderData orderData = getEnergizerCartConverter().convert((cartModel));

		return orderData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#simulateOrderMarshel(de.hybris.platform.commercefacades
	 * .order.data.CartData)
	 */
	public void updateSessionCart(final CartData cartData)
	{
		final CartModel model = getCart();
		model.setB2bUnit(getB2bUnitModel());
		model.setRequestedDeliveryDate(cartData.getRequestedDeliveryDate());
		final Double totalPrice = cartData.getTotalPrice().getValue().doubleValue();
		final Double totalTax = cartData.getTotalTax().getValue().doubleValue();
		final Double discount = cartData.getTotalDiscounts().getValue().doubleValue();
		model.setTotalPrice(totalPrice);
		model.setSubtotal(totalPrice - discount - totalTax);
		model.setTotalDiscounts(discount);
		model.setTotalTax(totalTax);
		final List<AbstractOrderEntryModel> modelEntries = model.getEntries();
		final List<OrderEntryData> dataEntries = cartData.getEntries();
		for (final AbstractOrderEntryModel modelEntry : modelEntries)
		{
			updateModelEntry(modelEntry, dataEntries);
			getModelService().save(modelEntry);
		}
		//getCartService().calculateCart(model);
		//model.setCalculated(true);
		getModelService().save(model);
		//getCartService().calculateCart(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#setLeadTime(int)
	 */
	@Override
	public void setLeadTime(final int leadTime)
	{
		final CartModel cartModel = cartService.getSessionCart();
		cartModel.setLeadTime(leadTime);

		final Date sysCurrentDate = new Date();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(sysCurrentDate);
		cal.add(Calendar.DATE, leadTime); // add LeadTime
		cartModel.setRequestedDeliveryDate(cal.getTime());

		modelService.save(cartModel);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.facades.flow.EnergizerB2BCheckoutFlowFacade#simulateOrder(de.hybris.platform.commercefacades.order
	 * .data.CartData)
	 */
	@Override
	public CartData simulateOrder(final CartData cartData) throws Exception
	{
		final EnergizerB2BUnitModel b2bUnitModel = energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser();
		final Date requestedDelDate = getCartService().getSessionCart().getRequestedDeliveryDate();
		cartData.setRequestedDeliveryDate(requestedDelDate);
		final EnergizerB2BUnitData unitdata = getEnergizerB2BUnitConverter().convert(b2bUnitModel);
		cartData.setB2bUnit(unitdata);
		return energizerB2BOrderService.simulateOrder(cartData);

	}


	private void updateModelEntry(final AbstractOrderEntryModel modelEntry, final List<OrderEntryData> dataEntries)
	{
		for (final OrderEntryData dataEntrie : dataEntries)
		{
			final ProductData prodData = dataEntrie.getProduct();
			final ProductModel prodModel = modelEntry.getProduct();
			if (prodData.getCode().equals(prodModel.getCode()))
			{
				final Long modelQuantity = modelEntry.getQuantity();
				final Long dataQuantity = dataEntrie.getQuantity();
				LOG.info("modelQuantity =" + modelQuantity + " dataQuantity =" + dataQuantity);
				if (!(modelQuantity == dataQuantity))
				{
					modelEntry.setQuantity(dataQuantity);
				}
				final double modelPrice = modelEntry.getBasePrice();
				final double dataPrice = dataEntrie.getBasePrice().getValue().doubleValue();
				LOG.info("modelPrice =" + modelPrice + " dataPrice =" + dataPrice);
				if (!(modelPrice == dataPrice))
				{
					modelEntry.setBasePrice(dataPrice);
				}
				modelEntry.setTotalPrice(dataEntrie.getTotalPrice().getValue().doubleValue());
			}
		}
	}



	/**
	 * @param currentUser
	 * @param string
	 */
	public void setOrderApprover(final EnergizerB2BCustomerModel orderApprover, final String orderCode)
	{
		final OrderModel orderModel = b2bOrderService.getOrderForCode(orderCode);
		orderModel.setOrderApprover(orderApprover);
		modelService.save(orderModel);
	}

	/**
	 * @return the energizerB2BUnitConverter
	 */
	public Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> getEnergizerB2BUnitConverter()
	{
		return energizerB2BUnitConverter;
	}

	/**
	 * @param energizerB2BUnitConverter
	 *           the energizerB2BUnitConverter to set
	 */
	@Required
	public void setEnergizerB2BUnitConverter(final Converter<EnergizerB2BUnitModel, EnergizerB2BUnitData> energizerB2BUnitConverter)
	{
		this.energizerB2BUnitConverter = energizerB2BUnitConverter;
	}

	/**
	 * @param productCode
	 * @return ProductData
	 */
	public ProductData getProduct(final String productCode)
	{
		return null;
	}

	/**
	 * @param productService
	 */
	public void setProductService(final ProductService productService)
	{
		this.productService = productService;
	}

	/**
	 * @param priceService
	 */
	public void setPriceService(final PriceService priceService)
	{
		this.priceService = priceService;
	}

	/**
	 * @return orderEntryConverter
	 */
	public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter()
	{
		return orderEntryConverter;
	}

	/**
	 * @param orderEntryConverter
	 */
	@Required
	public void setOrderEntryConverter(final Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter)
	{
		this.orderEntryConverter = orderEntryConverter;
	}

	/**
	 * @return energizerOrderConverter
	 */
	public Converter<OrderModel, OrderData> getEnergizerOrderConverter()
	{
		return energizerOrderConverter;
	}

	/**
	 * @param energizerOrderConverter
	 */
	public void setEnergizerOrderConverter(final Converter<OrderModel, OrderData> energizerOrderConverter)
	{
		this.energizerOrderConverter = energizerOrderConverter;
	}

	/**
	 * @return energizerAddressConverter
	 */
	public Converter<AddressModel, AddressData> getEnergizerAddressConverter()
	{
		return energizerAddressConverter;
	}

	/**
	 * @param energizerAddressConverter
	 */
	public void setEnergizerAddressConverter(final Converter<AddressModel, AddressData> energizerAddressConverter)
	{
		this.energizerAddressConverter = energizerAddressConverter;
	}

	/**
	 * @return energizerCartConverter
	 */
	public Converter<CartModel, CartData> getEnergizerCartConverter()
	{
		return energizerCartConverter;
	}

	/**
	 * @param energizerCartConverter
	 */
	public void setEnergizerCartConverter(final Converter<CartModel, CartData> energizerCartConverter)
	{
		this.energizerCartConverter = energizerCartConverter;
	}


	/**
	 * @return the b2bUnitModel
	 */
	public EnergizerB2BUnitModel getB2bUnitModel()
	{
		return b2bUnitModel;
	}

	/**
	 * @param b2bUnitModel
	 *           the b2bUnitModel to set
	 */
	public void setB2bUnitModel(final EnergizerB2BUnitModel b2bUnitModel)
	{
		this.b2bUnitModel = b2bUnitModel;
	}

	/**
	 * 
	 */
	public CartModel getSessionCart()
	{
		return cartService.getSessionCart();
	}

	/**
	 * @param entryModel
	 */
	public void saveEntry(final AbstractOrderEntryModel entryModel)
	{
		// YTODO Auto-generated method stub
		modelService.save(entryModel);
	}

}
