/**
 * 
 */
package com.energizer.core.services.email.context;

import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * @author M1023278
 * 
 */
public class EnergizerPasswordExpiryEmailContext extends EnergizerGenericEmailContext
{


	/**
	 * @param emailPageModel
	 * @param language
	 * @param contextMap
	 */
	@Override
	public void init(final EmailPageModel emailPageModel, final LanguageModel language, final Map<String, Object> contextMap)
	{

		super.init(emailPageModel, language, contextMap);

	}

	public String getSecureResetPasswordUrl() throws UnsupportedEncodingException
	{
		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(),"", true,
				"/USD/login/pw/request-page");
	}

	public String getDisplaySecureResetPasswordUrl() throws UnsupportedEncodingException
	{

		return getSiteBaseUrlResolutionService().getWebsiteUrlForSite(getBaseSite(), "", true, "/USD/login/pw/request-page");
	}



}
