package com.atlassian.jira.functest.unittests;

import com.atlassian.jira.functest.config.MissingTestFinder;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * Finds any Func Tests that are missing from our AcceptanceTestHarness.
 *
 * @since v3.13
 */
public class TestAllFuncTestsInHarness
{
    private final Logger log = Logger.getLogger(TestAllFuncTestsInHarness.class);

    private final MissingTestFinder missingTestFinder = new MissingTestFinder();

    @Test
    public void testFindTestsMissingFromTestHarness() throws Exception
    {
        final Set<Class<? extends TestCase>> testsDefinedInSuite = AcceptanceTestHarness.SUITE.getAllTests();
        List<Class<? extends TestCase>> allFuncTests = TestClassUtils.getAllFuncTests();
        Set<Class<? extends TestCase>> testsToIgnore = getIgnoredTests(allFuncTests);
        missingTestFinder.assertAllTestsInTestHarness(allFuncTests, "AcceptanceTestHarness", testsDefinedInSuite, testsToIgnore);
    }

    private Set<Class<? extends TestCase>> getIgnoredTests(List<Class<? extends TestCase>> allFuncTests)
    {
        Set<Class<? extends TestCase>> ignored = Sets.newHashSet();
        for (Class<? extends TestCase> testCase : allFuncTests)
        {
            Ignore ignore = testCase.getAnnotation(Ignore.class);
            if (ignore != null)
            {
                log.warn(String.format("IGNORED %s (%s)", testCase.getName(), ignore.value()));
                ignored.add(testCase);
            }
        }
        // temporary - we don't want the plugins tests to be in a main suite, they are run using categories
        ignored.addAll(TestClassUtils.getJUni3TestClasses("com.atlassian.jira.webtests.ztests.plugin.reloadable", true));
        return ignored;
    }
}
