/**
 * 
 */
package com.energizer.services.product.dao.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.core.model.EnergizerProductConversionFactorModel;
import com.energizer.core.model.EnergizerProductModel;
import com.energizer.core.model.EnergizerRegionGenericProductsModel;
import com.energizer.core.model.EnergizerRegionModel;
import com.energizer.core.model.EnergizerShippingPointModel;
import com.energizer.services.product.dao.EnergizerProductDAO;


/**
 * @author Bivash Pandit
 * 
 *         anitha.shastry added method getOrphanedProductList()
 */
public class DefaultEnergizerProductDAO implements EnergizerProductDAO
{

	@Resource
	FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getMoq()
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId)
	{


		final String queryString = //
		"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
				+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} "//
				+ "WHERE " + "{p:" + EnergizerCMIRModel.ERPMATERIALID + "}=?erpMaterialId ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getEnergizerCMIRList(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRList(final String erpMaterialId, final String b2bUnitId)
	{

		EnergizerB2BUnitModel energizerB2BUnitModel = null;
		final String queryString = //
		"SELECT {p:" + EnergizerB2BUnitModel.PK + "}" //
				+ "FROM {" + EnergizerB2BUnitModel._TYPECODE + " AS p} "//
				+ "WHERE " + "{p:" + EnergizerB2BUnitModel.UID + "}=?b2bUnitId ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("b2bUnitId", b2bUnitId);

		final List<EnergizerB2BUnitModel> b2bUnitModels = flexibleSearchService.<EnergizerB2BUnitModel> search(query).getResult();
		for (final Iterator iterator = b2bUnitModels.iterator(); iterator.hasNext();)
		{
			energizerB2BUnitModel = (EnergizerB2BUnitModel) iterator.next();
		}


		final String queryString1 = //
		"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
				+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p} "//
				+ "WHERE " + "{p:" + EnergizerCMIRModel.ERPMATERIALID + "}=?erpMaterialId "//
				+ "AND {p:" + EnergizerCMIRModel.B2BUNIT + "}=?energizerB2BUnitModel ";

		final FlexibleSearchQuery query1 = new FlexibleSearchQuery(queryString1);

		query1.addQueryParameter("erpMaterialId", erpMaterialId);
		query1.addQueryParameter("energizerB2BUnitModel", energizerB2BUnitModel);

		return flexibleSearchService.<EnergizerCMIRModel> search(query1).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.facades.product.dao.EnergizerProductDAO#getEnergizerProductConversionLst(java.lang.String)
	 */
	@Override
	public List<EnergizerProductConversionFactorModel> getEnergizerProductConversionLst(final String erpMaterialId)
	{

		final String queryString = //
		"SELECT {p:" + EnergizerProductConversionFactorModel.PK + "}" //
				+ "FROM {" + EnergizerProductConversionFactorModel._TYPECODE + " AS p} "//
				+ "WHERE " + "{p:" + EnergizerProductConversionFactorModel.ERPMATERIALID + "}=?erpMaterialId ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);

		return flexibleSearchService.<EnergizerProductConversionFactorModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerCMIRListForCustomerMaterialID(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRListForCustomerMaterialID(final String customerMaterialID,
			final String b2bUnitId)
	{
		final String queryString = //
		"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
				+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p JOIN EnergizerB2BUnit as eu ON {p.b2bUnit}={eu.pk}} "//
				+ "WHERE " + "{p:" + EnergizerCMIRModel.CUSTOMERMATERIALID + "}=?erpMaterialId "//
				+ "AND {eu:" + EnergizerB2BUnitModel.UID + "}=?energizerB2BUnitModel ";


		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("erpMaterialId", customerMaterialID);
		query.addQueryParameter("energizerB2BUnitModel", b2bUnitId);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}

	@Override
	public List<EnergizerCMIRModel> getEnergizerCMIRListForMatIdAndCustId(final String erpMaterialId,
			final String customerMaterialID)
	{

		final String queryString = "SELECT {C.PK},{C.ERPMATERIALID},{C.CUSTOMERMATERIALID},{C.SHIPPINGPOINT}, {C.UOM} from  "
				+ " {EnergizerCMIR as C   " + " JOIN EnergizerProduct as P ON {P:code}={C:ERPMaterialId}  "
				+ " AND {C:customerMaterialId}= ?customerMaterialId " + "AND {C:ERPMaterialId}= ?erpMaterialId" + " }   ";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("customerMaterialId", customerMaterialID);

		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerOrphanedProductList()
	 */
	@Override
	public List<EnergizerProductModel> getEnergizerOrphanedProductList()
	{
		final String queryString = "SELECT {prod.pk}, {prod.code} FROM {EnergizerProduct AS prod} WHERE not EXISTS "
				+ "({{select * from {CategoryProductRelation as cat} WHERE {cat.target}={prod:pk} }})";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);

		return flexibleSearchService.<EnergizerProductModel> search(query).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getEnergizerPriceRowForB2BUnit(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public EnergizerPriceRowModel getEnergizerPriceRowForB2BUnit(final String erpMaterialId, final String b2bUnitId)
	{
		final String queryString = "select {enrprice.pk} from " + "{EnergizerCMIR as cmir JOIN EnergizerB2BUnit AS myb2bunit ON "
				+ "{cmir.b2bUnit}={myb2bunit.pk} JOIN EnergizerProduct AS prod ON "
				+ "{cmir.erpMaterialId}={prod.code} JOIN EnergizerPriceRow AS enrprice ON "
				+ " {enrprice.b2bUnit}={myb2bunit.pk} and {enrprice.product}={prod.pk} " + "} " + " WHERE "
				+ "{myb2bunit.uid}=?b2bUnitId and " + "{cmir.erpMaterialId}=?erpMaterialId";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("b2bUnitId", b2bUnitId);
		query.setCount(1);

		final List<EnergizerPriceRowModel> result = flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();
		if (result != null && !result.isEmpty())
		{
			return result.get(0);
		}

		return null;
	}

	@Override
	public List<EnergizerProductModel> getEnergizerProductListForSapCatgy(final String sapCatgyCode)
	{
		final String queryString = "SELECT {prod.pk} FROM {EnergizerProduct AS prod} " + " WHERE {prod:"
				+ EnergizerProductModel.SAPCATEGORYCONCATVALUE + "}=?sapCatgyCode";
		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("sapCatgyCode", sapCatgyCode);
		return flexibleSearchService.<EnergizerProductModel> search(query).getResult();
	}




	@Override
	public List<EnergizerShippingPointModel> getShippingPointName(final String shippingPointId)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery(
				"SELECT {pk} FROM {EnergizerShippingPoint} where {shippingPointId}=?shippingPointId");
		retreiveQuery.addQueryParameter("shippingPointId", shippingPointId);
		return flexibleSearchService.<EnergizerShippingPointModel> search(retreiveQuery).getResult();

	}

	@Override
	public List<EnergizerCMIRModel> getAllEnergizerCMIRList()
	{
		final String querystring = //
		"SELECT {p:" + EnergizerCMIRModel.PK + "}" //
				+ "FROM {" + EnergizerCMIRModel._TYPECODE + " AS p}";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(querystring);
		return flexibleSearchService.<EnergizerCMIRModel> search(query).getResult();
	}

	@Override
	public List<EnergizerPriceRowModel> getAllEnergizerPriceRowForB2BUnit(final String erpMaterialId, final String b2bUnitId)
	{
		final String queryString = "select {enrprice.pk} from " + "{EnergizerCMIR as cmir JOIN EnergizerB2BUnit AS myb2bunit ON "
				+ "{cmir.b2bUnit}={myb2bunit.pk} JOIN EnergizerProduct AS prod ON "
				+ "{cmir.erpMaterialId}={prod.code} JOIN EnergizerPriceRow AS enrprice ON "
				+ " {enrprice.b2bUnit}={myb2bunit.pk} and {enrprice.product}={prod.pk} " + "} " + " WHERE "
				+ "{myb2bunit.uid}=?b2bUnitId and " + "{cmir.erpMaterialId}=?erpMaterialId";

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryString);
		query.addQueryParameter("erpMaterialId", erpMaterialId);
		query.addQueryParameter("b2bUnitId", b2bUnitId);

		return flexibleSearchService.<EnergizerPriceRowModel> search(query).getResult();

	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#getCountryRegion(java.lang.String)
	 */
	@Override
	public List<EnergizerRegionModel> getCountryRegion(final String country)
	{
		final FlexibleSearchQuery retreiveQuery = new FlexibleSearchQuery("SELECT {pk} FROM {EnergizerRegion}");
		//retreiveQuery.addQueryParameter("regionName", regionName);
		return flexibleSearchService.<EnergizerRegionModel> search(retreiveQuery).getResult();
	}



	/*
	 * (non-Javadoc)
	 * 
	 * @see com.energizer.services.product.dao.EnergizerProductDAO#hasCountryGenericCatalog(java.lang.String)
	 */
	@Override
	public Boolean hasCountryGenericCatalog(final String b2bunitCountry)
	{
		final FlexibleSearchQuery query = new FlexibleSearchQuery(
				"select *  from {EnergizerRegionGenericProducts} where {country}=?b2bunitCountry");
		query.addQueryParameter("b2bunitCountry", b2bunitCountry);

		final List<EnergizerRegionGenericProductsModel> genericProductsList = flexibleSearchService
				.<EnergizerRegionGenericProductsModel> search(query).getResult();
		return (genericProductsList == null || genericProductsList.isEmpty()) ? false : true;
	}
}
