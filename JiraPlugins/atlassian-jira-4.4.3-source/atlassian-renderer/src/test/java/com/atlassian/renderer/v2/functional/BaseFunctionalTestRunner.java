package com.atlassian.renderer.v2.functional;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class BaseFunctionalTestRunner extends TestCase
{
    protected Properties testCases;
    protected Set keys;
    protected FunctionalTestSetup testSetup = new FunctionalTestSetup();

    private class V2RendererSetup extends TestSetup
    {
        public V2RendererSetup(Test test)
        {
            super(test);
        }

        protected void setUp() throws Exception
        {
            super.setUp();
            testSetup.setUp();
        }

        protected void tearDown() throws Exception
        {
            super.tearDown();
        }
    }

    private class RendererFunctionalTest extends TestCase
    {
        private final FunctionalTest test;
        private final ExtraSetup extraSetup;

        public RendererFunctionalTest(String name, FunctionalTest test, ExtraSetup extraSetup)
        {
            super(name);
            this.test = test;
            this.extraSetup = extraSetup;
        }

        public void runBare() throws Throwable
        {
            try
            {
                if (extraSetup != null)
                    extraSetup.setUp(testSetup);

                test.run(testSetup);
            }
            finally
            {
                if (extraSetup != null)
                    extraSetup.tearDown(testSetup);
            }
        }
    }

    public static Test makeSuite(BaseFunctionalTestRunner testRunner) throws IOException
    {
        TestSuite suite = new TestSuite();
        suite.setName(testRunner.getClass().getName());
        testRunner.addTests(suite);
        return testRunner.makeSetup(suite);
    }

    public BaseFunctionalTestRunner(String testsFile) throws IOException
    {
        testCases = new Properties();
        InputStream stream = getClass().getResourceAsStream("/render-tests/" + testsFile + "-render-tests.properties");
        if (stream == null)
            System.err.println("Warning: Could not load tests for " + testsFile);

        testCases.load(stream);

        if (testCases.isEmpty())
            System.err.println("Warning: No testcases found for " + testsFile);
    }

    public void addTests(TestSuite suite) throws IOException
    {
        List keys = new ArrayList(testCases.keySet());
        Collections.sort(keys);

        for (Iterator it = keys.iterator(); it.hasNext();)
        {
            String key = (String) it.next();
            if (key.startsWith(FunctionalTest.WIKI_NOTATION))
            {
                String testName = key.substring(FunctionalTest.WIKI_NOTATION.length());
                FunctionalTest test = new FunctionalTest(testName, testCases);
                String testFullName = getClassName(getClass()) + ": " + test.getName();

                suite.addTest(new RendererFunctionalTest(testFullName, test, getExtraSetup()));
            }
        }
    }

    protected ExtraSetup getExtraSetup()
    {
        return null;
    }

    private Test makeSetup(Test test)
    {
        return new V2RendererSetup(test);
    }

    private String getClassName(Class clazz)
    {
        String name = clazz.getName();

        if (name.indexOf(".") >= 0)
            name = name.substring(name.lastIndexOf(".") + 1);

        return name;
    }
}
