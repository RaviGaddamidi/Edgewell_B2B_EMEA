/**
 * 
 */
package com.energizer.facades.product.populators;

import de.hybris.platform.b2b.model.B2BUnitModel;
import de.hybris.platform.b2b.services.B2BCustomerService;
import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.user.UserService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.MetricUnitModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Bivash Pandit
 * 
 */
public class EnergizerProductPopulator implements Populator<EnergizerProductModel, ProductData>
{
	@Resource
	B2BCommerceUserService b2bCommerceUserService;
	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;
	@Resource
	PriceService priceService;
	@Resource
	B2BCustomerService b2bCustomerService;
	@Resource
	EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;


	private B2BUnitModel loggedInUserB2bUnit;

	private static int ZERO = 0;
	private static String EMPTY = "";


	@Override
	public void populate(final EnergizerProductModel energizerProductModel, final ProductData productData)
			throws ConversionException
	{
		int baseUOM = 1;

		final String currentUserId = customerFacade.getCurrentCustomer().getUid();
		if (currentUserId.equals("anonymous"))
		{
			loggedInUserB2bUnit = null;
		}
		else
		{
			loggedInUserB2bUnit = b2bCommerceUserService.getParentUnitForCustomer(currentUserId);
		}
		productData.setObsolete((energizerProductModel.getObsolete() == null ? false : energizerProductModel.getObsolete()));
		productData.setDescription(energizerProductModel.getDescription() == null ? EMPTY : energizerProductModel.getDescription());
		productData.setErpMaterialID(energizerProductModel.getCode() == null ? EMPTY : energizerProductModel.getCode());


		//Setting the segment,family , group
		productData.setSegmentName(energizerProductModel.getSegmentCode() == null ? EMPTY : energizerProductModel.getSegmentCode());
		productData.setFamilyName(energizerProductModel.getFamilyCode() == null ? EMPTY : energizerProductModel.getFamilyCode());
		productData.setGroupName(energizerProductModel.getGroupCode() == null ? EMPTY : energizerProductModel.getGroupCode());

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerCMIRModel energizerCMIRModel;
		final EnergizerProductConversionFactorModel energizerProductConversionFactorModel;
		if (loggedInUserB2bUnit != null)
		{
			energizerCMIRModel = energizerProductService.getEnergizerCMIR(energizerProductModel.getCode(),
					loggedInUserB2bUnit.getUid());
			energizerProductConversionFactorModel = energizerProductService.getEnergizerProductConversion(
					energizerProductModel.getCode(), loggedInUserB2bUnit.getUid());
			productData.setCustomerMaterialId(energizerCMIRModel.getCustomerMaterialId() == null ? EMPTY : energizerCMIRModel
					.getCustomerMaterialId());
			productData.setCustomerProductName(energizerCMIRModel.getCustomerMaterialDescription() == null ? EMPTY
					: energizerCMIRModel.getCustomerMaterialDescription());
			productData.setMoq(energizerCMIRModel.getOrderingUnit() == null ? ZERO : energizerCMIRModel.getOrderingUnit());
			productData.setUom(energizerCMIRModel.getUom() == null ? EMPTY : energizerCMIRModel.getUom());

			final String shippingPointId = energizerCMIRModel.getShippingPoint();
			final String shippingPointName = energizerProductService.getShippingPointName(shippingPointId);
			productData.setShippingPoint(shippingPointId);
			productData.setShippingPointName(shippingPointName == null ? EMPTY : shippingPointName);

			if (energizerProductConversionFactorModel != null)
			{
				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM(); // EACH / CASE/ PALLET / LAYER

				if (null != alternateUOM && null != productData.getUom() && alternateUOM.equalsIgnoreCase(productData.getUom()))
				{
					baseUOM = (energizerProductConversionFactorModel.getConversionMultiplier()).intValue();
					final MetricUnitModel volumeMetricUnit = energizerProductConversionFactorModel.getPackageVolume();
					if (null != volumeMetricUnit)
					{
						productData.setVolume(volumeMetricUnit.getMeasurement() == null ? ZERO : volumeMetricUnit.getMeasurement());
						productData.setVolumeUom(volumeMetricUnit.getMeasuringUnits() == null ? EMPTY : volumeMetricUnit
								.getMeasuringUnits());
					}
					final MetricUnitModel weightMetricUnit = energizerProductConversionFactorModel.getPackageWeight();
					if (null != weightMetricUnit)
					{
						productData.setWeight(weightMetricUnit.getMeasurement() == null ? ZERO : weightMetricUnit.getMeasurement());
						productData.setWeightUom(weightMetricUnit.getMeasuringUnits() == null ? EMPTY : weightMetricUnit
								.getMeasuringUnits());

					}
				}
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
								: BigDecimal.valueOf(energizerPriceRowModel.getPrice() * baseUOM).setScale(2, RoundingMode.CEILING));
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
				productData.setCustomerProductPrice(priceRowModel.getPrice() == null ? BigDecimal.valueOf(ZERO) : BigDecimal.valueOf(
						priceRowModel.getPrice() * baseUOM).setScale(2, RoundingMode.CEILING));
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
