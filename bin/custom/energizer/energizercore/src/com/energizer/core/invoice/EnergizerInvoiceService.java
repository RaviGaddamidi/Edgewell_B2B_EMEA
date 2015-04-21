/**
 * 
 */
package com.energizer.core.invoice;

/**
 * @author kaushik.ganguly
 * 
 */
public interface EnergizerInvoiceService
{
	public byte[] getPDFInvoiceAsBytes(String invoiceNumber);

}
