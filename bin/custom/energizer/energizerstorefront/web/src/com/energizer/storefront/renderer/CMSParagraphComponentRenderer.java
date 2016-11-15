/*
 * ----------------------------------------------------------------
 * --- WARNING: THIS FILE IS GENERATED AND WILL BE OVERWRITTEN! ---
 * --- Generated at Sep 7, 2016 5:51:42 PM                      ---
 * ----------------------------------------------------------------
 *  
 * [y] hybris Platform
 *  
 * Copyright (c) 2000-2011 hybris AG
 * All rights reserved.
 *  
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *  
 */
package com.energizer.storefront.renderer;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.cms2.model.contents.components.SimpleCMSComponentModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ItemModelContext;
import java.util.Locale;

/**
 * Generated model class for type CMSParagraphComponent first defined at extension cms2.
 */
@SuppressWarnings("all")
public class CMSParagraphComponentRenderer extends SimpleCMSComponentModel
{
	/**<i>Generated model type code constant.</i>*/
	public final static String _TYPECODE = "CMSParagraphComponent";
	
	/** <i>Generated constant</i> - Attribute key of <code>CMSParagraphComponent.content</code> attribute defined at extension <code>cms2</code>. */
	public static final String CONTENT = "content";
	
	
	/**
	 * <i>Generated constructor</i> - Default constructor for generic creation.
	 */
	public CMSParagraphComponentRenderer()
	{
		super();
	}
	
	/**
	 * <i>Generated constructor</i> - Default constructor for creation with existing context
	 * @param ctx the model context to be injected, must not be null
	 */
	public CMSParagraphComponentRenderer(final ItemModelContext ctx)
	{
		super(ctx);
	}
	
	/**
	 * <i>Generated constructor</i> - Constructor with all mandatory attributes.
	 * @deprecated Since 4.1.1 Please use the default constructor without parameters
	 * @param _catalogVersion initial attribute declared by type <code>CMSItem</code> at extension <code>cms2</code>
	 * @param _uid initial attribute declared by type <code>CMSItem</code> at extension <code>cms2</code>
	 */
	@Deprecated
	public CMSParagraphComponentRenderer(final CatalogVersionModel _catalogVersion, final String _uid)
	{
		super();
		setCatalogVersion(_catalogVersion);
		setUid(_uid);
	}
	
	/**
	 * <i>Generated constructor</i> - for all mandatory and initial attributes.
	 * @deprecated Since 4.1.1 Please use the default constructor without parameters
	 * @param _catalogVersion initial attribute declared by type <code>CMSItem</code> at extension <code>cms2</code>
	 * @param _owner initial attribute declared by type <code>Item</code> at extension <code>core</code>
	 * @param _uid initial attribute declared by type <code>CMSItem</code> at extension <code>cms2</code>
	 */
	@Deprecated
	public CMSParagraphComponentRenderer(final CatalogVersionModel _catalogVersion, final ItemModel _owner, final String _uid)
	{
		super();
		setCatalogVersion(_catalogVersion);
		setOwner(_owner);
		setUid(_uid);
	}
	
	
	/**
	 * <i>Generated method</i> - Getter of the <code>CMSParagraphComponent.content</code> attribute defined at extension <code>cms2</code>. 
	 * @return the content
	 */
	public String getContent()
	{
		return getContent(null);
	}
	/**
	 * <i>Generated method</i> - Getter of the <code>CMSParagraphComponent.content</code> attribute defined at extension <code>cms2</code>. 
	 * @param loc the value localization key 
	 * @return the content
	 * @throws IllegalArgumentException if localization key cannot be mapped to data language
	 */
	public String getContent(final Locale loc)
	{
		return getPersistenceContext().getLocalizedValue(CONTENT, loc);
	}
	
	/**
	 * <i>Generated method</i> - Setter of <code>CMSParagraphComponent.content</code> attribute defined at extension <code>cms2</code>. 
	 *  
	 * @param value the content
	 */
	public void setContent(final String value)
	{
		setContent(value,null);
	}
	/**
	 * <i>Generated method</i> - Setter of <code>CMSParagraphComponent.content</code> attribute defined at extension <code>cms2</code>. 
	 *  
	 * @param value the content
	 * @param loc the value localization key 
	 * @throws IllegalArgumentException if localization key cannot be mapped to data language
	 */
	public void setContent(final String value, final Locale loc)
	{
		getPersistenceContext().setLocalizedValue(CONTENT, loc, value);
	}
	
}
