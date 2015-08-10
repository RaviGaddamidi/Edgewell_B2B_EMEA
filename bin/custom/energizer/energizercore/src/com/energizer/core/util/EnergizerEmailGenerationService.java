

/**
 * 
 */
package com.energizer.core.util;

import de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.b2b.model.B2BPermissionResultModel;
import de.hybris.platform.b2b.model.B2BUserGroupModel;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUnitService;
import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;




/**
 * @author m1023278
 * 
 */
public class EnergizerEmailGenerationService extends DefaultEmailGenerationService
{
	private static final Logger LOG = Logger.getLogger(EnergizerEmailGenerationService.class);

	private String salesPersonEmailId;

	private String displayName;

	B2BUserGroupModel b2bUserGroupModel;

	private List<B2BCustomerModel> b2bCustomerModelList;
	private Set<B2BCustomerModel> b2bCustomerModels;
	private B2BCustomerModel orderApprover;

	private List<String> emailList;

	private Set<String> emailSet;

	private List<String> emailCCList;
	@Resource
	ModelService modelService;

	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	private B2BCommerceUnitService b2bCommerceUnitService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.acceleratorservices.email.impl.DefaultEmailGenerationService#generate(de.hybris.platform.
	 * processengine.model.BusinessProcessModel, de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel)
	 */
	@Override
	public EmailMessageModel generate(final BusinessProcessModel businessProcessModel, final EmailPageModel emailPageModel)
			throws RuntimeException
	{

		if (businessProcessModel instanceof OrderProcessModel)
		{
			final OrderModel order = ((OrderProcessModel) businessProcessModel).getOrder();
			final String salesPersonEmailId = order.getDeliveryAddress().getSalesPersonEmailId();
			final String displayName = order.getDeliveryAddress().getDisplayName();
			setSalesPersonEmailId(salesPersonEmailId);
			setDisplayName(displayName);
			emailSet = new HashSet<String>();
			emailList = new ArrayList<String>();
			emailCCList = new ArrayList<String>();
			b2bCustomerModels = new HashSet<B2BCustomerModel>();
			b2bCustomerModels = order.getB2bUnit().getApprovers();


			final List<B2BPermissionResultModel> b2bPermissionResultModels = (List<B2BPermissionResultModel>) order
					.getPermissionResults();
			for (final B2BPermissionResultModel b2bPermissionResultModel : b2bPermissionResultModels)
			{

				emailSet.add(b2bPermissionResultModel.getApprover().getEmail());

				if (emailSet.size() > 0)
				{


					emailSet.add(b2bPermissionResultModel.getApprover().getEmail());


				}
				else
				{
					emailSet.add(b2bPermissionResultModel.getApprover().getEmail());
				}


			}
			emailList.addAll(emailSet);
			b2bCustomerModelList = new ArrayList<B2BCustomerModel>();
			orderApprover = ((OrderProcessModel) businessProcessModel).getOrder().getOrderApprover();
			b2bCustomerModelList.addAll(b2bCustomerModels);
			if (null != b2bCustomerModelList && b2bCustomerModelList.size() > 0)
			{
				for (final B2BCustomerModel customer : b2bCustomerModelList)
				{
					emailCCList.add(customer.getEmail());
				}
			}

		}
		return super.generate(businessProcessModel, emailPageModel);
	}

	@Override
	protected EmailMessageModel createEmailMessage(final String emailSubject, final String emailBody,
			final AbstractEmailContext<BusinessProcessModel> emailContext)
	{
		EmailMessageModel emailMessageModel;
		if (emailSubject.indexOf("approved") != -1)
		{
			final EmailAddressModel ccAddress = getEmailService().getOrCreateEmailAddressForEmail(getSalesPersonEmailId(),
					getSalesPersonEmailId());
			ccAddress.setEmailAddress(getSalesPersonEmailId());
			ccAddress.setDisplayName(getDisplayName());
			emailMessageModel = super.createEmailMessage(emailSubject, emailBody, emailContext);
			final List<EmailAddressModel> ccEmails = new ArrayList<EmailAddressModel>();
			if (null != ccAddress)
			{
				ccEmails.add(ccAddress);
			}
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getToEmail(),
					emailContext.getToDisplayName());
			toEmails.add(toAddress);
			final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
					emailContext.getFromDisplayName());
			return getEmailService().createEmailMessage(toEmails, ccEmails, new ArrayList<EmailAddressModel>(), fromAddress,
					emailContext.getFromEmail(), emailSubject, emailBody, null);

		}//order pending approval email : add List of reviewers in cc address field
		else if (emailSubject.indexOf("Energizer Reference Number Pending Approval") != -1)
		{
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			if (null != emailList && emailList.size() > 0)
			{
				// get the list of approver emails and search for the same in the EmailAddress table by flexiblesearchquery, 
				//if emailAddress is found then that is the cc address and set the email id
				//if no emailaddress create the new EmailAddressModel and make it as cc address 
				//add the cc address to list of EmailAddressModel
				for (int i = 0; i < emailList.size(); i++)
				{
					final String emailAddress = emailList.get(i);
					final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(emailAddress, emailAddress);
					toAddress.setEmailAddress(emailAddress);
					toAddress.setDisplayName(getDisplayName());

					if (null != toAddress)
					{
						toEmails.add(toAddress);
					}
				}//end of for loop
				 //emailList = null;
				final List<EmailAddressModel> ccEmails = new ArrayList<EmailAddressModel>();
				if (null != emailCCList && emailCCList.size() > 0)
				{
					for (int i = 0; i < emailCCList.size(); i++)
					{
						final String emailAddress = emailCCList.get(i);
						final EmailAddressModel ccAddress = getEmailService().getOrCreateEmailAddressForEmail(emailAddress,
								emailAddress);
						ccAddress.setEmailAddress(emailAddress);
						ccAddress.setDisplayName(getDisplayName());
						if (null != ccAddress)
						{
							ccEmails.add(ccAddress);
						}
					}
				}
				final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
						emailContext.getFromDisplayName());
				return getEmailService().createEmailMessage(toEmails, ccEmails, new ArrayList<EmailAddressModel>(), fromAddress,
						emailContext.getFromEmail(), emailSubject, emailBody, null);
			}//end of if loop
		}//end of order pending approval email : add List of reviewers in cc address field
		else if (emailSubject.indexOf("Energizer Approval Failed for Reference no") != -1)
		{
			final List<EmailAddressModel> toEmails = new ArrayList<EmailAddressModel>();
			final EmailAddressModel toAddress = getEmailService().getOrCreateEmailAddressForEmail(orderApprover.getEmail(),
					orderApprover.getEmail());
			toEmails.add(toAddress);
			final EmailAddressModel fromAddress = getEmailService().getOrCreateEmailAddressForEmail(emailContext.getFromEmail(),
					emailContext.getFromDisplayName());
			return getEmailService().createEmailMessage(toEmails, new ArrayList<EmailAddressModel>(),
					new ArrayList<EmailAddressModel>(), fromAddress, emailContext.getFromEmail(), emailSubject, emailBody, null);
		}
		else
		{
			return super.createEmailMessage(emailSubject, emailBody, emailContext);
		}
		return super.createEmailMessage(emailSubject, emailBody, emailContext);
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @param displayName
	 *           the displayName to set
	 */
	public void setDisplayName(final String displayName)
	{
		this.displayName = displayName;
	}

	/**
	 * @return the salesPersonEmailId
	 */
	public String getSalesPersonEmailId()
	{
		return salesPersonEmailId;
	}

	/**
	 * @param salesPersonEmailId
	 *           the salesPersonEmailId to set
	 */
	public void setSalesPersonEmailId(final String salesPersonEmailId)
	{
		this.salesPersonEmailId = salesPersonEmailId;
	}
}
