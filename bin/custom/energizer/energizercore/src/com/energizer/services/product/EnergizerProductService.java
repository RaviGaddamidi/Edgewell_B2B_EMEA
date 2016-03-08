/**
 *
 */
package com.energizer.services.product;

import java.util.List;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;


/**
 * @author Bivash Pandit
 * 
 */
public interface EnergizerProductService
{

	EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId);

	List<EnergizerCMIRModel> getEnergizerCMIRList(String erpMaterialId);

	EnergizerCMIRModel getEnergizerCMIR(String erpMaterialId, String b2bUnitId);

	EnergizerProductConversionFactorModel getEnergizerProductConversion(String erpMaterialId, String b2bUnitId);

	public EnergizerCMIRModel getEnergizerCMIRforCustomerMaterialID(String customerMaterialId, String b2bUnitId);

	public EnergizerCMIRModel getEnergizerCMIRListForMatIdAndCustId(String erpMaterialId, String customerMaterialId,
			String b2bUnitId);

	public List<EnergizerProductModel> getEnergizerOrphanedProductList();

	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(String erpMaterialID, String b2bUnitId);

	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(String sapCatgyCode);

	public String getShippingPointName(final String shippingPointId);

	public List<EnergizerCMIRModel> getAllEnergizerCMIRList();

	List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId);

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversion(final String erpMaterialId);

	public List<EnergizerProductModel> getEnergizerERPMaterialID();

	public EnergizerProductModel getProductWithCode(final String code);
}
