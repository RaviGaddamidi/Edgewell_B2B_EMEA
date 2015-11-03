/**
 *
 */
package com.energizer.facades.catalogdownload;

import de.hybris.platform.impex.model.ImpExMediaModel;
import de.hybris.platform.servicelayer.impex.ExportResult;

import java.io.IOException;
import java.nio.file.Path;



/**
 * @author m1030110
 *
 */
public interface EnergizerCatalogDownloadFacade
{

	public String generateScript();

	public void copyExportedMediaToExportDir(final ExportResult result);

	public void copyExportedMediaFile(final Path targetDir, final ImpExMediaModel impexModel) throws IOException;

	public void convertCSVToExcel(final String fileName, final String filePath, final Character fieldSeparator);

	public String findRealMediaPath(final ImpExMediaModel impexModel);


}
