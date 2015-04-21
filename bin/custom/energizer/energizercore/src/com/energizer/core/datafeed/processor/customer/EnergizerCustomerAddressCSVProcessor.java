/**
 * 
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerAddressFacade;
import com.energizer.core.model.EnergizerB2BUnitModel;



/**
 * 
 * This processors imports the customer address .
 * 
 * Sample file will look like
 * 
 * SPCustomerID,Status,SPNAME,          SPCITY,      SPPOSTALCODE,  SPSTREET,         SPCOUNTRYKEY,SPREGION,SPSalesPerson
 * 820,         1,     SONOCO CORRFLEX, CINNAMINSON, 8077,          2703 CINDEL DRIVE,US,           NJ,      ABC
 * 
 * Total column count : 9
 */
public class EnergizerCustomerAddressCSVProcessor extends AbstractEnergizerCSVProcessor
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#parse(de.hybris.platform.util.CSVReader)
	 */

	@Resource
	private ModelService modelService;

	@Resource
	private CatalogService catalogService;

	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource
	private DefaultEnergizerAddressFacade defaultEnergizerAddressFacade;

	private static final Logger LOG = Logger.getLogger(EnergizerCustomerAddressCSVProcessor.class);

	private static final String SP_CUSTOMER_ID = "SPCustomerID";
	private static final String SP_SALES_PERSON = "SPSalesPerson";
	private static final String SP_NAME = "SPNAME";
	private static final String SP_CITY = "SPCITY";
	private static final String SP_POSTAL_CODE = "SPPOSTALCODE";
	private static final String SP_STREET = "SPSTREET";
	private static final String SP_COUNTRY_KEY = "SPCOUNTRYKEY";
	private static final String SP_REGION = "SPREGION";
	private static final String STATUS = "Status";


	/**
    * 
    */
	public EnergizerCustomerAddressCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		final List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		List<AddressModel> energizerB2BUnitModelList = null;
		try
		{
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				final Map<String, String> csvValuesMap = record.toMap();
				String erpAddressId = "";
				if (null != csvValuesMap.get(SP_CUSTOMER_ID).trim())
				{
					erpAddressId = csvValuesMap.get(SP_CUSTOMER_ID).trim();
				}

				validate(record);
				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					continue;
				}
				AddressModel energizerAddress = modelService.create(AddressModel.class);

				//Retreive an energizer model based on erpAddressId.
				//If doesn't exist then insert into Database otherwise disallow the insertion.
				try
				{
					energizerB2BUnitModelList = defaultEnergizerAddressFacade.fetchAddress(erpAddressId);
				}
				catch (final Exception e)
				{
					LOG.error("Unknown Identifier Exception", e);
				}


				if (energizerB2BUnitModelList.size() == 0)
				{

					if (!csvValuesMap.get(SP_CUSTOMER_ID).trim().isEmpty())
					{
						energizerAddress.setErpAddressId(csvValuesMap.get(SP_CUSTOMER_ID).trim());
						energizerAddress.setCompany(csvValuesMap.get(SP_NAME).trim());
						energizerAddress.setTown(csvValuesMap.get(SP_CITY).trim());
						energizerAddress.setPostalcode(csvValuesMap.get(SP_POSTAL_CODE).trim());
						energizerAddress.setStreetname(csvValuesMap.get(SP_STREET).trim());
						final String[] display = csvValuesMap.get(SP_SALES_PERSON).trim().split("@");
						energizerAddress.setDisplayName(display[0]);
						energizerAddress.setSalesPersonEmailId(csvValuesMap.get(SP_SALES_PERSON).trim());
						energizerAddress.setShippingAddress(new Boolean(csvValuesMap.get(STATUS).trim()));


						try
						{
							final CountryModel countryModel = new CountryModel();
							countryModel.setIsocode(csvValuesMap.get(SP_COUNTRY_KEY).trim());
							final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
							energizerAddress.setCountry(countryModelfound);
							final RegionModel regionModel = new RegionModel();
							regionModel.setIsocode(csvValuesMap.get(SP_REGION).trim());
							regionModel.setCountry(countryModelfound);
							final RegionModel regionModelfound = flexibleSearchService.getModelByExample(regionModel);
							energizerAddress.setRegion(regionModelfound);
						}
						catch (final Exception e)
						{
							LOG.error("No Region or country Found", e);
						}
						final EnergizerB2BUnitModel b2bUnitmodel = (EnergizerB2BUnitModel) companyB2BCommerceService
								.getUnitForUid(erpAddressId);
						if (b2bUnitmodel != null)
						{
							energizerAddress.setOwner(b2bUnitmodel);
							modelService.save(energizerAddress);
							LOG.info("CustomerAddress saved");
							succeedRecord++;
							setRecordSucceeded(succeedRecord);
						}
						else
						{
							LOG.error("Invalid attempt!!! No corresponding B2BUnit found");
						}
					}
				}
				else
				{
					if (!csvValuesMap.get(SP_CUSTOMER_ID).trim().isEmpty())
					{

						energizerAddress = energizerB2BUnitModelList.get(0);
						energizerAddress.setCompany(csvValuesMap.get(SP_NAME).trim());
						energizerAddress.setTown(csvValuesMap.get(SP_CITY).trim());
						energizerAddress.setPostalcode(csvValuesMap.get(SP_POSTAL_CODE).trim());
						energizerAddress.setStreetname(csvValuesMap.get(SP_STREET).trim());
						try
						{
							final CountryModel countryModel = new CountryModel();
							countryModel.setIsocode(csvValuesMap.get(SP_COUNTRY_KEY).trim());
							final CountryModel countryModelfound = flexibleSearchService.getModelByExample(countryModel);
							energizerAddress.setCountry(countryModelfound);
							final RegionModel regionModel = new RegionModel();
							regionModel.setIsocode(csvValuesMap.get(SP_REGION).trim());
							regionModel.setCountry(countryModelfound);
						}
						catch (final Exception e)
						{
							LOG.error("No Region or country Found", e);
						}
						energizerAddress.setSalesPersonEmailId(csvValuesMap.get(SP_SALES_PERSON).trim());
						final String[] display = csvValuesMap.get(SP_SALES_PERSON).trim().split("@");
						energizerAddress.setDisplayName(display[0]);
						energizerAddress.setShippingAddress(new Boolean(csvValuesMap.get(STATUS).trim()));

						modelService.save(energizerAddress);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
						LOG.info("Models are updated");
					}
				}
			}
			LOG.info("EnergizerCustomerAddressCSVProcessor:process:Start");
		}
		catch (final Exception e)

		{

			LOG.error("EnergizerCustomerAddressCSVProcessor:Exception in saving EnergizerCustomerAddress", e);
		}
		return errors;
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (value.isEmpty())
			{
				long recordFailed = getRecordFailed();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber());
				columnNames.add(columnHeader);
				error.setUserType(TECHNICAL_USER);
				error.setColumnName(columnNames);
				error.setMessage(columnHeader + " column should not be empty");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}
		}
	}


}
