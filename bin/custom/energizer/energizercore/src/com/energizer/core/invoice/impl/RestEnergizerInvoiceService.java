/**
 * 
 */
package com.energizer.core.invoice.impl;

import de.hybris.platform.util.Config;

import org.springframework.web.client.RestTemplate;

import com.energizer.core.invoice.EnergizerInvoiceService;


/**
 * Fetch PDF from a file
 * 
 * @author kaushik.ganguly
 * 
 */
public class RestEnergizerInvoiceService implements EnergizerInvoiceService
{

	public static final String INVOICE_URL_PATH = "invoice.urlpath";

	public static final String INVOICE_FILE_EXTENSION = ".pdf";


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.core.invoice.EnergizerInvoiceService#getPDFInvoiceAsBytes()
	 */
	@Override
	public byte[] getPDFInvoiceAsBytes(final String invoiceNumber)
	{
		// YTODO Auto-generated method stub

		return getPDFFromFilePath(invoiceNumber);
	}

	private byte[] getPDFFromFilePath(final String invoiceNumber)
	{
		byte retVal[] = null;
		try
		{
			final RestTemplate restTemplate = new RestTemplate();
			retVal = restTemplate.getForObject(Config.getParameter(INVOICE_URL_PATH) + invoiceNumber + INVOICE_FILE_EXTENSION,
					byte[].class);
		}
		catch (final Exception ex)
		{
			retVal = null;
		}
		return retVal;
	}
}
