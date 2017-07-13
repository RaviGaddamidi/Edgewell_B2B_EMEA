package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * @author Bivash Pandit
 *
 *         CSV File : XXXPRODUCTDATAXXXX SAMPLE HEADERS and DATA: ERPMaterialID,Img Ref Mat,Product
 *         Group,ListPrice,ListPriceCurrency,ObsoleteStatus 000000000000000003,LITM, 10.00,USD,0
 * 
 */
public class EnergizerProductCSVProcessor extends AbstractEnergizerCSVProcessor
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
	@Resource
	B2BCommerceUserService b2bCommerceUserService;
	@Resource
	CategoryService categoryService;

	private static final String UNIT = "EA";
	private static final Logger LOG = Logger.getLogger(EnergizerProductCSVProcessor.class);

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		try
		{
			final CatalogVersionModel catalogVersion = getCatalogVersion();
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				try
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

					LOG.info("|| Start add or updating  EnergizerProductModel for product :  "
							+ (csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID));

					LOG.info("|| Start add or updating  ImageRefId for product :  "
							+ (csvValuesMap).get(EnergizerCoreConstants.IMAGEREFERENCE_ID));

					EnergizerProductModel existEnergizerProd = null;
					try
					{
						existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
								(csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID).trim());
					}
					catch (final Exception e)
					{
						LOG.error(
								(csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID) + " EnergizerProduct does  not exist  " + e);
					}

					if (null == existEnergizerProd)
					{
						final EnergizerProductModel energizerNewProd = modelService.create(EnergizerProductModel.class);

						addUpdateProductDetails(energizerNewProd, catalogVersion, csvValuesMap);
						LOG.info("|| EnergizerProductModel  " + (csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID)
								+ " saved successfully.");

					}
					else
					{
						addUpdateProductDetails(existEnergizerProd, catalogVersion, csvValuesMap);
						modelService.saveAll();
						LOG.info(" || EnergizerProductModel " + (csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID)
								+ " updated successfully.");
					}
					succeedRecord++;
					setRecordSucceeded(succeedRecord);
				}
				catch (final Exception e)
				{
					LOG.error(" Error to addUpdateProductDetails for EnergizerProductModel   ||  " + e);
					LOG.info("Processing the next record.....");
				}
			}
		}
		catch (final Exception e)
		{
			LOG.error(" Error to addUpdateProductDetails for EnergizerProductModel   ||  " + e);
		}

		getTechnicalFeedErrors().addAll(techFeedErrorRecords);
		getBusinessFeedErrors().addAll(businessFeedErrorRecords);
		techFeedErrorRecords.clear();
		businessFeedErrorRecords.clear();
		return getCsvFeedErrorRecords();
	}

	/**
	 * 
	 * @param energizerProd
	 * @param catalogVersion
	 * @param csvValuesMap
	 */
	private void addUpdateProductDetails(final EnergizerProductModel energizerProd, final CatalogVersionModel catalogVersion,
			final Map csvValuesMap)
	{
		final String productMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).toString();
		//TODO : Need to remove this code.
		final String imageReferenceId = csvValuesMap.get(EnergizerCoreConstants.IMAGEREFERENCE_ID) == null ? "NULL"
				: csvValuesMap.get(EnergizerCoreConstants.IMAGEREFERENCE_ID).toString();
		final String productGroup = csvValuesMap.get(EnergizerCoreConstants.PRODUCT_GROUP).toString();
		final String listPrice = csvValuesMap.get(EnergizerCoreConstants.LIST_PRICE).toString();
		final String listPriceCurrency = csvValuesMap.get(EnergizerCoreConstants.LIST_PRICE_CURRENCY).toString();
		final String obsolete = csvValuesMap.get(EnergizerCoreConstants.OBSOLETE_STATUS).toString();
		CategoryModel energizerCategory = null;
		energizerProd.setCatalogVersion(catalogVersion);
		energizerProd.setApprovalStatus(ArticleApprovalStatus.APPROVED);
		energizerProd.setCode(productMaterialId);
		energizerProd.setImageReferenceId(imageReferenceId);

		// Assigning The Category
		try
		{
			energizerCategory = categoryService.getCategoryForCode(catalogVersion, productGroup);

		}
		catch (final Exception e)
		{
			LOG.error(e.getMessage());
		}


		Collection<PriceRowModel> prices = energizerProd.getEurope1Prices();
		if (null != prices && prices.size() > 0)
		{
			for (final Iterator iterator = prices.iterator(); iterator.hasNext();)
			{
				final PriceRowModel oldPriceRow = (PriceRowModel) iterator.next();
				if (oldPriceRow instanceof EnergizerPriceRowModel)
				{
					LOG.info("EnergizerPriceRowModel ********************");
					continue;
				}
				else
				{
					oldPriceRow.setPrice(new Double(listPrice));
					modelService.save(oldPriceRow);
				}
			}
		}
		else
		{
			final PriceRowModel newPriceRow = modelService.create(PriceRowModel.class);
			prices = new ArrayList<PriceRowModel>();
			newPriceRow.setCatalogVersion(catalogVersion);
			newPriceRow.setProduct(energizerProd);
			newPriceRow.setCurrency(defaultCommonI18NService.getCurrency(listPriceCurrency));
			newPriceRow.setUnit(defaultUnitService.getUnitForCode(UNIT));
			newPriceRow.setPrice(new Double(listPrice));
			modelService.save(newPriceRow);
			prices.add(newPriceRow);
			energizerProd.setEurope1Prices(prices);
		}
		if (obsolete.equals("0"))
		{
			energizerProd.setObsolete(false);
		}
		else
		{
			energizerProd.setObsolete(true);
		}
		modelService.saveAll();
		final List<ProductModel> productModelLst = new ArrayList<ProductModel>();
		productModelLst.add(energizerProd);


		if (null != energizerCategory)
		{
			final List<ProductModel> productModelCurrentList = energizerCategory.getProducts();

			if (productModelCurrentList != null && productModelCurrentList.size() != 0)
			{
				for (final Iterator iterator = productModelCurrentList.iterator(); iterator.hasNext();)
				{
					final ProductModel obj = (ProductModel) iterator.next();
					productModelLst.add(obj);
				}
			}
			energizerCategory.setProducts(productModelLst);
		}

		modelService.saveAll();
		LOG.info(" EnergizerProductModel : " + productMaterialId + " saved successfully ******************** ");
		LOG.info(" EnergizerProductModel Image Reference: " + imageReferenceId + " saved successfully ******************** ");

	}

	/**
	 * 
	 * @param record
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError techError = null;
		EnergizerCSVFeedError busError = null;
		final Map<String, String> map = record.toMap();
		Integer columnNumber = 0;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			long recordFailed = getRecordFailed();
			setTotalRecords(record.getRecordNumber());
			final String value = map.get(columnHeader).trim();
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ERPMATERIAL_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.IMAGEREFERENCE_ID)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.PRODUCT_GROUP)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LIST_PRICE_CURRENCY)
					|| columnHeader.equalsIgnoreCase(EnergizerCoreConstants.ERPMATERIAL_ID))
			{
				if (value.isEmpty())
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					techError = new EnergizerCSVFeedError();
					techError.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					techError.setUserType(TECHNICAL_USER);
					techError.setColumnName(columnNames);
					columnNumbers.add(columnNumber);
					techError.setMessage(columnHeader + " column should not be empty");
					techError.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(techError);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.LIST_PRICE))
			{
				if (!NumberUtils.isNumber(value) || Double.valueOf(value) <= 0.0)
				{
					busError = new EnergizerCSVFeedError();
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					techError = new EnergizerCSVFeedError();
					techError.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					techError.setUserType(TECHNICAL_USER);
					techError.setColumnName(columnNames);
					columnNumbers.add(columnNumber);
					techError.setMessage(columnHeader + " column should be numeric");
					techError.setColumnNumber(columnNumbers);
					getTechnicalFeedErrors().add(techError);
					setTechRecordError(getTechnicalFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}
		}
	}
}
