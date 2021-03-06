/**
 *
 */
package com.energizer.services.product.impl;

import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Required;

import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerShippingPointModel;
import com.energizer.services.product.EnergizerProductService;
import com.energizer.services.product.dao.EnergizerProductDAO;



/**
 * @author Bivash Pandit
 * 
 */
public class DefaultEnergizerProductService implements EnergizerProductService
{

	@Resource
	EnergizerProductService energizerProductService;

	EnergizerProductDAO energizerProductDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerCMIR(java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId) throws AmbiguousIdentifierException,
			UnknownIdentifierException
	{
		EnergizerCMIRModel energizerCMIRModel = null;
		final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId);
		if (result.isEmpty())
		{
			throw new UnknownIdentifierException("EnergizerCMIR code '" + erpMaterialId + "' not found!");
		}
		else
		{
			energizerCMIRModel = result.get(0);
		}

		return energizerCMIRModel;
	}

	/**
	 * @param shippingPointId
	 * 
	 */
	public String getShippingPointName(final String shippingPointId)
	{
		final List<EnergizerShippingPointModel> result = energizerProductDAO.getShippingPointName(shippingPointId);
		return (result.isEmpty()) ? null : result.get(0).getShippingPointName();
	}


	public List<EnergizerProductModel> getEnergizerERPMaterialID()
	{

		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerERPMaterialIDList();


		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerCMIR(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIR(final String erpMaterialId, final String b2bUnitId)
	{
		EnergizerCMIRModel energizerCMIRModel = null;
		final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId, b2bUnitId);
		/*
		 * if (result.isEmpty()) { throw new UnknownIdentifierException("EnergizerCMIR  code '" + erpMaterialId +
		 * "' not found!"); } else if (result.size() > 1) { throw new AmbiguousIdentifierException("EnergizerCMIR code '"
		 * + erpMaterialId + "' is not unique, " + result.size() + " EnergizerCMIR found!"); }
		 */

		for (final EnergizerCMIRModel cmir : result)
		{
			if (cmir.getIsActive() == true)
			{
				energizerCMIRModel = cmir;
				return energizerCMIRModel;
			}
		}

		return (result.isEmpty()) ? null : result.get((result.size()) - 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.service.EnergizerProductService#getEnergizerProductConversion(java.lang.String)
	 */
	@Override
	public EnergizerProductConversionFactorModel getEnergizerProductConversion(final String erpMaterialId, final String b2bUnitId)
	{
		final EnergizerCMIRModel energizerCMIRModel = energizerProductService.getEnergizerCMIR(erpMaterialId, b2bUnitId);

		EnergizerProductConversionFactorModel coversionFactor = null;
		String cmirUom = "";

		if (null != energizerCMIRModel)
		{
			cmirUom = energizerCMIRModel.getUom();

			final List<EnergizerProductConversionFactorModel> result = energizerProductDAO
					.getEnergizerProductConversionLst(erpMaterialId);


			if (!result.isEmpty())
			{
				for (final Iterator iterator = result.iterator(); iterator.hasNext();)
				{
					final EnergizerProductConversionFactorModel energizerProductConversionFactorModel = (EnergizerProductConversionFactorModel) iterator
							.next();

					final String alternateUOM = energizerProductConversionFactorModel.getAlternateUOM();

					if (null != alternateUOM && alternateUOM.equalsIgnoreCase(cmirUom))
					{
						coversionFactor = energizerProductConversionFactorModel;
					}
				}
			}

		}


		return coversionFactor;
	}

	@Required
	public void setEnergizerProductDAO(final EnergizerProductDAO energizerProductDAO)
	{
		this.energizerProductDAO = energizerProductDAO;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.product.EnergizerProductService#getEnergizerCMIRforCustomerMaterialID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerCMIRModel getEnergizerCMIRforCustomerMaterialID(final String customerMaterialId, final String b2bUnitId)
	{
		EnergizerCMIRModel energizerCMIRModel = null;

		final List<EnergizerCMIRModel> cmirList = energizerProductDAO.getEnergizerCMIRListForCustomerMaterialID(customerMaterialId,
				b2bUnitId);
		if (cmirList != null && cmirList.size() > 0)
		{
			for (final EnergizerCMIRModel cmir : cmirList)
			{
				if (cmir.getIsActive() == true)
				{
					energizerCMIRModel = cmir;
					return energizerCMIRModel;
				}
			}
			return cmirList.get(0);
		}


		else
		{
			return null;
		}


	}

	public EnergizerCMIRModel getEnergizerCMIRListForMatIdAndCustId(final String erpMaterialId, final String customerMaterialId,
			final String b2bUnitId)
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRListForMatIdAndCustId(erpMaterialId,
				customerMaterialId, b2bUnitId);

		/*
		 * if (result.isEmpty()) { throw new UnknownIdentifierException("EnergizerCMIR  code '" + erpMaterialId +
		 * "' not found!"); } else if (result.size() > 1) { throw new AmbiguousIdentifierException("EnergizerCMIR code '"
		 * + erpMaterialId + "' is not unique, " + result.size() + " EnergizerCMIR found!"); }
		 */
		for (final EnergizerCMIRModel cmir : result)
		{
			if (cmir.getIsActive() == true)
			{

				return cmir;
			}
		}
		return (result.isEmpty()) ? null : result.get(0);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerCMIRList(java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId)
	{
		// YTODO Auto-generated method stub
		final List<EnergizerCMIRModel> result = energizerProductDAO.getEnergizerCMIRList(erpMaterialId);
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerOrphanedProductList()
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerOrphanedProductList()
	{
		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerOrphanedProductList();
		return result;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId)
	{
		final EnergizerPriceRowModel result = energizerProductDAO.getEnergizerPriceRowForB2BUnit(erpMaterialID, b2bUnitId);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.EnergizerProductService#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(final String sapCatgyCode)
	{
		final List<EnergizerProductModel> result = energizerProductDAO.getEnergizerProductListForSapCatgy(sapCatgyCode);
		return result;
	}

	@Override
	public List<EnergizerCMIRModel> getERPMaterialIdForImageReferenceId(final String imageId)
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getERPMaterialIdForImageReferenceId(imageId);
		return result;
	}


	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRList()
	{
		final List<EnergizerCMIRModel> result = energizerProductDAO.getAllEnergizerCMIRList();

		return result;
	}

	@Override
	public List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialID, final String b2bUnitId)
	{
		final List<EnergizerPriceRowModel> result = energizerProductDAO.getAllEnergizerPriceRowForB2BUnit(erpMaterialID, b2bUnitId);
		return result;
	}

	public List<EnergizerProductConversionFactorModel> getAllEnergizerProductConversion(final String erpMaterialId)
	{


		return energizerProductDAO.getEnergizerProductConversionLst(erpMaterialId);


	}

	@Override
	public EnergizerProductModel getProductWithCode(final String code)
	{
		return energizerProductDAO.getProductWithCode(code);
	}
}
