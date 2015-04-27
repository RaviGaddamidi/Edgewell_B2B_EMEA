/**
 * 
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.commercefacades.order.converters.populator.OrderPopulator;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import javax.annotation.Resource;


/**
 * @author M1023097
 * 
 */
public class EnergizerOrderPopulator extends OrderPopulator
{

	@Resource(name = "b2BCustomerConverter")
	private Converter<B2BCustomerModel, CustomerData> b2BCustomerConverter;

	@Override
	public void populate(final OrderModel source, final OrderData target)
	{
		super.populate(source, target);
		target.setErpOrderNumber(source.getErpOrderNumber());
		target.setInvoiceNumber(source.getInvoiceNumber());
		target.setInvoicePDF(source.getInvoicePDF());
		target.setErpOrderCreator(source.getErpOrderCreator());
		target.setShipmentTrackingURL(source.getShipmentTrackingURL());
		addTotals(source, target);
		target.setRequestedDeliveryDate(source.getRequestedDeliveryDate());
		if (source.getOrderApprover() != null)
		{
			target.setOrderApprover(b2BCustomerConverter.convert(source.getOrderApprover()));
		}
		target.setContainerId(source.getContainerId());
		target.setSealNumber(source.getSealNumber());
		target.setVesselNumber(source.getVesselNumber());
		target.setDocumentID(source.getDocumentID());
		target.setDocumentClass(source.getDocumentClass());
		target.setContrEP(source.getContrEP());
		//target.setArchiveID(source.getArchiveID());


	}

	protected void addTotals(final OrderModel orderEntry, final OrderData orderData)
	{
		if (orderEntry.getAdjustedTotalPrice() != null)
		{
			orderData.setAdjustedTotalPrice(createPrice(orderEntry, orderEntry.getAdjustedTotalPrice().doubleValue()));
		}
		if (orderEntry.getAdjustedShippingCharge() != null)
		{
			orderData.setAdjustedShippingCharge(createPrice(orderEntry, orderEntry.getAdjustedShippingCharge().doubleValue()));
		}
		if (orderEntry.getAdjustedTaxCharges() != null)
		{
			orderData.setAdjustedTaxCharges(createPrice(orderEntry, orderEntry.getAdjustedTaxCharges().doubleValue()));
		}
		if (orderEntry.getDeliveryCost() != null)
		{
			orderData.setDeliveryCost(createPrice(orderEntry, orderEntry.getDeliveryCost().doubleValue()));
		}

	}
}
