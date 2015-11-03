/**
 *
 */
package com.energizer.facades.catalogdownload;

import de.hybris.platform.b2bacceleratorservices.company.B2BCommerceUserService;
import de.hybris.platform.impex.jalo.ImpExManager;
import de.hybris.platform.impex.jalo.exp.generator.ExportScriptGenerator;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.type.AttributeDescriptor;
import de.hybris.platform.jalo.type.ComposedType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerB2BUnitModel;
import com.energizer.facades.accounts.EnergizerCompanyB2BCommerceFacade;


/**
 * @author M1028886
 *
 */
public class AdvancedTypeExportScriptGenerator extends ExportScriptGenerator
{
	protected static final Logger LOG = Logger.getLogger(AdvancedTypeExportScriptGenerator.class);


	@Resource
	private B2BCommerceUserService b2bCommerceUserService;

	@Resource(name = "energizerCompanyB2BCommerceFacade")
	protected EnergizerCompanyB2BCommerceFacade energizerCompanyB2BCommerceFacade;

	String b2bUnitId;

	/*
	 * (non-Javadoc)
	 *
	 * @see com.energizer.facades.order.EnergizerB2BOrderHistoryFacade#getParentUnitForCustomer(java.lang.String)
	 */

	public void getParentUnitForCustomer()
	{

		final EnergizerB2BUnitModel energizerB2BUnitModel = energizerCompanyB2BCommerceFacade
				.getEnergizerB2BUnitModelForLoggedInUser();

		b2bUnitId = energizerB2BUnitModel.getUid();

		LOG.info("B2BUnitID: " + b2bUnitId);
		//return b2bUnitModel;
	}



	/**
	 *
	 * @param type
	 * @return
	 */
	protected String generateQueryForType(final ComposedType type, final String b2bUnitID)
	{
		// query that returns all items but no subtypes
		/*
		 * String query = "SELECT {PK} FROM {" + type. + "!}"; if (isCatalogItem(type) &&
		 * CollectionUtils.isNotEmpty(catalogVersions)) { query += " WHERE {" +
		 * getCatalogVersionAttribute(type).getQualifier() + "} IN ("; for (final CatalogVersion catalogVersion :
		 * catalogVersions) { query += catalogVersion.getPK().toString() + ","; }
		 *
		 * query = StringUtils.removeEnd(query, ","); query += ")"; }
		 */
		//b2bUnitID = b2bUnitID;
		final String temp = ",Collections.EMPTY_MAP, Collections.singletonList( Item.class ), true, true, -1, -1";
		final String query = "SELECT {cmir.PK } from {EnergizerCMIR as cmir JOIN EnergizerB2BUnit AS myb2bunit ON {cmir.b2bUnit}={myb2bunit.pk} } where {myb2bunit.uid}="
				+ b2bUnitId + " and {cmir.isActive}=1";
		LOG.info("Script Generator Query: " + query);
		return query;
	}

	@Override
	protected void writeScript() throws IOException
	{
		writeComment(" -------------------------------------------------------");
		writeComment("# used 'header validation mode' during script generation was: "
				+ ImpExManager.getImportStrictMode().getCode());
		final Locale thisLocale = JaloSession.getCurrentSession().getSessionContext().getLanguage().getLocale();
		writeBeanShell("impex.setLocale( new Locale( \"" + thisLocale.getLanguage() + "\" , \"" + thisLocale.getCountry()
				+ "\" ) )");
		writeComment(" -------------------------------------------------------");

		for (final ComposedType type : getTypes())
		{
			/*
			 * if (LOG.isDebugEnabled()) { LOG.info("generating script statements for type " + type.getCode()); }
			 */

			System.out.println("Type name: " + type.getCode());

			if (type.getCode().equals("EnergizerCMIR"))
			{

				getScriptWriter().writeSrcLine("");
				writeComment("---- Extension: " + type.getExtensionName() + " ---- Type: " + type.getCode() + " ----");

				writeTargetFileStatement(type, ".csv");

				writeHeader(type);
				writeExportStatement(type, false);
			}
		}
	}

	/**
	 * Write the export statement, we use a flexible search so we don't export any sub types
	 *
	 * @param type
	 * @param inclSubtypes
	 * @throws IOException
	 */
	@Override
	protected void writeExportStatement(final ComposedType type, final boolean includeSubTypes) throws IOException
	{
		/*
		 * // if we don't have a catalog item or we have one but no catalog versions to filter // then we use the normal
		 * exportItems method of Exporter if (!isCatalogItem(type) || CollectionUtils.isEmpty(catalogVersions)) {
		 * writeBeanShell("impex.exportItems( \"" + type.getCode() + "\" , " + false + " )"); } else // we have catalog
		 * versions then we use the flexible search method {
		 */

		writeBeanShell("impex.exportItemsFlexibleSearch(\"" + generateQueryForType(type, b2bUnitId) + "\")");
		//}

	}


	/**
	 * Override to allow relations to be set inside the type, we need this to be able to handle deletes
	 */

	@Override
	protected void writeHeader(final ComposedType type) throws IOException
	{

		final Collection<AttributeDescriptor> attribs = type.getAttributeDescriptorsIncludingPrivate();

		final boolean hasUnique = false;
		final List<String> columns = new ArrayList<String>();// new HashSet<String>();

		AttributeDescriptor ad1 = null;

		try
		{

			// gather columns string
			if (type.getCode().equals("EnergizerCMIR"))
			{
				for (final AttributeDescriptor ad : attribs)
				{
					LOG.info(" In Script Header loop, Attribute: " + ad);
					/*
					 * if (!isIgnoreColumn(type, ad) && !(ad instanceof RelationDescriptor && !(ad.isProperty())) &&
					 * !ad.getQualifier().equals(Item.PK) && (ad.isInitial() || ad.isWritable()) &&
					 * (!ad.getQualifier().equals("itemtype") || TypeManager.getInstance().getType("EnumerationValue")
					 * .isAssignableFrom(type)) // export relations as attributes || ((!isIgnoreColumn(type, ad) && ad
					 * instanceof RelationDescriptor && overrideRelationAsAttribute(ad)))) { if (!ad.isOptional()) {
					 * addAdditionalModifier(type.getCode(), ad.getQualifier(), ImpExConstants.Syntax.Modifier.ALLOWNULL,
					 * "true"); } if (ad.isUnique() || getAdditionalModifiers(type,
					 * ad.getQualifier()).get(ImpExConstants.Syntax.Modifier.UNIQUE) != null) { hasUnique = true; } if
					 * (!ad.isWritable()) { addAdditionalModifier(type.getCode(), ad.getQualifier(),
					 * ImpExConstants.Syntax.Modifier.FORCE_WRITE, "true"); }
					 */
					/*
					 * if (ad.isLocalized()) { for (final de.hybris.platform.jalo.c2l.Language lang : this.getLanguages()) {
					 * generateColumn(ad, lang.getIsoCode()); columns.add(generateColumn(ad, lang.getIsoCode())); } } else {
					 */

					/*
					 * if (ad.isUnique() || getAdditionalModifiers(type,
					 * ad.getQualifier()).get(ImpExConstants.Syntax.Modifier.UNIQUE) != null) { hasUnique = true; }
					 */

					if (ad.getQualifier().equals("erpMaterialId"))
					{
						LOG.info("erpMaterialID attribute: --- " + ad.getQualifier());
						columns.add(generateColumn(ad, null));
						columns.add(generateColumn(ad1, null));

					}

					else if (ad.getQualifier().equals("customerMaterialId"))
					{
						LOG.info("customerMaterialId attribute: --- " + ad.getQualifier());
						ad1 = ad;
					}

					else if (ad.getQualifier().equals("orderingUnit"))
					{
						LOG.info("orderingUnit attribute: --- " + ad.getQualifier());

						columns.add(generateColumn(ad, null));
					}

					//columns.addAll(getAdditionalColumns(type));

				}
			}
			//columns.addAll(getAdditionalColumns(final type));
			/*
			 * final Map line = new HashMap(); int index = 0; final String firstColumn = generateFirstHeaderColumn(type,
			 * hasUniqueColumns); line.put(Integer.valueOf(index), firstColumn); index++;
			 */

			/*
			 * if (isUseDocumentID()) { line.put(Integer.valueOf(index), ImpExConstants.Syntax.DOCUMENT_ID_PREFIX +
			 * "Item"); } else { line.put(Integer.valueOf(index), Item.PK + (hasUnique ? "" :
			 * ImpExConstants.Syntax.MODIFIER_START + ImpExConstants.Syntax.Modifier.UNIQUE +
			 * ImpExConstants.Syntax.MODIFIER_EQUAL + Boolean.TRUE + ImpExConstants.Syntax.MODIFIER_END)); }
			 */
			//index++;

			final Map line = new HashMap();
			int index = 0;
			/*
			 * line.put(Integer.valueOf(index), "insert_update " + type.getCode()); index++;
			 */

			final String firstColumn = generateFirstHeaderColumn(type, hasUnique);
			line.put(Integer.valueOf(index), firstColumn);
			index++;


			/*
			 * if (isUseDocumentID()) { line.put(Integer.valueOf(index), ImpExConstants.Syntax.DOCUMENT_ID_PREFIX +
			 * "Item"); } else { line.put(Integer.valueOf(index), Item.PK + (hasUnique ? "" :
			 * ImpExConstants.Syntax.MODIFIER_START + ImpExConstants.Syntax.Modifier.UNIQUE +
			 * ImpExConstants.Syntax.MODIFIER_EQUAL + Boolean.TRUE + ImpExConstants.Syntax.MODIFIER_END)); } index++;
			 */

			for (final Iterator<String> iter = columns.iterator(); iter.hasNext(); index++)
			{
				final String column = iter.next();
				LOG.info("Column name: " + column);
				if (column.length() != 0)
				{
					line.put(Integer.valueOf(index), column);
				}
			}
			LOG.info("Script Generator's Header Line: " + line);
			getScriptWriter().write(line);

		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.impex.jalo.exp.generator.AbstractScriptGenerator#generateScript()
	 */
	@Override
	public String generateScript()
	{
		final String result = super.generateScript();
		/*
		 * if (exportConfig != null) { exportConfig.setLastExport(new Date()); }
		 */
		return result;
	}




}
