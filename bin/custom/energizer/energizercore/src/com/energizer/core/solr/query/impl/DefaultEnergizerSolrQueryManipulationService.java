/**
 * 
 */
package com.energizer.core.solr.query.impl;

import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.core.model.EnergizerRegionModel;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationService;
import com.energizer.core.solr.query.EnergizerSolrQueryManipulationServiceTest;
import com.energizer.services.product.dao.EnergizerProductDAO;



/**
 * This class implements the EnergizerSolrQueryManipulationService to define the logic for manipulating the solr query
 * to enforce the b2bunit specific catalog.
 * 
 * @author kaushik.ganguly
 * @see EnergizerSolrQueryManipulationServiceTest
 */
public class DefaultEnergizerSolrQueryManipulationService implements EnergizerSolrQueryManipulationService
{

	@Resource(name = "b2bCommerceUserService")
	private B2BCommerceUserService b2bUserService;

	@Resource(name = "userService")
	private UserService userService;

	@Resource(name = "energizerProductDAO")
	private EnergizerProductDAO energizerProductDAO;

	private static final Logger LOG = Logger.getLogger(DefaultEnergizerSolrQueryManipulationService.class);


	/**
	 * This method retrieves the EnergizerB2BUnitModel of the currently logged in user.
	 * 
	 * @see EnergizerB2BUnitModel
	 * @return EnergizerB2BUnitModel
	 */
	@Override
	public EnergizerB2BUnitModel getB2BUnitForLoggedInUser()
	{
		return b2bUserService.getParentUnitForCustomer(userService.getCurrentUser().getUid());
	}

	/**
	 * This method solr query part involving the b2bunit
	 * 
	 * @return String
	 */
	@Override
	public String getB2BUnitInSolrQuery()
	{
		final EnergizerB2BUnitModel b2bUnit = getB2BUnitForLoggedInUser();
		if (b2bUnit != null)
		{
			return B2B_UNIT_SEARCHQUERY_PREFIX + QUERY_CONNECTOR + b2bUnit.getUid();
		}
		else
		{
			return EMPTY_STRING;
		}

	}

	/**
	 * This method manipulates the SOLR query for the category listing page.
	 * 
	 * @param sortCode
	 *           This is the sort code selected by the user in the category listing page.
	 * @param existingQuery
	 *           This is the already constructed SOLR query that needs manipulation
	 * @return String
	 */
	@Override
	public String getSolrQueryForCategorySearch(final String sortCode, final String existingQuery)
	{
		final String b2bUnit = getB2BUnitInSolrQuery();
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();
		String retVal = EMPTY_STRING;
		if (b2bUnitModel == null)
		{
			return retVal;
		}

		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = EMPTY_STRING + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + b2bUnit;
			}
			else
			{
				retVal = EMPTY_STRING + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + b2bUnit;
			}

		}
		else
		{

			if (sortCode != null && existingQuery.indexOf(b2bUnitModel.getUid() + QUERY_CONNECTOR + sortCode) == -1)
			{
				final String strippingSortOrderfromQuery = existingQuery.substring(
						existingQuery.indexOf(QUERY_CONNECTOR, existingQuery.indexOf(QUERY_CONNECTOR) + 1) + 1, existingQuery.length());
				retVal = EMPTY_STRING + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + strippingSortOrderfromQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}
			else if (sortCode != null && existingQuery.indexOf(b2bUnitModel.getUid() + QUERY_CONNECTOR + sortCode) != -1)
			{
				retVal = existingQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}
			}
			else if (sortCode == null)
			{
				final String strippingSortOrderfromQuery = existingQuery.substring(
						existingQuery.indexOf(QUERY_CONNECTOR, existingQuery.indexOf(QUERY_CONNECTOR) + 1) + 1, existingQuery.length());
				retVal = EMPTY_STRING + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + strippingSortOrderfromQuery;

				if (retVal.indexOf(QUERY_CONNECTOR + b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}
			}
		}
		return retVal;
	}


	/**
	 * This method manipulates the SOLR query for the search listing page.
	 * 
	 * @param sortCode
	 *           This is the sort code selected by the user in the category listing page.
	 * @param existingQuery
	 *           This is the already constructed SOLR query that needs manipulation
	 * 
	 * @return String
	 */
	@Override
	public String getSolrQueryForTextSearchPage(final String sortCode, final String existingQuery, final String b2bunitCatalogType)
	{
		//final String b2bUnit = getB2BUnitInSolrQuery();
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();
		String retVal = EMPTY_STRING;
		if (b2bUnitModel == null)
		{
			return retVal;
		}

		if (b2bunitCatalogType != null && b2bunitCatalogType.equalsIgnoreCase("generic"))
		{
			retVal = getGenericCatalogQuery(sortCode, existingQuery);
		}

		/*
		 * else if (b2bunitCatalogType != null && b2bunitCatalogType.equalsIgnoreCase("genericpluscmir")) { retVal =
		 * getGenericCMIRQuery(sortCode, existingQuery); }
		 */

		else
		{
			//cmir only
			retVal = getCMIRCatalogQuery(sortCode, existingQuery);
		}
		return retVal;
	}

	private String getGenericCMIRQuery(final String sortCode, final String existingQuery)
	{
		String retVal = EMPTY_STRING;
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();
		final String b2bUnit = "(" + B2B_UNIT_SEARCHQUERY_PREFIX + QUERY_CONNECTOR + "" + b2bUnitModel.getUid() + ")";
		//final String regionStr = "(prdtRegion:" + b2bUnitModel.getDistributorRegion() + " AND  b2bunit:*) OR b2bunit:"
		//		+ b2bUnitModel.getUid();
		final java.util.List<EnergizerRegionModel> regions = energizerProductDAO.getCountryRegion(b2bUnitModel
				.getDistributorCountry());
		String regionStr = "";
		if (regions != null && !regions.isEmpty())
		{
			for (final EnergizerRegionModel region : regions)
			{
				final List<String> countries = region.getCountryList();
				if (countries != null && !countries.isEmpty())
				{
					if (countries.contains(b2bUnitModel.getDistributorCountry()))
					{
						regionStr = "region" + QUERY_CONNECTOR + "" + region.getRegionName() + "";
					}
				}

			}
		}
		String countryStr = "";
		if (energizerProductDAO.hasCountryGenericCatalog(b2bUnitModel.getDistributorCountry()))
		{
			countryStr = "country" + QUERY_CONNECTOR + "" + b2bUnitModel.getDistributorCountry() + "";
		}

		//String countryStr = "country" + QUERY_CONNECTOR + b2bUnitModel.getDistributorCountry();
		if (!regionStr.isEmpty())
		{
			countryStr += " " + regionStr;
		}

		//String searchQuery = countryStr + " " + b2bUnit;
		String searchQuery = "" + countryStr + "";

		//final String regionStr = "(prdtRegion_string_mv%3ANA+AND++b2bunit_string_mv%3A*)+OR+b2bunit_string_mv%3A0000001003";

		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR
						+ searchQuery;
			}
			else
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX
						+ QUERY_CONNECTOR + searchQuery;
			}
		}
		else
		{
			if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{
				retVal = existingQuery + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + searchQuery;

			}
			if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{

				retVal = existingQuery + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + searchQuery;
			}
			else if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(regionStr) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + searchQuery;
				}

			}
			else if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(regionStr) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + searchQuery;
				}

			}
		}
		LOG.info(" getGenericCMIRQuery == " + retVal);
		return retVal;
	}

	private String getGenericCatalogQuery(final String sortCode, final String existingQuery)
	{
		String retVal = EMPTY_STRING;
		final EnergizerB2BUnitModel b2bUnitModel = getB2BUnitForLoggedInUser();

		final java.util.List<EnergizerRegionModel> regions = energizerProductDAO.getCountryRegion(b2bUnitModel
				.getDistributorCountry());
		String regionStr = "";
		if (regions != null && !regions.isEmpty())
		{
			for (final EnergizerRegionModel region : regions)
			{
				final List<String> countries = region.getCountryList();
				if (countries != null && !countries.isEmpty())
				{
					if (countries.contains(b2bUnitModel.getDistributorCountry()))
					{
						regionStr = "region" + QUERY_CONNECTOR + region.getRegionName();
					}
				}

			}
		}

		String countryStr = "country" + QUERY_CONNECTOR + b2bUnitModel.getDistributorCountry();
		if (!regionStr.isEmpty())
		{
			countryStr += QUERY_CONNECTOR + regionStr;
		}

		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR
						+ countryStr;
			}
			else
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX
						+ QUERY_CONNECTOR + countryStr;
			}
		}
		else
		{
			if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{
				retVal = existingQuery + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + countryStr;

			}
			if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{

				retVal = existingQuery + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + countryStr;
			}
			else if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(regionStr) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + countryStr;
				}

			}
			else if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(regionStr) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + countryStr;
				}

			}
		}
		LOG.info(" getGenericCatalogQuery == " + retVal);
		return retVal;
	}

	private String getCMIRCatalogQuery(final String sortCode, final String existingQuery)
	{
		final String b2bUnit = getB2BUnitInSolrQuery();
		String retVal = EMPTY_STRING;
		if (existingQuery == null || existingQuery.isEmpty())
		{
			if (sortCode != null)
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR
						+ b2bUnit;
			}
			else
			{
				retVal = ((existingQuery == null) ? EMPTY_STRING : existingQuery) + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX
						+ QUERY_CONNECTOR + b2bUnit;
			}
		}
		else
		{
			if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{
				retVal = existingQuery + QUERY_CONNECTOR + B2B_UNIT_EXTRAFILTER_PREFIX + QUERY_CONNECTOR + b2bUnit;

			}
			if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) == -1)
			{

				retVal = existingQuery + QUERY_CONNECTOR + sortCode + QUERY_CONNECTOR + b2bUnit;
			}
			else if (sortCode != null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}
			else if (sortCode == null && existingQuery.indexOf(QUERY_CONNECTOR) != -1)
			{

				retVal = existingQuery;

				if (existingQuery.indexOf(b2bUnit) == -1)
				{
					retVal = retVal + QUERY_CONNECTOR + b2bUnit;
				}

			}
		}
		LOG.info(" getCMIRCatalogQuery == " + retVal);
		return retVal;
	}

}
