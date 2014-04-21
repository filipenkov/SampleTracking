package com.atlassian.support.tools.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.KeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

import com.atlassian.support.tools.salext.ApplicationInfoBundle;
import com.atlassian.support.tools.salext.SupportApplicationInfo;

/**
 * Copied from Confluence trunk as of 05 August 2010 (revision 123688).
 */
public final class ZipUtility
{
	private static final Logger log = Logger.getLogger(FileOptionsValidator.class);

	public static File createSupportZip(List<ApplicationInfoBundle> applicationFileBundles, SupportApplicationInfo appInfo) throws IOException
	{
		final File supportDir = new File(appInfo.getApplicationHome(), "logs/support");
		if( ! supportDir.exists() && ! supportDir.mkdirs())
		{
			throw new IOException("Couldn't create export directory " + supportDir.getAbsolutePath());
		}

		DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String baseFilename = appInfo.getApplicationName() + "_support_" + format.format(new Date());
		String filename = baseFilename + ".zip";

		int counter = 0;
		while(new File(filename).exists())
		{
			counter++;
			filename = baseFilename + "-" + counter + ".zip";
		}

		final File supportZipFile = new File(supportDir, filename);

		createSupportZip(supportZipFile, applicationFileBundles, appInfo);

		return supportZipFile;
	}

	private static void createSupportZip(File supportZip, List<ApplicationInfoBundle> applicationFileBundles, SupportApplicationInfo info) throws IOException
	{
		// Create File
		try
		{
			supportZip.createNewFile();
		}
		catch(IOException e)
		{
			throw new IOException(e.getMessage() + " - " + supportZip.getAbsolutePath());
		}

		// TODO print out cache statistics to file
		// TODO print out thread dump and include it in support zip

		OutputStream out = null;
		try
		{
			out = new FileOutputStream(supportZip);
			zip(out, applicationFileBundles, info);
		}
		finally
		{
			try
			{
				out.close();
			}
			catch(Exception e)
			{/* ignore */
			}
		}

		log.info("Saved Support Zip to: " + supportZip.getAbsolutePath());
	}

	/**
	 * Zip the files to the given destination.
	 * 
	 * @param destination
	 *            the place to zip the files to
	 * @param applicationFileBundles
	 * @throws java.io.IOException
	 * @throws GeneralSecurityException
	 * @throws KeyException
	 */
	private static void zip(OutputStream destination, List<ApplicationInfoBundle> applicationFileBundles, SupportApplicationInfo info) throws IOException
	{
		ZipOutputStream out = new ZipOutputStream(destination);
		int entryCount = 0;
		try
		{
			out.setComment("zip, created by Support Tools Plugin");
			
			for(ApplicationInfoBundle applicationFileBundle: applicationFileBundles)
			{
				for(String filePath: applicationFileBundle.getFiles())
				{
					File file = new File(filePath);

					if( ! file.exists())
					{
						log.debug("Unable to find " + file.getName() + " for " + applicationFileBundle.getKey());
						continue;
					}

					if(file.isDirectory())
					{
						log.debug(file.getName() + " is a directory in " + applicationFileBundle.getKey());
						continue;
					}

					entryCount++;
					
					FileSanitizer sanitizer = info.getFileSanitizer();

					String path = applicationFileBundle.getKey() + "/" + file.getName();
					log.debug("adding entry: " + file.getAbsolutePath() + ", as " + path);
					ZipEntry zentry = new ZipEntry(path);
					zentry.setTime(file.lastModified());
					out.putNextEntry(zentry);
					copyUpTo(out, sanitizer.sanitize(file), 20 * 1024 * 1024);
					out.closeEntry();
				}
			}
		}
		finally
		{
			if(entryCount > 0)
			{
				out.finish();
				out.close();
			}
			else
			{
				log.warn("No file entries were added to the zip file");
				destination.close();
			}
		}
	}

	private static void copyUpTo(OutputStream out, File file, int maxBytesToCopy) throws IOException
	{
		long totalSize = file.length();
		RandomAccessFile f = new RandomAccessFile(file, "r");
		if(totalSize > maxBytesToCopy)
		{
			f.skipBytes((int) (totalSize - maxBytesToCopy));
			// NB: We not longer skip to the end of the line, as it could
			// inadvertently skip a lot of content for non-standard files
			// (different EOL characters, etc.)
			// f.readLine(); // skip to the end of the line
		}

		byte[] buffer = new byte[1024 * 4]; // 4K buffer size by defaults
		long count = 0;
		int n = 0;
		while( - 1 != (n = f.read(buffer)))
		{
			out.write(buffer, 0, n);
			count += n;
		}
		out.flush();
		log.info("Copied " + count + " bytes for " + file.getName());
	}
}