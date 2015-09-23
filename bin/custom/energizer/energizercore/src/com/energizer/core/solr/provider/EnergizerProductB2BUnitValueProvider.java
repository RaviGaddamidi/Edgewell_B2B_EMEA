/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.core.solr.provider;

import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractPropertyFieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * This class provides b2bunit for the solr indexing
 * 
 * @author kaushik.ganguly
 */
public class EnergizerProductB2BUnitValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private static final Logger LOG = Logger.getLogger(EnergizerProductB2BUnitValueProvider.class);

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
				for (final PriceRowModel unit : energizerProduct.getEurope1Prices())
				{
					if (unit instanceof EnergizerPriceRowModel && ((EnergizerPriceRowModel) unit).getB2bUnit() != null)
					{
						final EnergizerPriceRowModel castedUnit = (EnergizerPriceRowModel) unit;
						LOG.info("enrPriceRowModel.getIsActive() = " + castedUnit.getIsActive());

						if (!castedUnit.getIsActive())
						{
							LOG.info("NOT ACTIVE...");
							continue;
						}
						final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);

						for (final String fieldName : fieldNames)
						{
							fieldValues.add(new FieldValue(fieldName, castedUnit.getB2bUnit().getUid()));
							LOG.info("Field Name :" + fieldName + " B2b unit ID : " + castedUnit.getB2bUnit().getUid());
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
			LOG.info("Created the B2Bunit");
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
