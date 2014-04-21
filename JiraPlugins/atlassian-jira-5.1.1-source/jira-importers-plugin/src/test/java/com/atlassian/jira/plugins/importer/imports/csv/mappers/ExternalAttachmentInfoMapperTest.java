/*
 * Copyright (c) 2012. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.csv.mappers;

import com.atlassian.jira.plugins.importer.imports.csv.CsvConfigBean;
import com.atlassian.jira.plugins.importer.imports.csv.CsvDateParser;
import junitx.framework.Assert;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import static com.atlassian.jira.plugins.importer.external.beans.ExternalAttachmentInfo.create;

public class ExternalAttachmentInfoMapperTest {
	private final ExternalAttachmentInfoMapper parser = new ExternalAttachmentInfoMapper(new CsvDateParser() {
		@Override
		public Date parseDate(String translatedValue) throws ParseException {
			return CsvConfigBean.parseDate(translatedValue, "yyyy-MM-dd");
		}
	});

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void testParsingHttpAttachments() throws ParseException {
		String a = "http://bla/file;";
		String b = "https://bla/file";
		String withFilename = "myfile.txt;" + b;
		String withAuthor = "an author;myfile2:1;" + a;
		String withTimeStamp = "2012-05-23;another http Author;namewithhttp://inside;http://localhost";
		Assert.assertEquals(create(toUri(a)), parser.parse(a));
		Assert.assertEquals(create(toUri(b)), parser.parse(b));
		Assert.assertEquals(create("myfile.txt", toUri(b)), parser.parse(withFilename));
		Assert.assertEquals(create("an author", "myfile2:1", toUri(a)), parser.parse(withAuthor));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0), "another http Author", "namewithhttp://inside",
				toUri("http://localhost")), parser.parse(withTimeStamp));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0), null, null,
				toUri("http://localhost")), parser.parse("2012-05-23;;;http://localhost"));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0), null, null,
				toUri("http://localhost")), parser.parse("  2012-05-23 ;  ;;http://localhost"));

	}

	@Test
	public void testParsingLocalFileAttachments() throws ParseException {
		String a = "file:file.png;";
		String b = "file:/file.png";
		String c = "file:path/file.png";
		String d = "file:/path/file.png";

		String withFilename = "myfile.txt;" + b;
		String withAuthor = "an author;myfile2:1;" + a;
		String withTimeStamp = "2012-05-23;another http Author;namewithhttp://inside;" + c;

		Assert.assertEquals(create(toUri(a)), parser.parse(a));
		Assert.assertEquals(create(toUri(b)), parser.parse(b));
		Assert.assertEquals(create("myfile.txt", toUri(b)), parser.parse(withFilename));
		Assert.assertEquals(create("an author", "myfile2:1", toUri(a)), parser.parse(withAuthor));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0, 0, 0), "another http Author", "namewithhttp://inside",
				toUri(c)), parser.parse(withTimeStamp));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0, 0, 0), null, null,
				toUri("file:///filepath")), parser.parse("2012-05-23;;;file:///filepath"));
		Assert.assertEquals(create(new DateTime(2012, 5, 23, 0, 0, 0, 0), null, null,
				toUri(d)), parser.parse("  2012-05-23 ;  ;;" + d));

	}

	@Test(expected = ParseException.class)
	public void testInvalidUrl() throws ParseException {
		parser.parse("http:/:/fdsf");
	}

	@Test(expected = ParseException.class)
	public void testInvalidUrl2() throws ParseException {
		parser.parse("http://");
	}

	@Test(expected = ParseException.class)
	public void testTooManySemicolons() throws ParseException {
		parser.parse("fds;fds;fds;fsd;http://goodurl.com");
	}

	@Test(expected = ParseException.class)
	public void testInvalidDate() throws ParseException {
		parser.parse("2010-10x04;author;file;http://localhost");
	}

	private URI toUri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}
