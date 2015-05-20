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

import java.math.BigDecimal;
import java.math.RoundingMode;
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


	private BigDecimal twentyFeetContainerVolume;
	private BigDecimal twentyFeetContainerWeight;
	private BigDecimal fourtyFeetContainerVolume;
	private BigDecimal fourtyFeetContainerWeight;
	private final BigDecimal hundred = new BigDecimal(100);

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCartService.class.getName());

	private static final String TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume";
	private static final String TWENTY_FEET_CONTAINER_WEIGHT_KEY = "twenty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume";
	private static final String FORTY_FEET_CONTAINER_WEIGHT_KEY = "fourty.feet.container.weight";

	private final BigDecimal ZERO = new BigDecimal(0);

	@Override
	public CartData calCartContainerUtilization(final CartData cartData)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();
			BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);
			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 * 
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248
			 * fourty.feet.container.volume=70.62209 fourty.feet.container.weight=18234.3948
			 */

			twentyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
					TWENTY_FEET_CONTAINER_VOLUME_KEY, null));
			twentyFeetContainerWeight = new BigDecimal(configurationService.getConfiguration().getDouble(
					TWENTY_FEET_CONTAINER_WEIGHT_KEY, null));
			fourtyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
					FORTY_FEET_CONTAINER_VOLUME_KEY, null));
			fourtyFeetContainerWeight = new BigDecimal(configurationService.getConfiguration().getDouble(
					FORTY_FEET_CONTAINER_WEIGHT_KEY, null));

			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = enerGizerProductModel.getCode();
				final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				BigDecimal baseUom = new BigDecimal(0);

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
						baseUom = new BigDecimal((coversionFactor.getConversionMultiplier()));
					}

					//  ************ setting the OrderBlock  flag  ****************
					if (b2bUnit.getOrderBlock())
					{
						cartData.setIsOrderBlocked(true);
					}
					else
					{
						cartData.setIsOrderBlocked(false);
					}
				}
				catch (final Exception e)
				{
					LOG.error("error in retreiving the cmir!");
				}


				if (null != coversionFactor && null != coversionFactor.getPackageVolume())
				{
					BigDecimal lineItemTotvolume = new BigDecimal(0);
					final BigDecimal unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
					if (unitVolume.compareTo(ZERO) != 0)
					{
						lineItemTotvolume = unitVolume.multiply(baseUom).multiply(itemQuantity);
					}
					final String volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
					totalCartVolume = totalCartVolume.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit,
							lineItemTotvolume));
				}

				if (null != coversionFactor && null != coversionFactor.getPackageWeight())
				{
					BigDecimal lineItemTotWt = new BigDecimal(0);
					final BigDecimal unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
					if (unitWeight.compareTo(ZERO) != 0)
					{
						lineItemTotWt = unitWeight.multiply(baseUom).multiply(itemQuantity);
					}
					final String weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
					totalCartWt = totalCartWt.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWt));
				}

			}

			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);

			final ContainerData containerData = getPercentageContainerUtil(totalCartWt, totalCartVolume);
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			if ((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
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
	private ContainerData getPercentageContainerUtil(BigDecimal totalCartWt, BigDecimal totalCartVolume)
	{

		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(2, BigDecimal.ROUND_UP);
		twentyFeetContainerVolume = twentyFeetContainerVolume.setScale(2, BigDecimal.ROUND_UP);
		twentyFeetContainerWeight = twentyFeetContainerVolume.setScale(2, BigDecimal.ROUND_UP);
		fourtyFeetContainerVolume = fourtyFeetContainerVolume.setScale(2, BigDecimal.ROUND_UP);
		fourtyFeetContainerWeight = fourtyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);


		final ContainerData containerData = new ContainerData();
		if (totalCartWt.compareTo(twentyFeetContainerWeight) == -1 && totalCartVolume.compareTo(twentyFeetContainerVolume) == -1)
		{
			containerData.setContainerType("twentyFeetContainer");
			final double volumePercentage = (totalCartVolume.multiply(hundred)).divide(twentyFeetContainerVolume, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			final double weightPercentage = (totalCartWt.multiply(hundred)).divide(twentyFeetContainerWeight, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(volumePercentage);
			containerData.setPercentWeightUses(weightPercentage);
		}
		else
		{
			containerData.setContainerType("fourtyFeetContainer");
			final double volumePercentage = (totalCartVolume.multiply(hundred)).divide(fourtyFeetContainerVolume, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			final double weightPercentage = (totalCartWt.multiply(hundred)).divide(fourtyFeetContainerWeight, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(volumePercentage);
			containerData.setPercentWeightUses(weightPercentage);
		}
		return containerData;
	}

}
