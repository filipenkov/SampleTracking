package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestCodeMacroCamelCaseOff extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestCodeMacroCamelCaseOff());
    }
    
    public TestCodeMacroCamelCaseOff() throws IOException
    {
        super("code-macro");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_OFF;
    }
}
