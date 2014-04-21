package com.atlassian.config.wizard;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 13:50:02
 * To change this template use File | Settings | File Templates.
 */
public interface SetupStep
{
    public String getName();

    public void setName(String name);

    public String getActionName();

    public void setActionName(String name);

    public int getIndex();

    public void setIndex(int index);

    public void onNext();

    public void onStart();
}
