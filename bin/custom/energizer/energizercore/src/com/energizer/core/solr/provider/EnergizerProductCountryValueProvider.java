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
import com.energizer.core.model.EnergizerRegionModel;


/**
 * @author M1028807
 * 
 */
public class EnergizerProductCountryValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private static final Logger LOG = Logger.getLogger(EnergizerProductCountryValueProvider.class);

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

				String query = "select {PK} from {EnergizerRegionGenericProducts}";

				FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery("select {PK} from {EnergizerRegionGenericProducts}");

				final List<EnergizerRegionGenericProductsModel> genericProductsList = flexibleSearchService
						.<EnergizerRegionGenericProductsModel> search(retreiveQuery).getResult();

				for (final EnergizerRegionGenericProductsModel genericProduct : genericProductsList)
				{
					if (genericProduct.getProducts() != null && !genericProduct.getProducts().isEmpty())
					{
						if (genericProduct.getProducts().contains(energizerProduct.getCode()))
						{
							LOG.info("GENERIC PRODUCT list not empty and matches = " + energizerProduct.getCode());

							LOG.info("Country = " + genericProduct.getCountry());
							if (genericProduct.getCountry() == null || genericProduct.getCountry().isEmpty())
							{
								query = //
								"SELECT {p:" + EnergizerRegionModel.PK + "}" //
										+ "FROM {" + EnergizerRegionModel._TYPECODE + " AS p} "//
										+ "WHERE " + "{p:" + EnergizerRegionModel.REGIONNAME + "}=?regionName";

								retreiveQuery = new FlexibleSearchQuery(query);
								retreiveQuery.addQueryParameter("regionName", genericProduct.getRegionName());

								final List<EnergizerRegionModel> countryList = flexibleSearchService.<EnergizerRegionModel> search(
										retreiveQuery).getResult();
								if (countryList != null && !countryList.isEmpty())
								{
									EnergizerRegionModel regionModel = countryList.get(0);
									if (regionModel != null && regionModel.getCountryList() != null
											&& !regionModel.getCountryList().isEmpty())
									{
										LOG.info("GETTING ALL COUNTRIES FOR REGION : " + genericProduct.getRegionName());
										for (String country : regionModel.getCountryList())
										{
											LOG.info(" ----- COUNTRY : " + country);
											final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
											for (final String fieldName : fieldNames)
											{
												fieldValues.add(new FieldValue(fieldName, country));
												LOG.info("Field Name :" + fieldName + " Country : " + country);
											}
										}
									}
								}
							}
							else
							{
								final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
								for (final String fieldName : fieldNames)
								{
									fieldValues.add(new FieldValue(fieldName, genericProduct.getCountry()));
									LOG.info("Field Name :" + fieldName + " Country : " + genericProduct.getCountry());
								}
							}

						}
					}
				}
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
