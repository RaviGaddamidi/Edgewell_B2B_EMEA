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
package com.energizer.core.jalo;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ExtensionManager;
import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.setup.CoreSystemSetup;

import org.apache.log4j.Logger;


/**
 * Don't use. User {@link CoreSystemSetup} instead.
 */
@SuppressWarnings("PMD")
public class EnergizerCoreManager extends GeneratedEnergizerCoreManager
{
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(EnergizerCoreManager.class.getName());

	public static final EnergizerCoreManager getInstance()
	{
		final ExtensionManager em = JaloSession.getCurrentSession().getExtensionManager();
		return (EnergizerCoreManager) em.getExtension(EnergizerCoreConstants.EXTENSIONNAME);
	}
}
