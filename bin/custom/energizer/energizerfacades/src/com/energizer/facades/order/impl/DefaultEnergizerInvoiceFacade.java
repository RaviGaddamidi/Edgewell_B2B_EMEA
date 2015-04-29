/**
 * 
 */
package com.energizer.facades.order.impl;

import de.hybris.platform.b2bacceleratorfacades.order.B2BOrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;

import javax.annotation.Resource;

import com.energizer.core.invoice.EnergizerInvoiceService;
import com.energizer.facades.order.EnergizerInvoiceFacade;


/**
 * @author M1023278
 * 
 */
public class DefaultEnergizerInvoiceFacade implements EnergizerInvoiceFacade
{

	@Resource(name = "invoiceService")
	private EnergizerInvoiceService invoiceService;

	@Resource(name = "b2bOrderFacade")
	private B2BOrderFacade orderFacade;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.order.EnergizerInvoiceFacade#getPDFInvoiceAsBytes(java.lang.String)
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final String orderNumber)
	{
		final OrderData orderData = orderFacade.getOrderDetailsForCode(orderNumber);
		return (invoiceService.getPDFInvoiceAsBytes(orderData));

	}


}
