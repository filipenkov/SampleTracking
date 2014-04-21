package com.atlassian.jira.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @since v4.3
 */
public class TestBuildUtilsInfoImpl
{
    @Test
    public void buildUtilsShouldReportSalVersionFromPropertiesFile() throws Exception
    {
        assertFalse(new BuildUtilsInfoImpl().getSalVersion().isEmpty());
    }

    @Test
    public void buildUtilsShouldReportAppLinksVersionFromPropertiesFile() throws Exception
    {
        assertFalse(new BuildUtilsInfoImpl().getApplinksVersion().isEmpty());
    }
}
