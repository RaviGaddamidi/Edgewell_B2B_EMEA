/**
 *
 */
package com.energizer.services.order;

import de.hybris.platform.commercefacades.order.data.CartData;

import java.util.HashMap;
import java.util.List;


/**
 * @author Bivash Pandit
 * 
 */
public interface EnergizerCartService
{

	public CartData calCartContainerUtilization(CartData cartData, String containerHeight, String packingOption,
			boolean enableButton);

	public List<String> messages();

	public HashMap productNotAddedToCart();

	public HashMap productsNotDoublestacked123();


}
