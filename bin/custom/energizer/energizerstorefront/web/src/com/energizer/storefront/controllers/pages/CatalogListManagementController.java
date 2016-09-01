/**
 * 
 */
package com.energizer.storefront.controllers.pages;


import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.servicelayer.session.SessionService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;


/**
 * @author M1028886
 *
 */
/**
 *
 */
@Controller
@Scope("tenant")
public class CatalogListManagementController extends AbstractPageController
{
	private static final Logger LOG = Logger.getLogger(CatalogListManagementController.class);

	@Resource(name = "sessionService")
	private SessionService sessionService;

	@Resource(name = "energizerCompanyB2BCommerceFacade")
	private EnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade;

	@Resource(name = "cartFacade")
	protected CartFacade cartfacade;

	@RequestMapping(value = "/catalogRedirect")
	@ResponseBody
	public String selecetdCatalogRequest(final Model model, final HttpServletRequest request)
	{

		String referer = null;
		final String firstSelectedCatalog = request.getParameter("selectedCatalog");

		String lastSelectedCatalog = sessionService.getAttribute("lastSelectedCatalog");

		if (lastSelectedCatalog == null)
		{
			lastSelectedCatalog = firstSelectedCatalog;
		}

		if (null != firstSelectedCatalog && cartfacade.hasEntries()
				&& !(firstSelectedCatalog.equalsIgnoreCase(lastSelectedCatalog)))
		{
			sessionService.setAttribute("cataloglistwarn", "true");
			sessionService.setAttribute("selectedCatalog", lastSelectedCatalog);
			sessionService.setAttribute("lastSelectedCatalog", lastSelectedCatalog);
			request.getSession().setAttribute("currentCatalog", lastSelectedCatalog);
			referer = "/";
		}
		else
		{
			sessionService.setAttribute("cataloglistwarn", "false");
			sessionService.setAttribute("selectedCatalog", firstSelectedCatalog);
			sessionService.setAttribute("lastSelectedCatalog", firstSelectedCatalog);
			request.getSession().setAttribute("currentCatalog", firstSelectedCatalog);
			referer = request.getHeader("referer");
		}

		return referer;

	}
}
