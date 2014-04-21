package com.atlassian.config;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApplicationConfigurationTestCase extends TestCase
{
    protected ApplicationConfiguration cfg;

    protected void setUp() throws Exception
    {
        super.setUp();
        cfg = new ApplicationConfig();
    }

    public void testApplicationConfigurationProperties() throws Exception
    {

        cfg.setProperty("testObject", new ArrayList());
        cfg.setProperty("testInteger", 4);
        cfg.setProperty("testBoolean", true);

        assertTrue(cfg.getProperty("testObject") instanceof List);
        Assert.assertEquals(4, cfg.getIntegerProperty("testInteger"));
        Assert.assertEquals(true, cfg.getBooleanProperty("testBoolean"));
        assertNull(cfg.getProperty("doesNotExist"));
        Assert.assertEquals(Integer.MIN_VALUE, cfg.getIntegerProperty("doesNotExist"));
        Assert.assertEquals(false, cfg.getBooleanProperty("doesNotExist"));

        assertTrue(!cfg.isSetupComplete());
        cfg.setSetupComplete(true);
        assertTrue(cfg.isSetupComplete());

        cfg.setProperty("prefix.property1", "value1");
        cfg.setProperty("prefix.property2", "value2");
        cfg.setProperty("prefix.property3", "value3");

        Assert.assertEquals(6, cfg.getProperties().size());

        Map prefixProps = cfg.getPropertiesWithPrefix("prefix.");
        assertEquals(3, prefixProps.size());
        assertEquals("value2", prefixProps.get("prefix.property2"));

        cfg.setProperty("prefix.property2", null);
        assertNull(cfg.getProperty("prefix.property2"));

    }

    public void testApplicationConfigurationVersion() throws Exception
    {
        cfg.setBuildNumber("62");
        cfg.setMajorVersion(1);
        cfg.setMinorVersion(6);

        Assert.assertEquals("1.6 build: 62", cfg.getApplicationVersion());
    }

    public void testApplicationConfigurationHome() throws Exception
    {
        cfg.setApplicationHome(".");

        Assert.assertEquals(new File(".").getCanonicalPath(), cfg.getApplicationHome());
        assertTrue(cfg.isApplicationHomeValid());

        //Should create path automatically
        String tempDir = System.getProperty("java.io.tmpdir");
        File f = new File(tempDir + "_does_not_exist_");
        if (f.exists()) f.delete();

        cfg.setApplicationHome(tempDir + "/_does_not_exist_");
        assertTrue(cfg.isApplicationHomeValid());

        try
        {
            cfg.setApplicationHome(getInvalidHomeForOperatingSystem());
            fail("should throw exception if home dir cannot be created");
        }
        catch (ConfigurationException e)
        {
            //expected
        }
        assertTrue(!cfg.isApplicationHomeValid());
    }

    private String getInvalidHomeForOperatingSystem()
    {
        // On OS X, we need to try to create the home in /private, because / is writable by default for admin
        // users.
        if ("Mac OS X".equals(System.getProperty("os.name")))
            return "/private/bad-confluence-home";

        // This should fail on Linux and Windows.
        return "/|does|not|exist/cheese";
    }
}
