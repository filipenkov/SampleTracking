package com.atlassian.jira.util;

import com.google.common.primitives.Ints;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void buildUtilsShouldReportGuavaOsgiVersion() throws Exception
    {
        Pattern p = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");
        final String ver = new BuildUtilsInfoImpl().getGuavaOsgiVersion();
        assertTrue(ver, p.matcher(ver).matches());
    }

    @Test
    public void parsingVersionNumbers() throws Exception
    {
        int[] v500 = {5, 0, 0};
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0"), equalTo(v500));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0-SNAPSHOT"), equalTo(v500));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0-beta1"), equalTo(v500));

        int[] v501 = {5, 0, 1};
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1"), equalTo(v501));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-SNAPSHOT"), equalTo(v501));
        assertThat(BuildUtilsInfoImpl.parseVersion("5.0.1-beta1"), equalTo(v501));

        int[] v000 = {0, 0, 0};
        assertThat(BuildUtilsInfoImpl.parseVersion("error.5.0.1"), equalTo(v000));
        assertThat(BuildUtilsInfoImpl.parseVersion("error5.0.1"), equalTo(v000));

        // make sure our documented use of Ints#lexicographicalComparator works

        int[] v510 = {5, 1, 0};
        assertThat(Ints.lexicographicalComparator().compare(v500, v501), lessThan(0));
        assertThat(Ints.lexicographicalComparator().compare(v500, v510), lessThan(0));
        assertThat(Ints.lexicographicalComparator().compare(v501, v510), lessThan(0));
    }
}
