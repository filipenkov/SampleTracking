package com.atlassian.config.wizard;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 13:55:06
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSetupStep implements SetupStep
{
    private String name;
    private String actionName;
    private int index;

    public DefaultSetupStep(String name, String actionName)
    {
        this.name = name;
        this.actionName = actionName;
    }

    public DefaultSetupStep()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public String getActionName()
    {
        return actionName;
    }

    public void setActionName(String actionName)
    {
        this.actionName = actionName;
    }

    public void onNext()
    {
        //noop
    }

    public void onStart()
    {
        //noop
    }
}
