/**
 * 
 */
package com.energizer.core.datafeed;

import de.hybris.platform.acceleratorservices.email.EmailService;
import de.hybris.platform.acceleratorservices.model.email.EmailAttachmentModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.energizer.core.model.EnergizerCronJobModel;


/**
 * @author M9005674
 * 
 */
public class EnergizerCSVFeedCronJob extends AbstractJobPerformable<EnergizerCronJobModel>
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.CronJobModel
	 * )
	 */

	private static final Logger LOG = Logger.getLogger(EnergizerCSVFeedCronJob.class);
	@Resource
	EmailService emailService;

	@Override
	public PerformResult perform(final EnergizerCronJobModel cronjob)
	{
		List<EnergizerCSVFeedError> errors = new ArrayList<EnergizerCSVFeedError>();
		PerformResult performResult = null;
		final List<String> emailAddress = new ArrayList<String>();
		final String type = cronjob.getType();
		if (type == null)
		{
			LOG.info("There is no Type defined for the job " + cronjob.getCode());
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.ABORTED);
		}
		final AbstractEnergizerCSVProcessor energizerCSVProcessor = (AbstractEnergizerCSVProcessor) Registry
				.getApplicationContext().getBean(type);
		final List<File> files = energizerCSVProcessor.getFilesForFeedType(type);
		LOG.info("Found " + files.size() + " CSV files to process");
		Boolean exceptionOccured = false;
		if (null != cronjob.getEmailAddress())
		{
			emailAddress.add(cronjob.getEmailAddress());
		}
		for (final File file : files)
		{
			Iterable<CSVRecord> csvRecords;
			try
			{
				csvRecords = energizerCSVProcessor.parse(file);
				errors = energizerCSVProcessor.process(csvRecords);
				exceptionOccured = (errors.size() != 0) ? true : false;
				energizerCSVProcessor.setMasterDataStream(new DataInputStream(new FileInputStream(file)));
				final List<EmailAttachmentModel> emailAttachmentList = new ArrayList<EmailAttachmentModel>();
				final EmailAttachmentModel attachmentModel = emailService.createEmailAttachment(
						energizerCSVProcessor.getMasterDataStream(),
						StringUtils.replace(file.getName().toLowerCase(), ".csv",
								"_" + new Date().getTime() + "." + de.hybris.platform.impex.constants.ImpExConstants.File.EXTENSION_CSV)
								.toLowerCase(), de.hybris.platform.impex.constants.ImpExConstants.File.MIME_TYPE_CSV);
				emailAttachmentList.add(attachmentModel);
				if (cronjob.getTechnicalEmailAddress().isEmpty())
				{
					cronjob.setTechnicalEmailAddress(emailAddress);
				}
				if (cronjob.getBusinessEmailAddress().isEmpty())
				{
					cronjob.setBusinessEmailAddress(emailAddress);
				}
				final List<EnergizerCSVFeedError> techfeedErrors = energizerCSVProcessor.getTechnicalFeedErrors();
				if (!techfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getTechRecordError());
					energizerCSVProcessor.mailErrors(cronjob, techfeedErrors, cronjob.getTechnicalEmailAddress(), emailAttachmentList);
					energizerCSVProcessor.cleanup(type, file, cronjob, techfeedErrors);
				}
				final List<EnergizerCSVFeedError> busfeedErrors = energizerCSVProcessor.getBusinessFeedErrors();
				if (!busfeedErrors.isEmpty())
				{
					energizerCSVProcessor.setRecordFailed(energizerCSVProcessor.getBusRecordError());
					energizerCSVProcessor.mailErrors(cronjob, busfeedErrors, cronjob.getBusinessEmailAddress(), emailAttachmentList);
					energizerCSVProcessor.cleanup(type, file, cronjob, busfeedErrors);
				}
				energizerCSVProcessor.setTotalRecords(0);
				energizerCSVProcessor.setRecordFailed(0);
				energizerCSVProcessor.setRecordSucceeded(0);
				emailAttachmentList.clear();
				energizerCSVProcessor.cleanup(type, file, cronjob, busfeedErrors);

			}
			catch (final FileNotFoundException e)
			{
				LOG.error("File Not found", e);
				exceptionOccured = true;
			}
			if (exceptionOccured)
			{
				performResult = new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
			}
			else
			{
				performResult = new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
			}

		}
		return performResult;
	}
}
