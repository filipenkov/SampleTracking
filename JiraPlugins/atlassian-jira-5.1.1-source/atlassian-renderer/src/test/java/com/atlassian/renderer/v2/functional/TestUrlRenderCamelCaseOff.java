package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestUrlRenderCamelCaseOff extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestUrlRenderCamelCaseOff());
    }
    
    public TestUrlRenderCamelCaseOff() throws IOException
    {
        super("url");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_OFF;
    }
}
