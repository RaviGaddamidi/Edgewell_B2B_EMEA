/**
 * 
 */
package com.energizer.facades.search.populators;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.search.resultdata.SearchResultValueData;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.product.ProductService;
import de.hybris.platform.servicelayer.user.UserService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.facades.product.populators.EnergizerSearchResultProductPopulator;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Bivash Pandit
 * 
 */
public class EnergizerProductSearchResultListPopulator extends EnergizerSearchResultProductPopulator
{
	@Resource
	ProductService productService;
	@Resource
	B2BCommerceUserService b2bCommerceUserService;
	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;
	@Resource
	PriceService priceService;
	@Resource
	EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	private B2BUnitModel loggedInUserB2bUnit;
	private static int ZERO = 0;
	private static String EMPTY = "";

	private static final Logger LOG = Logger.getLogger(EnergizerProductSearchResultListPopulator.class.getName());


	@Override
	public void populate(final SearchResultValueData source, final ProductData productData)
	{
		super.populate(source, productData);
		int baseUOM = 1;
		setLoggedInUserB2bUnit();
		final String productCode = source.getValues().get("code").toString();
		final EnergizerProductModel energizerProductModel = (EnergizerProductModel) productService.getProductForCode(productCode);
		productData.setObsolete((energizerProductModel.getObsolete() == null ? false : energizerProductModel.getObsolete()));
		productData.setDescription(energizerProductModel.getDescription() == null ? EMPTY : energizerProductModel.getDescription());
		productData.setErpMaterialID(energizerProductModel.getCode() == null ? EMPTY : energizerProductModel.getCode());

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(productCode, b2bUnit.getUid());

		productData.setCustomerMaterialId(energizerCMIRModel.getCustomerMaterialId() == null ? EMPTY : energizerCMIRModel
				.getCustomerMaterialId());
		productData.setCustomerProductName(energizerCMIRModel.getCustomerMaterialDescription() == null ? EMPTY : energizerCMIRModel
				.getCustomerMaterialDescription());
		productData.setMoq(energizerCMIRModel.getOrderingUnit() == null ? ZERO : energizerCMIRModel.getOrderingUnit());
		productData.setUom(energizerCMIRModel.getUom() == null ? EMPTY : energizerCMIRModel.getUom());
		productData.setShippingPoint(energizerCMIRModel.getShippingPoint());

		final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
				.getEnergizerProductConversion(productCode, b2bUnit.getUid());

		if (energizerProductConversionFactorModel != null)
		{
			final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM(); // EACH / CASE/ PALLET / LAYER

			if (null != alternateUOM && null != productData.getUom() && alternateUOM.equalsIgnoreCase(productData.getUom()))
			{
				baseUOM = (energizerProductConversionFactorModel.getConversionMultiplier()).intValue();
			}
		}

		final Collection<PriceRowModel> rowPrices = energizerProductModel.getEurope1Prices();
		boolean foundCmirPrice = false;
		for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
		{
			final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
			if (priceRowModel instanceof EnergizerPriceRowModel)
			{
				final EnergizerPriceRowModel energizerPriceRowModel = (EnergizerPriceRowModel) priceRowModel;
				if (null != energizerPriceRowModel.getB2bUnit() && null != loggedInUserB2bUnit
						&& energizerPriceRowModel.getB2bUnit().getUid().equalsIgnoreCase(loggedInUserB2bUnit.getUid()))
				{
					if (energizerPriceRowModel.getPrice() == null || energizerPriceRowModel.getPrice().doubleValue() == 0.0)
					{
						foundCmirPrice = false;
					}
					else
					{
						productData.setCustomerProductPrice(energizerPriceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO)
								: BigDecimal.valueOf(energizerPriceRowModel.getPrice() * baseUOM));
						productData.setCustomerPriceCurrency(energizerPriceRowModel.getCurrency().getSymbol() == null ? EMPTY
								: energizerPriceRowModel.getCurrency().getSymbol());
						foundCmirPrice = true;
						break;
					}

				}
			}
		}
		if (!foundCmirPrice)
		{
			for (final Iterator iterator = rowPrices.iterator(); iterator.hasNext();)
			{
				final PriceRowModel priceRowModel = (PriceRowModel) iterator.next();
				if (priceRowModel instanceof EnergizerPriceRowModel)
				{
					continue;
				}
				productData.setCustomerProductPrice(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO) : BigDecimal
						.valueOf(priceRowModel.getPrice() * baseUOM));
				productData.setCustomerPriceCurrency(priceRowModel.getCurrency().getSymbol() == null ? EMPTY : priceRowModel
						.getCurrency().getSymbol());
			}
		}
	}

	/**
	 * @return the loggedInUserB2bUnit
	 */
	public B2BUnitModel getLoggedInUserB2bUnit()
	{
		return loggedInUserB2bUnit;
	}

	/**
	 * the loggedInUserB2bUnit to set
	 */
	public void setLoggedInUserB2bUnit()
	{
		final String currentUserId = customerFacade.getCurrentCustomer().getUid();
		final B2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(currentUserId);


		this.loggedInUserB2bUnit = b2bUnit;
	}


}
