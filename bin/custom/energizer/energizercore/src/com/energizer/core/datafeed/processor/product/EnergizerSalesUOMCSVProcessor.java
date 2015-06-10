/**
 * 
 */
package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import com.energizer.core.model.EnergizerCategoryModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerB2BUnitService;
import com.energizer.services.product.EnergizerSalesUOMService;


/**
 * 
 * This processors imports the sales UOM.
 * 
 * Sample file will look like
 * 
 * customerId,salesOrganisation,distributionChannel ,division,segmentId,familyId,minimumOrderQuantity,unitOfMeasure
 * walmart,1000,10,10,SEG1002,SEG1002FAM1001,3,CASE
 * 
 * Total column count : 6
 */
public class EnergizerSalesUOMCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ProductService productService;

	@Resource
	private CategoryService categoryService;

	@Resource
	private ModelService modelService;

	@Resource
	private UserService userService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource(name = "energizerB2BUnitService")
	private EnergizerB2BUnitService energizerB2BUnitService;

	@Resource(name = "energizerSalesUOMService")
	private EnergizerSalesUOMService energizerSalesUOMService;

	private List<String> packgingUnits;

	private static final Logger LOG = Logger.getLogger(EnergizerSalesUOMCSVProcessor.class);

	/**
	 * @param uomRecords
	 * @return List<EnergizerCSVFeedError>
	 * 
	 *         LOGIC IMPLEMENTED: Two Scenarios would come into picture: 1) When customer Id is not present in the csv 2)
	 *         When customer Id is present in the csv
	 * 
	 *         Case 1: When the customer Id is absent : When the customer Id is absent , get all the customers under the
	 *         given salesArea say stored as saleaAreaCustomerList. Then get all products for the given segment and
	 *         family say productlist.Iterate over every product and get CMIR list , update the UOM and MOQ for every
	 *         record.
	 * 
	 *         Case 2: When the customer Id is present: When the customer Id is present,get all the products for the
	 *         given segment and family say productlist.Then get the CMIR list,get a particular CMIR record for the given
	 *         customerId and update UOM and MOQ.
	 * 
	 * 
	 */
	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> uomRecords)
	{
		LOG.info("Starting process method  of EnergizerSalesUOMCSVProcessor.............");
		try
		{
			EnergizerCategoryModel energizerSubCategory = null;
			EnergizerProductModel energizerProduct = null;
			final List<EnergizerCMIRModel> cmirList;

			packgingUnits = new ArrayList<String>();
			packgingUnits.add(EnergizerCoreConstants.EA);
			packgingUnits.add(EnergizerCoreConstants.INTERPACK);
			packgingUnits.add(EnergizerCoreConstants.CASE);
			packgingUnits.add(EnergizerCoreConstants.LAYER);
			packgingUnits.add(EnergizerCoreConstants.PALLET);
			packgingUnits.add(EnergizerCoreConstants.EA);

			long succeedRecord = getRecordSucceeded();
			final CatalogVersionModel catalogVersion = getCatalogVersion();
			for (final CSVRecord uomRecord : uomRecords)
			{
				LOG.info(" CSV Record number: " + uomRecord.getRecordNumber());
				LOG.info(" CSV Record: " + uomRecord.toMap());
				validate(uomRecord);
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					getTechnicalFeedErrors().addAll(getBusinessFeedErrors());
					getBusinessFeedErrors().clear();
					continue;
				}
				final Map<String, String> csvValuesMap = uomRecord.toMap();

				final String customerId = csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_ID).trim();
				final String salesOrganisation = csvValuesMap.get(EnergizerCoreConstants.SALES_ORG).trim();
				final String distributionChannel = csvValuesMap.get(EnergizerCoreConstants.DISTRIBUTION_CHANNEL).trim();
				final String division = csvValuesMap.get(EnergizerCoreConstants.DIVISION).trim();
				//final String salesAreaID = csvValuesMap.get(EnergizerCoreConstants.SALES_AREA_ID).trim();
				final String segmentId = csvValuesMap.get(EnergizerCoreConstants.SEGMENT_ID).trim();
				final String familyID = csvValuesMap.get(EnergizerCoreConstants.FAMILY_ID).trim();
				final String uom = csvValuesMap.get(EnergizerCoreConstants.UOM).trim();
				final String moq = csvValuesMap.get(EnergizerCoreConstants.MOQ).trim();
				final String erpMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).trim();

				CategoryModel energizerCategory = null;
				Collection<CategoryModel> subCategories = null;

				//in case of exception at account-material level
				if (erpMaterialId != null && !erpMaterialId.isEmpty() && customerId != null && !customerId.isEmpty())
				{
					EnergizerProductModel existEnergizerProd = null;
					try
					{
						existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(erpMaterialId);
						updateCMIRRecord(existEnergizerProd, customerId, uom, moq);
					}
					catch (final Exception e)
					{
						LOG.error("EnergizerProduct with material id: " + erpMaterialId + "  does  not exist.  " + e);
						continue;
					}
				}
				else
				{
					try
					{
						energizerCategory = categoryService.getCategoryForCode(catalogVersion, segmentId);
						if (energizerCategory == null)
						{
							LOG.error(segmentId + " category does  not exist ");
							continue;
						}
					}
					catch (final Exception exception)
					{
						LOG.error("Error in retreiving the category " + exception);
					}

					LOG.info("The category is : " + energizerCategory.getCode());
					try
					{
						subCategories = energizerCategory.getAllSubcategories();
						if (subCategories == null || subCategories.isEmpty())
						{
							LOG.error("No Subcategories found for " + segmentId + "Category");
							continue;
						}
						for (final CategoryModel subCategory : subCategories)
						{
							LOG.info("Energizer Sub Category : " + subCategory.getCode());
							if (subCategory.getCode().equalsIgnoreCase(familyID))
							{
								LOG.info("Sub Category matches with input..." + familyID);
								energizerSubCategory = (EnergizerCategoryModel) subCategory;
								break;
							}
						}
					}
					catch (final Exception exception)
					{
						LOG.error("Error in retreiving the subcategories" + exception);
					}

					if (energizerSubCategory == null)
					{
						LOG.error("No Subcategories found for the category : " + segmentId);
						continue;
					}

					final List<ProductModel> products = productService.getProductsForCategory(energizerSubCategory);
					if (products == null || products.isEmpty())
					{
						LOG.info("No Products found for the sub-category : " + energizerSubCategory);
					}

					if (customerId.isEmpty())
					{
						final List<EnergizerB2BUnitModel> b2bUnitModels = energizerB2BUnitService.getB2BUnitForSalesArea(
								salesOrganisation, distributionChannel, division);

						if (b2bUnitModels == null || b2bUnitModels.isEmpty())
						{
							LOG.info("No B2BUnits found for the sales org : " + salesOrganisation + " distributionChannel : "
									+ distributionChannel + " and division : " + division);
							continue;
						}

						for (final EnergizerB2BUnitModel energizerB2BUnitModel : b2bUnitModels)
						{
							LOG.info("The sales area of energizerB2BUnitModel is :" + energizerB2BUnitModel.getSalesOrganisation());

							for (final ProductModel product : products)
							{
								if (product == null)
								{
									continue;
								}
								if (product instanceof EnergizerProductModel)
								{
									energizerProduct = (EnergizerProductModel) product;
									updateCMIRRecord(energizerProduct, energizerB2BUnitModel.getUid(), uom, moq);
								}
							}
						}
					}
					else
					{
						for (final ProductModel product : products)
						{
							if (product == null)
							{
								continue;
							}
							if (product instanceof EnergizerProductModel)
							{
								LOG.info("The details about the product is :" + product.getCode());

								energizerProduct = (EnergizerProductModel) product;
								updateCMIRRecord(energizerProduct, customerId, uom, moq);
							}
						}
					}
				}
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error to process for EnergizerSalesAreaUOMModel ******************** " + e);
		}
		getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
		getTechnicalFeedErrors().clear();
		return getCsvFeedErrorRecords();
	}

	private void updateCMIRRecord(final EnergizerProductModel energizerProduct, final String b2bUnitId, final String uom,
			final String moq)
	{
		List<EnergizerCMIRModel> cmirList;
		cmirList = energizerProduct.getProductCMIR();
		if (cmirList == null || cmirList.isEmpty())
		{
			LOG.info("No CMIR records found for the product : " + energizerProduct.getCode());
			return;
		}
		LOG.info("The number of customers associated with this product is:" + cmirList.size());
		for (final EnergizerCMIRModel energizerCMIR : cmirList)
		{
			if (energizerCMIR.getB2bUnit().getUid().equalsIgnoreCase(b2bUnitId))
			{
				energizerCMIR.setUom(uom);
				energizerCMIR.setOrderingUnit(Integer.valueOf(moq));
				modelService.saveAll();
				break;
			}
		}
	}

	/**
	 * 
	 * @param record
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
			long recordFailed = getRecordFailed();
			final String value = map.get(columnHeader).trim();
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.SALES_ORG)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.DISTRIBUTION_CHANNEL)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.DIVISION)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.SEGMENT_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.FAMILY_ID))
			{
				if (value.isEmpty())
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					columnNumbers.add(columnNumber);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should not be empty");
					error.setColumnNumber(columnNumbers);
					error.setUserType(BUSINESS_USER);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.MOQ))
			{
				final boolean isValidMOQ = (NumberUtils.isNumber(value));
				if (!isValidMOQ)
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					columnNumbers.add(columnNumber);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be a valid numeric");
					error.setColumnNumber(columnNumbers);
					error.setUserType(BUSINESS_USER);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
				/*
				 * quantity cannot be 0, This has been commented and fixed in cart page.
				 * 
				 * if (isMOQZeroValue) { final List<String> columnNames = new ArrayList<String>(); final List<Integer>
				 * columnNumbers = new ArrayList<Integer>(); error = new EnergizerCSVFeedError();
				 * error.setLineNumber(record.getRecordNumber()); columnNames.add(columnHeader);
				 * columnNumbers.add(columnNumber); error.setColumnName(columnNames); error.setMessage(columnHeader +
				 * " column should be a valid numeric"); error.setColumnNumber(columnNumbers);
				 * error.setUserType(BUSINESS_USER); getBusinessFeedErrors().add(error);
				 * setBusRecordError(getBusinessFeedErrors().size()); recordFailed++; setRecordFailed(recordFailed); }
				 */
			}

			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.UOM))
			{
				if (!packgingUnits.contains(value))
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					columnNumbers.add(columnNumber);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column can be EA, Interpack, Case, Layer, Pallet");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					error.setUserType(BUSINESS_USER);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
		}
	}
}
