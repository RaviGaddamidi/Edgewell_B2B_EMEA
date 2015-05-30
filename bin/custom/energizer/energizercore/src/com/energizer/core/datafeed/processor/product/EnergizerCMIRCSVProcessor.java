/**
 * 
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * 
 * This processors imports the CMIR
 * 
 * Sample file will look like
 * 
 * EnergizerAccountID,ERPMaterialID,CustomerMaterialID,Language,CustomerMaterial Description,MaterialList
 * price,CustomerListPrice,CustomerListprice currency,ShipmentPointNumber 1000, 10, 10, EN, tanning creme spf2 6 oz
 * 4/3s,21, 12, USD, 712
 * 
 * Total column count : 9
 */
public class EnergizerCMIRCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ProductService productService;
	@Resource
	private ModelService modelService;
	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;
	@Resource
	private FlexibleSearchService flexibleSearchService;
	@Resource
	private UnitService unitService;
	@Resource
	private CommonI18NService defaultCommonI18NService;
	@Resource
	private UnitService defaultUnitService;
	@Resource
	ConfigurationService configurationService;


	boolean hasCustomerListPriceBusinessError = false;
	boolean hasCustomerListPriceTechnicalError = false;
	boolean hasCustomerBusinessError = false;
	boolean hasCustomerTechnicalError = false;

	private static final Logger LOG = Logger.getLogger(EnergizerCMIRCSVProcessor.class.getName());

	private static final String UNIT = "EA";

	private String defaultMOQ = "";

	private String defaultUOM = "";

	private final String ZERO = "0";

	@SuppressWarnings("unchecked")
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		Collection<PriceRowModel> priceRows;
		EnergizerProductModel energizerProduct = null;
		defaultMOQ = configurationService.getConfiguration().getString("feedprocessor.defalult.moq.value", null);
		defaultUOM = configurationService.getConfiguration().getString("feedprocessor.defalult.uom.value", null);
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
				/*
				 * if (!getBusinessFeedErrors().isEmpty() && hasCustomerBusinessError) {
				 * csvFeedErrorRecords.addAll(getBusinessFeedErrors()); continue; }
				 */
				LOG.info(" ENERGIZER_ACCOUNT_ID : " + csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID) + " "
						+ csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID));

				final String b2bUnitId = csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID);
				final String erpMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID);
				final String customerMaterialId = csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID);
				String currency = csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_LIST_PRICE_CURRENCY);
				String customerlistprice = csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_LIST_PRICE);

				try
				{
					energizerProduct = (EnergizerProductModel) productService.getProductForCode(catalogVersion, erpMaterialId);
				}
				catch (final Exception e)
				{
					LOG.error(e.getMessage());
				}
				if (energizerProduct == null)
				{
					LOG.warn(erpMaterialId + " EnergizerProduct does  not exist ");
					//TO DO log into EnergizerCSVFeedError...so that it can be mailed					
				}
				else
				{
					//chec if b2bunit exists
					final EnergizerB2BUnitModel energizerB2BUnitModel = getEnergizerB2BUnit(b2bUnitId);

					final List<EnergizerCMIRModel> energizerCmirModels = energizerProduct.getProductCMIR();
					final ArrayList<EnergizerCMIRModel> tmpCMIRModelList = new ArrayList<EnergizerCMIRModel>();

					//If there is no associated EnergizerCMIRModel then create and attach with the product
					EnergizerCMIRModel energizerCMIRModel = null;

					boolean matchingRecordFound = false;
					if (energizerCmirModels != null)
					{
						tmpCMIRModelList.addAll(energizerCmirModels);
						LOG.info("The size of productCMIRModels is :" + tmpCMIRModelList.size());
						//Retrieve the CMIRModel and perform the matching process and do an update in case of any mismatch

						for (final EnergizerCMIRModel energizerProductCMIRModel : energizerCmirModels)
						{
							LOG.info("CMIR Model Material ID is:" + energizerProductCMIRModel.getErpMaterialId());

							if (isCMIRModelSame(energizerProductCMIRModel, csvValuesMap))
							{
								matchingRecordFound = true;
								energizerCMIRModel = energizerProductCMIRModel;
								break;
							}
						}
					}
					if (!matchingRecordFound)
					{
						energizerCMIRModel = modelService.create(EnergizerCMIRModel.class);
						energizerCMIRModel.setErpMaterialId(erpMaterialId);
						energizerCMIRModel.setCustomerMaterialId(customerMaterialId);
						energizerCMIRModel.setB2bUnit(energizerB2BUnitModel);

						tmpCMIRModelList.add(energizerCMIRModel);
					}
					this.addUpdateCMIRRecord(energizerCMIRModel, csvValuesMap);
					energizerProduct.setProductCMIR(tmpCMIRModelList);
					modelService.saveAll();

					priceRows = energizerProduct.getEurope1Prices();
					final List<PriceRowModel> energizerPriceRowModels = new ArrayList<PriceRowModel>(
							energizerProduct.getEurope1Prices());

					final ArrayList<PriceRowModel> tmpPriceRowModelList = new ArrayList<PriceRowModel>();

					//If there is no associated EnergizerPriceRowModel then create and attach with the product
					EnergizerPriceRowModel priceRowModel = null;

					boolean matchingPriceRowFound = false;
					if (energizerPriceRowModels != null)
					{
						tmpPriceRowModelList.addAll(energizerPriceRowModels);
						LOG.info("The size of product price row models is :" + tmpPriceRowModelList.size());
						//Retrieve the PriceRowModel and perform the matching process and do an update in case of any mismatch
						for (final PriceRowModel enrPriceRowModel : energizerPriceRowModels)
						{
							LOG.info("Product price product :" + enrPriceRowModel.getPrice());
							if (!(enrPriceRowModel instanceof EnergizerPriceRowModel))
							{
								LOG.info("Not an energizer price row");
								continue;
							}
							final EnergizerPriceRowModel enrPriceRow = (EnergizerPriceRowModel) enrPriceRowModel;

							if (isENRPriceRowModelSame(enrPriceRow, csvValuesMap, energizerProduct))
							{
								matchingPriceRowFound = true;
								priceRowModel = enrPriceRow;
								break;
							}
						}
					}

					if (!matchingPriceRowFound)
					{
						priceRowModel = modelService.create(EnergizerPriceRowModel.class);
						priceRowModel.setB2bUnit(energizerB2BUnitModel);
						if (priceRowModel.getUnit() == null)
						{
							priceRowModel.setUnit(defaultUnitService.getUnitForCode(UNIT));
						}

						if (priceRowModel.getProduct() == null)
						{
							//priceRowModel.setProduct(energizerProduct);
							LOG.info("Empty product model for ENR price row");
						}
						tmpPriceRowModelList.add(priceRowModel);
					}

					if (customerlistprice == null || customerlistprice.isEmpty())
					{
						customerlistprice = ZERO;
					}
					if (currency == null || currency.isEmpty())
					{
						currency = energizerB2BUnitModel.getCurrencyPreference().getIsocode();
					}

					if (priceRowModel != null && priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode() != null
							&& !priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode().equals(currency))
					{
						LOG.error("Energizer price row currency preference "
								+ priceRowModel.getB2bUnit().getCurrencyPreference().getIsocode() + " does not match with : " + currency);
					}
					else
					{
						priceRowModel.setCatalogVersion(catalogVersion);
						priceRowModel.setCurrency(energizerB2BUnitModel.getCurrencyPreference());
						priceRowModel.setPrice(Double.parseDouble(customerlistprice));
						priceRowModel.setUnit(defaultUnitService.getUnitForCode(UNIT));

						/*
						 * if (priceRowModel.getProduct() == null) { //priceRowModel.setProduct(energizerProduct); }
						 */
						energizerProduct.setEurope1Prices(tmpPriceRowModelList);
						modelService.saveAll();
					}//else
					succeedRecord++;
					setRecordSucceeded(succeedRecord);

				}
			} //for
		}//try
		catch (final Exception e)
		{
			LOG.error("Error in adding or updating  Energizer Product Model  ||  " + e.getMessage());
		}
		getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
		getBusinessFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}//process

	private boolean isCMIRModelSame(final EnergizerCMIRModel cmirModel, final Map<String, String> csvValuesMap)
	{
		if (cmirModel != null && cmirModel.getErpMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID))
				&& cmirModel.getCustomerMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID))
				&& cmirModel.getB2bUnit().getUid().equals(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID)))
		{
			// record exists, just update other attributes
			LOG.info("Product CMIR record exists for ERP_ID : " + cmirModel.getErpMaterialId() + " Customer Material ID : "
					+ cmirModel.getCustomerMaterialId() + " B2B Unit ID : " + cmirModel.getB2bUnit().getUid());
			return true;
		}
		return false;
	}

	private void addUpdateCMIRRecord(final EnergizerCMIRModel energizerCMIRModel, final Map<String, String> csvValuesMap)
			throws Exception
	{
		energizerCMIRModel.setCustomerMaterialDescription(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_DESCRIPTION),
				new Locale(csvValuesMap.get(EnergizerCoreConstants.LANGUAGE).toLowerCase()));
		energizerCMIRModel.setShippingPoint(csvValuesMap.get(EnergizerCoreConstants.SHIPMENT_POINT_NO));
		// Setting Default UOM  and  MOQ

		final String currentCMIRUom = energizerCMIRModel.getUom();
		final Integer currentCMIRMoq = energizerCMIRModel.getOrderingUnit();

		if (null == currentCMIRUom || currentCMIRUom.isEmpty() || null == currentCMIRMoq)
		{
			energizerCMIRModel.setUom(defaultUOM);
			energizerCMIRModel.setOrderingUnit(Integer.parseInt(defaultMOQ));
		}
	}

	private boolean isENRPriceRowModelSame(final EnergizerPriceRowModel enrPriceRow, final Map<String, String> csvValuesMap,
			final EnergizerProductModel energizerProduct)
	{
		if (enrPriceRow != null
				&& enrPriceRow.getB2bUnit().getUid().equals(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID))
				&& (enrPriceRow.getProduct() == null || enrPriceRow.getProduct().getCode()
						.equals(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID))))
		{
			{
				LOG.info(" SAME PRICE ROW RECORD");
			}
			return true;
		}
		return false;
	}

	/**
	 * @param record
	 */
	private void validate(final CSVRecord record)
	{
		Integer columnNumber = 0;
		EnergizerCSVFeedError error = null;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			//			CMIRPartnerID, , , MaterialList price,
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ERPMATERIAL_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.SHIPMENT_POINT_NO)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LANGUAGE)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_MATERIAL_DESCRIPTION))
			{
				if (value.isEmpty())
				{
					long recordFailed = getRecordFailed();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(TECHNICAL_USER);
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setErrorCode("CMIR1001");
					error.setMessage(columnHeader + " column should not be empty");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(error);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
			if (!value.isEmpty() && columnHeader.equalsIgnoreCase(EnergizerCoreConstants.CUSTOMER_LIST_PRICE))
			{
				if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)
				{
					long recordFailed = getRecordFailed();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(TECHNICAL_USER);
					error.setErrorCode("CMIR2001");
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be numeric and greater than 0");
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

	/**
	 * 
	 * @param b2bUnitId
	 * @return
	 */

	public EnergizerB2BUnitModel getEnergizerB2BUnit(final String b2bUnitId)
	{
		final EnergizerB2BUnitModel energizerB2BUnitModel = (EnergizerB2BUnitModel) companyB2BCommerceService
				.getUnitForUid(b2bUnitId);
		return energizerB2BUnitModel;
	}

	/**
	 * @return the hasCustomerListPriceBusinessError
	 */
	public boolean isHasCustomerListPriceBusinessError()
	{
		return hasCustomerListPriceBusinessError;
	}

	/**
	 * * @param hasCustomerListPriceBusinessError the hasCustomerListPriceBusinessError to set
	 */
	public void setHasCustomerListPriceBusinessError(final boolean hasCustomerListPriceBusinessError)
	{
		this.hasCustomerListPriceBusinessError = hasCustomerListPriceBusinessError;
	}

	/**
	 * @return the hasCustomerListPriceTechnicalError
	 */
	public boolean isHasCustomerListPriceTechnicalError()
	{
		return hasCustomerListPriceTechnicalError;
	}

	/**
	 * @param hasCustomerListPriceTechnicalError
	 *           the hasCustomerListPriceTechnicalError to set
	 */
	public void setHasCustomerListPriceTechnicalError(final boolean hasCustomerListPriceTechnicalError)
	{
		this.hasCustomerListPriceTechnicalError = hasCustomerListPriceTechnicalError;
	}



}