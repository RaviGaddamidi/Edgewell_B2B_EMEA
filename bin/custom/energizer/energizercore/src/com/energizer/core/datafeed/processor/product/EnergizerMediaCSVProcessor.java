package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.media.MediaContainerModel;
import de.hybris.platform.core.model.media.MediaFormatModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.product.UnitService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.media.MediaContainerService;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.model.EnergizerProductModel;


/**
 * 
 * 
 * This processors imports the media.
 * 
 * Sample file will look like
 * 
 * ERPMaterialID,ThumnailPath, DisplayImagePath PRD001, /medias/Produc101-thumb.jpeg, /medias/Produc201-thumb.jpeg
 * 
 * Total column count : 3
 */
public class EnergizerMediaCSVProcessor extends AbstractEnergizerCSVProcessor
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
	MediaService mediaService;
	@Resource
	MediaContainerService mediaContainerService;

	private static final Logger LOG = Logger.getLogger(EnergizerMediaCSVProcessor.class);
	private final String PRD_IMG_QUALIFIER = "300Wx300H";
	private final String PRD_THUMB_QUALIFIER = "96Wx96H";
	private static String THUMB = "_thumb";
	private static String PICS = "_pic";
	private static final String JPEG = "JPEG";
	private static final String JPG = "JPG";

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{
		EnergizerProductModel existEnergizerProd = null;
		try
		{
			final CatalogVersionModel catalogVersion = getCatalogVersion();
			long succeedRecord = getRecordSucceeded();
			for (final CSVRecord record : records)
			{
				super.technicalFeedErrors = new ArrayList<EnergizerCSVFeedError>();
				super.businessFeedErrors = new ArrayList<EnergizerCSVFeedError>();

				final Map<String, String> csvValuesMap = record.toMap();
				validate(record);
				if (!getBusinessFeedErrors().isEmpty())
				{
					csvFeedErrorRecords.addAll(getBusinessFeedErrors());
					continue;
				}


				try
				{
					existEnergizerProd = (EnergizerProductModel) productService.getProductForCode(catalogVersion,
							(csvValuesMap).get(EnergizerCoreConstants.ERPMATERIAL_ID));
				}
				catch (final Exception e)
				{
					LOG.info("existEnergizerProd DOES NOT EXIST");
				}

				if (null != existEnergizerProd)
				{
					addUpdateProductMediaDetails(existEnergizerProd, catalogVersion, csvValuesMap);
					LOG.info("****************** ProductMediaModel updated successfully ****************** ");
				}
				else
				{
					LOG.info("ProductMediaModel can not be empty");
				}
				succeedRecord++;
				setRecordSucceeded(succeedRecord);
			}
		}
		catch (final Exception e)
		{
			LOG.error("Error in adding or updating  ProductMediaModel" + e.getMessage());
		}
		return getCsvFeedErrorRecords();
	}

	/**
	 * 
	 * @param energizerProd
	 * @param catalogVersion
	 * @param csvValuesMap
	 * @throws FileNotFoundException
	 */
	private void addUpdateProductMediaDetails(final EnergizerProductModel energizerProd, final CatalogVersionModel catalogVersion,
			final Map<String, String> csvValuesMap) throws FileNotFoundException
	{

		final String productMaterialId = csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID).toString().trim();
		final String thumnailPath = csvValuesMap.get(EnergizerCoreConstants.THUMBNAIIL_PATH).toString().trim();
		final String displayImagePath = csvValuesMap.get(EnergizerCoreConstants.DISPLAY_IMAGE_PATH).toString().trim();

		energizerProd.setCode(productMaterialId);
		energizerProd.setCatalogVersion(catalogVersion);
		energizerProd.setApprovalStatus(ArticleApprovalStatus.APPROVED);

		final MediaModel mediaThumbnail = createUploadProductMedia(thumnailPath, productMaterialId.concat(THUMB),
				PRD_THUMB_QUALIFIER, catalogVersion, productMaterialId);
		final MediaModel mediaPicture = createUploadProductMedia(displayImagePath, productMaterialId.concat(PICS),
				PRD_IMG_QUALIFIER, catalogVersion, productMaterialId);

		energizerProd.setThumbnail(mediaThumbnail);
		energizerProd.setPicture(mediaPicture);
		LOG.info("Flag Value" + modelService.isModified(energizerProd));
		LOG.info("Is New" + modelService.isNew(energizerProd));

		modelService.saveAll();
	}



	/**
	 * 
	 * @param fileLoc
	 * @param mediaModelCode
	 * @param mediaQualifier
	 * @param catalogVersion
	 * @param productMaterialId
	 * @return
	 * @throws FileNotFoundException
	 */
	private MediaModel createUploadProductMedia(final String fileLoc, final String mediaModelCode, final String mediaQualifier,
			final CatalogVersionModel catalogVersion, final String productMaterialId) throws FileNotFoundException
	{
		final InputStream mediaInputStream = new FileInputStream(new File(fileLoc));

		// Creating or Updating  Media
		MediaModel mediaModel = null;
		try
		{
			mediaModel = mediaService.getMedia(catalogVersion, mediaModelCode);
		}
		catch (final Exception e)
		{
			LOG.error(" Media does not exist for Product Media " + mediaModelCode + " || " + e);
		}

		if (null == mediaModel)
		{
			mediaModel = modelService.create(MediaModel.class);
			final MediaFormatModel format = mediaService.getFormat(mediaQualifier);
			mediaModel.setCode(mediaModelCode);
			mediaModel.setMediaFormat(format);
			mediaModel.setCatalogVersion(catalogVersion);
		}
		modelService.save(mediaModel);
		mediaService.setStreamForMedia(mediaModel, mediaInputStream);

		// Creating or Updating  mediaContainer and add media
		MediaContainerModel mediaContainer = null;
		final String mediaContainerQualifier = productMaterialId.concat("_mediaContainer");
		try
		{
			mediaContainer = mediaContainerService.getMediaContainerForQualifier(mediaContainerQualifier);
		}
		catch (final Exception e)
		{
			LOG.error(mediaContainerQualifier + " mediaContainer not exist" + e);
		}

		if (mediaContainer == null)
		{
			mediaContainer = modelService.create(MediaContainerModel.class);
			mediaContainer.setQualifier(mediaContainerQualifier);
			mediaContainer.setCatalogVersion(catalogVersion);
			modelService.save(mediaContainer);
		}
		mediaContainerService.addMediaToContainer(mediaContainer, Collections.singletonList(mediaModel));

		LOG.info(mediaModelCode + " mediaModel Saved Successfully *************");

		return mediaModel;

	}

	/**
	 * 
	 * @param record
	 * @return
	 */
	private void validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		long recordFailed = getRecordFailed();
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());
			final String value = record.toMap().get(columnHeader);
			if (value.isEmpty())
			{
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error = new EnergizerCSVFeedError();
				error.setUserType(BUSINESS_USER);
				error.setLineNumber(record.getRecordNumber());
				columnNames.add(columnHeader);
				error.setColumnName(columnNames);
				error.setMessage(columnHeader + " column should not be empty");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				getBusinessFeedErrors().add(error);
				setBusRecordError(getBusinessFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
			}
			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.DISPLAY_IMAGE_PATH))
			{
				final String extension = value.substring(value.lastIndexOf(".") + 1, value.length());

				if (!(extension.equalsIgnoreCase(JPEG) || extension.equalsIgnoreCase(JPG)))
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(BUSINESS_USER);
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be jpeg/jpg");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
					setBusRecordError(getBusinessFeedErrors().size());
					recordFailed++;
					setRecordFailed(recordFailed);
				}
			}

			if (columnHeader.equalsIgnoreCase(EnergizerCoreConstants.THUMBNAIIL_PATH))
			{
				final String extension = value.substring(value.lastIndexOf(".") + 1, value.length());

				if (!(extension.equalsIgnoreCase(JPEG) || extension.equalsIgnoreCase(JPG)))
				{
					final List<String> columnNames = new ArrayList<String>();
					final List<Integer> columnNumbers = new ArrayList<Integer>();
					error = new EnergizerCSVFeedError();
					error.setUserType(BUSINESS_USER);
					error.setLineNumber(record.getRecordNumber());
					columnNames.add(columnHeader);
					error.setColumnName(columnNames);
					error.setMessage(columnHeader + " column should be jpeg/jpg");
					columnNumbers.add(columnNumber);
					error.setColumnNumber(columnNumbers);
					getBusinessFeedErrors().add(error);
				}
			}
		}
	}
}
