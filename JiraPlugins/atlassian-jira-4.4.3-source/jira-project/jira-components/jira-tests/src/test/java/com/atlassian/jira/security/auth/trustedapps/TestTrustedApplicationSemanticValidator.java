package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;

import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.List;

public class TestTrustedApplicationSemanticValidator extends ListeningTestCase
{
    @Test
    public void testNullInCtor()
    {
        try
        {
            new TrustedApplicationSemanticValidator(null);
            fail("IAE expected");
        }
        catch (final IllegalArgumentException yay)
        {
            // expected
        }
    }

    @Test
    public void testValidateApplicationIDAlreadyExists()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "appId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDDoesntAlreadyExist()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "anAppId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertTrue(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDCantBeFound()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "anAppId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(2, "appId", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateApplicationIDCorrect()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "anAppId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(1, "anAppId", "new name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertTrue(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateBlankApplicationID()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "anAppId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "", "name", 1000));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertTrue(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(0, context.getErrorCollection().getErrorMessages().size());
    }

    @Test
    public void testValidateBadPublicKey()
    {
        final List infos = new ArrayList();
        infos.add(new MockTrustedApplicationInfo(1, "anAppId", "name", 1000));
        final TrustedApplicationSemanticValidator validator = new TrustedApplicationSemanticValidator(new MockTrustedApplicationManager(infos));

        final TrustedApplicationBuilder builder = new TrustedApplicationBuilder();
        builder.set(new MockTrustedApplicationInfo(0, "", "name", 1000, "1.1.1.1", "/some/url/", new KeyFactory.InvalidPublicKey(new IllegalArgumentException())));
        final JiraServiceContext context = new JiraServiceContextImpl(new User("name", new MockProviderAccessor(), new MockCrowdService()));
        assertFalse(validator.validate(context, new MockI18nHelper(), builder.toSimple()));
        assertEquals(1, context.getErrorCollection().getErrorMessages().size());
    }
}
