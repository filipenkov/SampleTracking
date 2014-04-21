package com.atlassian.renderer.v2.functional;

// TODO: rip this out when we have more time.
public abstract class ExtraSetup
{
    public static final ExtraSetup CAMEL_CASE_ON = new ExtraSetup()
    {
        public void setUp(FunctionalTestSetup setup) throws Exception
        {
        }
    };

    public static final ExtraSetup CAMEL_CASE_OFF = new ExtraSetup()
    {
        public void setUp(FunctionalTestSetup setup) throws Exception
        {
        }
    };


    public void setUp(FunctionalTestSetup setup) throws Exception {}
    public void tearDown(FunctionalTestSetup setup) throws Exception {}
}
