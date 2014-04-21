package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestTemplateParam extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestTemplateParam());
    }
    
    public TestTemplateParam() throws IOException
    {
        super("templateparam");
    }
}
