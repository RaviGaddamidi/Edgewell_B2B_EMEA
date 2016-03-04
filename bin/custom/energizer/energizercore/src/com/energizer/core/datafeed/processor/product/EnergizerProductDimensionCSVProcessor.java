/**
 *
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.MetricUnitModel;


/**
 * @author M1030110
 *
 */
public class EnergizerProductDimensionCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;
	@Resource
	private ProductService productService;

	private static final Logger LOG = Logger.getLogger(EnergizerProductDimensionCSVProcessor.class);

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		List<EnergizerProductConversionFactorModel> energizerProductFactorList = null;
		EnergizerProductModel energizerProduct = null;
		try
		{
			final CatalogVersionModel catalogVersion = getCatalogVersion();
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				LOG.info(" CSV Record number: " + record.getRecordNumber());
				LOG.info(" CSV Record: " + record.toMap());

				final Map<String, String> csvValuesMap = record.toMap();
				validate(record);

				if (!getTechnicalFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					continue;
				}
				try
				{
					energizerProduct = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
							csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim());

				}
				catch (final Exception e)
				{
					LOG.error(e.getMessage());
					continue;
				}
				if (energizerProduct == null)
				{
					LOG.error((csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID) + " EnergizerProduct does  not exist ");
					continue;
				}

				LOG.info("THE DETAILS OF THE PRODUCT IS :" + energizerProduct.getCode() + " ");

				energizerProductFactorList = energizerProduct.getProductConversionFactors();

				for (final EnergizerProductConversionFactorModel energizerProductConversionFactorModel : energizerProductFactorList)
				{
					if ((energizerProductConversionFactorModel.getAlternateUOM()).equalsIgnoreCase(csvValuesMap.get(
							EnergizerCoreConstants.ALTERNATIVEUNIT).trim()))
					{
						MetricUnitModel packageLengthMetric = energizerProductConversionFactorModel.getPackageLength();
						if (packageLengthMetric == null)
						{
							packageLengthMetric = modelService.create(MetricUnitModel.class);
						}
						packageLengthMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.LENGTH).trim()));
						packageLengthMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());

						MetricUnitModel packageWidthMetric = energizerProductConversionFactorModel.getPackageWidth();
						if (packageWidthMetric == null)
						{
							packageWidthMetric = modelService.create(MetricUnitModel.class);
						}
						packageWidthMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.WIDTH).trim()));
						packageWidthMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());

						MetricUnitModel packageHeightMetric = energizerProductConversionFactorModel.getPackageHeight();
						if (packageHeightMetric == null)
						{
							packageHeightMetric = modelService.create(MetricUnitModel.class);
						}
						packageHeightMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.HEIGHT).trim()));
						packageHeightMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.UNIT).trim());

						energizerProductConversionFactorModel.setPackageLength(packageLengthMetric);
						energizerProductConversionFactorModel.setPackageWidth(packageWidthMetric);
						energizerProductConversionFactorModel.setPackageHeight(packageHeightMetric);
						modelService.saveAll();
						succeedRecord++;
						setRecordSucceeded(succeedRecord);
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error ---- " + e);
		}
		getTechnicalFeedErrors().addAll(techFeedErrorRecords);
		getBusinessFeedErrors().addAll(businessFeedErrorRecords);
		techFeedErrorRecords.clear();
		businessFeedErrorRecords.clear();
		return getCsvFeedErrorRecords();



	}

	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		final Map<String, String> map = record.toMap();

		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());

		for (final String columnHeader : map.keySet())
		{
			columnNumber++;
			long recordFailed = getRecordFailed();
			setTotalRecords(record.getRecordNumber());
			final String value = map.get(columnHeader).trim();

			if (value.isEmpty())
			{
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error = new EnergizerCSVFeedError();
				error.setLineNumber(record.getRecordNumber());
				columnNames.add(columnHeader);
				error.setUserType(TECHNICAL_USER);
				error.setColumnName(columnNames);
				columnNumbers.add(columnNumber);
				error.setMessage(columnHeader + " column should not be empty");
				error.setColumnNumber(columnNumbers);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}



			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LENGTH)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.WIDTH)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.HEIGHT))
			{
				//if (uomValue.equalsIgnoreCase(EnergizerCoreConstants.EA) || uomValue.equalsIgnoreCase(EnergizerCoreConstants.CASE))
				//{
				if (!NumberUtils.isNumber(value) || new BigDecimal(value).compareTo(BigDecimal.ZERO) == 0)
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(TECHNICAL_USER);
					columnNames.add(columnHeader);
					error.setLineNumber(record.getRecordNumber());
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be numeric and greater than 0");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(error);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
				//}
			}
		}
	}
}
