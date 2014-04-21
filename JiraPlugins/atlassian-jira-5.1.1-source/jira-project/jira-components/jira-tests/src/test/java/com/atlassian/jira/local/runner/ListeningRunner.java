package com.atlassian.jira.local.runner;

import org.junit.runners.BlockJUnit4ClassRunner;

/**
 * A JUnit4 Runner that can listening to specific test events.   You really should not have to use this runner but
 * rather the {@link com.atlassian.jira.local.runner.ListeningPowerMockRunner}
 * <p/>
 * But its here if you want an absolutely vanilla JUnit4 runner
 *
 * @since v4.3
 */
public class ListeningRunner extends AbstractListeningRunner<Class<BlockJUnit4ClassRunner>>
{
    public ListeningRunner(final Class classUnderTest)
    {
        super(classUnderTest, BlockJUnit4ClassRunner.class);
    }
}

