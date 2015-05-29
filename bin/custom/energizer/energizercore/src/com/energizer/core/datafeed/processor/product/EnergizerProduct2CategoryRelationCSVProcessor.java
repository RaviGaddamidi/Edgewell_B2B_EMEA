/**
 * 
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;



public class EnergizerProduct2CategoryRelationCSVProcessor extends AbstractEnergizerCSVProcessor
{

	@Resource
	private ModelService modelService;

	@Resource
	CatalogService catalogService;

	@Resource
	CatalogVersionService catalogVersionService;

	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private CategoryService categoryService;

	private static String CSV_HEADERS[] = null;

	private static final String PRODUCT_CATEGORY_FEED_HEADERS_KEY = "feedprocessor.productcategoryrelationfeed.headers";
	private static final String PRODUCT_CATEGORY_FEED_HEADERS_MANDATORY_KEY = "feedprocessor.productcategoryrelationfeed.headers.mandatory";

	private static final Logger LOG = Logger.getLogger(EnergizerProduct2CategoryRelationCSVProcessor.class);

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		final String catalogName = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_NAME);
		final String catalogVersion = Config.getParameter(FEED_PROCESSOR_PRODUCT_CATALOG_VERSION);
		final CatalogVersionModel catalogModelVersionModel = catalogVersionService.getCatalogVersion(catalogName, catalogVersion);
		long succeedRecord = getRecordSucceeded();
		CSV_HEADERS = Config.getParameter(PRODUCT_CATEGORY_FEED_HEADERS_KEY).split(new Character(DELIMETER).toString());
		for (final CSVRecord record : records)
		{
			final Map<String, String> csvValuesMap = record.toMap();
			validate(record);
			if (!getTechnicalFeedErrors().isEmpty())
			{
				csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
				getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
				getTechnicalFeedErrors().clear();
				continue;
			}
			final List<EnergizerProductModel> products = energizerProductService.getEnergizerProductListForSapCatgy(csvValuesMap
					.get(CSV_HEADERS[0]).trim());

			if (products != null && !products.isEmpty())
			{
				for (final EnergizerProductModel enrProductModel : products)
				{
					if (csvValuesMap.get(CSV_HEADERS[4]) != null)
					{
						enrProductModel.setSearchAttribute(csvValuesMap.get(CSV_HEADERS[4]).trim());
					}
					mapProduct2Category(enrProductModel, csvValuesMap);
					modelService.save(enrProductModel);
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
			}
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void mapProduct2Category(final EnergizerProductModel enrProductModel, final Map<String, String> csvValuesMap)
	{
		//get category for concatenated value
		final StringBuilder concatCatgy = new StringBuilder(csvValuesMap.get(CSV_HEADERS[1]).trim());

		if (csvValuesMap.get(CSV_HEADERS[2]) != null && !csvValuesMap.get(CSV_HEADERS[2]).trim().isEmpty())
		{
			concatCatgy.append("-");
			concatCatgy.append(csvValuesMap.get(CSV_HEADERS[2]).trim());
		}
		if (csvValuesMap.get(CSV_HEADERS[3]) != null && !csvValuesMap.get(CSV_HEADERS[3]).trim().isEmpty())
		{
			concatCatgy.append("-");
			concatCatgy.append(csvValuesMap.get(CSV_HEADERS[3]).trim());
		}
		LOG.info(" PRODUCT CODE=" + enrProductModel.getCode() + ", CATGORY CODE=" + concatCatgy);
		//prdtCategoryModel.setProducts(enrProductModels);		
		enrProductModel.setSupercategories(categoryService.getCategoriesForCode(concatCatgy.toString()));
	}

	/**
	 * @param record
	 * @param csvValuesMap
	 * @return
	 */
	private EnergizerCSVFeedError validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;

		if (!hasMandatoryFields(record, getHeadersForFeed(PRODUCT_CATEGORY_FEED_HEADERS_KEY)))
		{
			final List<String> mandatoryFields = Arrays.asList(Config.getParameter(PRODUCT_CATEGORY_FEED_HEADERS_MANDATORY_KEY)
					.split(new Character(DELIMETER).toString()));
			final Map<String, String> map = record.toMap();
			Integer columnNumber = 0;
			final List<String> columnNames = new ArrayList<String>();
			final List<Integer> columnNumbers = new ArrayList<Integer>();
			long recordFailed = getRecordFailed();
			for (final String columnHeader : map.keySet())
			{
				setTotalRecords(record.getRecordNumber());
				if (mandatoryFields.contains(columnHeader))
				{
					columnNumber++;
					final String value = map.get(columnHeader);

					if (value.isEmpty())
					{
						error = new EnergizerCSVFeedError();
						error.setLineNumber(record.getRecordNumber());
						columnNames.add(columnHeader);
						error.setColumnName(columnNames);
						error.setMessage(columnHeader + " column should not be empty");
						columnNumbers.add(columnNumber);
						error.setUserType(TECHNICAL_USER);
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
