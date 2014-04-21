package com.atlassian.config.wizard;

import com.atlassian.config.ConfigurationException;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 15/03/2004
 * Time: 14:39:14
 * To change this template use File | Settings | File Templates.
 */
public interface SaveStrategy
{
    public void save(SetupWizard setupWizard) throws ConfigurationException;
}
