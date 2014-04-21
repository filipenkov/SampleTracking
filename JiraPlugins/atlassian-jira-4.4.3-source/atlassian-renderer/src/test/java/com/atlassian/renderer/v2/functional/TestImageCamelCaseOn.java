package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestImageCamelCaseOn extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestImageCamelCaseOn());
    }
    
    public TestImageCamelCaseOn() throws IOException
    {
        super("image");
    }

    protected ExtraSetup getExtraSetup()
    {
        return ExtraSetup.CAMEL_CASE_ON;
    }
}
