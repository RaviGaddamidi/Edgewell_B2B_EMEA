/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;

import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.energizer.core.invoice.EnergizerInvoiceService;
import com.energizer.storefront.annotations.RequireHardLogIn;


/**
 * Controller for home page.
 */
@Controller
@Scope("tenant")
@RequestMapping("/my-account/invoice")
public class InvoicePageController extends AbstractSearchPageController
{
	private static final String INVOICE_NUMBER_PATTERN = "{invoiceNumber:.*}";

	private static final String INVOICE_FILE_MIME = "application/pdf";

	public static final String INVOICE_FILE_EXTENSION = ".pdf";


	@Resource(name = "invoiceService")
	private EnergizerInvoiceService invoiceService;



	@RequestMapping(value = "/" + INVOICE_NUMBER_PATTERN, method = RequestMethod.GET)
	@RequireHardLogIn
	public void account(@PathVariable("invoiceNumber") final String invoiceNumber,
			@RequestParam(value = "inline", required = false) final Boolean inline, final Model model,
			final HttpServletRequest request, final HttpServletResponse response) throws CMSItemNotFoundException, IOException
	{

		final byte pdfFile[] = invoiceService.getPDFInvoiceAsBytes(invoiceNumber);
		response.setContentType(INVOICE_FILE_MIME);

		if (inline == null)
		{
			response.addHeader("Content-Disposition", "attachment; filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
		}

		if (inline != null && inline.booleanValue())
		{
			response.addHeader("Content-Disposition", "inline; filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
		}
		else if (inline != null && !inline.booleanValue())
		{
			response.addHeader("Content-Disposition", "attachment; filename=" + invoiceNumber + INVOICE_FILE_EXTENSION);
		}

		response.setContentLength(pdfFile.length);


		final OutputStream responseOutputStream = response.getOutputStream();
		responseOutputStream.write(pdfFile);


	}
}
