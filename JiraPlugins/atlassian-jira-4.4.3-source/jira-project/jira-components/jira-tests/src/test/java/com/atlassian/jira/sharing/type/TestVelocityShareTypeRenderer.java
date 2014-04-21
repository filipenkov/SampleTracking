package com.atlassian.jira.sharing.type;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.web.util.MockOutlookManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharedEntity.TypeDescriptor;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import java.text.DateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Test class for {@link com.atlassian.jira.sharing.type.VelocityShareTypeRenderer}.
 * 
 * @since v3.13
 */
public class TestVelocityShareTypeRenderer extends ListeningTestCase
{
    private static final String VELOCITY_RETURN = "DONE";
    private static final String TEMPLATE_NAME = "template";
    private static final String ENCODING = "UTF-8";
    private static final String I18N_KEY = "i18n";
    private static final String RENDERER_KEY = "renderer";

    @Test
    public void testConstructorWithNullVelocityManager()
    {
        final EncodingConfiguration encoding = new EncodingConfiguration()
        {
            public String getEncoding()
            {
                return "UTF-8";
            }
        };

        try
        {
            new MockVelocityShareTypeRenderer(encoding, null);
            fail("Expecting failure with null manager.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderVelocity()
    {
        final MockProviderAccessor mpa = new MockProviderAccessor();
        final User testUser = new User("test", mpa, new MockCrowdService());
        final JiraAuthenticationContext authenticationContext = new MockAuthenticationContext(testUser, new MockOutlookManager());

        final VelocityManager velocityMgr = new MockVelocityManager()
        {
            public String getEncodedBody(final String templateDir, final String template, final String encoding, final Map parameters) throws VelocityException
            {
                assertEquals(templateDir, "templates/jira/sharing/");
                assertEquals(TestVelocityShareTypeRenderer.TEMPLATE_NAME, template);
                assertEquals(encoding, TestVelocityShareTypeRenderer.ENCODING);
                assertTrue(parameters.containsKey(TestVelocityShareTypeRenderer.I18N_KEY));
                assertSame(authenticationContext.getI18nHelper(), parameters.get(TestVelocityShareTypeRenderer.I18N_KEY));
                assertTrue(parameters.containsKey(TestVelocityShareTypeRenderer.RENDERER_KEY));
                assertNotNull(parameters.get(TestVelocityShareTypeRenderer.RENDERER_KEY));

                return TestVelocityShareTypeRenderer.VELOCITY_RETURN;
            }
        };

        final MockVelocityShareTypeRenderer renderer = new MockVelocityShareTypeRenderer(new EncodingConfiguration.Static(TestVelocityShareTypeRenderer.ENCODING), velocityMgr);
        assertEquals(TestVelocityShareTypeRenderer.VELOCITY_RETURN, renderer.callRenderVelocity(TestVelocityShareTypeRenderer.TEMPLATE_NAME, Collections.EMPTY_MAP, authenticationContext));
    }

    @Test
    public void testRenderVelocityWithException()
    {
        final MockProviderAccessor mpa = new MockProviderAccessor();
        final User testUser = new User("test", mpa, new MockCrowdService());
        final JiraAuthenticationContext authenticationContext = new MockAuthenticationContext(testUser, new MockOutlookManager());

        final VelocityManager velocityMgr = new MockVelocityManager()
        {
            public String getEncodedBody(final String templateDir, final String template, final String encoding, final Map parameters) throws VelocityException
            {
                assertEquals(templateDir, "templates/jira/sharing/");
                assertEquals(TestVelocityShareTypeRenderer.TEMPLATE_NAME, template);
                assertEquals(encoding, TestVelocityShareTypeRenderer.ENCODING);
                assertTrue(parameters.containsKey(TestVelocityShareTypeRenderer.I18N_KEY));
                assertSame(authenticationContext.getI18nHelper(), parameters.get(TestVelocityShareTypeRenderer.I18N_KEY));
                assertTrue(parameters.containsKey(TestVelocityShareTypeRenderer.RENDERER_KEY));
                assertNotNull(parameters.get(TestVelocityShareTypeRenderer.RENDERER_KEY));

                throw new VelocityException("TestException");
            }
        };

        final MockVelocityShareTypeRenderer renderer = new MockVelocityShareTypeRenderer(new EncodingConfiguration.Static(TestVelocityShareTypeRenderer.ENCODING), velocityMgr);
        try
        {
            renderer.callRenderVelocity(TestVelocityShareTypeRenderer.TEMPLATE_NAME, Collections.EMPTY_MAP, authenticationContext);
            fail("A runtime exception should be thrown.");
        }
        catch (final RuntimeException e)
        {
            // expected.
        }
    }

    private class MockVelocityShareTypeRenderer extends VelocityShareTypeRenderer
    {
        public MockVelocityShareTypeRenderer(final EncodingConfiguration encoding, final VelocityManager velocityManager)
        {
            super(encoding, velocityManager);
        }

        public String renderPermission(final SharePermission permission, final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getSimpleDescription(SharePermission permission, JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getDisplayTemplate(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getShareTypeEditor(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isAddButtonNeeded(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public String getShareTypeLabel(final JiraAuthenticationContext userCtx)
        {
            throw new UnsupportedOperationException();
        }

        public Map /*<String, String>*/ getTranslatedTemplates(final JiraAuthenticationContext userCtx, final TypeDescriptor type, final RenderMode mode)
        {
            throw new UnsupportedOperationException();
        }

        protected Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
        {
            return new HashMap();
        }

        public String callRenderVelocity(final String template, final Map parameters, final JiraAuthenticationContext ctx)
        {
            return super.renderVelocity(template, parameters, ctx);
        }
    }

    private class MockVelocityManager implements VelocityManager
    {
        public String getBody(final String s, final String s1, final Map map) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }

        public String getBody(final String s, final String s1, final String s2, final Map map) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }

        public String getEncodedBody(final String s, final String s1, final String s2, final Map map) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }

        public String getEncodedBody(final String s, final String s1, final String s2, final String s3, final Map map) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }

        public String getEncodedBodyForContent(final String s, final String s1, final Map map) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }

        public DateFormat getDateFormat()
        {
            throw new UnsupportedOperationException();
        }

        public String getEncodedBody(final String s, final String s1, final String s2, final String s3, final Context context) throws VelocityException
        {
            throw new UnsupportedOperationException();
        }
    }
}
