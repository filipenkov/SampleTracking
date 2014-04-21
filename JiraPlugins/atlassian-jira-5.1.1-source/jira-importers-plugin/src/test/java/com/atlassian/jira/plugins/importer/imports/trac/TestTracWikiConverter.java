/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

public class TestTracWikiConverter {

	@Mock
	private ImportLogger log;

	private TracWikiConverter wikiConverter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		wikiConverter = new TracWikiConverter();
	}

	@Test
	public void testWikiMarkup() {
		String wiki = wikiConverter.convert("{{{\\r\n"
				+ " multiple lines, ''no wiki''\\r\n"
				+ "       white space respected\\r\n"
				+ " }}}", log);

		Assert.assertEquals("{{{\\r\n"
				+ " multiple lines, _no wiki_\\r\n"
				+ "       white space respected\\r\n"
				+ " }}}\n", wiki);

		wiki = wikiConverter.convert(" * '''bold''', \\r\n"
				+ "    ''' triple quotes !''' \\r\n"
				+ "    can be bold too if prefixed by ! ''', \\r\n"
				+ "  * ''italic''\\r\n"
				+ "  * '''''bold italic''''' or ''italic and\\r\n"
				+ "    ''' italic bold ''' ''\\r\n"
				+ "  * __underline__\\r\n"
				+ "  * {{{monospace}}} or `monospace`\\r\n"
				+ "    (hence `{{{` or {{{`}}} quoting)\\r\n"
				+ "  * ~~strike-through~~\\r\n"
				+ "  * ^superscript^ \\r\n"
				+ "  * ,,subscript,,\\r\n"
				+ "  * **also bold**, //italic as well//, \\r\n"
				+ "    and **'' bold italic **'' //(since 0.12)//", log);

		Assert.assertEquals("* *bold*, \\r\n"
				+ "    * triple quotes \\!* \\r\n"
				+ "    can be bold too if prefixed by \\! *, \\r\n"
				+ "  * _italic_\\r\n"
				+ "  * *_bold italic_* or _italic and\\r\n"
				+ "    * italic bold _' _\\r\n"
				+ "  * +underline+\\r\n"
				+ "  * {{monospace}} or {{monospace}}\\r\n"
				+ "    (hence {{{{}} or {{{`}} quoting)\\r\n"
				+ "  * -strike-through-\\r\n"
				+ "  * ^superscript^ \\r\n"
				+ "  * ~subscript~\\r\n"
				+ "  * **also bold**, //italic as well//, \\r\n"
				+ "    and **_ bold italic **'' //(since 0.12)//\n", wiki);
	}

	Map<String, String> conversions = ImmutableMap.<String, String>builder()
			.put("#1", "#1\n")
			.put("ticket:1", "ticket:1\n")
			.put("[ticket:1]", "[ticket:1]\n")
			.put("[[ticket:1]]", "[ticket:1]\n")
			.put("[ticket:1 ticket one]", "[ticket one|ticket:1]\n")
			.put("[[ticket:1|ticket one]]", "[ticket one|ticket:1]\n")
			.build();

	@Test
	public void testIssueLinks() throws Exception {
		for (Map.Entry<String, String> entry : conversions.entrySet()) {
			final String wiki = wikiConverter.convert(entry.getKey(), log);
			Assert.assertEquals(entry.getValue(), wiki);
		}
	}

}
