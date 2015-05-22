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
package com.energizer.storefront.controllers.util;

import java.util.Collection;


public class GlobalMessage
{
	private String code;
	private Collection<Object> attributes;
	private String attribute;

	/**
	 * @return the attribute
	 */
	public String getAttribute()
	{
		return attribute;
	}

	/**
	 * @param attribute
	 *           the attribute to set
	 */
	public void setAttribute(final String attribute)
	{
		this.attribute = attribute;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(final String code)
	{
		this.code = code;
	}

	public Collection<Object> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(final Collection<Object> attributes)
	{
		this.attributes = attributes;
	}
}
