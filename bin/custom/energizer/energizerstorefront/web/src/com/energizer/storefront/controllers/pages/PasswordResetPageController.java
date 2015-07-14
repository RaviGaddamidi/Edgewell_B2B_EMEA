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
package com.energizer.storefront.controllers.pages;

import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.util.Config;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.energizer.core.model.EnergizerB2BCustomerModel;
import com.energizer.facades.accounts.impl.DefaultEnergizerB2BPasswordQuestionsFacade;
import com.energizer.facades.accounts.impl.DefaultEnergizerCompanyB2BCommerceFacade;
import com.energizer.facades.accounts.populators.EnergizerB2BCustomerReversePopulator;
import com.energizer.storefront.breadcrumb.ResourceBreadcrumbBuilder;
import com.energizer.storefront.constants.WebConstants;
import com.energizer.storefront.controllers.ControllerConstants;
import com.energizer.storefront.controllers.util.GlobalMessages;
import com.energizer.storefront.forms.ForgottenPwdForm;
import com.energizer.storefront.forms.ResetPwdForm;
import com.energizer.storefront.forms.UpdatePwdForm;


/**
 * Controller for the forgotten password pages. Supports requesting a password reset email as well as changing the
 * password once you have got the token that was sent via email.
 */
@Controller
@Scope("tenant")
@RequestMapping(value = "/login/pw")
public class PasswordResetPageController extends AbstractPageController
{
	private static final Logger LOG = Logger.getLogger(PasswordResetPageController.class);

	private static final String REDIRECT_LOGIN = "redirect:/login";
	private static final String REDIRECT_HOME = "redirect:/";

	private static final String UPDATE_PWD_CMS_PAGE = "updatePassword";
	private static final String FORGOTTEN_PASSWORD_EXP_VALUE = "forgottenPassword.emailContext.expiresInMinutes";
	private static final String EXP_IN_SECONDS = "forgottenPassword.emailContext.expiresInSeconds";

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "b2bCommerceFacade")
	protected CompanyB2BCommerceFacade companyB2BCommerceFacade;

	@Resource(name = "defaultEnergizerCompanyB2BCommerceFacade")
	protected DefaultEnergizerCompanyB2BCommerceFacade defaultEnergizerCompanyB2BCommerceFacade;

	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;

	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "defaultEnergizerCustomerReversePopulator")
	protected EnergizerB2BCustomerReversePopulator energizerReversePopulator;

	@Resource(name = "defaultEnergizerB2BPasswordQuestionsFacade")
	private DefaultEnergizerB2BPasswordQuestionsFacade passwordQuestionsFacade;

	@RequestMapping(value = "/request", method = RequestMethod.GET)
	public String getPasswordRequest(final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(new ForgottenPwdForm());

		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
	}

	@RequestMapping(value = "/request", method = RequestMethod.POST)
	public String passwordRequest(@Valid final ForgottenPwdForm form, final BindingResult bindingResult)
			throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPopup;
		}
		else
		{
			try
			{
				getCustomerFacade().forgottenPassword(form.getEmail());
			}
			catch (final UnknownIdentifierException unknownIdentifierException)
			{
				LOG.warn("Email: " + form.getEmail() + " does not exist in the database.");
			}
			return ControllerConstants.Views.Fragments.Password.ForgotPasswordValidationMessage;
		}
	}


	@RequestMapping(value = "/reset-password", method = RequestMethod.GET)
	public String getPasswordResetPage(final Model model, @RequestParam(required = false) final String uid)
			throws CMSItemNotFoundException
	{
		final ResetPwdForm resetPwdForm = new ResetPwdForm();
		if (null != uid)
		{
			resetPwdForm.setEmail(uid);
		}
		//model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
		model.addAttribute(resetPwdForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
	}


	@RequestMapping(value = "/reset-password", method = RequestMethod.POST)
	public String passwordResetPage(@Valid final ResetPwdForm resetPwdForm, final BindingResult bindingResult, final Model model)
			throws CMSItemNotFoundException
	{
		model.addAttribute(resetPwdForm);
		final String forgottenPassExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));

		if (!bindingResult.hasErrors())
		{
			try
			{
				final EnergizerB2BCustomerModel customerModel = defaultEnergizerCompanyB2BCommerceFacade
						.getExistingUserForUID(resetPwdForm.getEmail());


				if (customerModel == null)
				{
					bindingResult.rejectValue("email", "profile.email.incorrect", new Object[] {}, "profile.email.incorrect");
				}
				else
				{

					getCustomerFacade().forgottenPassword(resetPwdForm.getEmail());
					GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
							"account.confirmation.forgotten.password.link.sent", new Object[]
							{ forgottenPassExpValue });
					model.addAttribute(new ForgottenPwdForm());

				}
			}
			catch (final Exception e)
			{

			}

		}
		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			//model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
			model.addAttribute(resetPwdForm);
			storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
			return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
		}

		return ControllerConstants.Views.Fragments.Password.PasswordResetPage;
	}


	@RequestMapping(value = "/request-page", method = RequestMethod.GET)
	public String getPasswordRequestPage(final Model model, @RequestParam(required = false) final String uid)
			throws CMSItemNotFoundException
	{
		final ForgottenPwdForm forgottenPwdForm = new ForgottenPwdForm();
		if (null != uid)
		{
			forgottenPwdForm.setEmail(uid);
		}
		model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
		model.addAttribute(forgottenPwdForm);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
	}

	@RequestMapping(value = "/request-page", method = RequestMethod.POST)
	public String passwordRequestPage(@Valid final ForgottenPwdForm forgottenPwdForm, final BindingResult bindingResult,
			final Model model) throws CMSItemNotFoundException
	{
		model.addAttribute(forgottenPwdForm);
		final String forgottenPassExpValue = Config.getParameter(FORGOTTEN_PASSWORD_EXP_VALUE);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));

		if (!bindingResult.hasErrors())
		{
			try
			{
				final EnergizerB2BCustomerModel customerModel = defaultEnergizerCompanyB2BCommerceFacade
						.getExistingUserForUID(forgottenPwdForm.getEmail());


				if (customerModel == null)
				{
					bindingResult.rejectValue("email", "profile.email.incorrect", new Object[] {}, "profile.email.incorrect");
				}
				else
				{

					if (!(customerModel.getPasswordQuestion()).equals(forgottenPwdForm.getPasswordQuestion()))
					{
						bindingResult.rejectValue("passwordQuestion", "profile.passwordQuestion.incorrect", new Object[] {},
								"profile.passwordQuestion.incorrect");
					}
					else if (!(customerModel.getPasswordAnswer()).equals(forgottenPwdForm.getPasswordAnswer()))
					{
						bindingResult.rejectValue("passwordAnswer", "profile.passwordAnswer.incorrect", new Object[] {},
								"profile.passwordAnswer.incorrect");
					}

					else
					{
						getCustomerFacade().forgottenPassword(forgottenPwdForm.getEmail());
						GlobalMessages.addForgotPwdConfMessage(model, GlobalMessages.FORGOT_PWD_CONF_MESSAGES,
								"account.confirmation.forgotten.password.link.sent", new Object[]
								{ forgottenPassExpValue });
						model.addAttribute(new ForgottenPwdForm());
					}
				}
			}
			catch (final Exception e)
			{

			}

		}
		if (bindingResult.hasErrors())
		{
			GlobalMessages.addErrorMessage(model, "form.global.error");
			model.addAttribute("passwordQuestionsList", passwordQuestionsFacade.getEnergizerPasswordQuestions());
			model.addAttribute(forgottenPwdForm);
			storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
			model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("forgottenPwd.title"));
			return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
		}

		return ControllerConstants.Views.Fragments.Password.PasswordResetRequestPage;
	}

	@RequestMapping(value = "/change", method = RequestMethod.GET)
	public String getChangePassword(@RequestParam(required = false) final String token, final Model model)
			throws CMSItemNotFoundException
	{
		if (StringUtils.isBlank(token))
		{
			return REDIRECT_HOME;
		}
		final UpdatePwdForm form = new UpdatePwdForm();
		form.setToken(token);
		model.addAttribute(form);
		storeCmsPageInModel(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(UPDATE_PWD_CMS_PAGE));
		model.addAttribute(WebConstants.BREADCRUMBS_KEY, resourceBreadcrumbBuilder.getBreadcrumbs("updatePwd.title"));
		return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
	}

	@RequestMapping(value = "/change", method = RequestMethod.POST)
	public String changePassword(@Valid final UpdatePwdForm form, final BindingResult bindingResult, final Model model,
			final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		if (bindingResult.hasErrors())
		{
			prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
			return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
		}
		if (!StringUtils.isBlank(form.getToken()))
		{
			try
			{
				LOG.debug("The password link expriy time in seconds :"
						+ configurationService.getConfiguration().getLong(EXP_IN_SECONDS, 1800));


				final boolean flag = defaultEnergizerCompanyB2BCommerceFacade.updatingPassword(form.getPwd(), form.getToken());

				if (!flag)
				{
					bindingResult.rejectValue("pwd", "profile.newPassword.match", new Object[] {}, "profile.newPassword.match");
				}

				if (bindingResult.hasErrors())
				{
					prepareErrorMessage(model, UPDATE_PWD_CMS_PAGE);
					return ControllerConstants.Views.Pages.Password.PasswordResetChangePage;
				}
				//	getCustomerFacade().updatePassword(form.getToken(), form.getPwd());

				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
						"account.confirmation.password.updated");
				//adding a session attribute that will be removed in the StorefrontAuthenticationSuccessHandler.java once the successful login happens
				getSessionService().setAttribute(JUST_UPDATED_PWD, JUST_UPDATED_PWD);
			}
			catch (final TokenInvalidatedException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("update passwoed failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalidated");
			}
			catch (final RuntimeException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("update passwoed failed due to, " + e.getMessage(), e);
				}
				GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, "updatePwd.token.invalid");
			}
		}
		return REDIRECT_LOGIN;
	}

	/**
	 * Prepares the view to display an error message
	 * 
	 * @param model
	 * @param page
	 * @throws CMSItemNotFoundException
	 */
	protected void prepareErrorMessage(final Model model, final String page) throws CMSItemNotFoundException
	{
		GlobalMessages.addErrorMessage(model, "form.global.error");
		storeCmsPageInModel(model, getContentPageForLabelOrId(page));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(page));
	}
}
