package com.atlassian.support.tools.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.atlassian.support.tools.salext.AbstractSupportApplicationInfo;

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
			nonMatchingFile.delete();
			matchingFile.delete();
//			assertFalse("Temp files were not cleaned up.", returnedFile2.exists() || returnedFile3.exists());

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testTomcatSecurity() throws IOException {
		FileSanitizer sanitizer = new FileSanitizer(AbstractSupportApplicationInfo.FILE_PATTERNS);

		// username|password|
		String[] badWords = { "username", "password", "keystorePass", "truststorePass", "connectionPassword", "connectionName" };

		File tempFile = new File("/tmp/server.xml");
		tempFile.createNewFile();
		FileWriter writer = new FileWriter(tempFile);
		for ( String word : badWords) {
			writer.write(word + "=\"secret\"\n");
			writer.write(" " + word + "=\"secret\"\n");
			writer.write(word + " =\"secret\"\n");
			writer.write(" " + word + " =\"secret\"\n");
			writer.write(" " + word + " = \"secret\"\n");
			writer.write(word + "='secret'\n");
			writer.write(word + "= 'secret'\n");
			writer.write(word + "=secret\n");
			writer.write(word + "= secret\n");
			writer.write(word.toUpperCase() + "=\"secret\"\n");
		}
		writer.close();

		File sanitizedFile = sanitizer.sanitize(tempFile);
		
		char[] buffer = new char[(int) sanitizedFile.length()];
		FileReader reader = new FileReader(sanitizedFile);
		reader.read(buffer);
		reader.close();
		
		String sanitizedOutput = String.valueOf(buffer);
		
		for (String word : badWords) {
			assertTrue("Our test file wasn't a fair test, there was no match for the control word '" + word + "' in the output.",sanitizedOutput.contains(word));
		}
		assertFalse("At least one controlled word wasn't sanitized, I still see the original 'secret' text at least once.",sanitizedOutput.contains("secret"));
		
		tempFile.delete();
		sanitizedFile.delete();
	}

	public void testCrowdSecurity() throws IOException {
		FileSanitizer sanitizer = new FileSanitizer(AbstractSupportApplicationInfo.FILE_PATTERNS);
		
		File originalFile = new File("target/test-classes/crowd.properties");
		File sanitizedFile = sanitizer.sanitize(originalFile);
		
		char[] buffer = new char[(int) sanitizedFile.length()];
		FileReader reader = new FileReader(sanitizedFile);
		reader.read(buffer);
		reader.close();
		
		String sanitizedOutput = String.valueOf(buffer);
		
		assertFalse("At least one piece of sensitive data wasn't sanitized, I still see the original 'secret' text at least once.",sanitizedOutput.contains("secret"));
		sanitizedFile.delete();
	}
}
