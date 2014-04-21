package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestLists extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestLists());
    }
    
    public TestLists() throws IOException
    {
        super("list");
    }
}
