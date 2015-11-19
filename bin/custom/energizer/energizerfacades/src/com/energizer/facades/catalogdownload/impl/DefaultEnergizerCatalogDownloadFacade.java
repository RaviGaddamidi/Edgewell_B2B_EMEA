/**
 *
 */
package com.energizer.facades.catalogdownload.impl;

import static java.io.File.separatorChar;

import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.impex.ExportResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;

import com.energizer.facades.catalogdownload.AdvancedTypeExportScriptGenerator;
import com.energizer.facades.catalogdownload.EnergizerCatalogDownloadFacade;


/**
 * @author m1030110
 *
 */
public class DefaultEnergizerCatalogDownloadFacade implements EnergizerCatalogDownloadFacade
{
	protected static final Logger LOG = Logger.getLogger(DefaultEnergizerCatalogDownloadFacade.class);

	@Resource(name = "advancedTypeExportScriptGenerator")
	public AdvancedTypeExportScriptGenerator exportScriptGenerator;

	@Autowired
	private ConfigurationService configurationService;

	private static final int BUFFER_SIZE = 4096;


	@Override
	public String generateScript()
	{
		exportScriptGenerator.getParentUnitForCustomer();
		return exportScriptGenerator.generateScript();
	}

	@Override
	public void copyExportedMediaToExportDir(final ExportResult result)
	{
		final String exportDir = configurationService.getConfiguration().getString("catalogdownload.downloadPath");

		if (StringUtils.isNotBlank(exportDir))
		{

			final File dir = new File(System.getProperty("user.home") + "\\" + exportDir);
			try
			{
				if (!dir.exists())
				{
					if (!dir.mkdirs())
					{
						LOG.error("Directory " + exportDir + " does not exist. Unable to create it.");
					}
				}
				else if (dir.isDirectory() && dir.canWrite())
				{
					final Path dirPath = Paths.get(dir.getAbsolutePath());
					copyExportedMediaFile(dirPath, result.getExportedData());
				}
				else
				{
					LOG.error("Unable to write to " + exportDir + " or it is not a directory");
				}
			}
			catch (final IOException ioe)
			{
				LOG.error("Unable to copy generated script files to " + exportDir, ioe);
			}
		}
	}

	@Override
	public void copyExportedMediaFile(final Path targetDir, final ImpExMediaModel impexModel) throws IOException
	{

		Files.copy(Paths.get(findRealMediaPath(impexModel)), targetDir.resolve(impexModel.getRealFileName()));
		LOG.info(" The Source Filename: " + impexModel.getRealFileName() + " Path " + impexModel.getLocation()
				+ " Field Separator: " + impexModel.getFieldSeparator() + " Target DIR: " + targetDir);
		convertCSVToExcel(targetDir + "\\" + impexModel.getRealFileName(), impexModel.getLocation(), impexModel.getFieldSeparator());
	}

	public void convertCSVToExcel(final String fileName, final String filePath, final Character fieldSeparator)

	{
		final String exportDownloadDir = configurationService.getConfiguration().getString("catalogdownload.downloadPath");
		LOG.info("filename:" + fileName);
		LOG.info("filePath:" + filePath);
		LOG.info("fieldSeparator:" + fieldSeparator);
		ArrayList arList = null;
		ArrayList al = null;
		String thisLine;
		final int count = 0;


		try
		{

			LOG.info("CSV Filepath: " + fileName);
			final FileInputStream fis = new FileInputStream(fileName);
			final DataInputStream myInput = new DataInputStream(fis);
			int i = 0;
			arList = new ArrayList();
			while ((thisLine = myInput.readLine()) != null)
			{
				al = new ArrayList();
				final String strar[] = thisLine.split(fieldSeparator.toString());
				for (int j = 1; j < strar.length; j++)
				{

					if (strar[j].equals("orderingUnit"))
					{
						al.add("quantity");
					}

					else
					{
						al.add(strar[j]);
					}
				}
				arList.add(al);
				System.out.println();
				i++;
			}


			final HSSFWorkbook hwb = new HSSFWorkbook();
			final HSSFSheet sheet = hwb.createSheet("new sheet");
			for (int k = 0; k < arList.size(); k++)
			{
				final ArrayList ardata = (ArrayList) arList.get(k);
				final HSSFRow row = sheet.createRow((short) 0 + k);
				for (int p = 0; p < ardata.size(); p++)
				{
					final HSSFCell cell = row.createCell((short) p);
					String data = ardata.get(p).toString();
					if (data.startsWith("="))
					{
						cell.setCellType(Cell.CELL_TYPE_STRING);
						data = data.replaceAll("\"", "");
						data = data.replaceAll("=", "");
						cell.setCellValue(data);
					}
					else if (data.startsWith("\""))
					{
						data = data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_STRING);
						cell.setCellValue(data);
					}
					else
					{
						data = data.replaceAll("\"", "");
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
						cell.setCellValue(data);
					}
				}
				System.out.println();
			}

			LOG.info("File Path: " + System.getProperty("user.home") + "\\" + exportDownloadDir);

			final FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home") + "\\" + exportDownloadDir + "\\"
					+ "catalogDownload.xls");

			hwb.write(fileOut);
			fileOut.close();
			fis.close();
			final File file = new File(fileName);

			if (file.delete())
			{
				System.out.println(file.getName() + " is deleted!");
			}
			else
			{
				System.out.println("Delete operation is failed.");
			}

			System.out.println("Your excel file has been generated");
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Override
	public String findRealMediaPath(final ImpExMediaModel impexModel)
	{
		final StringBuilder sb = new StringBuilder(64);
		sb.append(configurationService.getConfiguration().getProperty("HYBRIS_DATA_DIR")).append(separatorChar);
		sb.append("media").append(separatorChar);
		sb.append("sys_").append(impexModel.getFolder().getTenantId());
		sb.append(separatorChar).append(impexModel.getLocation());
		return sb.toString();
	}

	@Override
	public void saveFile(final HttpServletRequest request, final HttpServletResponse response)
	{

		final String exportDir = configurationService.getConfiguration().getString("catalogdownload.downloadPath");
		final ServletContext context = request.getServletContext();


		final String fullPath = System.getProperty("user.home") + "\\" + exportDir + "\\" + "catalogDownload.xls";
		final File downloadFile = new File(fullPath);
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(downloadFile);
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}

		String mimeType = context.getMimeType(fullPath);
		if (mimeType == null)
		{
			mimeType = "application/octet-stream";
		}
		LOG.info("MIME type: " + mimeType);

		// set content attributes for the response
		response.setContentType(mimeType);
		response.setContentLength((int) downloadFile.length());


		// set headers for the response
		final String headerKey = "Content-Disposition";
		final String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		response.setHeader(headerKey, headerValue);

		OutputStream outStream = null;
		try
		{
			outStream = response.getOutputStream();
		}
		catch (final IOException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}

		final byte[] buffer = new byte[BUFFER_SIZE];
		int bytesRead = -1;

		// write bytes read from the input stream into the output stream
		try
		{
			while ((bytesRead = inputStream.read(buffer)) != -1)
			{
				outStream.write(buffer, 0, bytesRead);
			}
		}
		catch (final IOException e)
		{
			// YTODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{

			inputStream.close();
			outStream.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();

		}


	}


}
