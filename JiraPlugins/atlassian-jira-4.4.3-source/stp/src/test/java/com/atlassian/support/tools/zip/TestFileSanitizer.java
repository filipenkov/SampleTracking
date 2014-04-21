package com.atlassian.support.tools.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

public class TestFileSanitizer extends TestCase
{

	public void testSanitize()
	{
		List<Pattern> patterns = new ArrayList<Pattern>();
		String passwordMatchString = "password";

		Pattern passwordPattern = Pattern.compile(passwordMatchString + "=(.*)");
		patterns.add(passwordPattern);

		Map<String, List<Pattern>> patternMap = new HashMap<String, List<Pattern>>();
		FileSanitizer sanitizer = new FileSanitizer(patternMap);

		// Test with a file and no patterns, the file itself should be returned
		try
		{
			File matchingFile = new File("/tmp/matching.txt");
			if(matchingFile.exists())
			{
				matchingFile.delete();
			}

			matchingFile.createNewFile();

			// Generate the sample data
			FileWriter matchingOut = new FileWriter(matchingFile);
			matchingOut.append(passwordMatchString + "=12345678\n");
			matchingOut.close();

			byte[] matchingData = IOUtils.toByteArray(new FileInputStream(matchingFile));

			File nonMatchingFile = new File("/tmp/nonmatching.txt");
			if(nonMatchingFile.exists())
			{
				nonMatchingFile.delete();
			}
			nonMatchingFile.createNewFile();

			// Generate the sample data
			FileWriter nonMatchingOut = new FileWriter(nonMatchingFile);
			nonMatchingOut.append("something=12345678\n");
			nonMatchingOut.close();
			byte[] nonMatchingData = IOUtils.toByteArray(new FileInputStream(nonMatchingFile));

			File returnedFile = sanitizer.sanitize(matchingFile);
			assertEquals("Sanitizer doesn't return the original file when there are no patterns.", matchingFile,
					returnedFile);

			patternMap.put(matchingFile.getName(), patterns);
			File returnedFile2 = sanitizer.sanitize(matchingFile);
			byte[] returnedData2 = IOUtils.toByteArray(new FileInputStream(returnedFile2));

			assertFalse("Sanitizer returns the same file data when there is a matching pattern.",
					MessageDigest.isEqual(matchingData, returnedData2));

			patternMap.put(nonMatchingFile.getName(), patterns);
			File returnedFile3 = sanitizer.sanitize(nonMatchingFile);
			byte[] returnedData3 = IOUtils.toByteArray(new FileInputStream(returnedFile3));
			assertTrue("Sanitizer doesn't return the same file data when there are no matches.",
					MessageDigest.isEqual(nonMatchingData, returnedData3));

			sanitizer.cleanUpTempFiles();
//			assertFalse("Temp files were not cleaned up.", returnedFile2.exists() || returnedFile3.exists());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
