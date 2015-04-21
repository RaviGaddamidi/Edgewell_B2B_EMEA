/**
 * 
 */
package com.energizer.facades.order.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.product.data.EnergizerFileUploadData;
import com.energizer.facades.order.EnergizerExcelUploadFacade;
import com.energizer.services.product.EnergizerExcelRowtoModelService;


/**
 * @author M9005674
 * 
 */
public class DefaultEnergizerExcelUploadFacade implements EnergizerExcelUploadFacade
{

	@Resource(name = "energizerExcelRowtoModelService")
	EnergizerExcelRowtoModelService energizerExcelRowtoModelService;

	@Override
	public List<EnergizerFileUploadData> convertExcelRowtoBean(final List<EnergizerFileUploadData> energizerFileUploadModels)
	{
		List<EnergizerFileUploadData> energizerFileUploadBeans = new ArrayList<EnergizerFileUploadData>();
		energizerFileUploadBeans = energizerExcelRowtoModelService.processExcelRowtoBean(energizerFileUploadModels);
		return energizerFileUploadBeans;
	}

}
