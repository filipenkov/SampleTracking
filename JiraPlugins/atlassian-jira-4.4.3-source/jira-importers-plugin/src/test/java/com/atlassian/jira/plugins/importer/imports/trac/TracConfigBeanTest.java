/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */

package com.atlassian.jira.plugins.importer.imports.trac;

import com.atlassian.jira.plugins.importer.external.ExternalUtils;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TracConfigBeanTest {
	public static final List<String> EXPECTED_RADIO = Arrays.asList("A", "B", "c", "DDDDD");
	public static final List<String> EXPECTED_TYPE_OF_SERVICE = Arrays
			.asList("Systems Access", "New UIN/Token", "New Joiners", "Exit procedure", "BT assets, EINs and BT e-mails", "Aliases Management", "RSA (SecureID) tokens", "Other", "SYSTEM ACCESS");

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private ExternalUtils externalUtils;
	@Mock
	private JdbcConnection jdbcConnection;

	private File tracPgFile;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(externalUtils.getAuthenticationContext().getI18nHelper().getText(Mockito.anyString())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return invocation.getArguments()[0];
			}
		});

		tracPgFile = new File(getClass().getResource("/trac/trac-pg.zip").getFile());
	}

	@Test
	public void testReadOptionsForSelectAndRadio() throws Exception {

		final TracConfigBean configBean = new TracConfigBean(jdbcConnection, externalUtils, tracPgFile);

		final ImmutableMap<String, ExternalCustomField> customFields = Maps.uniqueIndex(
				configBean.getCustomFields(),
				new Function<ExternalCustomField, String>() {
					@Override
					public String apply(@Nullable ExternalCustomField input) {
						return input.getName();
					}
				});
		Assert.assertEquals(EXPECTED_RADIO,
				customFields.get("Custom radio").getValueSet());
		Assert.assertEquals(EXPECTED_TYPE_OF_SERVICE,
				customFields.get("Type of service requested").getValueSet());
	}
}
