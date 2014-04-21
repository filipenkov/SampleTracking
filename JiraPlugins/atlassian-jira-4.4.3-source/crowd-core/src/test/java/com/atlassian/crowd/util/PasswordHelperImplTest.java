package com.atlassian.crowd.util;

import com.atlassian.crowd.embedded.api.PasswordCredential;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class PasswordHelperImplTest
{
    private PasswordHelper passwordHelper;

    @Before
    public void setUp()
    {
        passwordHelper = new PasswordHelperImpl();
    }

    @Test
    public void testValidateRegexAsTrue()
    {
        assertTrue(passwordHelper.validateRegex("[a-zA-Z]", new PasswordCredential("secret")));
        assertTrue(passwordHelper.validateRegex("[0-9]", new PasswordCredential("123456789")));
    }

    @Test
    public void testValidateRegexAsBad()
    {
        assertFalse(passwordHelper.validateRegex("[a-zA-Z]", new PasswordCredential("123345")));
        assertFalse(passwordHelper.validateRegex("[0-9]", new PasswordCredential("abc")));
    }

    @Test
    public void testWithNullCredential()
    {
        assertFalse(passwordHelper.validateRegex("[0-9]", new PasswordCredential(null, false)));
    }
}
