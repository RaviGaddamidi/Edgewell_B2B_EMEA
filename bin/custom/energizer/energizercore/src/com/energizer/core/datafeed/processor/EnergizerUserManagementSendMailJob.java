/**
 *
 */
package com.energizer.core.datafeed.processor;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.b2b.constants.B2BConstants;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.services.B2BUnitService;
import de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade;
import de.hybris.platform.b2bcommercefacades.company.B2BUnitFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BUnitData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.util.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.energizer.core.model.EnergizerB2BCustomerModel;

/**
 * @author Ravikiran Pise
 * @author m9005673/selvaraja savarimuthu
 */
public class EnergizerUserManagementSendMailJob extends AbstractJobPerformable<CronJobModel>
{
	Logger LOG = Logger.getLogger(EnergizerUserManagementSendMailJob.class);

	@Resource(name = "customerAccountService")
	private CustomerAccountService customerAccountService;
	private final static String DEFAULT_PASSWORD = "energizer.default.password";
	@Resource(name = "userService")
	private UserService userService;
	private final StringBuilder message = new StringBuilder();
	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bCommerceUserService;
	@Resource
	I18NService i18nService;
	@Resource(name = "b2bCommerceUnitService")
	private B2BCommerceUnitService b2BCommerceUnitService;
	public static final String EMAIL_REPLY_TO = "mail.smtp.user";
	@SuppressWarnings("rawtypes")
	@Resource(name = "defaultB2BUnitService")
	private B2BUnitService defaultB2BUnitService;

	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;
	@Resource(name = "b2BCustomerConverter")
	private Converter<B2BCustomerModel, CustomerData> b2BCustomerConverter;
	@Resource(name = "baseMessageSource")
	private ReloadableResourceBundleMessageSource messageSource;
	@Resource
	EmailService emailService;
	@Resource
	CompanyB2BCommerceFacade b2bCommerceFacade;
	private String subject;

	/*
	 * @Resource B2BCommerceUnitFacade b2bCommerceUnitFacade;
	 */

	@Resource(name = "b2bUnitFacade")
	B2BUnitFacade b2bCommerceUnitFacade;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */
	@Override
	@SuppressWarnings("unchecked")
	public PerformResult perform(final CronJobModel cronJobModel)
	{
		PerformResult result = null;
		final Set<EnergizerB2BCustomerModel> b2bCustomerModels = defaultB2BUnitService.getAllUserGroupMembersForType(
				userService.getUserGroupForUID(B2BConstants.B2BADMINGROUP), EnergizerB2BCustomerModel.class);
		try
		{
			final String password = Config.getParameter(DEFAULT_PASSWORD);
			for (final EnergizerB2BCustomerModel customerModel : b2bCustomerModels)
			{
				if (null == customerModel.getRegistrationEmailFlag())
				{
					b2BCustomerConverter.convert(customerModel);
					customerModel.setRegistrationEmailFlag(Boolean.TRUE);
					customerAccountService.register(customerModel, password);
				}
			}
			/*
			 * final List<String> list = new ArrayList<String>();
			 * list.add(Config.getParameter("energizer.default.admin.emailid"));
			 * list.add(Config.getParameter("energizer.default.helpdesk.emailid")); sendEmail(list);
			 */
			result = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}
		catch (final Exception execption)
		{
			result = new PerformResult(CronJobResult.ERROR, CronJobStatus.UNKNOWN);
			LOG.error("Error : " + execption.getCause());
		}
		return result;
	}

	private void createEmailBody()
	{
		final Locale locale = i18nService.getCurrentLocale();
		setSubject("Error: ");
		message.append(messageSource.getMessage("text.error.message.email.template.section7", new Object[]
		{ getSubject() }, locale));
		final String actionmsg = messageSource.getMessage("text.error.message.email.template.section10", null, locale);
		int lineNumber = 0;
		final List<String> unitList = getAllUnitsOfOrganization();
		for (final String unitName : unitList)
		{
			lineNumber++;
			message.append(messageSource.getMessage("text.error.message.email.template.section8", new Object[]
			{ lineNumber, unitName, actionmsg }, locale));
		}
		message.append(messageSource.getMessage("text.error.message.email.template.section9", null, locale));
		message.append("\n");
	}

	private void sendEmail(final List<String> toAddresses)
	{
		try
		{
			createEmailBody();
			EmailMessageModel emailMessageModel = null;
			EmailAddressModel emailAddress = null;
			final List<EmailAddressModel> toAddressModels = new ArrayList<EmailAddressModel>();
			for (final String toAddress : toAddresses)
			{
				emailAddress = emailService.getOrCreateEmailAddressForEmail(toAddress, "Error Message");
				toAddressModels.add(emailAddress);
			}
			emailMessageModel = emailService.createEmailMessage(toAddressModels, null, null, emailAddress,
					Config.getParameter(EMAIL_REPLY_TO), getSubject(), message.toString(), null);
			emailService.send(emailMessageModel);
			message.setLength(0);
		}
		catch (final Exception e)
		{
			LOG.error("Exception in Mail Errors", e);
		}
	}

	private List<String> getAllUnitsOfOrganization()
	{
		final List<String> getAllActiveUnits = b2bCommerceUnitFacade.getAllActiveUnitsOfOrganization();
		final List<String> details = new ArrayList<String>();
		for (final String unitName : getAllActiveUnits)
		{
			final B2BUnitData unit = b2bCommerceFacade.getUnitForUid(unitName);
			final Collection<CustomerData> collections = unit.getAdministrators();
			if (collections == null)
			{
				details.add(unit.getUid());
			}
		}
		return details;
	}

	/**
	 * @return the subject
	 */
	public String getSubject()
	{
		return subject;
	}

	/**
	 * @param subject
	 *           the subject to set
	 */
	public void setSubject(final String subject)
	{
		this.subject = subject;
	}

}
