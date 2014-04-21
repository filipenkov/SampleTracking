package com.atlassian.johnson.setup;

import com.atlassian.johnson.Initable;

/**
 * This class determines whether an application is setup or not
 */
public interface SetupConfig extends Initable
{
    boolean isSetup();

    boolean isSetupPage(String uri);
}
