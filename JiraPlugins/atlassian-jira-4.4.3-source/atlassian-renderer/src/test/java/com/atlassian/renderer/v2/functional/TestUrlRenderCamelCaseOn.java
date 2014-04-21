package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestUrlRenderCamelCaseOn extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestUrlRenderCamelCaseOn());
    }
    
    public TestUrlRenderCamelCaseOn() throws IOException
    {
        super("url");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_ON;
    }
}
