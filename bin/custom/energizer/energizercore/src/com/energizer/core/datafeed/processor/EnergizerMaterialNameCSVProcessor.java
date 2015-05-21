/**
 * 
 */
package com.energizer.core.datafeed.processor;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerProductModel;


/**
 * 
 * This processors imports the product meterail name.
 * 
 * Sample file will look like
 * 
 * ERPMaterialID,Language,ProductDesription 10, EN, omkar singh spf2 6 oz 4/3s
 * 
 * Total column count : 3
 */
public class EnergizerMaterialNameCSVProcessor extends AbstractEnergizerCSVProcessor
{


	@Resource
	private ModelService modelService;

	@Resource
	private SessionService sessionService;


	@Resource
	private ProductService productService;

	@Resource
	CatalogService catalogService;

	@Resource
	CatalogVersionService catalogVersionService;

	private static String CSV_HEADERS[] = null;

	private static final String MATERIAL_NAME_FEED_HEADERS_KEY = "feedprocessor.materialnamefeed.headers";
	private static final String MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.materialnamefeed.headers.mandatory";


	private static final Logger LOG = Logger.getLogger(EnergizerMaterialNameCSVProcessor.class);

	/**
	 * 
	 */
	public EnergizerMaterialNameCSVProcessor()
	{
		super();
	}

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		final String catalogName = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_NAME);
		final String catalogVersion = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_VERSION);
		final CatalogVersionModel catalogModelVersionModel = catalogVersionService.getCatalogVersion(catalogName, catalogVersion);
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(MATERIAL_NAME_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
		for (final CSVRecord record : records)
		{

			super.technicalFeedErrors = new ArrayList<EnergizerCSVFeedError>();
			super.businessFeedErrors = new ArrayList<EnergizerCSVFeedError>();

			final Map<String, String> csvValuesMap = record.toMap();
			validate(record);
			if (!getTechnicalFeedErrors().isEmpty())
			{
				csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
				continue;
			}
			EnergizerProductModel pm = null;
			try
			{
				pm = (EnergizerProductModel) productService.getProductForCode(catalogModelVersionModel,
						(csvValuesMap).get(CSV_HEADERS[0]));
			}
			catch (final UnknownIdentifierException ep)
			{
				LOG.error("No such Product", ep);
				final EnergizerCSVFeedError error = new EnergizerCSVFeedError();
				long recordFailed = getRecordFailed();
				error.setLineNumber(record.getRecordNumber());
				error.setMessage("No such Product : " + csvValuesMap.get(CSV_HEADERS[0]));
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}
			if (pm != null)
			{
				try
				{
					pm.setName(csvValuesMap.get(CSV_HEADERS[2]), new Locale(csvValuesMap.get(CSV_HEADERS[1]).toLowerCase()));
					modelService.save(pm);
					LOG.info("EnergizerProductModel is saved");
				}
				catch (final Exception e)
				{
					LOG.error("Error ", e);
				}

			}

		}
		succeedRecord++;
		setRecordSucceeded(succeedRecord);
		return getCsvFeedErrorRecords();
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		setRecordFailed(getRecordFailed());
		if (!hasMandatoryFields(record, getHeadersForFeed(MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(Config.getParameter(MATERIAL_NAME_FEED_HEADERS_MANDATORY_KEY).split(
					new Character(DELIMETER).toString()));
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			setRecordFailed(getRecordFailed());
			for (final String columnHeader : map.keySet())
			{
				setTotalRecords(record.getRecordNumber());
				if (mandatoryFields.contains(columnHeader))
				{
					columnNumber++;
					final String value = map.get(columnHeader);
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
		return error;
	}


}
