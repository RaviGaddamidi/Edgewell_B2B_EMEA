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
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * This class provides b2bunit for the solr indexing
 * 
 * @author kaushik.ganguly
 */
public class EnergizerProductShippingPointValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private EnergizerProductService energizerProductService;
	private static final Logger LOG = Logger.getLogger(EnergizerProductShippingPointValueProvider.class);

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

						final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);

						for (final String fieldName : fieldNames)
						{
							final List<EnergizerCMIRModel> cmirList = energizerProductService.getEnergizerCMIRList(energizerProduct
									.getCode());
							for (final EnergizerCMIRModel cmir : cmirList)
							{
								if (cmir.getShippingPoint() != null)
								{
									fieldValues.add(new FieldValue(fieldName, cmir.getShippingPoint()));
									LOG.info("Field Name :" + fieldName + " Shipping Point : " + cmir.getShippingPoint());
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

	/**
	 * @return the energizerProductService
	 */
	public EnergizerProductService getEnergizerProductService()
	{
		return energizerProductService;
	}

	/**
	 * @param energizerProductService
	 *           the energizerProductService to set
	 */
	@Required
	public void setEnergizerProductService(final EnergizerProductService energizerProductService)
	{
		this.energizerProductService = energizerProductService;
	}


}
