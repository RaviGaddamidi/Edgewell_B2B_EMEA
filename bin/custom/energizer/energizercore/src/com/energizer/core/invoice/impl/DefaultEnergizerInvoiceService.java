/**
 * 
 */
package com.energizer.core.invoice.impl;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.energizer.core.invoice.EnergizerInvoiceService;


/**
 * Fetch PDF from a file
 * 
 * @author kaushik.ganguly
 * 
 */
public class DefaultEnergizerInvoiceService implements EnergizerInvoiceService
{


	public static final String INVOICE_FILE_PATH = "invoice.filepath";
	public static final String INVOICE_FILE_EXTENSION = ".pdf";


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.invoice.EnergizerInvoiceService#getPDFInvoiceAsBytes()
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final OrderData orderData)
	{
		// YTODO Auto-generated method stub

		return getPDFFromFilePath(orderData.getCode());
	}

	private byte[] getPDFFromFilePath(final String orderNumber)
	{
		byte retVal[] = null;
		try
		{
			final String filePath = Config.getParameter(INVOICE_FILE_PATH);
			retVal = IOUtils.toByteArray(new FileInputStream(new File(filePath + orderNumber + INVOICE_FILE_EXTENSION)));
		}
		catch (final IOException ex)
		{
			retVal = null;
		}
		return retVal;
	}
}
