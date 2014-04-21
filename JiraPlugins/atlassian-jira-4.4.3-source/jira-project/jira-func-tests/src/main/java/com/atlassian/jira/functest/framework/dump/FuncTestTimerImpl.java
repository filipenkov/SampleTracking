package com.atlassian.jira.functest.framework.dump;

/**
 * @since v4.1
 */
public class FuncTestTimerImpl implements FuncTestTimer
{
    private final String name;
    private final long then;

    public FuncTestTimerImpl(final String name)
    {
        this.name = name;
        this.then = System.currentTimeMillis();
    }

    public String getName()
    {
        return name;
    }

    public long end()
    {
        long time = System.currentTimeMillis() - this.then;
        TestInformationKit.recordCounter(name,time);
        return time;
    }
}
