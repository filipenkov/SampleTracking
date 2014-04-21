/**
 * Copyright 2002-2007 Atlassian.
 */
package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

public class TestTrustedApplicationData extends ListeningTestCase
{
    @Test
    public void testAllNotNullNoException()
    {
        new TrustedApplicationData("applicationId", "name", "publikKey", 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()),"ipMatch", "urlMatch");
    }

    @Test
    public void testApplicationIdNullException()
    {
        try
        {
            new TrustedApplicationData(null, "name", "publikKey", 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), "ipMatch", "urlMatch");
            fail("IAE Expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testNameNullException()
    {
        try
        {
            new TrustedApplicationData("applicationId", null, "publikKey", 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), "ipMatch", "urlMatch");
            fail("IAE Expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testPublicKeyNullException()
    {
        try
        {
            new TrustedApplicationData("applicationId", "name", null, 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), "ipMatch", "urlMatch");
            fail("IAE Expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCreatedNullException()
    {
        try
        {
            new TrustedApplicationData("applicationId", "name", "publikKey", 0, null, new AuditLog("updated", new Date()), "ipMatch", "urlMatch");
            fail("IAE Expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testUpdatedNullException()
    {
        try
        {
            new TrustedApplicationData("applicationId", "name", "publikKey", 0, new AuditLog("created", new Date()), null, "ipMatch", "urlMatch");
            fail("IAE Expected");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testIpMatchNullNoException()
    {
        new TrustedApplicationData("applicationId", "name", "publikKey", 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), null, "urlMatch");
    }

    @Test
    public void testUrlMatchNullNoException()
    {
        new TrustedApplicationData("applicationId", "name", "publikKey", 0, new AuditLog("created", new Date()), new AuditLog("updated", new Date()), "ipMatch", null);
    }
}
