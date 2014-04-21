package com.atlassian.jira.plugin.ext.bamboo.upgrade.legacy.manager;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BambooStringEncrypterTest
{
    private BambooStringEncrypter bambooStringEncrypter;

    @Before
    public void setUp() throws Exception
    {
        bambooStringEncrypter = new BambooStringEncrypter();
    }

    @Test
    public void testEncryptEmptyString()
    {
        assertEquals("", bambooStringEncrypter.encrypt(""));
    }

    @Test
    public void testEncryptNullString()
    {
        assertEquals("", bambooStringEncrypter.encrypt(null));
    }

    @Test
    public void testDecryptEmptyString()
    {
        assertEquals("", bambooStringEncrypter.decrypt(""));
    }

    @Test
    public void testDecryptNullString()
    {
        assertEquals("", bambooStringEncrypter.decrypt(null));
    }

    @Test
    public void testEncryptedStringCanBeDecrypted()
    {
        String stringToDecrypt = "foobar";

        assertEquals(
                stringToDecrypt,
                bambooStringEncrypter.decrypt(
                        bambooStringEncrypter.encrypt(
                                stringToDecrypt
                        )
                )
        );
    }
}
