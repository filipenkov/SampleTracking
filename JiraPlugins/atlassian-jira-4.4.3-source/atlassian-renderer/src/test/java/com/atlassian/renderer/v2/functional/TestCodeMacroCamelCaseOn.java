package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestCodeMacroCamelCaseOn extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestCodeMacroCamelCaseOn());
    }
    
    public TestCodeMacroCamelCaseOn() throws IOException
    {
        super("code-camelcase-macro");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_ON;
    }
}
