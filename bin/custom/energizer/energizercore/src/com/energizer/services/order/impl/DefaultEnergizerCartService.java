/**
 * 
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.util.ContainerData;
import com.energizer.core.util.EnergizerWeightOrVolumeConverter;
import com.energizer.services.order.EnergizerCartService;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author Bivash Pandit
 * 
 */
public class DefaultEnergizerCartService implements EnergizerCartService
{
	@Resource
	CartService cartService;
	@Resource
	ConfigurationService configurationService;

	@Resource(name = "energizerProductService")
	EnergizerProductService energizerProductService;

	@Resource
	private UserService userService;

	@Resource
	private B2BCommerceUserService b2bCommerceUserService;


	private double twentyFeetContainerVolume;
	private double twentyFeetContainerWeight;
	private double fourtyFeetContainerVolume;
	private double fourtyFeetContainerWeight;
	private final double hundred = 100;

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCartService.class.getName());

	private static final String TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume";
	private static final String TWENTY_FEET_CONTAINER_WEIGHT_KEY = "twenty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume";
	private static final String FORTY_FEET_CONTAINER_WEIGHT_KEY = "fourty.feet.container.weight";

	@Override
	public CartData calCartContainerUtilization(final CartData cartData)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();
			double totalCartVolume = 0;
			double totalCartWt = 0;
			/**
			 * # volume in M3 and weight in KG *******************************************
			 * twenty.feet.container.volume=11.32672, twenty.feet.container.weight=10000
			 * fourty.feet.container.volume=22.65344 , fourty.feet.container.weight=20000
			 */

			twentyFeetContainerVolume = configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_VOLUME_KEY, null);
			twentyFeetContainerWeight = configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_WEIGHT_KEY, null);
			fourtyFeetContainerVolume = configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_VOLUME_KEY, null);
			fourtyFeetContainerWeight = configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_WEIGHT_KEY, null);

			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = enerGizerProductModel.getCode();
				final long itemQuantity = abstractOrderEntryModel.getQuantity().longValue();
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				double baseUom = 0;

				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());

					cmirUom = energizerCMIRModel.getUom();
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
						baseUom = coversionFactor.getConversionMultiplier();
					}

				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
				}


				if (null != coversionFactor && null != coversionFactor.getPackageVolume())
				{
					double lineItemTotvolume = 0;
					final double unitVolume = coversionFactor.getPackageVolume().getMeasurement();
					if (unitVolume != 0)
					{
						lineItemTotvolume = unitVolume * baseUom * itemQuantity;
					}
					final String volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
					totalCartVolume = totalCartVolume
							+ EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit, lineItemTotvolume);
				}

				if (null != coversionFactor && null != coversionFactor.getPackageWeight())
				{
					double lineItemTotWt = 0;
					final double unitWeight = coversionFactor.getPackageWeight().getMeasurement();
					if (unitWeight != 0)
					{
						lineItemTotWt = unitWeight * baseUom * itemQuantity;
					}
					final String weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
					totalCartWt = totalCartWt + EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWt);
				}

			}

			final ContainerData containerData = getPercentageContainerUtil(totalCartWt, totalCartVolume);
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			if ((cartData.getTotalProductVolumeInPercent().doubleValue() > hundred)
					|| (cartData.getTotalProductWeightInPercent().doubleValue() > hundred))
			{
				cartData.setIsContainerFull(true);
			}
			else
			{
				cartData.setIsContainerFull(false);
			}

		}
		catch (final Exception exception)
		{
			LOG.error("Error in calCartContainerUtilization", exception);
		}
		return cartData;
	}

	/**
	 * 
	 * @param totalCartWt
	 * @param totalCartVolume
	 * @return
	 */
	private ContainerData getPercentageContainerUtil(final double totalCartWt, final double totalCartVolume)
	{
		final ContainerData containerData = new ContainerData();
		if (totalCartWt < twentyFeetContainerWeight && totalCartVolume < twentyFeetContainerVolume)
		{
			containerData.setContainerType("twentyFeetContainer");
			final double volumePercentage = (totalCartVolume * hundred) / twentyFeetContainerVolume;
			final double weightPercentage = (totalCartWt * hundred) / twentyFeetContainerWeight;
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(Math.round(volumePercentage));
			containerData.setPercentWeightUses(Math.round(weightPercentage));
		}
		else
		{
			containerData.setContainerType("fourtyFeetContainer");
			final double volumePercentage = (totalCartVolume * hundred) / fourtyFeetContainerVolume;
			final double weightPercentage = (totalCartWt * hundred) / fourtyFeetContainerWeight;
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(Math.round(volumePercentage));
			containerData.setPercentWeightUses(Math.round(weightPercentage));
		}
		return containerData;
	}

}
