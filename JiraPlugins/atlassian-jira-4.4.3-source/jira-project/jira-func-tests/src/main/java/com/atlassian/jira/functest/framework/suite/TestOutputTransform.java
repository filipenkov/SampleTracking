package com.atlassian.jira.functest.framework.suite;

import com.atlassian.jira.functest.framework.log.FuncTestOut;
import com.google.common.collect.ImmutableList;
import org.junit.runner.Description;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Transform that logs single run tests.
 *
 * @since v4.4
 */
public class TestOutputTransform implements SuiteTransform
{
    private boolean hasRun = false;


    @Override
    public Iterable<Description> apply(@Nullable Iterable<Description> input)
    {
        if (!hasRun)
        {
            outputTests(input);
            hasRun = true;
        }
        return input;
    }

    private void outputTests(Iterable<Description> descriptions)
    {
        final List<Description> singleTests = getSingleTests(descriptions);
        FuncTestOut.log("***** Running " + singleTests.size() + " tests in total *****");
        for (Description singleTest : singleTests)
        {
            FuncTestOut.log(singleTest.getDisplayName());
        }
    }

    private List<Description> getSingleTests(Iterable<Description> input)
    {
        ImmutableList.Builder<Description> builder = ImmutableList.builder();
        for (Description description : input)
        {
            if (description.isTest())
            {
                builder.add(description);
            }
            else
            {
                builder.addAll(getSingleTests(description.getChildren()));
            }
        }
        return builder.build();
    }

}
