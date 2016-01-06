/**
 * 
 */
package com.energizer.services.order;

import de.hybris.platform.commercefacades.order.data.CartData;

import java.util.List;


/**
 * @author Bivash Pandit
 * 
 */
public interface EnergizerCartService
{

		public CartData calCartContainerUtilization(CartData cartData, String containerHeight, String packingOption);
		public List<String> messages();
	    public List<String> productNotAddedToCart();

}
