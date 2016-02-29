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
import de.hybris.platform.util.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerCartService.class.getName());

	private static final String TWENTY_FEET_CONTAINER_VOLUME_KEY = "twenty.feet.container.volume";
	private static final String TWENTY_FEET_CONTAINER_WEIGHT_KEY = "twenty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_VOLUME_KEY = "fourty.feet.container.volume";
	private static final String FORTY_FEET_CONTAINER_WEIGHT_KEY = "fourty.feet.container.weight";
	private static final String FORTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.40FT";
	private static final String TWENTY_FEET_CONTAINER_HEIGHT_INCHES = "heightInInches.20FT";

	private final BigDecimal ZERO = new BigDecimal(0);

	boolean nonPalletProductsExists = false;

	List<String> message = null;
	List<EnergizerProductPalletHeight> products = null;
	HashMap doubleStackMap = null;
	ArrayList<EnergizerProductPalletHeight> productsListA = null;
	final ArrayList<EnergizerProductPalletHeight> sortedProductsListA = new ArrayList<EnergizerProductPalletHeight>();
	ArrayList<EnergizerProductPalletHeight> productsListB = null;


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

		twentyFeetContainerVolume = twentyFeetContainerVolume.setScale(2, BigDecimal.ROUND_UP);
		twentyFeetContainerWeight = twentyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);
		fourtyFeetContainerVolume = fourtyFeetContainerVolume.setScale(2, BigDecimal.ROUND_UP);
		fourtyFeetContainerWeight = fourtyFeetContainerWeight.setScale(2, BigDecimal.ROUND_UP);


		if (packingOption.equals(packingOptionWithNoAlgorithm) || !enableContOptimization)
		{
			if (message != null && message.size() > 0)
			{
				message.clear();
			}
			if (products != null && products.size() > 0)
			{
				products.clear();
			}
			if (!enableContOptimization)
			{
				containerHeight = null;
			}
			cartDataTemp = calCartContainerUtilizationWithSlipsheets(cartData, containerHeight);
			doubleStackMap = new HashMap();
		}
		else
		{
			cartDataTemp = calCartContainerUtilizationWithSlipSheetsWoodenBase(cartData, containerHeight, packingOption);
		}

		return cartDataTemp;
	}

	public CartData calCartContainerUtilizationWithSlipSheetsWoodenBase(final CartData cartData, final String containerHeight,
			final String packingOption)
	{
		doubleStackMap = new HashMap();
		double availableVolume = 100;
		double availableWeight = 100;
		double availableHeight = 0;
		double volumeOfNonPalletProducts = 0;
		double weightOfNonPalletProducts = 0;
		int floorSpaceCount = 0;
		double volume = 0;
		double weight = 0;
		long palletCount = 0;
		double totalPalletHeight = 0;
		int totalPalletsCount = 0;
		double productWeight = 0;

		message = new ArrayList<String>();
		products = new ArrayList<EnergizerProductPalletHeight>();
		productsListA = new ArrayList<EnergizerProductPalletHeight>();
		productsListB = new ArrayList<EnergizerProductPalletHeight>();

		if (containerHeight.equals("20FT"))
		{
			totalPalletsCount = 20;
		}
		else if (containerHeight.equals("40FT"))
		{
			totalPalletsCount = 40;
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

		LOG.info("Pallet count:" + palletCount);

		if (volume > 100 || palletCount > totalPalletsCount)
		{
			//Display an error message "Reduce the products in the cart"
			LOG.info("Either volume is greater than 100 or pallet count is greater than" + totalPalletsCount);
			message.add("Please reduce  products fom the cart");
			cartData.setIsContainerFull(true);
		}
		else
		{
			if (nonPalletProductsExists)
			{
				LOG.info("Non pallet products exist");
				volumeOfNonPalletProducts = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFNONPALLETPRODUCTS"),
						getVolumeOfProductsInCart(cartData, "VOLUMEOFNONPALLETPRODUCTS"), containerHeight).getPercentVolumeUses();

				weightOfNonPalletProducts = getPercentage(getWeightOfProductsInCart(cartData, "WEIGHTOFNONPALLETPRODUCTS"),
						getVolumeOfProductsInCart(cartData, "VOLUMEOFNONPALLETPRODUCTS"), containerHeight).getPercentWeightUses();

				availableVolume = availableVolume - volumeOfNonPalletProducts;
				availableWeight = availableWeight - weightOfNonPalletProducts;
				LOG.info("available volume after deleting volume of non pallet products" + availableVolume);
			}

			Collections.sort(productsListA);

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
										+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId())
										+ getWeightOfGivenMaterial(productsListA.get(minIndex - 1).getErpMaterialId());
								productsListA.remove(maxIndex - 1); // removeHighestLowestPalletList(productsListA, maxIndex);;
								productsListA.remove(minIndex - 1);
							}
							else
							{
								productWeight = productWeight
										+ getWeightOfGivenMaterial(productsListA.get(maxIndex - 1).getErpMaterialId());
								productsListA.remove(maxIndex - 1);
							}

							if (containerHeight.equals("20FT"))
							{
								availableVolume = availableVolume - 10;
							}
							if (containerHeight.equals("40FT"))
							{
								availableVolume = availableVolume - 5;
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
							final double volumeOfHighestPallet = getVolumeOfHighestPallet(tempProduct.getErpMaterialId());
							final double percentageVolumeOfHighestPallet = getPercentage(new BigDecimal(0),
									new BigDecimal(volumeOfHighestPallet), containerHeight).getPercentVolumeUses();
							LOG.info("volume of single product:" + volumeOfHighestPallet);

							if (!doubleStackMap.containsKey(tempProduct.getErpMaterialId()))
							{
								doubleStackMap = getPossibleDoubleStackedProducts(tempProduct.getErpMaterialId(), availableHeight,
										doubleStackMap);
							}
							productWeight = productWeight + getWeightOfGivenMaterial(tempProduct.getErpMaterialId());
							productsListA.remove(tempProduct);

							LOG.info("Available Height:" + availableHeight);
							availableVolume = availableVolume - percentageVolumeOfHighestPallet;
						}
						else
						{
							if (containerHeight.equals("20FT"))
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

			if (productsListA.size() > 0)
			{
				LOG.info("The list of products which are not added to the cart: ");

				if (products != null && products.size() > 0)
				{
					products.clear();
				}

				for (final Iterator iterator = productsListA.iterator(); iterator.hasNext();)
				{
					final EnergizerProductPalletHeight tempEnergizerProductPalletHeight = (EnergizerProductPalletHeight) iterator
							.next();
					products.add(tempEnergizerProductPalletHeight);
					LOG.info(" ERP MaterialID: " + tempEnergizerProductPalletHeight.getErpMaterialId());
				}
				message.add("Below mentioned products cannot be added to the container with selected packing type");
			}

			LOG.info("******************* The final data is as below: *****************");
			LOG.info("FloorSpace count: " + floorSpaceCount);
			LOG.info(" Available Height: " + availableHeight);
			LOG.info(" After filling container Available Volume: " + availableVolume);
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
		availableWeight = availableWeight
				- getPercentage(new BigDecimal(productWeight), new BigDecimal(0), containerHeight).getPercentWeightUses();
		cartData.setTotalProductVolumeInPercent((Math.round((100 - availableVolume) * 100.0) / 100.0));
		cartData.setTotalProductWeightInPercent((Math.round((100 - availableWeight) * 100.0) / 100.0));
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
		if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE") && containerHeight.equals("40FT"))
		{
			availableHeight = (fortyFeetContainerHeightInInches - 5) * 2.54;
		}
		else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals("40FT"))
		{
			availableHeight = (fortyFeetContainerHeightInInches - (5 * 2)) * 2.54;
		}
		else if (packingOption.equals("1 SLIP SHEET AND 1 WOODEN BASE") && containerHeight.equals("20FT"))
		{
			availableHeight = (twentyFeetContainerHeightInInches - 5) * 2.54;
		}
		else if (packingOption.equals("2 WOODEN BASE") && containerHeight.equals("20FT"))
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
			BigDecimal totalCartVolume = new BigDecimal(0);
			BigDecimal totalCartWt = new BigDecimal(0);
			/**
			 * # Container volume in M3 and weight in KG ##########################################
			 *
			 * twenty.feet.container.volume=30.44056 twenty.feet.container.weight=15961.90248
			 * fourty.feet.container.volume=70.62209 fourty.feet.container.weight=18234.3948
			 */

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
						lineItemTotvolume = unitVolume.multiply(itemQuantity);
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
						lineItemTotWt = unitWeight.multiply(itemQuantity);
					}
					final String weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
					totalCartWt = totalCartWt.add(EnergizerWeightOrVolumeConverter.getConversionValue(weightUnit, lineItemTotWt));
				}

			}

			LOG.info("|| totalCartWt => " + totalCartWt + " || totalCartVolume => " + totalCartVolume);
			ContainerData containerData = null;
			if (containerHeight != null)
			{
				containerData = getPercentage(totalCartWt, totalCartVolume, containerHeight);
				cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
				cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());
			}
			else
			{
				containerData = getPercentageContainerUtil(totalCartWt, totalCartVolume);
				cartData.setTotalProductVolumeInPercent(containerData.getPercentVolumeUses());
				cartData.setTotalProductWeightInPercent(containerData.getPercentWeightUses());
			}
			cartData.setContainerHeight(containerData.getContainerType());

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

		final ContainerData containerData = new ContainerData();
		if (totalCartWt.compareTo(twentyFeetContainerWeight) == -1 && totalCartVolume.compareTo(twentyFeetContainerVolume) == -1)
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

	private ContainerData getPercentage(BigDecimal totalCartWt, BigDecimal totalCartVolume, final String containerHeight)
	{

		totalCartWt = totalCartWt.setScale(2, BigDecimal.ROUND_UP);
		totalCartVolume = totalCartVolume.setScale(2, BigDecimal.ROUND_UP);

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
			final BigDecimal unitVolume = new BigDecimal(coversionFactor.getPackageVolume().getMeasurement());
			final String volumeUnit = coversionFactor.getPackageVolume().getMeasuringUnits();
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
			final BigDecimal unitWeight = new BigDecimal(coversionFactor.getPackageWeight().getMeasurement());
			final String weightUnit = coversionFactor.getPackageWeight().getMeasuringUnits();
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

	public double getVolumeOfHighestPallet(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		final String measuringUnit;
		final double volume;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals("PAL"))
			{
				measuringUnit = UOM.getPackageVolume().getMeasuringUnits();
				volume = UOM.getPackageVolume().getMeasurement();
				return EnergizerWeightOrVolumeConverter.getConversionValue(measuringUnit, new BigDecimal(volume)).doubleValue();
				//return UOM.getPackageVolume().getMeasurement();
			}

		}
		return 0;
	}

	public double getWeightOfGivenMaterial(final String erpMaterialId)
	{
		final List<EnergizerProductConversionFactorModel> energizerProductConversionFactorModel = energizerProductService
				.getAllEnergizerProductConversion(erpMaterialId);
		final String measuringUnit;
		final double weight;
		for (final EnergizerProductConversionFactorModel UOM : energizerProductConversionFactorModel)
		{
			if (UOM.getAlternateUOM().equals("PAL"))
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

	public HashMap getProductsNotDoublestacked()
	{
		return doubleStackMap;
	}

}
