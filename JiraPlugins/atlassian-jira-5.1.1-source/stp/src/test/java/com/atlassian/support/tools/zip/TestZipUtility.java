package com.atlassian.support.tools.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.KeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import com.atlassian.support.tools.ValidationLog;
import com.atlassian.support.tools.mock.MockApplicationInfo;
import com.atlassian.support.tools.salext.bundle.ApplicationInfoBundle;
import com.atlassian.support.tools.salext.bundle.BundleManifest;
import com.atlassian.support.tools.salext.bundle.DefaultApplicationFileBundle;

public class TestZipUtility extends TestCase
{
	private static final int THIRTY_MB = 30 * 1024 * 1024;
	private static final int TWENTY_MB = 20 * 1024 * 1024;

	public TestZipUtility(String name) throws GeneralSecurityException
	{
		super(name);
	}

	public void testEncryptedSupportZip() throws Exception
	{
		performZipTest(false);
	}

	// public void testPlainSupportZip() throws Exception
	// {
	// performZipTest(CryptTestHelper.DUMMY, true);
	// }

	private void performZipTest(boolean testTheSize) throws Exception
	{
		MockApplicationInfo applicationInfo = new MockApplicationInfo();

		performFileTest(testTheSize, applicationInfo, 4096, "Tiny");
		performFileTest(testTheSize, applicationInfo, TWENTY_MB, "Medium");
		performFileTest(testTheSize, applicationInfo, THIRTY_MB, "Large");
	}

	protected void performFileTest(boolean testSize, MockApplicationInfo applicationInfo, int fileSizeToTest, String name) throws FileNotFoundException, IOException, KeyException, GeneralSecurityException, ZipException, InterruptedException
	{
		String filename = "/tmp/" + name;
		File file = new File(filename);
		int counter = 0;
		while(file.exists() && counter < 5000)
		{
			counter++;
		}

		RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
		randomFile.setLength(fileSizeToTest);
		randomFile.close();

		ApplicationInfoBundle fileBundle = new DefaultApplicationFileBundle(BundleManifest.APPLICATION_LOGS, "title", "description",
				file.getAbsolutePath());
		List<ApplicationInfoBundle> fileBundles = new ArrayList<ApplicationInfoBundle>();
		fileBundles.add(fileBundle);
		File supportZip = ZipUtility.createSupportZip(fileBundles, applicationInfo, new ValidationLog(applicationInfo));
		ZipInputStream zipStream = new ZipInputStream(new FileInputStream(supportZip));
		ZipEntry zipEntry = zipStream.getNextEntry();
		assertNotNull("Zip file '" + name + "' is null.", zipEntry);
		assertTrue("Zip entry '" + name + "'does not start with bundle key", zipEntry.getName().startsWith(BundleManifest.APPLICATION_LOGS.getKey()));
		assertTrue("Zip entry '" + name + "' does not end with the correct file name.", zipEntry.getName().endsWith(zipEntry.getName()));
		
		if(testSize)
		{
			// ZipInputStream has a known limitation. It does not initialize zip
			// entry sizes
			// if the content is encrypted - we can not use ZipFile as it does
			// not allow us to pass a CipherInputStream
			ZipFile zipFile = new ZipFile(supportZip);
			int expectedSize = Math.min(fileSizeToTest, TWENTY_MB);
			assertEquals("Zip entry '" + name + "'is the wrong size.", expectedSize, zipFile.entries().nextElement()
					.getSize());
		}
		file.delete();
		supportZip.delete();
	}

	public void testCryptRoundtrip() throws Exception
	{
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(byteOut);

		ZipEntry dataEntry = new ZipEntry("Data");
		dataEntry.setTime(1000000L);
		zipOut.putNextEntry(dataEntry);

		DataOutputStream out = new DataOutputStream(zipOut);
		for(int i = 0; i < 100; i++)
		{
			out.writeInt(i);
			out.writeUTF(" ");
		}
		out.close();

		byte[] bytes = byteOut.toByteArray();

		ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(bytes));

		ZipEntry entry = zipIn.getNextEntry();
		while(entry != null && ! entry.getName().equals("Data"))
		{
			entry = zipIn.getNextEntry();
		}

		if(entry == null) fail("Could not find the data entry");

		DataInputStream in = new DataInputStream(zipIn);
		for(int i = 0; i < 100; i++)
		{
			assertEquals(i, in.readInt());
			assertEquals("Expected an empty string here.", in.readUTF(), " ");
		}
		in.close();
	}
	
	public void testCreateZipWithLargeFiles() throws IOException
	{
		MockApplicationInfo info = new MockApplicationInfo();
		File file = new File("/tmp/large.txt");
		RandomAccessFile randomaccessfile = new RandomAccessFile(file,"rw");
		randomaccessfile.setLength(THIRTY_MB);
		randomaccessfile.close();
		
		info.getApplicationFileBundles().add(new DefaultApplicationFileBundle(BundleManifest.APPLICATION_LOGS, "large-file", "a really large file to test zip truncation", file.getAbsolutePath()));
		// write a lot of data to a file in the info bundle
		
		ValidationLog limitedValidationLog = new ValidationLog(info);
		ZipUtility.createSupportZip(info.getApplicationFileBundles(), info, limitedValidationLog, true);		
		assertTrue("No warning was issued when truncating an overly large file...",limitedValidationLog.hasWarnings());
		
		ValidationLog unlimitedValidationLog = new ValidationLog(info);
		ZipUtility.createSupportZip(info.getApplicationFileBundles(), info, unlimitedValidationLog, false);		
		assertFalse("A warning was issued regarding a large file even when the option to limit file sizes was disabled.",unlimitedValidationLog.hasWarnings());

		file.delete();
	}

}
