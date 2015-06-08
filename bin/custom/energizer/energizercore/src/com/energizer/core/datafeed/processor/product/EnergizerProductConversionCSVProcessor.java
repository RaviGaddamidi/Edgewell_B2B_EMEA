/**
 * 
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;

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
 * 
 * This processors imports the product conversion.
 * 
 * Sample file will look like
 * 
 * ERPMaterialID,AlternateUOM,BaseUOMMultiplier,VolumeInUOM,VolumeUOM,WeightInUOM,WeightUOM 3, EA, 1, 0.311, CDM,
 * 126.552 ,G
 * 
 * Total column count : 7
 */
public class EnergizerProductConversionCSVProcessor extends AbstractEnergizerCSVProcessor
{

	@Resource
	private ModelService modelService;
	@Resource
	private SessionService sessionService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private ProductService productService;
	@Resource
	private CommonI18NService defaultCommonI18NService;
	@Resource
	private UnitService defaultUnitService;

	private static final Logger LOG = Logger.getLogger(EnergizerProductConversionCSVProcessor.class);

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
					techFeedErrorRecords.addAll(getTechnicalFeedErrors());
					getTechnicalFeedErrors().clear();
					//continue;
				}
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					businessFeedErrorRecords.addAll(getBusinessFeedErrors());
					getBusinessFeedErrors().clear();
					continue;
				}

				/*
				 * Retrieve the energizerProduct based on an erpMaterialId. After retrieval check for the existence of
				 * EnergizerProductConversionFactors field.
				 */
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
				//if (energizerProduct != null)
				//{
				LOG.info("THE DETAILS OF THE PRODUCT IS :" + energizerProduct.getCode() + " " + energizerProduct.getApprovalStatus());
				//Check for the EnergizerConversionFactor
				energizerProductFactorList = energizerProduct.getProductConversionFactors();
				final ArrayList<EnergizerProductConversionFactorModel> tmpFactorList = new ArrayList<EnergizerProductConversionFactorModel>();

				//If there is no associated productConversionFactor then create and attach with the product
				EnergizerProductConversionFactorModel energizerProductConversionModel = null;

				try
				{
					boolean matchingRecordFound = false;
					if (energizerProductFactorList != null)
					{
						tmpFactorList.addAll(energizerProductFactorList);
						LOG.info("The size of productConversionFactorModels is :" + tmpFactorList.size());
						//Retrieve the productConversionFactor and perform the matching process and do an update in case of any mismatch

						for (final EnergizerProductConversionFactorModel energizerProductConversionFactorModel : energizerProductFactorList)
						{
							LOG.info("Factor Model UOM is:" + energizerProductConversionFactorModel.getAlternateUOM());

							if (isModelSame(energizerProductConversionFactorModel, csvValuesMap))
							{
								matchingRecordFound = true;
								energizerProductConversionModel = energizerProductConversionFactorModel;
								break;
							}
						}
					}
					if (!matchingRecordFound)
					{
						energizerProductConversionModel = modelService.create(EnergizerProductConversionFactorModel.class);
						energizerProductConversionModel
								.setErpMaterialId(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim());
						energizerProductConversionModel.setAlternateUOM(csvValuesMap.get(EnergizerCoreConstants.ALTERNATE_UOM).trim()); //PALLET, LAYER, CASE				
						tmpFactorList.add(energizerProductConversionModel);
					}
					this.addUpdateRecord(energizerProductConversionModel, csvValuesMap);
					energizerProduct.setProductConversionFactors(tmpFactorList);
					modelService.saveAll();
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
				catch (final Exception e)
				{
					LOG.error("Error ---- " + e);
					//this.logErrors(errors);
					continue;
				}

				//}
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

	private void addUpdateRecord(final EnergizerProductConversionFactorModel conversionModel,
			final Map<String, String> csvValuesMap) throws Exception
	{
		LOG.info("Entering method addUpdateRecord.....");

		conversionModel.setConversionMultiplier(Integer.parseInt(csvValuesMap.get(EnergizerCoreConstants.BASE_UOM_MULTIPLIER)
				.trim())); //conversion multiplier

		MetricUnitModel packageVolumeMetric = conversionModel.getPackageVolume();
		if (packageVolumeMetric == null)
		{
			packageVolumeMetric = modelService.create(MetricUnitModel.class);
		}
		packageVolumeMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.VOLUME_IN_UOM).trim()));
		packageVolumeMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.VOLUME_UOM).trim());

		conversionModel.setPackageVolume(packageVolumeMetric);

		MetricUnitModel packageWeightMetric = conversionModel.getPackageWeight();
		if (packageWeightMetric == null)
		{
			packageWeightMetric = modelService.create(MetricUnitModel.class);
		}
		packageWeightMetric.setMeasurement(Double.parseDouble(csvValuesMap.get(EnergizerCoreConstants.WEIGHT_IN_UOM).trim()));
		packageWeightMetric.setMeasuringUnits(csvValuesMap.get(EnergizerCoreConstants.WEIGHT_UOM).trim());

		conversionModel.setPackageWeight(packageWeightMetric);

		LOG.info("Ending method addUpdateRecord.....");
	}

	private boolean isModelSame(final EnergizerProductConversionFactorModel conversionModel, final Map<String, String> csvValuesMap)
	{
		if (conversionModel != null
				&& conversionModel.getErpMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim())
				&& conversionModel.getAlternateUOM().equalsIgnoreCase(csvValuesMap.get(EnergizerCoreConstants.ALTERNATE_UOM).trim()))
		{
			// record exists, just update other attributes
			LOG.info("Product Conversion record exists for ERP_ID : " + conversionModel.getErpMaterialId() + " AlternateUOM : "
					+ conversionModel.getAlternateUOM());
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param record
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		final Map<String, String> map = record.toMap();

		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());

		String uomValue = "";
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
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ALTERNATE_UOM))
			{
				uomValue = value;
			}
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.BASE_UOM_MULTIPLIER))
			{
				if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(TECHNICAL_USER);
					columnNames.add(columnHeader);
					error.setLineNumber(record.getRecordNumber());
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be numeric");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(error);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}

			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.VOLUME_IN_UOM)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.WEIGHT_IN_UOM))
			{
				//if (uomValue.equalsIgnoreCase(EnergizerCoreConstants.EA) || uomValue.equalsIgnoreCase(EnergizerCoreConstants.CASE))
				//{
				if (!NumberUtils.isNumber(value) || new BigDecimal(value).compareTo(BigDecimal.ZERO) == 0)
				{
					final List<String> columnNamesB = new ArrayList<String>();
					final List<Integer> columnNumbersB = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(BUSINESS_USER);
					error.setLineNumber(record.getRecordNumber());
					columnNamesB.add(columnHeader);
					error.setColumnName(columnNamesB);
					error.setMessage(columnHeader + " column should be numeric and greater than 0");
					columnNumbersB.add(columnNumber);
					error.setColumnNumber(columnNumbersB);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
				//}
			}
		}
	}
}
