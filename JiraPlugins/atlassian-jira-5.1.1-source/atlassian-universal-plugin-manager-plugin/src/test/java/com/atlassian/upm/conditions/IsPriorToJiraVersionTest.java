package com.atlassian.upm.conditions;

import com.atlassian.sal.api.ApplicationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is a unit test for IsPriorToJiraVersion
 *
 * @since v4.4
 */
@RunWith(MockitoJUnitRunner.class)
public class IsPriorToJiraVersionTest {

    private final String majorVersionKey = "majorVersion";
    private final String minorVersionKey = "minorVersion";

    @Mock ApplicationProperties applicationProperties;
    @Mock Map<String,String> paramMap;
    private IsPriorToJiraVersion isPriorToJiraVersion;

    @Test
    public void assertThatIsPriorReturnsCorrectlyForVariousVersionStrings()
    {
        checkVersionStringAgainstMinorMajorVersion("1.1","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.1-SNAPSHOT","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.1.1","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.1.1.1.1","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.1-r039","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.1-r039","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("1.01-r039","4","4",true);
        checkVersionStringAgainstMinorMajorVersion("01.4","4","4",true);

        checkVersionStringAgainstMinorMajorVersion("4.4","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("4.4-SNAPSHOT","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("4.4.1","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("4.5.1","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("5.0-r039","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("5.5-r039","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("5.05","4","4",false);
        checkVersionStringAgainstMinorMajorVersion("05.4","4","4",false);
    }

    private void checkVersionStringAgainstMinorMajorVersion(String versionString, String majorVersion, String minorVersion, boolean expectedResult)
    {
        when(applicationProperties.getVersion()).thenReturn(versionString);
        when(paramMap.get(majorVersionKey)).thenReturn(majorVersion);
        when(paramMap.get(minorVersionKey)).thenReturn(minorVersion);

        isPriorToJiraVersion = new IsPriorToJiraVersion(applicationProperties);
        isPriorToJiraVersion.init(paramMap);

        assertThat("Failed for version string "+versionString+" and majorVersion "+majorVersion+", minorVersion "+minorVersion,
                expectedResult, is(equalTo(isPriorToJiraVersion.shouldDisplay(null))));
    }

}
