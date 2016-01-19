/**
 *
 */
package com.energizer.services.order;

import de.hybris.platform.commercefacades.order.data.CartData;

import java.util.HashMap;
import java.util.List;

import com.energizer.core.util.EnergizerProductPalletHeight;


/**
 * @author Bivash Pandit
 *
 */
public interface EnergizerCartService
{

	public CartData calCartContainerUtilization(CartData cartData, String containerHeight, String packingOption);

	public List<String> messages();

	public HashMap productNotAddedToCart();

	public HashMap productsNotDoublestacked123();
}
