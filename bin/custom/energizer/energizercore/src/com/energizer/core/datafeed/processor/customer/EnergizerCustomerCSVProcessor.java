/**
 * 
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * 
 * This processors imports the customer address .
 * 
 * Sample file will look like
 * 
 * CustomerID,SalesOrg,DistributionChannel,Division,Customername,OrderType,DeletionFlag,Status,Currency,
 * MinimumOrderValue,DefaultLanguage 1006, 1000, 10, 10, tyfg, ZOR, 1, 1, USD, 10, EN
 * 
 * Total column count : 11
 */
public class EnergizerCustomerCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;
	private static final Logger LOG = Logger.getLogger(EnergizerCustomerCSVProcessor.class);
	@Resource
	private CommonI18NService commonI18NService;

	private static final String MAX_USER_LIMIT = "MaxUserLimit";
	private static final String MAX_USER_LIMIT_COUNT = "10";
	private static final String CUSTOMER_ID = "CustomerID";
	private final String SALES_ORG = "SalesOrg";
	private final String DISTRIBUTION_CHANNEL = "DistributionChannel";
	private final String DIVISION = "Division";
	private final String CUSTOMER_NAME = "Customername";
	private final String ORDER_TYPE = "OrderType";
	private final String STATUS = "Status";
	private final String DELETION_FLAG = "DeletionFlag";
	private final String CURRENCY = "Currency";
	private final String DEFAULT_LANGUAGE = "DefaultLanguage";
	private final String MINIMUM_ORDER_VALUE = "MinimumOrderValue";

	/**
	 * 
	 */
	public EnergizerCustomerCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		try
		{
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{

				super.technicalFeedErrors = new ArrayList<EnergizerCSVFeedError>();
				super.businessFeedErrors = new ArrayList<EnergizerCSVFeedError>();

				final Map<String, String> csvValuesMap = record.toMap();
				validate(record);
				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				EnergizerB2BUnitModel b2bUnit = null;
				try
				{
					b2bUnit = (EnergizerB2BUnitModel) companyB2BCommerceService.getUnitForUid(csvValuesMap.get(CUSTOMER_ID).trim());
				}
				catch (final UnknownIdentifierException ep)
				{
					LOG.error("---No existing b2bunit found - New Record Inserting----", ep);
				}
				if (b2bUnit == null)
				{
					if (!csvValuesMap.get(CUSTOMER_ID).trim().isEmpty())
					{
						final EnergizerB2BUnitModel energizeB2BUnit = modelService.create(EnergizerB2BUnitModel.class);
						energizeB2BUnit.setUid(csvValuesMap.get(CUSTOMER_ID).trim());
						energizeB2BUnit.setSalesOrganisation(csvValuesMap.get(SALES_ORG).trim());
						energizeB2BUnit.setDistributionChannel(csvValuesMap.get(DISTRIBUTION_CHANNEL).trim());
						energizeB2BUnit.setDivision(csvValuesMap.get(DIVISION).trim());
						energizeB2BUnit.setName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setCustomerAccountName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setErpOrderingType(csvValuesMap.get(ORDER_TYPE).trim());
						final boolean blockStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						energizeB2BUnit.setOrderBlock(blockStatus);
						final boolean active = csvValuesMap.get(DELETION_FLAG).trim().equalsIgnoreCase("1") ? false : true;
						energizeB2BUnit.setActive(active);
						try
						{
							final CurrencyModel currencyModel = commonI18NService.getCurrency(csvValuesMap.get(CURRENCY).trim());
							energizeB2BUnit.setCurrencyPreference(currencyModel);
							final LanguageModel languageModel = commonI18NService.getLanguage(csvValuesMap.get(DEFAULT_LANGUAGE).trim());
							energizeB2BUnit.setLanguagePreference(languageModel.getIsocode());

						}
						catch (final Exception exception)
						{
							LOG.error("Currency Model or Language Model Not Found!", exception);
						}
						energizeB2BUnit.setLocName(csvValuesMap.get(CUSTOMER_NAME).trim());
						energizeB2BUnit.setMaxUserLimit(Integer.parseInt(Config.getString(MAX_USER_LIMIT, MAX_USER_LIMIT_COUNT)));
						energizeB2BUnit.setBuyerSpecificID(csvValuesMap.get(CUSTOMER_ID).trim());
						energizeB2BUnit.setMinimumOrderValue(new BigDecimal(csvValuesMap.get(MINIMUM_ORDER_VALUE).trim()));
						try
						{
							modelService.save(energizeB2BUnit);
							succeedRecord++;
							setRecordSucceeded(succeedRecord);
						}
						catch (final Exception exception)
						{
							LOG.error("Exception while saving customer!", exception);
						}
						LOG.info("Energizer B2BUnit created");
					}

				}
				else
				{
					//update model and save
					if (!csvValuesMap.get(CUSTOMER_ID).trim().isEmpty())
					{
						b2bUnit.setUid(csvValuesMap.get(CUSTOMER_ID).trim());
						b2bUnit.setSalesOrganisation(csvValuesMap.get(SALES_ORG).trim());
						b2bUnit.setDistributionChannel(csvValuesMap.get(DISTRIBUTION_CHANNEL).trim());
						b2bUnit.setDivision(csvValuesMap.get(DIVISION).trim());
						b2bUnit.setName(csvValuesMap.get(CUSTOMER_NAME).trim());
						b2bUnit.setErpOrderingType(csvValuesMap.get(ORDER_TYPE).trim());
						final boolean active = csvValuesMap.get(DELETION_FLAG).trim().equalsIgnoreCase("1") ? false : true;
						b2bUnit.setActive(active);
						final boolean blockStatus = csvValuesMap.get(STATUS).trim().equalsIgnoreCase("1") ? true : false;
						b2bUnit.setOrderBlock(blockStatus);
						b2bUnit.setMinimumOrderValue(new BigDecimal(csvValuesMap.get(MINIMUM_ORDER_VALUE).trim()));
						try
						{
							final CurrencyModel currencyModel = commonI18NService.getCurrency(csvValuesMap.get(CURRENCY).trim());
							b2bUnit.setCurrencyPreference(currencyModel);
							final LanguageModel languageModel = commonI18NService.getLanguage(csvValuesMap.get(DEFAULT_LANGUAGE).trim());
							b2bUnit.setLanguagePreference(languageModel.getIsocode());
						}
						catch (final Exception exception)
						{
							LOG.info("Currency Model or Language Model Not Found!");
						}
						b2bUnit.setMaxUserLimit(Integer.parseInt(Config.getString(MAX_USER_LIMIT, MAX_USER_LIMIT_COUNT)));
						b2bUnit.setLocname(csvValuesMap.get(CUSTOMER_NAME).trim());
						try
						{
							modelService.save(b2bUnit);
							succeedRecord++;
							setRecordSucceeded(succeedRecord);
						}
						catch (final Exception exception)
						{
							LOG.error("Exception while saving customer!", exception);
						}
						LOG.info("Updating the EnergizerB2BUnit");
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("EnergizerCustomerCSVProcessor:Exception in saving EnergizerCustomer", e);
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		final Map<String, String> map = record.toMap();
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
