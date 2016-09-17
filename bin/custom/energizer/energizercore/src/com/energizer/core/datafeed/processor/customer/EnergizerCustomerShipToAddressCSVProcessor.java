/**
 *
 */
package com.energizer.core.datafeed.processor.customer;

/**
 * @author M1030106
 *
 */

import de.hybris.platform.b2bacceleratorservices.company.CompanyB2BCommerceService;
import de.hybris.platform.catalog.CatalogService;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.RegionModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.datafeed.EnergizerCSVFeedError;
import com.energizer.core.datafeed.facade.impl.DefaultEnergizerAddressFacade;
import com.energizer.core.model.EnergizerB2BUnitModel;


public class EnergizerCustomerShipToAddressCSVProcessor extends AbstractEnergizerCSVProcessor
{
	@Resource
	private ModelService modelService;

	@Resource
	private CatalogService catalogService;

	@Resource
	private CompanyB2BCommerceService companyB2BCommerceService;

	@Resource
	private FlexibleSearchService flexibleSearchService;

	@Resource
	private DefaultEnergizerAddressFacade defaultEnergizerAddressFacade;

	private static final Logger LOG = Logger.getLogger(EnergizerCustomerShipToAddressCSVProcessor.class);

	private static final String SP_CUSTOMER_ID = "SPCustomerID";
	private static final String SH_SALES_PERSON = "SHSalesPerson";
	private static final String SH_NAME = "SHNAME";
	private static final String SH_CITY = "SHCITY";
	private static final String SH_POSTAL_CODE = "SHPOSTALCODE";
	private static final String SH_STREET = "SHSTREET";
	private static final String SH_COUNTRY_KEY = "SHCOUNTRYKEY";
	private static final String SH_REGION = "SHREGION";
	private static final String STATUS = "Status";
	private static final String CUSTOMER_ID = "CustomerID";
	private static final String SH_CUSTOMER_ID = "SHCustomerID";

	@Override
	public List<EnergizerCSVFeedError> process(final Iterable<CSVRecord> records)
	{

		for (final CSVRecord record : records)
		{
			try
			{

				if (record != null && validate(record))
				{
					final Map<String, String> valuemap = record.toMap();
					LOG.info("THE record no" + record.getRecordNumber() + "is valid");

					final List<AddressModel> energizeraddress = defaultEnergizerAddressFacade.fetchAddressOnSHCustID(valuemap
							.get(SH_CUSTOMER_ID));
					try
					{
						if (valuemap != null && energizeraddress != null && energizeraddress.size() == 1
								&& energizeraddress.get(0) != null)
						{
							final AddressModel shiptoadd = energizeraddress.get(0);
							LOG.info("existing unique shipping [SH] address found just update");
							modelService.refresh(shiptoadd);
							shiptoadd.setShCustomerid(valuemap.get(SH_CUSTOMER_ID).trim());
							shiptoadd.setTown(valuemap.get(SH_CITY).trim());
							shiptoadd.setPostalcode(valuemap.get(SH_POSTAL_CODE).trim());
							shiptoadd.setSalesPersonEmailId(valuemap.get(SH_SALES_PERSON).trim());
							shiptoadd.setActive(valuemap.get(STATUS).trim().equalsIgnoreCase("1") ? Boolean.TRUE : Boolean.FALSE);
							shiptoadd.setStreetname(valuemap.get(SH_STREET).trim());
							final String dispname[] = valuemap.get(SH_SALES_PERSON).split("@");
							shiptoadd.setDisplayName(dispname[0].trim());
							shiptoadd.setCompany(valuemap.get(SH_NAME).trim());
							shiptoadd.setShippingAddress(Boolean.TRUE);

							try
							{
								LOG.info("adding country model for existing shpto");
								final CountryModel country = new CountryModel();
								country.setIsocode(valuemap.get(SH_COUNTRY_KEY).trim());
								final CountryModel countryfound = flexibleSearchService.getModelByExample(country);
								shiptoadd.setCountry(countryfound);

								final RegionModel region = new RegionModel();
								region.setIsocode(valuemap.get(SH_REGION).trim());
								region.setCountry(country);
								final RegionModel regionfound = flexibleSearchService.getModelByExample(region);
								shiptoadd.setRegion(regionfound);


							}

							catch (final Exception e)
							{
								LOG.error("EXCEPTION OCCURED while updating country/region/owner-----model save failure");
							}


							/*
							 * if (shiptoadd.getErpAddressId() == null &&
							 * valuemap.get(SH_CUSTOMER_ID).trim().equalsIgnoreCase(valuemap.get(SP_CUSTOMER_ID).trim())) { //
							 * alternative for self association in shipto prty list, i.e a it will be used in case a soldto is
							 * shipto as well. shiptoadd.setErpAddressId(valuemap.get(SH_CUSTOMER_ID).trim());
							 * shiptoadd.setShCustomerid(valuemap.get(SH_CUSTOMER_ID).trim()); }
							 */

							modelService.save(shiptoadd);
							modelService.saveAll(shiptoadd);
							LOG.info("record updated for" + valuemap.get(SH_CUSTOMER_ID));


							//association with soldto begins

							final List<AddressModel> soldTo = defaultEnergizerAddressFacade.fetchAddress(valuemap.get(SP_CUSTOMER_ID)
									.trim());


							try
							{

								if (soldTo != null && soldTo.size() == 1 && soldTo.get(0) != null
										&& soldTo.get(0).getErpAddressId().trim().equals(valuemap.get(SP_CUSTOMER_ID).trim())
										&& soldTo.get(0).getSoldTo() && shiptoadd != null && !shiptoadd.getSoldTo())//if sh/sp/erp are same then that add model is soldto : shuln't be associated, self association is denied.
								{

									AddressModel selectedsoldTo = null;
									selectedsoldTo = soldTo.get(0);
									modelService.refresh(selectedsoldTo);
									LOG.info("a valid/unique soldto found for association for SHID\t" + valuemap.get(SH_CUSTOMER_ID));
									final List<AddressModel> shiptoList = new ArrayList<AddressModel>();

									if (selectedsoldTo != null && selectedsoldTo.getShipToPartyList() != null
											&& !selectedsoldTo.getShipToPartyList().isEmpty())

									{
										final List<String> shiptoshlist = new ArrayList<String>();
										shiptoadd.setErpAddressId(selectedsoldTo.getErpAddressId());
										shiptoList.addAll(selectedsoldTo.getShipToPartyList());
										for (final AddressModel adrs : selectedsoldTo.getShipToPartyList())
										{
											if (adrs != null && adrs.getShCustomerid() != null)
											{
												shiptoshlist.add(adrs.getShCustomerid());
											}
										}

										if (!shiptoshlist.contains(shiptoadd.getShCustomerid().toString().trim()))
										{
											shiptoList.add(shiptoadd);
											selectedsoldTo.setShipToPartyList(shiptoList);
										}

									}

									else
									{
										shiptoadd.setErpAddressId(selectedsoldTo.getErpAddressId());
										shiptoList.add(shiptoadd);
										selectedsoldTo.setShipToPartyList(shiptoList);
									}


									/*
									 * else { if (selectedsoldTo != null) { shiptoList.add(shiptoadd);
									 * selectedsoldTo.setShipToPartyList(shiptoList); } }
									 */
									shiptoadd.setSoldTo(false);

									modelService.save(selectedsoldTo);
									modelService.saveAll();
									LOG.info("successfull association done for existing [sH]\t");
								}

								else
								{

									LOG.info("failed association for an update SH-ID or the record is a soldto/shipto as well \t"
											+ valuemap.get(SH_CUSTOMER_ID));
								}


							}

							catch (final Exception e)
							{
								LOG.error("error occured while association\t" + e.getMessage());
								continue;
							}


						}

						else
						{
							if (valuemap != null && energizeraddress != null && energizeraddress.size() == 0
									&& !(valuemap.get(SH_CUSTOMER_ID).equalsIgnoreCase(valuemap.get(SP_CUSTOMER_ID))))//confirming that no duplicates getting created.
							{
								LOG.info("creating new SH address model");
								AddressModel nwshiptoadd = null;
								nwshiptoadd = modelService.create(AddressModel.class);
								nwshiptoadd.setShCustomerid(valuemap.get(SH_CUSTOMER_ID).trim());
								nwshiptoadd.setTown(valuemap.get(SH_CITY).trim());
								nwshiptoadd.setPostalcode(valuemap.get(SH_POSTAL_CODE).trim());
								nwshiptoadd.setSalesPersonEmailId(valuemap.get(SH_SALES_PERSON).trim());
								nwshiptoadd.setActive(valuemap.get(STATUS).trim().equalsIgnoreCase("1") ? Boolean.TRUE : Boolean.FALSE);
								nwshiptoadd.setStreetname(valuemap.get(SH_STREET).trim());
								final String dispname[] = valuemap.get(SH_SALES_PERSON).trim().split("@");
								nwshiptoadd.setDisplayName(dispname[0].trim());
								nwshiptoadd.setCompany(valuemap.get(SH_NAME).trim());
								nwshiptoadd.setShippingAddress(Boolean.TRUE);
								nwshiptoadd.setSoldTo(Boolean.FALSE);//newly created shipto's can't be sold to.

								try
								{
									LOG.info("adding country model for nw shpto");
									final CountryModel country = new CountryModel();
									country.setIsocode(valuemap.get(SH_COUNTRY_KEY).trim());
									final CountryModel countryfound = flexibleSearchService.getModelByExample(country);
									nwshiptoadd.setCountry(countryfound);

									final RegionModel region = new RegionModel();
									region.setIsocode(valuemap.get(SH_REGION).trim());
									region.setCountry(countryfound);
									final RegionModel regionfound = flexibleSearchService.getModelByExample(region);
									nwshiptoadd.setRegion(regionfound);
								}

								catch (final Exception e)
								{
									LOG.error("EEOR OCCURED while updating country/owner/region for new sh" + valuemap.get(SH_CUSTOMER_ID));
									//continue;
								}

								final EnergizerB2BUnitModel b2bunit = (EnergizerB2BUnitModel) companyB2BCommerceService
										.getUnitForUid(valuemap.get(CUSTOMER_ID).trim());

								if (b2bunit != null)
								{
									nwshiptoadd.setOwner(b2bunit);
									modelService.save(nwshiptoadd);
									modelService.saveAll(nwshiptoadd);
									//	modelService.s

									LOG.info("record created for" + valuemap.get(SH_CUSTOMER_ID));

								}


								try
								{
									final List<AddressModel> soldTo = defaultEnergizerAddressFacade.fetchAddress(valuemap.get(
											SP_CUSTOMER_ID).trim());

									if (soldTo != null && soldTo.size() == 1 && soldTo.get(0) != null
											&& soldTo.get(0).getErpAddressId().trim().equals(valuemap.get(SP_CUSTOMER_ID).trim())
											&& soldTo.get(0).getSoldTo())//assumption is a newly created sh model can't be a sold to
									{

										AddressModel selectedsoldTo = null;
										selectedsoldTo = soldTo.get(0);
										modelService.refresh(selectedsoldTo);
										LOG.info("a valid/unique soldto found for association for NEW SHID\t"
												+ valuemap.get(SH_CUSTOMER_ID));
										final List<AddressModel> shiptoList = new ArrayList<AddressModel>();
										if (selectedsoldTo != null && selectedsoldTo.getShipToPartyList() != null
												&& !selectedsoldTo.getShipToPartyList().isEmpty())
										{
											nwshiptoadd.setErpAddressId(selectedsoldTo.getErpAddressId());
											shiptoList.addAll(selectedsoldTo.getShipToPartyList());//used to avoid overriding of models with just current updating one.
											shiptoList.add(nwshiptoadd);//associating current record
											selectedsoldTo.setShipToPartyList(shiptoList);
										}

										else
										{
											if (selectedsoldTo != null)
											{
												nwshiptoadd.setErpAddressId(selectedsoldTo.getErpAddressId());
												shiptoList.add(nwshiptoadd);
												selectedsoldTo.setShipToPartyList(shiptoList);
											}
										}



										modelService.saveAll(selectedsoldTo);
										modelService.saveAll();
										LOG.info("successfull association done for new [SH]\t");
									}

									else
									{
										LOG.error("failed association for new SH-ID\t" + valuemap.get(SH_CUSTOMER_ID));
									}
								}

								catch (final Exception e)
								{
									LOG.error("error occured while associating the newly created shipto add model\t" + e.getMessage());
									continue;
								}

							}
						}
					}

					catch (final Exception e)
					{
						LOG.error("ERROR OCCURED OR SHIP-SOLD TO CASE to avoid association WHILE CREATING/UPDATING SH_CUST_ID:"
								+ valuemap.get(SH_CUSTOMER_ID) + "---cause\t" + e.getCause() + "\tmsg is\t" + e.getMessage());
						continue;
					}
				}

				else
				{
					LOG.error("validation fails for record\t" + record.getRecordNumber());
					if (!getTechnicalFeedErrors().isEmpty())
					{
						csvFeedErrorRecords.addAll(getTechnicalFeedErrors());
						getBusinessFeedErrors().addAll(getTechnicalFeedErrors());
						getTechnicalFeedErrors().clear();
						continue;
					}
				}

			}
			catch (final Exception e)
			{
				LOG.error("GOT INVALID VALUE MAP\t" + e.getCause());
			}
		}


		return getCSVFeedErrorRecords();
	}

	private boolean validate(final CSVRecord record)
	{
		EnergizerCSVFeedError error = null;
		Integer columnNumber = 0;
		boolean validrecord = true;
		setRecordFailed(getRecordFailed());
		for (final String columnHeader : record.toMap().keySet())
		{
			columnNumber++;
			setTotalRecords(record.getRecordNumber());

			if (columnHeader != null && columnHeader.equalsIgnoreCase(SH_SALES_PERSON))
			{
				continue;
			}

			final String value = record.toMap().get(columnHeader);
			if (value != null && value.isEmpty())
			{
				long recordFailed = getRecordFailed();
				error = new EnergizerCSVFeedError();
				final List<String> columnNames = new ArrayList<String>();
				final List<Integer> columnNumbers = new ArrayList<Integer>();
				error.setLineNumber(record.getRecordNumber());
				columnNames.add(columnHeader);
				error.setUserType(TECHNICAL_USER);
				error.setColumnName(columnNames);
				error.setMessage(columnHeader + " column should not be empty");
				columnNumbers.add(columnNumber);
				error.setColumnNumber(columnNumbers);
				getTechnicalFeedErrors().add(error);
				setTechRecordError(getTechnicalFeedErrors().size());
				recordFailed++;
				setRecordFailed(recordFailed);
				validrecord = false;

			}
		}


		return (validrecord == true ? true : false);
	}
}
