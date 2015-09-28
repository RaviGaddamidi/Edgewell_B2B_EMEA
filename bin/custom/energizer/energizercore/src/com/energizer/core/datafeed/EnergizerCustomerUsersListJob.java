/**
 *
 */
package com.energizer.core.datafeed;

import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.energizer.core.model.EnergizerB2BUnitModel;


/**
 * @author M1027489
 *
 */
public class EnergizerCustomerUsersListJob extends AbstractJobPerformable<CronJobModel>
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable#perform(de.hybris.platform.cronjob.model.
	 * CronJobModel )
	 */

	private static final Logger LOG = Logger.getLogger(EnergizerCustomerUsersListJob.class);


	@Resource(name = "configurationService")
	private ConfigurationService configurationService;


	@Override
	public PerformResult perform(final CronJobModel cronjob)
	{
		System.out.println("test this");

		final String flexiSearchQuery = "SELECT {PK} FROM {EnergizerB2BUnit}";

		final SearchResult<EnergizerB2BUnitModel> result = flexibleSearchService.search(flexiSearchQuery);
		final List<EnergizerB2BUnitModel> energizerB2BUnitModels = result.getResult();

		final String path = configurationService.getConfiguration().getString("customerUserListPath") + "\\CustomerUsersList.xls";

		try
		{
			writeToExcelfile(energizerB2BUnitModels, path);
		}
		catch (final IOException e)
		{

			e.printStackTrace();
		}

		return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
	}

	public void writeToExcelfile(final List<EnergizerB2BUnitModel> energizerB2BUnitModels, final String path) throws IOException
	{

		final Workbook workbook = new HSSFWorkbook();
		final FileOutputStream out = new FileOutputStream(new File(path));

		final Sheet sheet = workbook.createSheet();
		Row row = null;
		row = sheet.createRow(0);
		final CellStyle style = workbook.createCellStyle();
		style.setFillBackgroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
		style.setFillPattern(CellStyle.ALIGN_CENTER);


		final Cell cell00 = row.createCell(0);
		cell00.setCellStyle(style);
		cell00.setCellValue("CUSTOMER");

		final Cell cell11 = row.createCell(1);
		cell11.setCellStyle(style);
		cell11.setCellValue("USERS");

		final Cell cell22 = row.createCell(2);
		cell22.setCellStyle(style);
		cell22.setCellValue("USERS CREATED DATE");

		int rownum = 1;

		for (final EnergizerB2BUnitModel unit : energizerB2BUnitModels)
		{
			final int fromRow = rownum;
			boolean flag = true;

			Set<PrincipalModel> nameList = unit.getMembers();

			if (nameList.size() == 0)
			{
				nameList = new HashSet<PrincipalModel>();
				final PrincipalModel user = new PrincipalModel();
				user.setName("-");
				user.setCreationtime(null);
				nameList.add(user);
			}

			for (final PrincipalModel s : nameList)
			{
				row = sheet.createRow(rownum++);
				final Cell cell0 = row.createCell(0);
				if (flag)
				{
					cell0.setCellValue(unit.getName());
					flag = false;
				}
				final Cell cell1 = row.createCell(1);
				cell1.setCellValue(s.getName());

				final Cell cell2 = row.createCell(2);

				if (s.getCreationtime() != null)
				{
					cell2.setCellValue(s.getCreationtime().toString());
				}

				else
				{
					cell2.setCellValue("-");
				}

			}
			if (nameList.size() > 1)
			{
				sheet.addMergedRegion(new CellRangeAddress(fromRow, fromRow + nameList.size() - 1, 0, 0));
			}
		}

		workbook.write(out);
		out.close();
	}

}