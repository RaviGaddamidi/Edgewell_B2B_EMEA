/**
 * 
 */
package com.energizer.core.datafeed.service;



import java.util.List;

import com.energizer.core.model.EnergizerB2BUnitLeadTimeModel;


/**
 * @author M1023278
 * 
 */
public interface EnergizerCustomerLeadTimeService
{
	public List<EnergizerB2BUnitLeadTimeModel> fecthEnergizerB2BUnitLeadTime(final String pk_EnergizerB2BUnit,
			final String shippingPointNo, final String shipTo);
}
