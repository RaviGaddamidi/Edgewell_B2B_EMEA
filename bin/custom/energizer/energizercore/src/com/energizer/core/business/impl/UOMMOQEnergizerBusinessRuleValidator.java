/**
 * 
 */
package com.energizer.core.business.impl;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.util.localization.Localization;

import javax.annotation.Resource;

import com.energizer.business.BusinessRuleError;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author kaushik.ganguly
 * 
 */
public class UOMMOQEnergizerBusinessRuleValidator extends AbstractEnergizerOrderEntryBusinessRulesValidator
{
	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;

	@Resource(name = "energizerSolrQueryManipulationService")
	private EnergizerSolrQueryManipulationService energizerSolrQueryManipulationService;

	private static final String PRODUCT_NOT_FOUND = "uom.business.rule.productnotfound";
	private static final String UOM_NOT_VALID = "uom.business.rule.uomnotvalid";
	private static final String MOQ_NOT_VALID = "uom.business.rule.moqnotvalid";
	private static final String MOQ_NOT_EXIST = "basket.page.moq.notexists";

	@Override
	public void validate(final OrderEntryData orderEntryData)
	{
		//final List<BusinessRuleError> orderEntryDataErrorList = new ArrayList<BusinessRuleError>();
		String productCode = null;
		EnergizerCMIRModel cmir = null;
		final boolean isCustomerMaterialId = false;
		if (hasErrors())
		{
			errors.clear();
		}
		if (orderEntryData.getProduct().getCode() == null && orderEntryData.getProduct().getCustomerMaterialId() != null)
		{
			productCode = orderEntryData.getProduct().getCustomerMaterialId();
		}
		else if (orderEntryData.getProduct().getCode() != null
				&& (orderEntryData.getProduct().getCustomerMaterialId() == null || orderEntryData.getProduct()
						.getCustomerMaterialId().isEmpty()))
		{
			productCode = orderEntryData.getProduct().getCode();
		}
		else if (orderEntryData.getProduct().getCode() != null && orderEntryData.getProduct().getCustomerMaterialId() != null)
		{
			productCode = orderEntryData.getProduct().getCode();
		}

		if (productCode != null)
		{
			if (isCustomerMaterialId)
			{
				cmir = energizerProductService.getEnergizerCMIRforCustomerMaterialID(productCode,
						energizerSolrQueryManipulationService.getB2BUnitForLoggedInUser().getUid());
			}
			else
			{
				cmir = energizerProductService.getEnergizerCMIR(productCode, energizerSolrQueryManipulationService
						.getB2BUnitForLoggedInUser().getUid());
			}

			if (cmir == null)
			{
				final BusinessRuleError error = new BusinessRuleError();
				error.setMessage(productCode + " " + Localization.getLocalizedString(PRODUCT_NOT_FOUND));
				addError(error);
			}
			else
			{
				if (orderEntryData.getProduct().getUom() == null || !orderEntryData.getProduct().getUom().equals(cmir.getUom()))
				{
					final BusinessRuleError error = new BusinessRuleError();
					error.setMessage(productCode + " " + orderEntryData.getProduct().getUom() + " "
							+ Localization.getLocalizedString(UOM_NOT_VALID));
					addError(error);
				}

				if (cmir.getOrderingUnit() == 0)
				{
					final BusinessRuleError error = new BusinessRuleError();
					error.setMessage(productCode + " " + Localization.getLocalizedString(MOQ_NOT_EXIST));
					addError(error);

				}
				else if (orderEntryData.getQuantity() == null
						|| orderEntryData.getQuantity().intValue() % cmir.getOrderingUnit() != 0)
				{
					final BusinessRuleError error = new BusinessRuleError();
					error.setMessage(productCode + " " + Localization.getLocalizedString(MOQ_NOT_VALID) + cmir.getOrderingUnit() + " "
							+ cmir.getUom());
					addError(error);
				}

			}

		}
		else
		{
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(productCode + " " + Localization.getLocalizedString(PRODUCT_NOT_FOUND));
			addError(error);

		}
	}
}
