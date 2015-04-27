/**
 * 
 */
package com.energizer.services.product.dao;

import java.util.List;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerShippingPointModel;


/**
 * @author Bivash Pandit
 * 
 *         anitha.shastry added method getOrphanedProductList()
 * 
 *         anitha.shastry added method getEnergizerProductListForSapCatgy()
 * 
 */
public interface EnergizerProductDAO
{


	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId);

	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId, String b2bUnitId);

	List<EnergizerProductConversionFactorModel> getEnergizerProductConversionLst(String erpMaterialId);

	List<EnergizerCMIRModel> getEnergizerCMIRListForCustomerMaterialID(String customerMaterialID, String b2bUnitId);

	List<EnergizerCMIRModel> getEnergizerCMIRListForMatIdAndCustId(String erpMaterialID, String customerMaterialID);

	List<EnergizerProductModel> getEnergizerOrphanedProductList();

	EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(String erpMaterialID, String b2bUnitId);

	List<EnergizerProductModel> getEnergizerProductListForSapCatgy(String sapCatgyCode);

	List<EnergizerShippingPointModel> getShippingPointName(final String shippingPointId);


}
