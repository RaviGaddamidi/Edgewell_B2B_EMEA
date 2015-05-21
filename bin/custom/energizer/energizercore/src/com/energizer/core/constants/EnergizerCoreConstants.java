/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2014 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 *
 *  
 */
package com.energizer.core.constants;

/**
 * Global class for all B2BAcceleratorCore constants. You can add global constants for your extension into this class.
 */
@SuppressWarnings("PMD")
public class EnergizerCoreConstants extends GeneratedEnergizerCoreConstants
{
	public static final String EXTENSIONNAME = "energizercore";

	// Constants for ProductCSVProcessor 
	public static final String ERPMATERIAL_ID = "ERPMaterialID";
	public static final String PRODUCT_GROUP = "Product Group";
	public static final String LIST_PRICE = "ListPrice";
	public static final String LIST_PRICE_CURRENCY = "ListPriceCurrency";
	public static final String OBSOLETE_STATUS = "ObsoleteStatus";
	public static final String LANGUAGE = "Language";
	public static final String PRODUCT_DESCRIPTION = "ProductDesription";

	// Constants for EnergizerMediaCSVProcessor 
	public static final String THUMBNAIIL_PATH = "ThumnailPath";
	public static final String DISPLAY_IMAGE_PATH = "DisplayImagePath";

	// Constants for  EnergizerCMIRCSVProcessor
	public static final String ENERGIZER_ACCOUNT_ID = "EnergizerAccountID";
	public static final String CUSTOMER_MATERIAL_ID = "CustomerMaterialID";
	public static final String CUSTOMER_MATERIAL_DESCRIPTION = "CustomerMaterial Description";
	public static final String SHIPMENT_POINT_NO = "ShipmentPointNumber";
	public static final String CUSTOMER_LIST_PRICE_CURRENCY = "CustomerListprice currency";
	public static final String CUSTOMER_LIST_PRICE = "CustomerListPrice";

	// Constants for  EnergizerProductConversionCSVProcessor
	public static final String ALTERNATE_UOM = "AlternateUOM";
	public static final String BASE_UOM_MULTIPLIER = "BaseUOMMultiplier";
	public static final String VOLUME_IN_UOM = "VolumeInUOM";
	public static final String VOLUME_UOM = "VolumeUOM";
	public static final String WEIGHT_IN_UOM = "WeightInUOM";
	public static final String WEIGHT_UOM = "WeightUOM";

	// Constants for  EnergizerSalesUOMCSVProcessor
	public static final String CUSTOMER_ID = "customerId";
	public static final String SALES_ORG = "salesOrganisation";
	public static final String DISTRIBUTION_CHANNEL = "distributionChannel";
	public static final String DIVISION = "division";
	//public static final String SALES_AREA_ID = "salesAreaId";
	public static final String SEGMENT_ID = "segmentId";
	public static final String FAMILY_ID = "familyId";
	public static final String UOM = "unitOfMeasure";
	public static final String MOQ = "minimumOrderQuantity";

	// Constants for  EnergizerProductCategoryCSVProcessor
	public static final String SEGMENT_NAME = "SegmentName";
	public static final String SEGMENT_DESCRIPTION = "SegmentDescription";
	public static final String FAMILY_NAME = "FamilyName";
	public static final String FAMILY_DESCRIPTION = "FamilyDescription";
	public static final String GROUP_NAME = "GroupName";
	public static final String GROUP_DESCRIPTION = "GroupDescription";
	public static final String SUBGROUP_NAME = "SubGroupName";
	public static final String SUBGROUP_DESCRIPTION = "SubGroupDescription";

	// Constants for  EnergizerCategoryCSVProcessor
	public static final String ERP_CATEGORY_CODE = "ERPCategoryCode";
	public static final String MARKETING_CATEGORY_CODE = "MarketingCategoryCode";
	public static final String MARKETING_CATEGORY_NAME = "MarketingCategoryName";

	// uom constants
	public static final String EA = "EA";
	public static final String INTERPACK = "PK";
	public static final String CASE = "CS";
	public static final String LAYER = "LAY";
	public static final String PALLET = "PAL";

	private EnergizerCoreConstants()
	{
		super();
		assert false;
	}

}
