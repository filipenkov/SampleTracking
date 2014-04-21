package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.MockProviderAccessor;
import com.opensymphony.user.User;

public class TestTrustedApplicationDelegateValidator extends ListeningTestCase
{
    @Test
    public void testReturnsTrue()
    {
        MockValidator one = new MockValidator(true);
        MockValidator two = new MockValidator(true);
        assertTrue(new TrustedApplicationDelegateValidator(one, two).validate(new Context("fred"), null, new TrustedApplicationBuilder().toSimple()));
        assertEquals(1, one.calledCount);
        assertEquals(1, two.calledCount);
    }

    @Test
    public void testReturnsFalseIfSecondOneReturnsFalse()
    {
        MockValidator one = new MockValidator(true);
        MockValidator two = new MockValidator(false);
        assertFalse(new TrustedApplicationDelegateValidator(one, two).validate(new Context("fred"), null, new TrustedApplicationBuilder().toSimple()));
        assertEquals(1, one.calledCount);
        assertEquals(1, two.calledCount);
    }

    @Test
    public void testReturnsFalseIfFirstOneReturnsFalse()
    {
        MockValidator one = new MockValidator(false);
        MockValidator two = new MockValidator(true);
        assertFalse(new TrustedApplicationDelegateValidator(one, two).validate(new Context("fred"), null, new TrustedApplicationBuilder().toSimple()));
        assertEquals(1, one.calledCount);
        assertEquals(1, two.calledCount);
    }

    @Test
    public void testReturnsFalseIfBothReturnsFalse()
    {
        MockValidator one = new MockValidator(false);
        MockValidator two = new MockValidator(false);
        assertFalse(new TrustedApplicationDelegateValidator(one, two).validate(new Context("fred"), null, new TrustedApplicationBuilder().toSimple()));
        assertEquals(1, one.calledCount);
        assertEquals(1, two.calledCount);
    }

    @Test
    public void testThrowsNPEIfNullArray()
    {
        try
        {
            new TrustedApplicationDelegateValidator(null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testThrowsNPEIfNullValidators()
    {
        try
        {
            new TrustedApplicationDelegateValidator(null, null);
            fail("IAE expected");
        }
        catch (IllegalArgumentException yay)
        {
            // expected
        }
    }

    private static class Context extends JiraServiceContextImpl
    {
        private Context(String name)
        {
            super(new User(name, new MockProviderAccessor(), new MockCrowdService()));
        }
    }
}
