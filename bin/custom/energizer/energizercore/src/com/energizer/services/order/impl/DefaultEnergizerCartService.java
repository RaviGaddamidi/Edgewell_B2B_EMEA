/**
 *
 */
package com.energizer.services.order.impl;

import de.hybris.platform.b2b.company.B2BCommerceUserService;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.util.ContainerData;
import com.energizer.core.util.EnergizerProductPalletHeight;
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
	private Double twentyFeetContainerHeightInInches;
	private Double fortyFeetContainerHeightInInches;
	private final BigDecimal hundred = new BigDecimal(100);

	private BigDecimal disableTwentyFeetContainerVolume;
	private BigDecimal disableFourtyFeetContainerVolume;

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCartService.class.getName());

	private static final String TWENTY_FEET_CONTAINER = "twenty.feet.container";
	private static final String FORTY_FEET_CONTAINER = "forty.feet.container";
	private static final String TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume";
	private static final String TWENTY_FEET_CONTAINER_WEIGHT_KEY = "twenty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume";
	private static final String FORTY_FEET_CONTAINER_WEIGHT_KEY = "fourty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.40FT";
	private static final String TWENTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.20FT";
	private static final String TOTAL_PALLET_COUNT_FOR_2SLIPSHEET = "total.palletcount.2slipsheet";
	private static final String SLOT_PERCENTAGE_40FT = "slot.percentage.40ft";
	private static final String SLOT_PERCENTAGE_20FT = "slot.percentage.20ft";

	private static final String DISABLE_TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume.disable";
	private static final String DISABLE_FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume.disable";

	private final BigDecimal ZERO = new BigDecimal(0);

	boolean nonPalletProductsExists = false;

	int palletCount = 0;
	int virtualPallet = 0;
	int partialPallet = 0;

	List<String> message = null;
	List<EnergizerProductPalletHeight> products = null;
	HashMap doubleStackMap = null;
	LinkedHashMap<Integer, Integer> floorSpaceProductMap = null;
	LinkedHashMap<Integer, Double> nonPalletFloorSpaceProductMap = null;
	ArrayList<EnergizerProductPalletHeight> productsListA = null;
	List<EnergizerProductPalletHeight> nonPalletProductsList = new ArrayList<EnergizerProductPalletHeight>();
	final ArrayList<EnergizerProductPalletHeight> sortedProductsListA = new ArrayList<EnergizerProductPalletHeight>();
	ArrayList<EnergizerProductPalletHeight> productsListB = null;
	LinkedHashMap<Integer, List<String>> palStackData = null;


	@Override
	public CartData calCartContainerUtilization(final CartData cartData, String containerHeight, final String packingOption,
			final boolean enableContOptimization)
	{
		CartData cartDataTemp = null;
		final String packingOptionWithNoAlgorithm = Config.getParameter("energizer.disable.packingOption");
		twentyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
				TWENTY_FEET_CONTAINER_VOLUME_KEY, null));
		twentyFeetContainerWeight = new BigDecimal(configurationService.getConfiguration().getDouble(
				TWENTY_FEET_CONTAINER_WEIGHT_KEY, null));
		fourtyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
				FORTY_FEET_CONTAINER_VOLUME_KEY, null));
		fourtyFeetContainerWeight = new BigDecimal(configurationService.getConfiguration().getDouble(
				FORTY_FEET_CONTAINER_WEIGHT_KEY, null));

		disableTwentyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
				DISABLE_TWENTY_FEET_CONTAINER_VOLUME_KEY, null));
		disableFourtyFeetContainerVolume = new BigDecimal(configurationService.getConfiguration().getDouble(
				DISABLE_FORTY_FEET_CONTAINER_VOLUME_KEY, null));

		twentyFeetContainerVolume = twentyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
		twentyFeetContainerWeight = twentyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);
		fourtyFeetContainerVolume = fourtyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
		fourtyFeetContainerWeight = fourtyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);

		disableTwentyFeetContainerVolume = disableTwentyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);
		disableFourtyFeetContainerVolume = disableFourtyFeetContainerVolume.setScale(6, BigDecimal.ROUND_UP);


		if (!enableContOptimization)
		{
			if (message != null && message.size() > 0)
			{
				message.clear();
			}
			if (products != null && products.size() > 0)
			{
				products.clear();
			}

			containerHeight = null;
			doubleStackMap = new HashMap();
			cartDataTemp = calCartContainerUtilizationWithSlipsheets(cartData, containerHeight);
			cartDataTemp.setContainerPackingType("Not Applicable");

		}
		else if (packingOption.equals(packingOptionWithNoAlgorithm) && enableContOptimization)
		{
			if (message != null && message.size() > 0)
			{
				message.clear();
			}
			if (products != null && products.size() > 0)
			{
				products.clear();
			}
			doubleStackMap = new HashMap();
			cartDataTemp = calCartContainerUtilizationWithEnableSlipsheets(cartData, containerHeight);
			cartDataTemp.setContainerPackingType(packingOptionWithNoAlgorithm);

		}
		else
		{
			cartDataTemp = calCartContainerUtilizationWithSlipSheetsWoodenBase(cartData, containerHeight, packingOption);
			cartDataTemp.setEnableFloorSpaceGraphics(true);
			cartDataTemp.setContainerHeight(containerHeight);
			cartDataTemp.setContainerPackingType(packingOption);
		}

		return cartDataTemp;
	}

	public CartData calCartContainerUtilizationWithSlipSheetsWoodenBase(final CartData cartData, final String containerHeight,
			final String packingOption)
	{
		doubleStackMap = new HashMap();
		floorSpaceProductMap = new LinkedHashMap<Integer, Integer>();
		nonPalletFloorSpaceProductMap = new LinkedHashMap<Integer, Double>();
		palStackData = new LinkedHashMap<Integer, List<String>>();
		double availableVolume = 100;
		double availableWeight = 100;
		double availableHeight = 0;
		double volumeOfNonPalletProduct = 0;
		double weightOfNonPalletProduct = 0;
		double volumeOfAllNonPalletProduct = 0;
		double weightOfAllNonPalletProduct = 0;
		final double halfFilledSlotVolume = 0;
		double nonPalletVolumePercent = 0;
		int floorSpaceCount = 0;
		double volume = 0;
		double weight = 0;
		//int palletCount = 0;
		double totalPalletHeight = 0;
		int totalPalletsCount = 0;
		double productWeight = 0;
		double nonPalletProductWeightPercent = 0;
		double percentVolumePerSlot = 0.0;
		double slotPercentageForNonPallet = 0;
		double percentVolumePerSlotForNonPallet = 0.0;

		int actualPallet = 0;
		int virtualPallet = 0;
		int partialPallet = 0;

		String matrix[][] = null;

		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);

		message = new ArrayList<String>();
		products = new ArrayList<EnergizerProductPalletHeight>();
		productsListA = new ArrayList<EnergizerProductPalletHeight>();
		productsListB = new ArrayList<EnergizerProductPalletHeight>();

		//percentage=configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE, null);


		if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
		{
			totalPalletsCount = 20;
			percentVolumePerSlot = availableVolume * 0.05;
			slotPercentageForNonPallet = configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE_20FT, null);
		}
		else if (containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
		{
			totalPalletsCount = 40;
			percentVolumePerSlot = availableVolume * 0.025;
			slotPercentageForNonPallet = configurationService.getConfiguration().getDouble(SLOT_PERCENTAGE_40FT, null);
		}

		volume = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFALLPRODUCTS"),
				getVolumeOfProductsInCart(cartData, "VOLUMEOFALLPRODUCTS"), containerHeight).getPercentVolumeUses();

		LOG.info("Voume of all products with packing option " + packingOption + ":" + volume);

		weight = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFALLPRODUCTS"),
				getVolumeOfProductsInCart(cartData, "VOLUMEOFALLPRODUCTS"), containerHeight).getPercentWeightUses();

		LOG.info("weight of all products with packing option " + packingOption + ":" + weight);
		cartData.setContainerHeight(containerHeight);

		productsListA = getPalletsCountOfProductsInCart(cartData, productsListA);

		palletCount = productsListA.size();

		for (final Iterator iterator = productsListA.iterator(); iterator.hasNext();)
		{

			final EnergizerProductPalletHeight enrProductPalTemp = (EnergizerProductPalletHeight) iterator.next();

			if (enrProductPalTemp.getCalculatedUOM().equals(enrProductPalTemp.getOrderedUOM())
					&& enrProductPalTemp.getOrderedUOM().equals("PAL"))
			{
				actualPallet++;
			}
			else
			{
				virtualPallet++;
			}

		}

		LOG.info("Pallet count:" + palletCount);

		if (weight > 100 || volume > 100 || palletCount > totalPalletsCount)
		{
			floorSpaceCount = totalPalletsCount / 2;
			//Display an error message "Reduce the products in the cart"
			LOG.info("Either volume is greater than 100 or pallet count is greater than" + totalPalletsCount);

			//cartData.setTotalProductVolumeInPercent(100.00);
			if ((weight > 100 || volume > 100) && palletCount > totalPalletsCount)
			{
				message
						.add("Dear Customer, your order will not fit in one container. You can order maximum "
								+ totalPalletsCount
								+ " PAL in one order with selected container packing material. Please, adjust the cart and/or place multiple orders.");

				availableVolume = -1;
				availableWeight = -1;
				cartData.setIsFloorSpaceFull(true);
				cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
				cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));
			}

			else if (weight > 100 || volume > 100)
			{
				cartData.setTotalProductVolumeInPercent(volume);
				cartData.setTotalProductWeightInPercent(weight);
				cartData.setIsContainerFull(true);
			}

			else if (palletCount > totalPalletsCount)
			{
				message
						.add("Dear Customer, your order will not fit in one container. You can order maximum "
								+ totalPalletsCount
								+ " PAL in one order with selected container packing material. Please, adjust the cart and/or place multiple orders.");

				availableVolume = -1;
				availableWeight = -1;
				cartData.setIsFloorSpaceFull(true);
				cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
				cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));

			}
		}
		else
		{

			Collections.sort(productsListA);

			matrix = new String[2][totalPalletsCount / 2];

			for (floorSpaceCount = 0; floorSpaceCount < totalPalletsCount / 2; floorSpaceCount++)
			{
				LOG.info("***************** The floorSpace Count loop " + floorSpaceCount + " starts ****************");

				availableHeight = getAvailableHeight(packingOption, containerHeight);
				LOG.info("Available Height:" + availableHeight);
				if (availableVolume <= 0)
				{
					//Display an error message "Reduce the products in the cart"
					LOG.info(" Reduce the products in the cart!!! ");
					message.add("Please reduce  products fom the cart");
				}
				else
				{
					if ((productsListA.size() == 0))
					{
						if (floorSpaceCount >= (totalPalletsCount / 2))
						{
							LOG.info("Some products cannot be double stacked");
							break;
						}
						else
						{
							LOG.info(" The filledup volume is: " + (100 - availableVolume));
							break;
						}
					}
					else
					{
						final int minIndex = 1;
						final int maxIndex = productsListA.size();
						LOG.info("Maximum n Minimum Index: " + maxIndex + " : " + minIndex);
						if ((productsListA.size() != 1))
						{
							LOG.info("smallest height:" + productsListA.get(maxIndex - 1).getPalletHeight());
							LOG.info("largest height:" + productsListA.get(minIndex - 1).getPalletHeight());
							totalPalletHeight = productsListA.get(maxIndex - 1).getPalletHeight()
									+ productsListA.get(minIndex - 1).getPalletHeight();
						}
						else
						{
							totalPalletHeight = productsListA.get(maxIndex - 1).getPalletHeight();
						}
						if ((availableHeight > totalPalletHeight))
						{
							if (productsListA.size() > 0 && (productsListA.size() != 1))
							{
								final EnergizerProductPalletHeight tempHighestPalletHeightProduct = productsListA.get(0);
								productWeight = productWeight
										+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId(),
												productsListA.get(maxIndex - 1).getCalculatedUOM())
										+ getWeightOfGivenMaterial(productsListA.get(minIndex - 1).getErpMaterialId(),
												productsListA.get(minIndex - 1).getCalculatedUOM());

								matrix[0][floorSpaceCount] = productsListA.get(maxIndex - 1).getErpMaterialId();
								matrix[1][floorSpaceCount] = productsListA.get(minIndex - 1).getErpMaterialId();
								productsListA.remove(maxIndex - 1); // removeHighestLowestPalletList(productsListA, maxIndex);;
								productsListA.remove(minIndex - 1);
								floorSpaceProductMap.put(floorSpaceCount, 2);


								if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
								{
									availableVolume = availableVolume - 10;
								}
								if (containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
								{
									availableVolume = availableVolume - 5;
								}
							}
							else
							{

								productWeight = productWeight
										+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId(),
												productsListA.get(maxIndex - 1).getCalculatedUOM());
								final double volumeOfSinglePallet = getVolumeOfHighestPallet(productsListA.get(0).getErpMaterialId(),
										productsListA.get(0).getCalculatedUOM());
								final double percentageVolumeOfSinglePallet = getPercentage(new BigDecimal(0),
										new BigDecimal(volumeOfSinglePallet), containerHeight).getPercentVolumeUses();
								availableVolume = availableVolume - percentageVolumeOfSinglePallet;

								matrix[0][floorSpaceCount] = productsListA.get(0).getErpMaterialId();

								productsListA.remove(maxIndex - 1);
								floorSpaceProductMap.put(floorSpaceCount, 1);
							}


							availableHeight = availableHeight - totalPalletHeight;
							LOG.info("Available volume:" + availableVolume);
							LOG.info("Available Height:" + availableHeight);
						}
						else if (availableHeight > productsListA.get(0).getPalletHeight())
						{
							final EnergizerProductPalletHeight tempProduct = productsListA.get(0);
							LOG.info("first element :" + tempProduct.getErpMaterialId());
							availableHeight = availableHeight - tempProduct.getPalletHeight();

							productsListB.add(tempProduct);
							//	final long noOfcasesinHighestPallet = noOfCases(productsListA.get(minIndex).getErpMaterialId());
							final double volumeOfHighestPallet = getVolumeOfHighestPallet(tempProduct.getErpMaterialId(),
									tempProduct.getCalculatedUOM());
							final double percentageVolumeOfHighestPallet = getPercentage(new BigDecimal(0),
									new BigDecimal(volumeOfHighestPallet), containerHeight).getPercentVolumeUses();
							LOG.info("volume of single product:" + volumeOfHighestPallet);

							if (!doubleStackMap.containsKey(tempProduct.getErpMaterialId()) && tempProduct.getOrderedUOM().equals("PAL"))
							{
								doubleStackMap = getPossibleDoubleStackedProducts(tempProduct.getErpMaterialId(), availableHeight,
										doubleStackMap);
							}
							productWeight = productWeight
									+ getWeightOfGivenMaterial(tempProduct.getErpMaterialId(), tempProduct.getCalculatedUOM());

							matrix[0][floorSpaceCount] = tempProduct.getErpMaterialId();

							productsListA.remove(tempProduct);
							floorSpaceProductMap.put(floorSpaceCount, 1);

							LOG.info("Available Height:" + availableHeight);
							availableVolume = availableVolume - percentageVolumeOfHighestPallet;
							LOG.info("Available volume:" + availableVolume);
						}
						else
						{
							if (containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
							{
								LOG.info("Please select the different container height!!!");
								message.add("Please select the different container height");
								break;
							}
						}
					}
				}
				LOG.info("*************** The floorSpace count loop ends******************");
			}

			LOG.info(" Productweight: " + productWeight);

			availableWeight = availableWeight
					- getPercentage(new BigDecimal(productWeight), new BigDecimal(0), containerHeight).getPercentWeightUses();

			/*
			 * if (nonPalletProductsList.size() > 0 && !packingOption.equalsIgnoreCase("2 WOODEN BASE")) {
			 *
			 * LOG.info("Select packing Option : 2 Wooden Base "); message .add(
			 * "Dear Customer, Your order contains some products whose quantity is less than a full pallet (partial pallet). Partial pallets can be shipped only with 2 wooden base packing material. Please change your packing material or remove the partial pallet products."
			 * ); cartData.setErrorMessage(true); }
			 *
			 * else
			 */if (nonPalletProductsList.size() > 0 && palletCount < totalPalletsCount)
			{
				LOG.info("******************** NonPallet Products Volume calculation starts***********************************");
				LOG.info("Non pallet products exist");


				percentVolumePerSlotForNonPallet = percentVolumePerSlot * (slotPercentageForNonPallet / 100);

				LOG.info(" NonPalletProductsList size: " + nonPalletProductsList.size());
				LOG.info("Available Volume after filling pallets: " + availableVolume);
				LOG.info("Available Weight: " + availableWeight);

				for (final Iterator iterator = nonPalletProductsList.iterator(); iterator.hasNext();)
				{

					final EnergizerProductPalletHeight enrProductPalTemp = (EnergizerProductPalletHeight) iterator.next();

					if (enrProductPalTemp.getCalculatedUOM().equalsIgnoreCase("LAY"))
					{
						volumeOfNonPalletProduct = EnergizerWeightOrVolumeConverter.getConversionValue(
								getLayerVolumeUnit(enrProductPalTemp.getErpMaterialId()),
								new BigDecimal(getLayerVolume(enrProductPalTemp.getErpMaterialId()))).doubleValue();
						weightOfNonPalletProduct = EnergizerWeightOrVolumeConverter.getConversionValue(
								getLayerWeightUnit(enrProductPalTemp.getErpMaterialId()),
								new BigDecimal(getLayerWeight(enrProductPalTemp.getErpMaterialId()))).doubleValue();
					}
					else
					{
						volumeOfNonPalletProduct = getVolumeOfHighestPallet(enrProductPalTemp.getErpMaterialId(),
								enrProductPalTemp.getOrderedUOM());
						weightOfNonPalletProduct = getWeightOfGivenMaterial(enrProductPalTemp.getErpMaterialId(),
								enrProductPalTemp.getOrderedUOM());
					}

					LOG.info("volume & weight of " + enrProductPalTemp.getErpMaterialId() + " are " + volumeOfNonPalletProduct + " & "
							+ weightOfNonPalletProduct);

					volumeOfAllNonPalletProduct = volumeOfAllNonPalletProduct + volumeOfNonPalletProduct;
					weightOfAllNonPalletProduct = weightOfAllNonPalletProduct + weightOfNonPalletProduct;

					LOG.info("NonPalletProduct fitted inside the container: " + enrProductPalTemp.getErpMaterialId());
					iterator.remove();

				}

				LOG.info("******************** NonPallet Products Volume calculation ends***********************************");
				LOG.info("Volume of all Nonpallet products: " + volumeOfAllNonPalletProduct);
				LOG.info("Weight of all Nonpallet products: " + weightOfAllNonPalletProduct);

				nonPalletProductWeightPercent = getPercentage(new BigDecimal(weightOfAllNonPalletProduct), new BigDecimal(0),
						containerHeight).percentWeightUses;
				availableWeight = availableWeight - nonPalletProductWeightPercent;
				nonPalletVolumePercent = getPercentage(new BigDecimal(0), new BigDecimal(volumeOfAllNonPalletProduct),
						containerHeight).percentVolumeUses;
				LOG.info("% Volume of all Nonpallet products: " + nonPalletVolumePercent);
				LOG.info("% Weight of all Nonpallet products: " + nonPalletProductWeightPercent);

				for (int nonPalletFloorSpaceCount = 0; nonPalletFloorSpaceCount < totalPalletsCount / 2; nonPalletFloorSpaceCount++)
				{
					boolean isBlockFilled;
					isBlockFilled = floorSpaceProductMap.containsKey(nonPalletFloorSpaceCount);
					int value = 0;

					if (isBlockFilled && nonPalletVolumePercent > 0)
					{
						value = floorSpaceProductMap.get(nonPalletFloorSpaceCount);
						if (value == 1)
						{
							if (nonPalletVolumePercent >= percentVolumePerSlotForNonPallet)
							{
								availableVolume = availableVolume - percentVolumePerSlot;
								nonPalletVolumePercent = nonPalletVolumePercent - percentVolumePerSlotForNonPallet;
								nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, 1.0);
								matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								partialPallet = partialPallet + 1;
							}

							else
							{
								availableVolume = availableVolume - nonPalletVolumePercent;
								nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, nonPalletVolumePercent
										/ percentVolumePerSlotForNonPallet);
								partialPallet = (int) (partialPallet + Math.ceil(nonPalletVolumePercent
										/ percentVolumePerSlotForNonPallet));
								matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
								nonPalletVolumePercent = 0;

							}
						}
					}
					else
					{
						if (nonPalletVolumePercent > 0 && nonPalletVolumePercent >= 2 * percentVolumePerSlotForNonPallet)
						{
							availableVolume = availableVolume - (2 * percentVolumePerSlot);
							nonPalletVolumePercent = nonPalletVolumePercent - (2 * percentVolumePerSlotForNonPallet);
							nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, 2.0);
							matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
							matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
							partialPallet = partialPallet + 2;
						}
						else if (nonPalletVolumePercent > 0 && nonPalletVolumePercent < 2 * percentVolumePerSlotForNonPallet
								&& nonPalletVolumePercent > percentVolumePerSlotForNonPallet)
						{
							nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, nonPalletVolumePercent
									/ percentVolumePerSlotForNonPallet);
							partialPallet = (int) (partialPallet + Math.ceil(nonPalletVolumePercent / percentVolumePerSlotForNonPallet));
							availableVolume = availableVolume - percentVolumePerSlot;
							availableVolume = availableVolume - (nonPalletVolumePercent - percentVolumePerSlotForNonPallet);
							matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
							matrix[1][nonPalletFloorSpaceCount] = "Mixed-Pallet";
							nonPalletVolumePercent = 0;
						}
						else if (nonPalletVolumePercent > 0 && nonPalletVolumePercent < percentVolumePerSlotForNonPallet)
						{
							nonPalletFloorSpaceProductMap.put(nonPalletFloorSpaceCount, nonPalletVolumePercent
									/ percentVolumePerSlotForNonPallet);
							partialPallet = (int) (partialPallet + Math.ceil(nonPalletVolumePercent / percentVolumePerSlotForNonPallet));
							availableVolume = availableVolume - nonPalletVolumePercent;
							nonPalletVolumePercent = 0;
							matrix[0][nonPalletFloorSpaceCount] = "Mixed-Pallet";
						}
					}
				}
			}
			if ((nonPalletProductsExists && palletCount > totalPalletsCount) || nonPalletVolumePercent > 0)
			{
				LOG.info("******************Some nonpallet products cannot fit inside container.Please remove nonpallets products from the cart***************************");
				//message.add("Please remove nonpallets products from the cart");
				cartData.setIsContainerFull(true);
			}

			if (productsListA.size() > 0)
			{

				List<EnergizerProductPalletHeight> productList = new ArrayList<EnergizerProductPalletHeight>();
				productList = getProductsWithOrderedUOM(productsListA);

				for (final EnergizerProductPalletHeight product : productList)
				{
					products.add(product);
				}

			}
			/*
			 * if (nonPalletProductsList.size() > 0) { for (final Iterator nonPalletProduct =
			 * nonPalletProductsList.iterator(); nonPalletProduct.hasNext();) { products.add((EnergizerProductPalletHeight)
			 * nonPalletProduct); } }
			 */

			LOG.info("******************* The final data is as below: *****************");
			LOG.info("FloorSpace count: " + floorSpaceCount);
			LOG.info(" Available Height: " + availableHeight);
			LOG.info(" After filling container Available Volume: " + availableVolume);
			LOG.info("last volume: " + halfFilledSlotVolume);

			LOG.info("Actual pallet count:" + actualPallet);
			LOG.info("Virtual pallet count:" + virtualPallet);
			LOG.info("partial pallet count:" + partialPallet);

			cartData.setFloorSpaceCount(floorSpaceCount);
			cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
			cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));


			for (int innerCount = 0; innerCount < (totalPalletsCount / 2); innerCount++)
			{
				final List<String> palStackProduct = new ArrayList<String>();

				for (int outerCount = 0; outerCount < 2; outerCount++)
				{
					if (matrix[outerCount][innerCount] != null)
					{
						LOG.info("Element at i=" + outerCount + "and j=" + innerCount + "value " + matrix[outerCount][innerCount]);
						palStackProduct.add(matrix[outerCount][innerCount]);
					}
					else
					{
						palStackProduct.add("NA");
					}


				}
				palStackData.put(innerCount, palStackProduct);
			}


			cartData.setPalStackData(palStackData);

		}

		LOG.info("******************************** Possible Double Stacked Products *******************************");
		LOG.info("Map size : " + doubleStackMap.size());

		final Set doubleStackMapEntrySet = doubleStackMap.entrySet();

		for (final Iterator iterator = doubleStackMapEntrySet.iterator(); iterator.hasNext();)
		{
			final Map.Entry mapEntry = (Map.Entry) iterator.next();
			LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
		}

		LOG.info("*********************** End *******************************************8");


		LOG.info("******************************** Floor Space Information *******************************");
		LOG.info("FloorSpaceProductsMap size : " + floorSpaceProductMap.size());

		final Set floorSpaceProductMapEntrySet = floorSpaceProductMap.entrySet();

		for (final Iterator iterator = floorSpaceProductMapEntrySet.iterator(); iterator.hasNext();)
		{
			final Map.Entry mapEntry = (Map.Entry) iterator.next();
			LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
		}

		LOG.info("===================================nonPallet floor List==============================");

		final Set nonPalletFloorSpaceProductMapEntrySet = nonPalletFloorSpaceProductMap.entrySet();

		for (final Iterator iterator = nonPalletFloorSpaceProductMapEntrySet.iterator(); iterator.hasNext();)
		{
			final Map.Entry mapEntry = (Map.Entry) iterator.next();
			LOG.info("key: " + mapEntry.getKey() + " value: " + mapEntry.getValue());
		}

		cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
		cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));
		cartData.setTotalPalletCount(palletCount);
		cartData.setVirtualPalletCount(virtualPallet);
		cartData.setPartialPalletCount(partialPallet);
		LOG.info("Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());

		//  ************ setting the OrderBlock  flag  ****************
		if (b2bUnit.getOrderBlock())
		{
			cartData.setIsOrderBlocked(true);
		}
		else
		{
			cartData.setIsOrderBlocked(false);
		}

		LOG.info("*********************** End *******************************************8");
		return cartData;
	}

	/**
	 * As discussed with Kalyan, we need to show height in cms only 1 inch = 2.54cms Need to store the values in
	 * local.properties file.
	 *
	 * 20ft container - 10 floorSpaceCount 40ft container - 20 floorSpaceCount
	 *
	 */
	public double getAvailableHeight(final String packingOption, final String containerHeight)
	{
		twentyFeetContainerHeightInInches = configurationService.getConfiguration().getDouble(TWENTY_FEET_CONTAINER_HEIGHT_INCHES,
				null);
		fortyFeetContainerHeightInInches = configurationService.getConfiguration().getDouble(FORTY_FEET_CONTAINER_HEIGHT_INCHES,
				null);

		LOG.info("twentyFeetContainerHeightInInches : " + twentyFeetContainerHeightInInches);
		LOG.info("fortyFeetContainerHeightInInches : " + fortyFeetContainerHeightInInches);
		double availableHeight = 0;
		if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE")
				&& containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
		{
			availableHeight = (fortyFeetContainerHeightInInches - 5) * 2.54;
		}
		else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals(Config.getParameter(FORTY_FEET_CONTAINER)))
		{
			availableHeight = (fortyFeetContainerHeightInInches - (5 * 2)) * 2.54;
		}
		else if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE")
				&& containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
		{
			availableHeight = (twentyFeetContainerHeightInInches - 5) * 2.54;
		}
		else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals(Config.getParameter(TWENTY_FEET_CONTAINER)))
		{
			availableHeight = (twentyFeetContainerHeightInInches - (5 * 2)) * 2.54;
		}

		return availableHeight;
	}


	/**
	 *
	 * @param cartData
	 * @return
	 */
	public ArrayList<EnergizerProductPalletHeight> getPalletsCountOfProductsInCart(final CartData cartData,
			final ArrayList<EnergizerProductPalletHeight> productsListA)
	{
		long palletsCount = 0;

		double palletHeight = 0;

		LOG.info("----------------------------------------" + productsListA.size());
		final CartModel cartModel = cartService.getSessionCart();
		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		if (productsListA != null && productsListA.size() > 0)
		{
			LOG.info("Clearing productsListA ");
			productsListA.clear();
		}

		if (nonPalletProductsList != null && nonPalletProductsList.size() > 0)
		{
			LOG.info("Clearing nonPalletProductsList: ");
			nonPalletProductsList.clear();
		}

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			long casePerPallet = 0, layerPerPallet = 0, casePerLayer = 0;
			long baseUOMPal = 0, baseUOMLayer = 0, baseUOMCase = 0;
			final String erpMaterialId = enerGizerProductModel.getCode();
			final long itemQuantity = abstractOrderEntryModel.getQuantity();
			final EnergizerProductConversionFactorModel coversionFactor = null;
			String cmirUom = null;

			LOG.info("cart ProductCode: " + enerGizerProductModel.getCode());

			//	final BigDecimal baseUom = new BigDecimal(0);

			try
			{

				final String userId = userService.getCurrentUser().getUid();
				final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
				final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
						b2bUnit.getUid());
				final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
						.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());


				cmirUom = energizerCMIRModel.getUom();

				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();


				if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
				{
					palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();
					LOG.info("pallet Height: " + palletHeight);

					final EnergizerProductPalletHeight energizerProductPalletHeight = new EnergizerProductPalletHeight();
					energizerProductPalletHeight.setErpMaterialId(erpMaterialId);
					energizerProductPalletHeight.setPalletHeight(palletHeight);
					energizerProductPalletHeight.setOrderedUOM(cmirUom);
					energizerProductPalletHeight.setIsVirtualPallet(false);
					energizerProductPalletHeight.setCalculatedUOM(cmirUom);

					productsListA.add(energizerProductPalletHeight);

					palletsCount = palletsCount + itemQuantity;

					if (itemQuantity > 1)
					{
						for (int i = 1; i <= itemQuantity - 1; i++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
							energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
							energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
							energizerProductPalletHeightTemp.setIsVirtualPallet(false);
							energizerProductPalletHeightTemp.setCalculatedUOM(cmirUom);
							productsListA.add(energizerProductPalletHeightTemp);
						}
					}
				}

				else if (!(cmirUom.equals("PAL")) && cmirUom.equals(alternateUOM))
				{
					LOG.info("----------------------- Start of nonPalletProducts calculation -----------------------------");

					double convertedPAL = 0, palFractionalPart = 0, balanceNonPal = 0;
					final double convertedLay = 0, layerFractionalPart = 0;

					/***** Calculation of all UOMs ********/
					final List<EnergizerProductConversionFactorModel> enrProductConversionList = energizerProductService
							.getAllEnergizerProductConversion(erpMaterialId);

					if (!enrProductConversionList.isEmpty())
					{
						for (final Iterator iterator = enrProductConversionList.iterator(); iterator.hasNext();)
						{
							final EnergizerProductConversionFactorModel enrProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
									.next();

							final String altUOM = enrProductConversionFactorModel.getAlternateUOM();

							if (null != altUOM && altUOM.equalsIgnoreCase("PAL"))
							{
								baseUOMPal = enrProductConversionFactorModel.getConversionMultiplier();
								palletHeight = enrProductConversionFactorModel.getPackageHeight().getMeasurement();
								//enrProductConversionFactorModel.getPackageVolume();
								LOG.info(" &&& PAL Height &&&&&&&&" + palletHeight);
							}
							if (null != altUOM && altUOM.equalsIgnoreCase("CS"))
							{
								baseUOMCase = enrProductConversionFactorModel.getConversionMultiplier();

							}
							if (null != altUOM && altUOM.equalsIgnoreCase("LAY"))
							{
								baseUOMLayer = enrProductConversionFactorModel.getConversionMultiplier();
							}
						}
					}

					casePerPallet = baseUOMPal / baseUOMCase;
					layerPerPallet = baseUOMPal / baseUOMLayer;
					casePerLayer = baseUOMLayer / baseUOMCase;

					/***** End ********/


					LOG.info("pallet Height: " + palletHeight);
					LOG.info(" ***** UOM Conversion Multiplier details of" + erpMaterialId + " ------ casePerPallet:" + casePerPallet
							+ " -- casePerLayer: " + casePerLayer + " --layerPerPallet: " + layerPerPallet);

					if (cmirUom.equalsIgnoreCase("CS"))
					{
						LOG.info("Ordered Quantity of CS UOM: " + itemQuantity);

						convertedPAL = itemQuantity / casePerPallet;

						palFractionalPart = convertedPAL % 1;
						convertedPAL = convertedPAL - palFractionalPart;

						if (convertedPAL != 0 && convertedPAL >= 1)
						{
							for (int iPALCount = 1; iPALCount <= convertedPAL; iPALCount++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
								energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
								energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
								energizerProductPalletHeightTemp.setIsVirtualPallet(true);
								energizerProductPalletHeightTemp.setCalculatedUOM("PAL");
								productsListA.add(energizerProductPalletHeightTemp);
							}
						}
						balanceNonPal = itemQuantity - (casePerPallet * convertedPAL);

						LOG.info(" Remaining CS after converting CS into Pallets: " + balanceNonPal);

					}

					else if (cmirUom.equalsIgnoreCase("LAY"))
					{
						LOG.info("Ordered Quantity of LAY UOM: " + itemQuantity);

						convertedPAL = itemQuantity / layerPerPallet;

						palFractionalPart = convertedPAL % 1;
						convertedPAL = convertedPAL - palFractionalPart;

						if (convertedPAL != 0 && convertedPAL >= 1)
						{
							for (int iPALCount = 1; iPALCount <= convertedPAL; iPALCount++)
							{
								final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
								energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
								energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
								energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
								energizerProductPalletHeightTemp.setIsVirtualPallet(true);
								energizerProductPalletHeightTemp.setCalculatedUOM("PAL");
								productsListA.add(energizerProductPalletHeightTemp);
							}
						}
						balanceNonPal = itemQuantity - (layerPerPallet * convertedPAL);

						LOG.info(" Remaining LAY after converting LAY into Pallets: " + balanceNonPal);

					}





					if (balanceNonPal > 0)
					{

						for (int iCaseCount = 1; iCaseCount <= balanceNonPal; iCaseCount++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
							energizerProductPalletHeightTemp.setOrderedUOM(cmirUom);
							energizerProductPalletHeightTemp.setPalletHeight(0);
							energizerProductPalletHeightTemp.setCalculatedUOM(cmirUom);
							nonPalletProductsList.add(energizerProductPalletHeightTemp);
						}

					}
					LOG.info(" NonPalletProductsList size after calculating remaining CS and LAY: " + nonPalletProductsList.size());



					// end of if(orderedUOM==CS)

					if (nonPalletProductsList.size() > 0)
					{
						nonPalletProductsExists = true;
					}

					LOG.info("----------------------- End of nonPalletProducts calculation -----------------------------");

				}

			}
			catch (final Exception e)
			{
				e.printStackTrace();
				/*
				 * LOG.info(
				 * "*********************Exception occured in getPalletsCount method of DefaultEnergizerCartService class*************"
				 * + e.getMessage());
				 */
			}

		}

		LOG.info(" Products in nonPalletProductsList: " + nonPalletProductsList.size());
		LOG.info(" Products in ListA and palletscount: " + productsListA.size() + " : " + palletsCount);

		return productsListA;
	}

	public ArrayList<EnergizerProductPalletHeight> getPalletsCountOfProductsInCartForSlipSheet(final CartData cartData,
			final ArrayList<EnergizerProductPalletHeight> productsListA)
	{
		long palletsCount = 0;
		LOG.info("----------------------------------------" + productsListA.size());
		final CartModel cartModel = cartService.getSessionCart();
		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		if (productsListA != null && productsListA.size() > 0)
		{
			LOG.info("Clearing productsListA ");
			productsListA.clear();
		}

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			final String erpMaterialId = enerGizerProductModel.getCode();
			final long itemQuantity = abstractOrderEntryModel.getQuantity();
			final EnergizerProductConversionFactorModel coversionFactor = null;
			String cmirUom = null;
			final BigDecimal baseUom = new BigDecimal(0);

			try
			{
				final String userId = userService.getCurrentUser().getUid();
				final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
				final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
						b2bUnit.getUid());
				final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
						.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

				cmirUom = energizerCMIRModel.getUom();

				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

				if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
				{
					final double palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();
					final EnergizerProductPalletHeight energizerProductPalletHeight = new EnergizerProductPalletHeight();
					energizerProductPalletHeight.setErpMaterialId(erpMaterialId);
					energizerProductPalletHeight.setPalletHeight(palletHeight);
					productsListA.add(energizerProductPalletHeight);
					final BigDecimal lineItemToheight = new BigDecimal(0);

					palletsCount = palletsCount + itemQuantity;

					if (itemQuantity > 1)
					{
						for (int i = 1; i <= itemQuantity - 1; i++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(erpMaterialId);
							energizerProductPalletHeightTemp.setPalletHeight(palletHeight);
							productsListA.add(energizerProductPalletHeightTemp);
						}
					}
				}
				else if (!(cmirUom.equals("PAL")) && cmirUom.equals(alternateUOM))
				{
					nonPalletProductsExists = true;
				}

			}
			catch (final Exception e)
			{
				//e.printStackTrace();
				LOG.info("*********************Exception occured*************" + e.getMessage());
			}

		}

		LOG.info(" Products in ListA and palletscount: " + productsListA.size() + " : " + palletsCount);

		return productsListA;
	}

	public HashMap getPossibleDoubleStackedProducts(final String cartERPMaterialID, final double availableHeight,
			final HashMap doubleStackMap)
	{
		String cmirUom = null;
		final Set<EnergizerProductModel> erpMaterialIDSetFromDB = new HashSet<EnergizerProductModel>();
		List<EnergizerProductModel> energizerERPMaterialIDList = null;
		final List<String> possibleDoubleStackList = new ArrayList();
		final String userId = userService.getCurrentUser().getUid();
		final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
		energizerERPMaterialIDList = energizerProductService.getEnergizerERPMaterialID();

		erpMaterialIDSetFromDB.addAll(energizerERPMaterialIDList);

		for (final EnergizerProductModel energizerProductModel : erpMaterialIDSetFromDB)
		{

			final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(energizerProductModel.getCode(),
					b2bUnit.getUid());

			if (energizerCMIRModel != null && energizerCMIRModel.getIsActive())
			{
				final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
						.getEnergizerProductConversion(energizerProductModel.getCode(), b2bUnit.getUid());

				cmirUom = energizerCMIRModel.getUom();

				final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

				if (cmirUom.equals("PAL") && cmirUom.equals(alternateUOM))
				{

					final double palletHeight = energizerProductConversionFactorModel.getPackageHeight().getMeasurement();

					if (palletHeight <= availableHeight)
					{
						possibleDoubleStackList.add(energizerProductModel.getCode());
						if (possibleDoubleStackList.size() > 0)
						{
							doubleStackMap.put(cartERPMaterialID, possibleDoubleStackList);
						}
					}
				}
			}
		}
		if (!doubleStackMap.containsKey(cartERPMaterialID))
		{
			doubleStackMap.put(cartERPMaterialID, null);
		}
		return doubleStackMap;
	}

	public CartData calCartContainerUtilizationWithSlipsheets(final CartData cartData, final String containerHeight)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

			//final BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);

			BigDecimal totalVolumeDisable20FT = new BigDecimal(0);
			BigDecimal totalVolumeDisable40FT = new BigDecimal(0);

			message = new ArrayList<String>();
			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 *
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248
			 * fourty.feet.container.volume=70.62209 fourty.feet.container.weight=18234.3948
			 */

			//Converted the container volume from Metric unit to Cubic centimeter

			final BigDecimal twentyFeetContainerVolumInCCM = disableTwentyFeetContainerVolume.multiply(new BigDecimal(1000000));

			final BigDecimal fourtyFeetContainerVolumeInCCM = disableFourtyFeetContainerVolume.multiply(new BigDecimal(1000000));

			LOG.info("==============Disable 2 Slip Sheet=============");
			for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
			{
				final EnergizerProductModel enerGizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
				final String erpMaterialId = enerGizerProductModel.getCode();
				final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;

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

				if (null != coversionFactor)
				{
					BigDecimal lineItemToVolume_20FT = new BigDecimal(0);
					BigDecimal lineItemToVolume_40FT = new BigDecimal(0);
					BigDecimal unitVolumeDisable20FT = new BigDecimal(0);
					BigDecimal unitVolumeDisable40FT = new BigDecimal(0);
					String volumeUnit = "";
					BigDecimal unitVolume = new BigDecimal(0);

					if (cmirUom.equalsIgnoreCase("LAY"))
					{
						volumeUnit = getLayerVolumeUnit(erpMaterialId);
						unitVolume = new BigDecimal(getLayerVolume(erpMaterialId));
					}
					else if (!cmirUom.equalsIgnoreCase("LAY") && null != coversionFactor.getPackageVolume())
					{
						volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
						unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
					}

					LOG.info("-------" + erpMaterialId + "    : " + coversionFactor.getAlternateUOM() + "--------------");

					/*
					 * Comparing the Pallet volume with the container volume(2.5% of container volume for 40 FT container and
					 * 5% of container volume for 20 FT container)
					 */

					if (unitVolume.compareTo(ZERO) != 0 && coversionFactor.getAlternateUOM().equals("PAL"))
					{

						LOG.info("% volume of product if placed inside 20FT container :" + (unitVolume.doubleValue() * 100)
								/ (twentyFeetContainerVolumInCCM.doubleValue()));
						if ((unitVolume.doubleValue() * 100) / (twentyFeetContainerVolumInCCM.doubleValue()) < 5)
						{
							unitVolumeDisable20FT = twentyFeetContainerVolumInCCM.multiply(new BigDecimal(0.05));

							LOG.info(enerGizerProductModel.getCode() + " " + coversionFactor.getAlternateUOM()
									+ " have volume less than 5 % when container is 20 FT");
							LOG.info("Volume in 20 FT :  " + unitVolumeDisable20FT);
						}
						else
						{
							unitVolumeDisable20FT = unitVolume;
							LOG.info(enerGizerProductModel.getCode() + " " + coversionFactor.getAlternateUOM()
									+ " Volume is greater than 5% for twenty feet");
							LOG.info("Volume in 20 FT :  " + unitVolumeDisable20FT);
						}

						LOG.info("% volume of product if placed inside 40FT container :" + (unitVolume.doubleValue() * 100)
								/ (fourtyFeetContainerVolumeInCCM.doubleValue()));
						if ((unitVolume.doubleValue() * 100) / (fourtyFeetContainerVolumeInCCM.doubleValue()) < 2.5)
						{
							unitVolumeDisable40FT = fourtyFeetContainerVolumeInCCM.multiply(new BigDecimal(0.025));
							LOG.info(enerGizerProductModel.getCode() + "" + coversionFactor.getAlternateUOM()
									+ " have volume less than 2.5% when container is 40 FT");
							LOG.info("Volume in 40 FT :  " + unitVolumeDisable40FT);
						}

						else
						{
							unitVolumeDisable40FT = unitVolume;
							LOG.info(enerGizerProductModel.getCode() + "" + coversionFactor.getAlternateUOM()
									+ " Volume is greater than 2.5% for forty feet");
							LOG.info("Volume in 40 FT :  " + unitVolumeDisable40FT);
						}

						lineItemToVolume_20FT = unitVolumeDisable20FT.multiply(itemQuantity);
						lineItemToVolume_40FT = unitVolumeDisable40FT.multiply(itemQuantity);
					}
					else if (unitVolume.compareTo(ZERO) != 0)
					{
						LOG.info("Volume of nonPallet Product:   " + unitVolume + volumeUnit);
						lineItemToVolume_20FT = unitVolume.multiply(itemQuantity);
						lineItemToVolume_40FT = unitVolume.multiply(itemQuantity);
					}
					totalVolumeDisable20FT = totalVolumeDisable20FT.add(EnergizerWeightOrVolumeConverter.getConversionValue(
							volumeUnit, lineItemToVolume_20FT));
					totalVolumeDisable40FT = totalVolumeDisable40FT.add(EnergizerWeightOrVolumeConverter.getConversionValue(
							volumeUnit, lineItemToVolume_40FT));
				}
				if (null != coversionFactor)
				{
					BigDecimal lineItemTotWt = new BigDecimal(0);
					BigDecimal unitWeight = new BigDecimal(0);
					String weightUnit = "";//coversionFactor.getPackageWeight().getMeasuringUnits();
					if (cmirUom.equalsIgnoreCase("LAY"))
					{
						weightUnit = getLayerWeightUnit(erpMaterialId);
						unitWeight = new BigDecimal(getLayerWeight(erpMaterialId));
					}
					else if (!cmirUom.equalsIgnoreCase("LAY") && null != coversionFactor.getPackageWeight())
					{
						weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
						unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
					}

					if (unitWeight.compareTo(ZERO) != 0)
					{
						lineItemTotWt = unitWeight.multiply(itemQuantity);
					}

					totalCartWt = totalCartWt.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWt));
					LOG.info("Weight : " + lineItemTotWt);
				}
				LOG.info("==============================================");

			}
			totalVolumeDisable20FT = totalVolumeDisable20FT.setScale(6, BigDecimal.ROUND_HALF_EVEN);
			totalVolumeDisable40FT = totalVolumeDisable40FT.setScale(6, BigDecimal.ROUND_HALF_EVEN);

			LOG.info("container Volume : 20 FT: " + disableTwentyFeetContainerVolume);
			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume or 20FT: " + totalVolumeDisable20FT
					+ " || totalCartVolume or 40FT: " + totalVolumeDisable40FT);

			ContainerData containerData = null;

			if (totalVolumeDisable20FT.compareTo(disableTwentyFeetContainerVolume) == 1)
			{
				containerData = getPercentageContainerUtil(totalCartWt, totalVolumeDisable40FT);
			}
			else
			{
				containerData = getPercentageContainerUtil(totalCartWt, totalVolumeDisable20FT);
			}
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			cartData.setContainerHeight(containerData.getContainerType());

			if (((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
					&& cartData.getContainerHeight().equalsIgnoreCase(Config.getParameter(TWENTY_FEET_CONTAINER)))
			{
				cartData.setIsContainerFull(false);
			}
			else if (((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
					&& cartData.getContainerHeight().equalsIgnoreCase(Config.getParameter(FORTY_FEET_CONTAINER)))
			{
				cartData.setIsContainerFull(true);
			}
			else
			{
				cartData.setIsContainerFull(false);
			}
			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));

			LOG.info("Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());
		}
		catch (final Exception exception)
		{
			LOG.error("Error in calCartContainerUtilization", exception);
		}
		return cartData;
	}

	public CartData calCartContainerUtilizationWithEnableSlipsheets(final CartData cartData, final String containerHeight)
	{
		try
		{
			final CartModel cartModel = cartService.getSessionCart();
			final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();
			final int totalPalletCountForSlipSheet = configurationService.getConfiguration().getInt(
					TOTAL_PALLET_COUNT_FOR_2SLIPSHEET);
			productsListA = new ArrayList<EnergizerProductPalletHeight>();
			//int palletCount = 0;
			long numberOfEachInCases = 0;
			long numberOfEachInPallet = 0;
			long numberOfcasesPerPallet = 0;
			BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal casesVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);
			BigDecimal calculatedUnitVolume = new BigDecimal(0);
			BigDecimal casesWeight = new BigDecimal(0);
			BigDecimal calculatedUnitWeight = new BigDecimal(0);


			message = new ArrayList<String>();

			productsListA = getPalletsCountOfProductsInCart(cartData, productsListA);
			palletCount = productsListA.size();

			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 *
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248
			 * fourty.feet.container.volume=70.62209 fourty.feet.container.weight=18234.3948
			 */

			LOG.info("==========For enable 2 Slip sheet ==========");

			//	LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);
			//	ContainerData containerData = null;

			//	containerData = getPercentage(totalCartWt, totalCartVolume, containerHeight);
			//	cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			//	cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			//	cartData.setContainerHeight(containerData.getContainerType());

			//for (final EnergizerProductPalletHeight product : productsListA)
			for (final Iterator iterator = productsListA.iterator(); iterator.hasNext();)
			{
				final EnergizerProductPalletHeight fullPallet = (EnergizerProductPalletHeight) iterator.next();
				final String erpMaterialId = fullPallet.getErpMaterialId();
				EnergizerProductConversionFactorModel coversionFactor = null;
				String cmirUom = null;
				String casesVolumeUnit = null;
				String casesWeightUnit = null;
				try
				{
					final String userId = userService.getCurrentUser().getUid();
					final EnergizerB2BUnitModel b2bUnit = b2bCommerceUserService.getParentUnitForCustomer(userId);
					final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId,
							b2bUnit.getUid());
					cmirUom = energizerCMIRModel.getUom();
					final List<EnergizerProductConversionFactorModel> allEnergizerProductConversionFactorModel = energizerProductService
							.getAllEnergizerProductConversion(erpMaterialId);
					for (final EnergizerProductConversionFactorModel productConversioFactor : allEnergizerProductConversionFactorModel)
					{
						if (productConversioFactor.getAlternateUOM().equalsIgnoreCase("CS"))
						{
							casesVolume = new BigDecimal(productConversioFactor.getPackageVolume().getMeasurement());
							casesWeight = new BigDecimal(productConversioFactor.getPackageWeight().getMeasurement());
							numberOfEachInCases = productConversioFactor.getConversionMultiplier();
							casesVolumeUnit = productConversioFactor.getPackageVolume().getMeasuringUnits();
							casesWeightUnit = productConversioFactor.getPackageWeight().getMeasuringUnits();
						}
						if (productConversioFactor.getAlternateUOM().equalsIgnoreCase("PAL"))
						{
							numberOfEachInPallet = productConversioFactor.getConversionMultiplier();
						}
					}
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = energizerProductService
							.getEnergizerProductConversion(erpMaterialId, b2bUnit.getUid());

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();
					if (null != alternateUOM && null != cmirUom && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
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
				if (null != coversionFactor && null != casesVolume)
				{
					LOG.info(erpMaterialId + "    " + coversionFactor.getAlternateUOM());
					numberOfcasesPerPallet = numberOfEachInPallet / numberOfEachInCases;
					LOG.info("Number of cases per pallet: " + numberOfcasesPerPallet);
					calculatedUnitVolume = casesVolume.multiply(new BigDecimal(numberOfcasesPerPallet));
					LOG.info("Product volume on basis of cases volume: " + calculatedUnitVolume);


					final String volumeUnit = casesVolumeUnit;
					totalCartVolume = totalCartVolume.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit,
							calculatedUnitVolume));
				}
				if (null != coversionFactor && null != casesWeight)
				{
					numberOfcasesPerPallet = numberOfEachInPallet / numberOfEachInCases;
					LOG.info("Number of cases per pallet: " + numberOfcasesPerPallet);
					calculatedUnitWeight = casesWeight.multiply(new BigDecimal(numberOfcasesPerPallet));
					LOG.info("Product Weight on basis of cases Weight: " + calculatedUnitWeight);

					final String weightUnit = casesWeightUnit;
					totalCartWt = totalCartWt.add(EnergizerWeightOrVolumeConverter
							.getConversionValue(weightUnit, calculatedUnitWeight));
				}
				LOG.info("-----------------------------------------------------------------");


			}

			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);
			ContainerData containerData = null;

			containerData = getPercentage(totalCartWt, totalCartVolume, containerHeight);
			cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
			cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());

			cartData.setContainerHeight(containerData.getContainerType());

			if (nonPalletProductsList.size() > 0)
			{
				LOG.info("select 2 wooden base as packing option");
				message
						.add("Dear Customer, Your order contains some products whose quantity is less than a full pallet (partial pallet). Partial pallets can be shipped only with 2 wooden base packing material. Please change your packing material or remove the partial pallet products.");
				cartData.setErrorMessage(true);
			}
			else if ((cartData.getTotalProductVolumeInPercent() > 100) || (cartData.getTotalProductWeightInPercent() > 100))
			{
				cartData.setIsContainerFull(true);
			}
			else if (palletCount > totalPalletCountForSlipSheet)
			{
				cartData.setIsFloorSpaceFull(true);
			}
			else
			{
				cartData.setIsContainerFull(false);
			}
			cartData.setAvailableVolume((Math.round((100 - cartData.getTotalProductVolumeInPercent()) * 100.0) / 100.0));
			cartData.setAvailableWeight((Math.round((100 - cartData.getTotalProductWeightInPercent()) * 100.0) / 100.0));

			cartData.setTotalPalletCount(palletCount);
			cartData.setVirtualPalletCount(virtualPallet);
			cartData.setPartialPalletCount(partialPallet);
			LOG.info("Available volume: " + cartData.getAvailableVolume() + " Available Weight: " + cartData.getAvailableWeight());

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
		totalCartVolume = totalCartVolume.setScale(6, BigDecimal.ROUND_UP);

		final ContainerData containerData = new ContainerData();
		if (totalCartWt.compareTo(twentyFeetContainerWeight) == -1
				&& (totalCartVolume.compareTo(disableTwentyFeetContainerVolume) == -1 || totalCartVolume
						.compareTo(disableTwentyFeetContainerVolume) == 0))
		{
			containerData.setContainerType("20FT");
			final double volumePercentage = (totalCartVolume.multiply(hundred)).divide(disableTwentyFeetContainerVolume, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			final double weightPercentage = (totalCartWt.multiply(hundred)).divide(twentyFeetContainerWeight, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(volumePercentage);
			containerData.setPercentWeightUses(weightPercentage);
		}
		else
		{
			containerData.setContainerType("40FT");
			final double volumePercentage = (totalCartVolume.multiply(hundred)).divide(disableFourtyFeetContainerVolume, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			final double weightPercentage = (totalCartWt.multiply(hundred)).divide(fourtyFeetContainerWeight, 2,
					RoundingMode.HALF_EVEN).doubleValue();
			LOG.info("|| volumePercentage => " + volumePercentage + " || weightPercentage => " + weightPercentage);
			containerData.setPercentVolumeUses(volumePercentage);
			containerData.setPercentWeightUses(weightPercentage);
		}

		return containerData;
	}

	private ContainerData getPercentage(BigDecimal totalCartWt, BigDecimal totalCartVolume, final String containerHeight)
	{

		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(6, BigDecimal.ROUND_UP);

		final ContainerData containerData = new ContainerData();
		if (containerHeight.equals("20FT"))
		{
			containerData.setContainerType("20FT");
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
			containerData.setContainerType("40FT");
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

	public BigDecimal getVolumeOfProductsInCart(final CartData cartData, final String str)
	{

		BigDecimal totalCartVolume = new BigDecimal(0);
		BigDecimal VolumeOfNonPalletProduct = new BigDecimal(0);
		final CartModel cartModel = cartService.getSessionCart();
		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel energizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			final String erpMaterialId = energizerProductModel.getCode();
			final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
			EnergizerProductConversionFactorModel coversionFactor = null;
			String cmirUom = null;
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
				}
			}
			catch (final Exception e)
			{
				LOG.error("error in retreiving the cmir!");
			}
			BigDecimal lineItemTotvolume = new BigDecimal(0);
			BigDecimal unitVolume = new BigDecimal(0);
			String volumeUnit = null;
			if (cmirUom.equalsIgnoreCase("LAY"))
			{
				unitVolume = new BigDecimal(getLayerVolume(erpMaterialId));
				volumeUnit = getLayerVolumeUnit(erpMaterialId);
			}
			else
			{
				unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
				volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
			}
			if (unitVolume.compareTo(ZERO) != 0)
			{
				lineItemTotvolume = unitVolume.multiply(itemQuantity);
			}
			if (str.equals("VOLUMEOFALLPRODUCTS"))
			{
				totalCartVolume = totalCartVolume.add(EnergizerWeightOrVolumeConverter.getConversionValue(volumeUnit,
						lineItemTotvolume));
			}
			if (str.equals("VOLUMEOFNONPALLETPRODUCTS") && !(coversionFactor.getAlternateUOM().equals("PAL")))
			{
				VolumeOfNonPalletProduct = VolumeOfNonPalletProduct.add(EnergizerWeightOrVolumeConverter.getConversionValue(
						volumeUnit, lineItemTotvolume));
			}
		}
		if (str.equals("VOLUMEOFALLPRODUCTS"))
		{
			return totalCartVolume;
		}
		if (str.equals("VOLUMEOFNONPALLETPRODUCTS"))
		{
			return VolumeOfNonPalletProduct;
		}
		else
		{
			return new BigDecimal(0);
		}
	}

	public BigDecimal getWeightOfProductsInCart(final CartData cartData, final String str)
	{
		BigDecimal totalCartWeight = new BigDecimal(0);
		BigDecimal weightOfNonPalletProduct = new BigDecimal(0);
		final CartModel cartModel = cartService.getSessionCart();
		final List<AbstractOrderEntryModel> cartEntries = cartModel.getEntries();

		for (final AbstractOrderEntryModel abstractOrderEntryModel : cartEntries)
		{
			final EnergizerProductModel energizerProductModel = (EnergizerProductModel) abstractOrderEntryModel.getProduct();
			final String erpMaterialId = energizerProductModel.getCode();
			final BigDecimal itemQuantity = new BigDecimal((abstractOrderEntryModel.getQuantity().longValue()));
			EnergizerProductConversionFactorModel coversionFactor = null;
			String cmirUom = null;
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
				}
			}
			catch (final Exception e)
			{
				LOG.error("error in retreiving the cmir!");
			}
			BigDecimal lineItemTotWeight = new BigDecimal(0);
			BigDecimal unitWeight = new BigDecimal(0);
			String weightUnit = null;
			if (cmirUom.equalsIgnoreCase("LAY"))
			{
				unitWeight = new BigDecimal(getLayerWeight(erpMaterialId));
				weightUnit = getLayerWeightUnit(erpMaterialId);
			}
			else
			{
				unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
				weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
			}

			if (unitWeight.compareTo(ZERO) != 0)
			{
				lineItemTotWeight = unitWeight.multiply(itemQuantity);
			}

			if (str.equals("WEIGHTOFALLPRODUCTS"))
			{
				totalCartWeight = totalCartWeight.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit,
						lineItemTotWeight));
			}
			if (str.equals("WEIGHTOFNONPALLETPRODUCTS") && !(coversionFactor.getAlternateUOM().equals("PAL")))
			{
				weightOfNonPalletProduct = weightOfNonPalletProduct.add(EnergizerWeightOrVolumeConverter.getConversionValue(
						weightUnit, lineItemTotWeight));
			}
		}
		if (str.equals("WEIGHTOFALLPRODUCTS"))
		{
			return totalCartWeight;
		}
		if (str.equals("WEIGHTOFNONPALLETPRODUCTS"))
		{
			return weightOfNonPalletProduct;
		}
		else
		{
			return new BigDecimal(0);
		}
	}

	public long noOfCases(final String erpMaterialId)
	{

		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		long multiplierForCase = 0;
		long multiplierForPAL = 0;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals("PAL"))
			{
				multiplierForPAL = UOM.getConversionMultiplier();
				continue;
			}
			if (UOM.getAlternateUOM().equals("CASE"))
			{
				multiplierForCase = UOM.getConversionMultiplier();
				continue;
			}
		}
		return (multiplierForPAL / multiplierForCase);
	}

	public double getVolumeOfHighestPallet(final String erpMaterialId, final String convertedUOM)
	{
		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		final String measuringUnit;
		final double volume;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals(convertedUOM))
			{
				measuringUnit = UOM.getPackageVolume().getMeasuringUnits();
				volume = UOM.getPackageVolume().getMeasurement();
				return EnergizerWeightOrVolumeConverter.getConversionValue(measuringUnit, new BigDecimal(volume)).doubleValue();
			}
		}
		return 0;
	}

	public double getWeightOfGivenMaterial(final String erpMaterialId, final String convertedUOM)
	{
		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		final String measuringUnit;
		final double weight;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals(convertedUOM))
			{
				measuringUnit = UOM.getPackageWeight().getMeasuringUnits();
				weight = UOM.getPackageWeight().getMeasurement();
				return EnergizerWeightOrVolumeConverter.getConversionValue(measuringUnit, new BigDecimal(weight)).doubleValue();
				//return UOM.getPackageVolume().getMeasurement();
			}
		}
		return 0;
	}

	public List<String> getMessages()
	{
		return message;
	}

	public List<EnergizerProductPalletHeight> getProductsWithOrderedUOM(final List<EnergizerProductPalletHeight> products)
	{
		int casePerPallet = 0;
		int layerPerPallet = 0;
		long baseUOMPal = 0;
		long baseUOMLayer = 0, baseUOMCase = 0;
		final List<EnergizerProductPalletHeight> productsWithOrderedUOM = new ArrayList<EnergizerProductPalletHeight>();
		List<EnergizerProductConversionFactorModel> enrProductConversionList;

		for (final Iterator productsWithCalculatedUOM = products.iterator(); productsWithCalculatedUOM.hasNext();)
		{
			final EnergizerProductPalletHeight tempEnergizerProductPalletHeight = (EnergizerProductPalletHeight) productsWithCalculatedUOM
					.next();
			if (tempEnergizerProductPalletHeight.getOrderedUOM().equalsIgnoreCase(
					tempEnergizerProductPalletHeight.getCalculatedUOM()))
			{
				productsWithOrderedUOM.add(tempEnergizerProductPalletHeight);
			}
			else
			{
				final String orderedUOM = tempEnergizerProductPalletHeight.getOrderedUOM();
				final String calculatedUOM = tempEnergizerProductPalletHeight.getCalculatedUOM();
				enrProductConversionList = energizerProductService.getAllEnergizerProductConversion(tempEnergizerProductPalletHeight
						.getErpMaterialId());
				if (!enrProductConversionList.isEmpty())
				{
					for (final Iterator iterator = enrProductConversionList.iterator(); iterator.hasNext();)
					{
						final EnergizerProductConversionFactorModel enrProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
								.next();

						final String altUOM = enrProductConversionFactorModel.getAlternateUOM();
						if (null != altUOM && altUOM.equalsIgnoreCase("PAL"))
						{
							baseUOMPal = enrProductConversionFactorModel.getConversionMultiplier();
						}

						if (null != altUOM && altUOM.equalsIgnoreCase("LAY"))
						{
							baseUOMLayer = enrProductConversionFactorModel.getConversionMultiplier();
						}
						if (null != altUOM && altUOM.equalsIgnoreCase("CS"))
						{
							baseUOMCase = enrProductConversionFactorModel.getConversionMultiplier();
						}
					}
					layerPerPallet = (int) (baseUOMPal / baseUOMLayer);
					casePerPallet = (int) (baseUOMPal / baseUOMCase);
					if (orderedUOM.equalsIgnoreCase("LAY"))
					{
						for (int layCount = 1; layCount <= layerPerPallet; layCount++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(tempEnergizerProductPalletHeight.getErpMaterialId());
							energizerProductPalletHeightTemp.setOrderedUOM(tempEnergizerProductPalletHeight.getOrderedUOM());
							energizerProductPalletHeightTemp.setPalletHeight(0);
							energizerProductPalletHeightTemp.setCalculatedUOM(tempEnergizerProductPalletHeight.getCalculatedUOM());
							productsWithOrderedUOM.add(energizerProductPalletHeightTemp);
						}
					}
					else if (orderedUOM.equalsIgnoreCase("CS"))
					{
						for (int caseCount = 1; caseCount <= casePerPallet; caseCount++)
						{
							final EnergizerProductPalletHeight energizerProductPalletHeightTemp = new EnergizerProductPalletHeight();
							energizerProductPalletHeightTemp.setErpMaterialId(tempEnergizerProductPalletHeight.getErpMaterialId());
							energizerProductPalletHeightTemp.setOrderedUOM(tempEnergizerProductPalletHeight.getOrderedUOM());
							energizerProductPalletHeightTemp.setPalletHeight(0);
							energizerProductPalletHeightTemp.setCalculatedUOM(tempEnergizerProductPalletHeight.getCalculatedUOM());
							productsWithOrderedUOM.add(energizerProductPalletHeightTemp);
						}
					}

				}
			}
		}
		return productsWithOrderedUOM;
	}

	public HashMap getProductNotAddedToCart()
	{

		final List possibleDoubleStackList = new ArrayList();
		final HashMap possibleDoubleStackMap = new HashMap();
		int size = 0;
		if (products != null && products.size() > 0)
		{
			for (final EnergizerProductPalletHeight energizerProductModel : products)
			{

				if (!possibleDoubleStackMap.containsKey(energizerProductModel.getErpMaterialId()))
				{
					size = 1;
					possibleDoubleStackMap.put(energizerProductModel.getErpMaterialId(), size);
				}
				else
				{
					size = (int) possibleDoubleStackMap.get(energizerProductModel.getErpMaterialId());
					size++;
					possibleDoubleStackMap.put(energizerProductModel.getErpMaterialId(), size);
				}
			}
		}

		return possibleDoubleStackMap;
	}

	public double getLayerVolume(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		double layerVolume = 0.0;
		double casesVolume = 0.0;
		int casesPerLayer = 0;
		int numberOfEachInCase = 0;
		int numberOfEacInLayer = 0;
		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				numberOfEachInCase = factor.getConversionMultiplier();
				casesVolume = factor.getPackageVolume().getMeasurement();
			}
			if (factor.getAlternateUOM().equalsIgnoreCase("LAY"))
			{
				numberOfEacInLayer = factor.getConversionMultiplier();
			}

		}
		casesPerLayer = numberOfEacInLayer / numberOfEachInCase;
		layerVolume = casesPerLayer * casesVolume;
		return layerVolume;

	}

	public String getLayerVolumeUnit(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		String unit = null;

		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				unit = factor.getPackageVolume().getMeasuringUnits();
			}
		}

		return unit;

	}

	public double getLayerWeight(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		double layerWeight = 0.0;
		double casesWeight = 0.0;
		int casesPerLayer = 0;
		int numberOfEachInCase = 0;
		int numberOfEacInLayer = 0;
		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				numberOfEachInCase = factor.getConversionMultiplier();
				casesWeight = factor.getPackageWeight().getMeasurement();
			}
			if (factor.getAlternateUOM().equalsIgnoreCase("LAY"))
			{
				numberOfEacInLayer = factor.getConversionMultiplier();
			}

		}
		casesPerLayer = numberOfEacInLayer / numberOfEachInCase;
		layerWeight = casesPerLayer * casesWeight;
		return layerWeight;

	}

	public String getLayerWeightUnit(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> conversionFactor = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		String unit = null;

		for (final EnergizerProductConversionFactorModel factor : conversionFactor)
		{
			if (factor.getAlternateUOM().equalsIgnoreCase("CS"))
			{
				unit = factor.getPackageWeight().getMeasuringUnits();
			}
		}

		return unit;

	}

	public HashMap getProductsNotDoublestacked()
	{
		return doubleStackMap;
	}

	public LinkedHashMap<Integer, Integer> getFloorSpaceProductsMap()
	{
		return floorSpaceProductMap;
	}

	public LinkedHashMap getNonPalletFloorSpaceProductsMap()
	{
		return nonPalletFloorSpaceProductMap;
	}



}
