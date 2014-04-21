/*
 * Copyright (c) 2011. Atlassian
 * All rights reserved
 */
package com.atlassian.jira.plugins.importer.imports.mantis.transformer;

import com.atlassian.jira.issue.customfields.converters.DateTimePickerConverter;
import com.atlassian.jira.plugins.importer.external.beans.ExternalCustomField;
import com.atlassian.jira.plugins.importer.external.beans.ExternalProject;
import com.atlassian.jira.plugins.importer.imports.importer.ImportLogger;
import com.atlassian.jira.plugins.importer.imports.importer.SingleStringResultTransformer;
import com.atlassian.jira.plugins.importer.imports.mantis.MantisConfigBean;
import com.atlassian.jira.plugins.importer.web.JdbcConnection;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

public class TestIssueTransformerVer118OrOlder {

	private MantisConfigBean configBean;
	private JdbcConnection jdbcConnection;
	private ExternalProject externalProject;
	private DateTimePickerConverter dateTimePickerConverter;
	private ImportLogger log;
	private ResultSet rs;

	@Before
	public void setUp() {
		configBean = mock(MantisConfigBean.class);
        jdbcConnection = mock(JdbcConnection.class);

		when(configBean.getJdbcConnection()).thenReturn(jdbcConnection);

		externalProject = new ExternalProject();
		dateTimePickerConverter = mock(DateTimePickerConverter.class);
		log = mock(ImportLogger.class);

		rs = mock(ResultSet.class);
	}

    /**
     * Test case for https://studio.atlassian.com/browse/JIM-300
     */
    @Test
    public void testEmptyDateCustomField() throws SQLException {
        List<String> customFieldValues = Lists.newArrayList("");
        when(jdbcConnection.queryDb(Matchers.<SingleStringResultTransformer> any())).thenReturn(customFieldValues);

        ExternalCustomField customField = ExternalCustomField.createDatetime("345", "Date");

        IssueTransformerVer118OrOlder transformer = new IssueTransformerVer118OrOlder("http://localhost",
				configBean, externalProject, dateTimePickerConverter, log);
        Assert.assertNull(transformer.getCustomFieldValue(rs, "12", customField));
    }

	/**
     * Test case for https://studio.atlassian.com/browse/JIM-304
     */
    @Test
    public void testDateCustomField() throws SQLException, ParseException {
		Long timeFromEpoch = 1323990000l;
        List<String> customFieldValues = Lists.newArrayList(timeFromEpoch.toString());
        when(jdbcConnection.queryDb(Matchers.<SingleStringResultTransformer> any())).thenReturn(customFieldValues);

        ExternalCustomField customField = ExternalCustomField.createDatetime("345", "Date");

        IssueTransformerVer118OrOlder transformer = new IssueTransformerVer118OrOlder("http://localhost",
				configBean, externalProject, dateTimePickerConverter, log);
		transformer.getCustomFieldValue(rs, "12", customField);

        verify(dateTimePickerConverter).getString(new Date(Long.valueOf(timeFromEpoch) * 1000));
    }

}
