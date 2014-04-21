package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestPreformattedMacroCamelCaseOn extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestPreformattedMacroCamelCaseOn());
    }
    
    public TestPreformattedMacroCamelCaseOn() throws IOException
    {
        super("preformatted-camelcase-macro");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_ON;
    }
}
