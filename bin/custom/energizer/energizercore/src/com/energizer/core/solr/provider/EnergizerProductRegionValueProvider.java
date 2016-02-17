/**
 * 
 */
package com.energizer.core.solr.provider;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerRegionGenericProductsModel;


/**
 * This class is used for solr indexing the Region attribute of product
 * 
 * @author M1028807
 * 
 */
public class EnergizerProductRegionValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private static final Logger LOG = Logger.getLogger(EnergizerProductRegionValueProvider.class);

	@Resource
	FlexibleSearchService flexibleSearchService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{

		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();

		try
		{
			if (model instanceof EnergizerProductModel)
			{
				final EnergizerProductModel energizerProduct = (EnergizerProductModel) model;

				final String query = "select {PK} from {EnergizerRegionGenericProducts}";


				final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery("select{PK}  from {EnergizerRegionGenericProducts}");

				final List<EnergizerRegionGenericProductsModel> genericProductsList = flexibleSearchService
						.<EnergizerRegionGenericProductsModel> search(retreiveQuery).getResult();
				if (genericProductsList != null && genericProductsList.size() > 0)
				{
					for (final EnergizerRegionGenericProductsModel genericProduct : genericProductsList)
					{
						if (genericProduct.getProducts() != null && !genericProduct.getProducts().isEmpty())
						{
							if (genericProduct.getProducts().contains(energizerProduct.getCode()))
							{
								LOG.info("GENERIC PRODUCT list not empty and matches = " + energizerProduct.getCode());
								final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);

								LOG.info("Region = " + genericProduct.getRegionName());
								for (final String fieldName : fieldNames)
								{
									fieldValues.add(new FieldValue(fieldName, genericProduct.getRegionName()));
									LOG.info("REGION Field Name :" + fieldName + " Region : " + genericProduct.getRegionName());
								}
							}
						}
					}
				}

				//fetch product list for each of the record in the EnergizerRegionGenericProducts
				//for each record - if current product being indexed exist in the list, erpRegion:<region-name1>,<region-name2> ## erpCountry:<country1>, <country2>

				/*
				 * if (energizerProduct.getRegion() != null && !energizerProduct.getRegion().isEmpty()) {
				 * LOG.info("REGION list not empty = " + energizerProduct.getRegion().size()); final Collection<String>
				 * fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
				 * 
				 * for (final String region : energizerProduct.getRegion()) { LOG.info("Region = " + region); for (final
				 * String fieldName : fieldNames) { fieldValues.add(new FieldValue(fieldName, region));
				 * LOG.info("Field Name :" + fieldName + " Region : " + region); } } }
				 */
			}

		}
		catch (final Exception ex)
		{
			LOG.error("Exception Occured", ex);
		}
		finally
		{
			LOG.info("Created index for Region");
		}
		return fieldValues;
	}

	protected FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}
}
