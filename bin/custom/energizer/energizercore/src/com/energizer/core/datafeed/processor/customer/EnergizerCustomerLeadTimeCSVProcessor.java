/**
 * 
 */
package com.energizer.core.datafeed.processor.customer;

import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerCustomerLeadTimeFacade;
import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;
import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * 
 * This processors imports the customer lead time.
 * 
 * Sample file will look like
 * 
 * EnergizerAccountID, ShippingPointNo, ShipTo, LeadTimeInDays 43211, 100, dist112, 1
 * 
 * Total column count : 4
 */
public class EnergizerCustomerLeadTimeCSVProcessor extends AbstractEnergizerCSVProcessor
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#parse(java.io.File)
	 */
	@Resource
	private ModelService modelService;

	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	protected FlexibleSearchService flexibleSearchService;

	@Resource
	DefaultEnergizerCustomerLeadTimeFacade defaultEnergizerCustomerLeadTimeFacade;
	private static final Logger LOG = Logger.getLogger(EnergizerCustomerLeadTimeCSVProcessor.class);

	private static String ENERGIZER_ACCOUNT_ID = "EnergizerAccountID";
	private static String SHIPPING_POINT_NO = "ShippingPointNo";
	private static String SHIP_TO = "ShipTo";
	private static String LEAD_TIME_IN_DAYS = "LeadTimeInDays";
	String energizerAccountID = "";
	String shippingPointNo = "";
	String shipTo = "";
	Integer leadTimeInDays = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.datafeed.EnergizerCSVProcessor#process(java.lang.Iterable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		try
		{
			LOG.info("EnergizerCustomerLeadTimeCSVProcessor:process:Start");
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				validate(record);
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					continue;
				}
				final Map<String, String> csvValuesMap = record.toMap();
				energizerAccountID = csvValuesMap.get(ENERGIZER_ACCOUNT_ID).trim();
				shippingPointNo = csvValuesMap.get(SHIPPING_POINT_NO).trim();
				shipTo = csvValuesMap.get(SHIP_TO).trim();
				leadTimeInDays = Integer.parseInt(csvValuesMap.get(LEAD_TIME_IN_DAYS).trim());
				energizerAccountID = csvValuesMap.get(ENERGIZER_ACCOUNT_ID).trim();
				final EnergizerB2BUnitModel energizerB2BUnitModel = isEnergizerAccountExist(energizerAccountID);
				if (energizerB2BUnitModel != null)
				{
					EnergizerB2BUnitLeadTimeModel energizerB2BUnitLeadTimeModel = getEnergizerB2BUnitLeadTime(energizerB2BUnitModel,
							shippingPointNo, shipTo);
					if (energizerB2BUnitLeadTimeModel == null)
					{
						energizerB2BUnitLeadTimeModel = modelService.create(EnergizerB2BUnitLeadTimeModel.class);
						energizerB2BUnitLeadTimeModel.setB2bUnitId(energizerB2BUnitModel);
						energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
						energizerB2BUnitLeadTimeModel.setSoldToAddressId(shipTo);
						energizerB2BUnitLeadTimeModel.setLeadTime(leadTimeInDays);
						modelService.save(energizerB2BUnitLeadTimeModel);
						LOG.info("Customer lead time is saved for " + energizerB2BUnitModel + "with Lead time " + leadTimeInDays);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
					}
					else
					{
						energizerB2BUnitLeadTimeModel.setB2bUnitId(energizerB2BUnitModel);
						energizerB2BUnitLeadTimeModel.setShippingPointId(shippingPointNo);
						energizerB2BUnitLeadTimeModel.setSoldToAddressId(shipTo);
						energizerB2BUnitLeadTimeModel.setLeadTime(leadTimeInDays);
						modelService.save(energizerB2BUnitLeadTimeModel);
						LOG.info("Customer lead time is updated for the unit " + energizerB2BUnitModel + "with Lead time "
								+ leadTimeInDays);
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
					}
				}
			}//end of for loop
		}//end of try block
		catch (final Exception e)
		{
			LOG.error("EnergizerCustomerLeadTimeCSVProcessor ", e);
		}
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param energizerB2BUnitModel
	 * @param shippingPointNo
	 * @param shipTo
	 * @return EnergizerB2BUnitLeadTimeModel
	 */
	private EnergizerB2BUnitLeadTimeModel getEnergizerB2BUnitLeadTime(final EnergizerB2BUnitModel energizerB2BUnitModel,
			final String shippingPointNo, final String shipTo)
	{
		EnergizerB2BUnitLeadTimeModel energizerB2BUnitLeadTimeModel = null;
		final String pkEnergizerB2BUnit;
		pkEnergizerB2BUnit = energizerB2BUnitModel.getPk().getLongValueAsString();
		final List<EnergizerB2BUnitLeadTimeModel> energizerB2BUnitLeadTimeModels = defaultEnergizerCustomerLeadTimeFacade
				.fetchEnergizerB2BUnitLeadTime(pkEnergizerB2BUnit, shippingPointNo, shipTo);

		if (!(energizerB2BUnitLeadTimeModels.isEmpty()))
		{
			energizerB2BUnitLeadTimeModel = energizerB2BUnitLeadTimeModels.get(0);
		}
		return energizerB2BUnitLeadTimeModel;
	}

	/**
	 * @param record
	 * @return boolean
	 */
	@SuppressWarnings("unused")
	private boolean isValidRecord(final CSVRecord record)
	{
		boolean isValid = true;
		final StringBuffer errBuffer = new StringBuffer();
		errBuffer.append("Record Number " + record.getRecordNumber() + " has invalid field");
		final Map<String, String> csvValuesMap = record.toMap();
		energizerAccountID = csvValuesMap.get(ENERGIZER_ACCOUNT_ID).trim();
		shippingPointNo = csvValuesMap.get(SHIPPING_POINT_NO).trim();
		shipTo = csvValuesMap.get(SHIP_TO).trim();
		if (StringUtils.isEmpty(energizerAccountID) || null == energizerAccountID)
		{
			isValid = false;
			errBuffer.append(" || energizerAccountID = " + energizerAccountID);
		}
		if (StringUtils.isEmpty(shippingPointNo) || null == shippingPointNo)
		{
			isValid = false;
			errBuffer.append(" || shippingPointNo = " + shippingPointNo);
		}
		if (StringUtils.isEmpty(shipTo) || null == shipTo)
		{
			isValid = false;
			errBuffer.append(" || shipTo = " + shipTo);
		}
		try
		{
			leadTimeInDays = Integer.parseInt(csvValuesMap.get(LEAD_TIME_IN_DAYS).trim());
		}
		catch (final NumberFormatException e)
		{
			isValid = false;
			errBuffer.append(" || leadTimeInDays = " + leadTimeInDays + " " + e.getMessage());
		}
		if (!isValid)
		{
			LOG.info(errBuffer);//Generate Log if record is not valid
		}
		return isValid;
	}



	//Check Energizer Account is exist or not if exist return correspond EnergizerB2BUnitModel object otherwise return null
	public EnergizerB2BUnitModel isEnergizerAccountExist(final String accountId)
	{
		final EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) companyB2BCommerceService
				.getUnitForUid(accountId);
		return energizerB2BUnitModel;
	}

	/**
	 * @param record
	 * 
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
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				long recordFailed = getRecordFailed();
				error = new EnergizerCSVFeedError();
				error.setLineNumber(record.getRecordNumber());
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setMessage(columnHeader + " column should not be empty");
				columnNumbers.add(columnNumber);
				error.setUserType(BUSINESS_USER);
				error.setColumnNumber(columnNumbers);
				getBusinessFeedErrors().add(error);
				setBusRecordError(getBusinessFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}
			if (columnHeader.equalsIgnoreCase(LEAD_TIME_IN_DAYS))
			{
				if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0)
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();

					long recordFailed = getRecordFailed();
					error = new EnergizerCSVFeedError();
					error.setUserType(BUSINESS_USER);
					error.setErrorCode("ECLT2001");
					error.setLineNumber(record.getRecordNumber());
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be numeric value & greater than 0");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
		}
	}
}
