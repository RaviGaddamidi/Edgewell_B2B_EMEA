/**
 * 
 */
package com.energizer.services.product;

import java.util.List;

import com.energizer.core.product.data.EnergizerFileUploadData;


/**
 * @author M9005674
 * 
 */
public interface EnergizerExcelRowtoModelService
{

	/**
	 * @param energizerFileUploadModels
	 * @return
	 */
	List<EnergizerFileUploadData> processExcelRowtoBean(List<EnergizerFileUploadData> energizerFileUploadModels);
}
