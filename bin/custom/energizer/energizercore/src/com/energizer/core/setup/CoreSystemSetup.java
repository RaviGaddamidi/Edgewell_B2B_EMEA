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
package com.energizer.core.setup;

import de.hybris.platform.commerceservices.setup.AbstractSystemSetup;
import de.hybris.platform.commerceservices.setup.data.ImportData;
import de.hybris.platform.commerceservices.setup.events.CoreDataImportedEvent;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetup.Process;
import de.hybris.platform.core.initialization.SystemSetup.Type;
import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.core.initialization.SystemSetupParameter;
import de.hybris.platform.core.initialization.SystemSetupParameterMethod;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.validation.services.ValidationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.energizer.core.constants.EnergizerCoreConstants;


/**
 * This class provides hooks into the system's initialization and update processes.
 * 
 * @see "https://wiki.hybris.com/display/release4/Hooks+for+Initialization+and+Update+Process"
 */
@SystemSetup(extension = EnergizerCoreConstants.EXTENSIONNAME)
public class CoreSystemSetup extends AbstractSystemSetup
{
	public static final String IMPORT_SITES = "importSites";
	public static final String IMPORT_SYNC_CATALOGS = "syncProducts&ContentCatalogs";
	public static final String IMPORT_ACCESS_RIGHTS = "accessRights";
	public static final String ACTIVATE_SOLR_CRON_JOBS = "activateSolrCronJobs";

	//	public static final String ENERGIZER = "energizer";
		public static final String ENERGIZER = "personalCare";
	//public static final String ENERGIZER = "houseHold";



	/**
	 * This method will be called by system creator during initialization and system update. Be sure that this method can
	 * be called repeatedly.
	 * 
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.ESSENTIAL, process = Process.ALL)
	public void createEssentialData(final SystemSetupContext context)
	{
		importImpexFile(context, "/energizercore/import/essential-data.impex");
		importImpexFile(context, "/energizercore/import/countries.impex");
		importImpexFile(context, "/energizercore/import/delivery-modes.impex");
		importImpexFile(context, "/energizercore/import/essentialdata-energizerCronjobs.impex");
		importImpexFile(context, "/energizercore/import/themes.impex");
	}

	/**
	 * Generates the Dropdown and Multi-select boxes for the project data import
	 */
	@Override
	@SystemSetupParameterMethod
	public List<SystemSetupParameter> getInitializationOptions()
	{
		final List<SystemSetupParameter> params = new ArrayList<SystemSetupParameter>();

		params.add(createBooleanSystemSetupParameter(IMPORT_SITES, "Import Sites", true));
		params.add(createBooleanSystemSetupParameter(IMPORT_SYNC_CATALOGS, "Sync Products & Content Catalogs", false));
		params.add(createBooleanSystemSetupParameter(IMPORT_ACCESS_RIGHTS, "Import Users & Groups", true));
		params.add(createBooleanSystemSetupParameter(ACTIVATE_SOLR_CRON_JOBS, "Activate Solr Cron Jobs", false));

		return params;
	}

	/**
	 * This method will be called during the system initialization.
	 * 
	 * @param context
	 *           the context provides the selected parameters and values
	 */
	@SystemSetup(type = Type.PROJECT, process = Process.ALL)
	public void createProjectData(final SystemSetupContext context)
	{
		final boolean importSites = getBooleanSystemSetupParameter(context, IMPORT_SITES);
		//final boolean syncProducts = getBooleanSystemSetupParameter(context, IMPORT_SYNC_PRODUCTS);

		final boolean importAccessRights = getBooleanSystemSetupParameter(context, IMPORT_ACCESS_RIGHTS);

		if (importSites)
		{
			importProductCatalog(context, ENERGIZER);

			importContentCatalog(context, ENERGIZER);

			executeCatalogSyncJob(context, ENERGIZER);

			importStore(context, ENERGIZER);

			createAndActivateSolrIndex(context, ENERGIZER);

			((ValidationService) Registry.getApplicationContext().getBean("validationService")).reloadValidationEngine();
		}

		final List<String> extensionNames = Registry.getCurrentTenant().getTenantSpecificExtensionNames();

		if (importAccessRights && extensionNames.contains("cmscockpit"))
		{
			importImpexFile(context, "/energizercore/import/cockpits/cmscockpit/cmscockpit-users.impex");
			importImpexFile(context, "/energizercore/import/cockpits/cmscockpit/cmscockpit-access-rights.impex");
		}

		if (importAccessRights && extensionNames.contains("btgcockpit"))
		{
			importImpexFile(context, "/energizercore/import/cockpits/cmscockpit/btgcockpit-users.impex");
			importImpexFile(context, "/energizercore/import/cockpits/cmscockpit/btgcockpit-access-rights.impex");
		}

		if (importAccessRights && extensionNames.contains("productcockpit"))
		{
			importImpexFile(context, "/energizercore/import/cockpits/productcockpit/productcockpit-users.impex");
			importImpexFile(context, "/energizercore/import/cockpits/productcockpit/productcockpit-access-rights.impex");
			importImpexFile(context, "/energizercore/import/cockpits/productcockpit/productcockpit-constraints.impex");
		}

		if (importAccessRights && extensionNames.contains("cscockpit"))
		{
			importImpexFile(context, "/energizercore/import/cockpits/cscockpit/cscockpit-users.impex");
			importImpexFile(context, "/energizercore/import/cockpits/cscockpit/cscockpit-access-rights.impex");
		}

		if (extensionNames.contains("mcc"))
		{
			importImpexFile(context, "/energizercore/import/mcc-sites-links.impex");
		}

		final ImportData powertoolsImportData = new ImportData();
		powertoolsImportData.setProductCatalogName(ENERGIZER);
		powertoolsImportData.setContentCatalogNames(Arrays.asList(ENERGIZER));
		powertoolsImportData.setStoreNames(Arrays.asList(ENERGIZER));
		getEventService().publishEvent(new CoreDataImportedEvent(context, Arrays.asList(powertoolsImportData)));

	}

	@Override
	public PerformResult executeCatalogSyncJob(final SystemSetupContext context, final String catalogName)
	{
		final boolean syncCatalogs = getBooleanSystemSetupParameter(context, IMPORT_SYNC_CATALOGS);
		logInfo(context, "Begin preparing catalogs sync job  [" + catalogName + "]");
		importImpexFile(context, "/energizercore/import/catalogs-sync.impex", true);

		getSetupSyncJobService().assignDependentSyncJobs(catalogName + "ProductCatalog",
				Collections.singleton(catalogName + "ContentCatalog"));

		logInfo(context, "Done preparing catalogs sync job  [" + catalogName + "]");
		PerformResult syncCronJobResult = null;
		if (syncCatalogs)
		{
			logInfo(context, "Executing catalogs sync job  [" + catalogName + "]");
			syncCronJobResult = super.executeCatalogSyncJob(context, ENERGIZER + "Catalog");
			logInfo(context, "Executed catalogs sync job  [" + catalogName + "]");
		}
		return syncCronJobResult;
	}

	protected void importProductCatalog(final SystemSetupContext context, final String catalogName)
	{
		logInfo(context, "Begin importing catalog [" + catalogName + "]");

		importImpexFile(context, "/energizercore/import/productCatalogs/" + catalogName + "ProductCatalog/catalog.impex", true);

		createProductCatalogSyncJob(context, catalogName + "ProductCatalog");

	}

	protected void createAndActivateSolrIndex(final SystemSetupContext context, final String storeName)
	{
		logInfo(context, "Begin SOLR index setup [" + storeName + "]");

		importImpexFile(context, "/energizercore/import/stores/" + storeName + "/solr.impex");

		createSolrIndexerCronJobs(storeName + "Index");

		importImpexFile(context, "/energizercore/import/stores/" + storeName + "/solrtrigger.impex");

		if (getBooleanSystemSetupParameter(context, ACTIVATE_SOLR_CRON_JOBS))
		{
			executeSolrIndexerCronJob(storeName + "Index", true);
			activateSolrIndexerCronJobs(storeName + "Index");
		}

		logInfo(context, "Done SOLR index setup [" + storeName + "]");
	}

	protected void importContentCatalog(final SystemSetupContext context, final String catalogName)
	{
		logInfo(context, "Begin importing catalog [" + catalogName + "]");

		importImpexFile(context, "/energizercore/import/contentCatalogs/" + catalogName + "ContentCatalog/catalog.impex", true);
		importImpexFile(context, "/energizercore/import/contentCatalogs/" + catalogName + "ContentCatalog/cms-content.impex", true);
		importImpexFile(context,
				"/energizercore/import/contentCatalogs/" + catalogName + "ContentCatalog/cms-mobile-content.impex", false);

		importImpexFile(context, "/energizercore/import/contentCatalogs/" + catalogName + "ContentCatalog/email-content.impex",
				false);

		createContentCatalogSyncJob(context, catalogName + "ContentCatalog");



		logInfo(context, "Done importing catalog [" + catalogName + "]");
	}

	protected void importStore(final SystemSetupContext context, final String storeName)
	{
		logInfo(context, "Begin importing store [" + storeName + "]");

		importImpexFile(context, "/energizercore/import/stores/" + storeName + "/store.impex");
		importImpexFile(context, "/energizercore/import/stores/" + storeName + "/site.impex");

		logInfo(context, "Done importing store [" + storeName + "]");
	}
}
