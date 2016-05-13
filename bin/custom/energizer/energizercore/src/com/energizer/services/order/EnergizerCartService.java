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

	public List<String> getMessages();

	public HashMap getProductNotAddedToCart();

	public HashMap getProductsNotDoublestacked();

	public HashMap getFloorSpaceProductsMap();

	public HashMap getNonPalletFloorSpaceProductsMap();
}
