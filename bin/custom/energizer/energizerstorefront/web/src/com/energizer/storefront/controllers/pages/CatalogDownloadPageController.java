/**
 *
 */
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.impex.ExportConfig;
import de.hybris.platform.servicelayer.impex.ExportConfig.ValidationMode;
import de.hybris.platform.servicelayer.impex.ExportResult;
import de.hybris.platform.servicelayer.impex.ExportService;
import de.hybris.platform.servicelayer.impex.ImpExResource;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.util.CSVConstants;

import java.io.StringBufferInputStream;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.facades.catalogdownload.EnergizerCatalogDownloadFacade;
import com.energizer.storefront.annotations.RequireHardLogIn;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;


/**
 * @author M1028886
 *
 */
@Controller
@Scope("tenant")
@RequestMapping("/my_account")
public class CatalogDownloadPageController extends AbstractSearchPageController
{
	protected static final Logger LOG = Logger.getLogger(CatalogDownloadPageController.class);

	@Resource(name = "accountBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder accountBreadcrumbBuilder;

	private static final String CATALOG_DOWNLOAD_PAGE = "catalogdownload";

	@Resource(name = "defaultEnergizerCatalogDownloadFacade")
	private EnergizerCatalogDownloadFacade defaultEnergizerCatalogDownloadFacade;

	@Resource
	private ExportService exportService;

	@Autowired
	private ConfigurationService configurationService;


	@RequestMapping(value = "/catalogDownload")
	//, method = RequestMethod.POST)
	@RequireHardLogIn
	public String downloadCatalog(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException
	{

		storeCmsPageInModel(model, getContentPageForLabelOrId(CATALOG_DOWNLOAD_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(CATALOG_DOWNLOAD_PAGE));
		model.addAttribute("breadcrumbs", accountBreadcrumbBuilder.getBreadcrumbs("text.account.catalogDownload"));
		model.addAttribute("metaRobots", "no-index,no-follow");

		LOG.info("catalog Download");

		try
		{

			final String exportString = defaultEnergizerCatalogDownloadFacade.generateScript();

			LOG.info("In Controller class, script generator result  ---  " + exportString);


			final ImpExResource exportResource = new StreamBasedImpExResource(new StringBufferInputStream(exportString),
					CSVConstants.HYBRIS_ENCODING);



			final ExportConfig exportConfig = new ExportConfig();
			exportConfig.setFailOnError(false);
			exportConfig.setValidationMode(ValidationMode.RELAXED);
			exportConfig.setScript(exportResource);
			exportConfig.setSingleFile(true);


			if (exportConfig != null)
			{
				LOG.info("Export Config Object " + exportConfig.getScript());
			}
			else
			{
				LOG.info("Export Config Object is null");
			}

			final ExportResult result = exportService.exportData(exportConfig);
			if (result.isSuccessful())
			{
				defaultEnergizerCatalogDownloadFacade.copyExportedMediaToExportDir(result);
				GlobalMessages.addErrorMessage(model, "catalog.download.success");

				//"Catalog is Successfully downloaded to your Download Folder");

				/*
				 * cron.setExportedData(result.getExportedData()); cron.setExportedMedia(result.getExportedMedia());
				 * modelService.save(cron);
				 */
			}


		}
		catch (final Exception e)
		{
			LOG.info("Error: " + e.getMessage());
			LOG.info("Exception cause: " + e.getCause());
			e.printStackTrace();
		}

		return ControllerConstants.Views.Pages.Account.AccountCatalogDownload;

	}





}
