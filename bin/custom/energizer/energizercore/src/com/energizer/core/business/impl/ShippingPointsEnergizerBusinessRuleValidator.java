/**
 * 
 */
package com.energizer.core.business.impl;

import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.util.localization.Localization;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.energizer.business.BusinessRuleError;


/**
 * @author M9005672
 * 
 */
@Component("shippingPointsEnergizerBusinessRuleValidator")
public class ShippingPointsEnergizerBusinessRuleValidator extends AbstractEnergizerOrderEntryBusinessRulesValidator
{
	private static final String SHIPPING_POINT_MISMATCH = "shippingpoint.business.rule.shipfromnotvalid";
	private static final String SHIPPING_POINT_NOT_FOUND = "shippingpoint.business.rule.shipfromnotfound";

	private static final Logger LOG = Logger.getLogger(ShippingPointsEnergizerBusinessRuleValidator.class.getName());

	@Override
	public void validate(final OrderEntryData orderEntryData)
	{
		LOG.info("Starting the shippingPoint validation process...!!! ");

		final String shippingPointNo = orderEntryData.getShippingPoint();
		final String referenceShippingPoint = orderEntryData.getReferenceShippingPoint();
		final String productCode = orderEntryData.getProduct().getCode();

		LOG.info("The shippingPointNo of product : " + productCode + "is" + shippingPointNo);
		LOG.info("The shippingPointNo of the product in the cart: " + referenceShippingPoint);

		if (hasErrors())
		{
			errors.clear();
		}
		if (shippingPointNo == null)
		{
			LOG.info("The shipping point of this product is not found!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(SHIPPING_POINT_NOT_FOUND, new Object[]
			{ productCode }));
			addError(error);
		}

		if (referenceShippingPoint != null && !referenceShippingPoint.isEmpty()
				&& !shippingPointNo.equalsIgnoreCase(referenceShippingPoint))
		{
			LOG.info("The shipping point of this product must be similar to the product(s) in Cart!!!");
			final BusinessRuleError error = new BusinessRuleError();
			error.setMessage(Localization.getLocalizedString(SHIPPING_POINT_MISMATCH, new Object[]
			{ productCode }));
			addError(error);
		}
	}
}
