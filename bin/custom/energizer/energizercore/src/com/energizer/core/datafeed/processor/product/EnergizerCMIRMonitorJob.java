package com.energizer.core.datafeed.processor.product;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAddressModel;
import de.hybris.platform.acceleratorservices.model.email.EmailMessageModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.Config;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.energizer.core.constants.EnergizerCoreConstants;
import com.energizer.core.datafeed.AbstractEnergizerCSVProcessor;
import com.energizer.core.model.EnergizerCMIRModel;
import com.energizer.core.model.EnergizerCronJobModel;
import com.energizer.core.model.EnergizerPriceRowModel;
import com.energizer.services.product.EnergizerProductService;


/**
 * @author M1030106
 *
 */
public class EnergizerCMIRMonitorJob extends AbstractJobPerformable<EnergizerCronJobModel>
{
	@Resource
	private EnergizerProductService energizerProductService;

	@Resource
	private ModelService modelService;
	//private List<EnergizerCMIRModel> energizerCMIRModels = energizerProductService;
	@Resource
	private ConfigurationService configurationService;

	@Value("{$sharedFolderPath}")
	private String path;

	@Resource
	private EmailService emailService;

	private static final Logger LOG = Logger.getLogger(EnergizerOrphanedProductProcessor.class);


	@Resource(name = "energizerCMIRCSVProcessor")
	private AbstractEnergizerCSVProcessor csvUtils;


	@SuppressWarnings("unused")
	@Override
	public PerformResult perform(final EnergizerCronJobModel arg0)
	{

		List<EnergizerCMIRModel> cmirListFromDB = null; //= energizerProductService.getAllEnergizerCMIRList();
		List<EnergizerCMIRModel> cmirListFromDB_buff = null; // = energizerProductService.getAllEnergizerCMIRList();
		//final List<EnergizerCMIRModel> cmirFinalList = new ArrayList<EnergizerCMIRModel>();
		Set<EnergizerCMIRModel> cmirFinalSet = null;
		cmirFinalSet = new HashSet<EnergizerCMIRModel>();
		Set<EnergizerCMIRModel> cmirSetFromDB = null;
		cmirSetFromDB = new HashSet<EnergizerCMIRModel>();
		try
		{

			cmirListFromDB = energizerProductService.getAllEnergizerCMIRList();
			cmirSetFromDB.addAll(cmirListFromDB);
			cmirListFromDB_buff = energizerProductService.getAllEnergizerCMIRList();
			if (cmirListFromDB != null && cmirListFromDB_buff != null)
			{
				LOG.info("list of cmirs" + cmirListFromDB.size());
			}

			else
			{
				LOG.error("no cmirs fetched from db,aborting cronjob");
				return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}


		}


		catch (final Exception e)
		{
			LOG.error("error occured while loading data from db" + e.getMessage());
		}



		try
		{
			final List<File> files = csvUtils.getFilesForFeedType("energizerCMIRCSVProcessor");
			LOG.info("LOADING FILES FROM CMIR folder  FOR MONITORING" + files);


			if (files == null && files.size() == 0)
			{
				LOG.info("NO FILES FOUND TO PROCESS");
				return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}

			else
			{
				Iterable<CSVRecord> csvRecords = null;

				try
				{
					for (final File f : files)
					{
						csvRecords = csvUtils.parse(f);

						if (cmirListFromDB_buff != null && csvRecords != null)
						{

							cmirFinalSet.addAll(checkUpdate(cmirListFromDB_buff, csvRecords));
						}
					}


					if (cmirFinalSet.size() > 0 && cmirFinalSet != null)
					{
						cmirSetFromDB.removeAll(cmirFinalSet);
						LOG.info("the final number of models whose cmir set to be false" + cmirSetFromDB.size());
						for (final EnergizerCMIRModel cmir : cmirSetFromDB)
						{
							LOG.info("cmirs getting disabled for moels having erpmaterialid-" + cmir.getErpMaterialId() + "\tcust_matid"
									+ cmir.getCustomerMaterialId() + "\tb2bunit" + cmir.getB2bUnit());
							cmir.setIsActive(false);
							modelService.save(cmir);
							final List<EnergizerPriceRowModel> energizerPriceRow = energizerProductService
									.getAllEnergizerPriceRowForB2BUnit(cmir.getErpMaterialId(), cmir.getB2bUnit().getUid());
							if (energizerPriceRow != null)
							{
								LOG.info("Number of price rows to be modified for " + cmir.getErpMaterialId() + "="
										+ energizerPriceRow.size());
								for (final EnergizerPriceRowModel priceRow : energizerPriceRow)
								{
									priceRow.setIsActive(false);
									modelService.save(priceRow);
								}
							}

							/* c.getErpMaterialId(). */

						}

						sendMail(cmirSetFromDB.toString(), arg0.getEmailAddress());

					}

					else
					{
						LOG.info("nothing to update");
					}


				}


				catch (final Exception e)
				{
					LOG.error("EXC CAUSED BY" + e);
				}
			}


		}

		catch (final Exception e)
		{
			LOG.error("ERROR OCCURED WHILE LOADING FILES" + "\t\t" + e.getMessage());
		}





		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}



	public Set<EnergizerCMIRModel> checkUpdate(final List<EnergizerCMIRModel> cmirList, final Iterable<CSVRecord> csvRecords)
	{


		Map<String, String> csvValuesMap = null;
		Set<EnergizerCMIRModel> preparedSet = null;
		preparedSet = new HashSet<EnergizerCMIRModel>();
		Set<EnergizerCMIRModel> cmirSet = null;
		cmirSet = new HashSet<EnergizerCMIRModel>();
		cmirSet.addAll(cmirList);



		for (final CSVRecord record : csvRecords)
		{
			csvValuesMap = record.toMap();

			for (final EnergizerCMIRModel cmirModel : cmirList)
			{

				if (cmirModel != null
						&& (cmirModel.getErpMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.ERPMATERIAL_ID))
								&& cmirModel.getB2bUnit().getUid().equals(csvValuesMap.get(EnergizerCoreConstants.ENERGIZER_ACCOUNT_ID)) && cmirModel
								.getCustomerMaterialId().equals(csvValuesMap.get(EnergizerCoreConstants.CUSTOMER_MATERIAL_ID))))//checking whether the corresponding row attributes and model attrbutes matches or not
				{

					preparedSet.add(cmirModel);//here set is taken to avoid the duplication of models satisfying the condition within if block.
					/*
					 * LOG.info("cmir record exists in both back end and excel file for erpmat id" +
					 * cmirModel.getErpMaterialId() + "\tb2bunit id-" + cmirModel.getB2bUnit() + "\tcustomer_matid" +
					 * cmirModel.getCustomerMaterialId());
					 */
				}

			}

		}


		return preparedSet;
	}

	void sendMail(final String cmirsList, final String toemail)
	{
		final EmailAddressModel toaddress = emailService.getOrCreateEmailAddressForEmail(toemail, "CMIR Monitor Job");
		final EmailAddressModel fromaddress = emailService.getOrCreateEmailAddressForEmail(configurationService.getConfiguration()
				.getString("cronjobs.from.email", Config.getParameter("fromEmailAddress.orderEmailSender")), "");
		final EmailMessageModel message = emailService.createEmailMessage(Arrays.asList(toaddress), null, null, fromaddress, "",
				"list of cmirs to be deactivated", "" + cmirsList.toString(), null);
		LOG.info("sending mail for list of cmirs to be deleted");
		emailService.send(message);
		LOG.info("mail send");
		return;

	}
}
