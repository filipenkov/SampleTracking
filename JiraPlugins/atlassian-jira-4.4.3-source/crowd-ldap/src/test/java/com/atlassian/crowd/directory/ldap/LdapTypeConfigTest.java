package com.atlassian.crowd.directory.ldap;

import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.2
 */
public class LdapTypeConfigTest
{
    @Test
    public void testGetKey()
    {
        LdapTypeConfig config = new LdapTypeConfig("TestDirectorykey", "Test Directory Name", new Properties());
        Assert.assertEquals("TestDirectorykey", config.getKey());
    }

    @Test
    public void testGetDisplayName()
    {
        LdapTypeConfig config = new LdapTypeConfig("TestDirectorykey", "Test Directory Name", new Properties());
        Assert.assertEquals("Test Directory Name", config.getDisplayName());
    }

    @Test
    public void testGetLdapTypeAsJson()
    {
        final Properties defaultValues = new Properties();
        defaultValues.put("field.1", "default value 1");
        defaultValues.put("field.2", "default value 2");
        defaultValues.put("field.3", "default value 3");
        LdapTypeConfig config = new LdapTypeConfig("TestDirectorykey", "Test Directory Name", defaultValues);
        config.setHiddenField("field.1");
        config.setHiddenField("field.23");
        config.setHiddenField("field.24");

        // We use this horrible mix of contains and regex because the order of the contents of the Properties objects backing this is undefined.

        final String json = config.getLdapTypeAsJson();
        Assert.assertTrue(json.contains("{ \"key\": \"TestDirectorykey\", \"defaults\": {"));
        Assert.assertTrue(json.contains("\"field-1\":\"default value 1\""));
        Assert.assertTrue(json.contains("\"field-2\":\"default value 2\""));
        Assert.assertTrue(json.contains("\"field-3\":\"default value 3\""));
        Assert.assertTrue(json.contains("},\"hidden\": ["));
        Assert.assertTrue(json.contains("\"field-1\""));
        Assert.assertTrue(json.contains("\"field-23\""));
        Assert.assertTrue(json.contains("\"field-24\""));

        Assert.assertTrue(json.matches("\\{ \"key\": \"TestDirectorykey\", \"defaults\": \\{\"field-[1-9]*\":\"default value [1-9]*\",\"field-[1-9]*\":\"default value [1-9]*\",\"field-[1-9]*\":\"default value [1-9]*\"\\},\"hidden\": \\[\"field-[1-9]*\",\"field-[1-9]*\",\"field-[1-9]*\"\\]\\}"));
    }
}
