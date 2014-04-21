package com.atlassian.renderer.v2.functional;

import junit.framework.Test;

import java.io.IOException;

public class TestListsAndTables extends BaseFunctionalTestRunner
{
    public static Test suite() throws IOException
    {
        return BaseFunctionalTestRunner.makeSuite(new TestListsAndTables());
    }
    
    public TestListsAndTables() throws IOException
    {
        super("lists-and-tables");
    }
}
