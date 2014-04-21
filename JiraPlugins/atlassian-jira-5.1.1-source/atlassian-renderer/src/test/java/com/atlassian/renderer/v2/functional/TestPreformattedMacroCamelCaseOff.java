package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestPreformattedMacroCamelCaseOff extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestPreformattedMacroCamelCaseOff());
    }
    
    public TestPreformattedMacroCamelCaseOff() throws IOException
    {
        super("preformatted-macro");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_OFF;
    }
}
