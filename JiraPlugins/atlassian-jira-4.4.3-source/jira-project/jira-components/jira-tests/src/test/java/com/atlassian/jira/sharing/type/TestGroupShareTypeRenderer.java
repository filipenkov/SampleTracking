package com.atlassian.jira.sharing.type;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.config.util.EncodingConfiguration;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.mock.web.util.MockOutlookManager;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.user.MockCrowdService;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.exception.VelocityException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for {@link com.atlassian.jira.sharing.type.GroupShareTypeRenderer}. We test the #getShareTypeEditor in functional tests as it relies on
 * Velocity.
 *
 * @since v3.13
 */

public class TestGroupShareTypeRenderer extends ListeningTestCase
{
    private static final String GROUP1 = "group1";
    private static final String GROUP2 = "abcgroup2";
    private static final SharePermission GROUP_PREM_1 = new SharePermissionImpl(GroupShareType.TYPE, TestGroupShareTypeRenderer.GROUP1, null);
    private static final SharePermission GROUP_PREM_XSS = new SharePermissionImpl(GroupShareType.TYPE, "<b>XSS</b>", null);
    private static final SharePermission GLOBAL_PREM = new SharePermissionImpl(GlobalShareType.TYPE, null, null);
    private static final String VELOCITY_RETURN = "<selector><option>b</option></selector>";
    private static final String GROUPS_KEY = "groups";

    private JiraAuthenticationContext userCtx;
    private JiraAuthenticationContext anonymousCtx;
    private GroupShareTypeRenderer renderer;
    private User user;
    private VelocityManager velocityManager;
    private EncodingConfiguration encoding = new EncodingConfiguration()
    {
        public String getEncoding()
        {
            return "UTF-8";
        }
    };

    @Before
    public void setUp() throws Exception
    {
        final MockProviderAccessor mpa = new MockProviderAccessor();
        user = new User("test", mpa, new MockCrowdService());
        userCtx = createAuthenticationContext(user, new MockI18nBean());
        anonymousCtx = createAuthenticationContext(null, new MockI18nBean());
        velocityManager = (VelocityManager) DuckTypeProxy.getProxy(VelocityManager.class, new Object()
        {
            public String getEncodedBody(final String templateDirectory, final String template, final String encoding, final Map contextParameters)
                    throws VelocityException
            {
                return TestGroupShareTypeRenderer.VELOCITY_RETURN;
            }
        });
        renderer = new GroupShareTypeRenderer(encoding, velocityManager);
    }

    @After
    public void tearDown() throws Exception
    {
        renderer = null;
        userCtx = null;
        anonymousCtx = null;
        user = null;
        velocityManager = null;
        encoding = null;
    }

    @Test
    public void testRenderPermission()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, userCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isBlank(html));
        assertTrue(html.indexOf(TestGroupShareTypeRenderer.GROUP1) >= 0);
    }

    @Test
    public void testRenderPermissionXSS()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_XSS, userCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isBlank(html));
        assertFalse(html.indexOf(TestGroupShareTypeRenderer.GROUP_PREM_XSS.getParam1()) >= 0);
        assertTrue(html.indexOf(TextUtils.htmlEncode(TestGroupShareTypeRenderer.GROUP_PREM_XSS.getParam1())) >= 0);
    }

    @Test
    public void testRenderPermissionWithNullUser()
    {
        final String html = renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, anonymousCtx);
        assertNotNull(html);
        assertFalse(StringUtils.isBlank(html));
        assertTrue(html.indexOf(TestGroupShareTypeRenderer.GROUP1) >= 0);
    }

    @Test
    public void testRenderPermissionNullPermission()
    {
        try
        {
            renderer.renderPermission(null, userCtx);
            fail("Renderer should not accept null permission.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionNullCtx()
    {
        try
        {
            renderer.renderPermission(TestGroupShareTypeRenderer.GROUP_PREM_1, null);
            fail("Renderer should not accept null permission.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testRenderPermissionInvalidPermission()
    {
        try
        {
            renderer.renderPermission(TestGroupShareTypeRenderer.GLOBAL_PREM, anonymousCtx);
            fail("Renderer should not accept invalid permission.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected.
        }
    }

    @Test
    public void testGetShareTypeLabel()
    {
        final String result = renderer.getShareTypeLabel(userCtx);
        assertNotNull(result);
        assertFalse(StringUtils.isBlank(result));
    }

    @Test
    public void testGetShareTypeLabelWithAnonymousUser()
    {
        final String result = renderer.getShareTypeLabel(anonymousCtx);
        assertNotNull(result);
        assertFalse(StringUtils.isBlank(result));
    }

    @Test
    public void testGetShareTypeLabelWithNullCtx()
    {
        try
        {
            renderer.getShareTypeLabel(null);
            fail("Renderer should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected/
        }
    }

    @Test
    public void testIsAddButtonNeededTrue()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertSame(TestGroupShareTypeRenderer.this.user, user);
                return Collections.singletonList(TestGroupShareTypeRenderer.GROUP1);
            }
        };

        assertTrue(renderer.isAddButtonNeeded(userCtx));
    }

    @Test
    public void testIsAddButtonNeededFalse()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertSame(TestGroupShareTypeRenderer.this.user, user);
                return Collections.EMPTY_LIST;
            }
        };

        assertFalse(renderer.isAddButtonNeeded(userCtx));
    }

    @Test
    public void testIsAddButtonNeededWithAnonymousUser()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertNull(user);
                return Collections.EMPTY_LIST;
            }
        };

        assertFalse(renderer.isAddButtonNeeded(anonymousCtx));
    }

    @Test
    public void testIsAddButtonNeededWithNullCtx()
    {
        try
        {
            renderer.isAddButtonNeeded(null);
            fail("Renderer should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected/
        }
    }

    @Test
    public void testGetShareTypeEditorWithNullCtx()
    {
        try
        {
            renderer.getShareTypeEditor(null);
            fail("Renderer should not accept null context.");
        }
        catch (final IllegalArgumentException e)
        {
            // expected/
        }
    }

    @Test
    public void testGetShareTypeEditor()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertSame(TestGroupShareTypeRenderer.this.user, user);
                return EasyList.build(TestGroupShareTypeRenderer.GROUP1, TestGroupShareTypeRenderer.GROUP2);
            }

            Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertNotNull(groups);
                assertEquals(EasyList.build(TestGroupShareTypeRenderer.GROUP2, TestGroupShareTypeRenderer.GROUP1), groups);

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test
    public void testGetShareTypeEditorNoGroups()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertSame(TestGroupShareTypeRenderer.this.user, user);
                return Collections.EMPTY_LIST;
            }

            Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertTrue(groups.isEmpty());

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(userCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test
    public void testGetShareTypeEditorAnonymousUser()
    {
        renderer = new GroupShareTypeRenderer(encoding, velocityManager)
        {
            List /* <Group> */getGroupsForUser(final User user)
            {
                assertNull(user);
                return Collections.EMPTY_LIST;
            }

            Map addDefaultVelocityParameters(final Map params, final JiraAuthenticationContext authCtx)
            {
                assertTrue(params.containsKey(TestGroupShareTypeRenderer.GROUPS_KEY));
                final List groups = (List) params.get(TestGroupShareTypeRenderer.GROUPS_KEY);
                assertTrue(groups.isEmpty());

                return params;
            }
        };

        final String html = renderer.getShareTypeEditor(anonymousCtx);
        assertEquals(TestGroupShareTypeRenderer.VELOCITY_RETURN, html);
    }

    @Test
    public void testGetTranslatedTemplatesNullCtx()
    {
        try
        {
            renderer.getTranslatedTemplates(null, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplatesNullEntityType()
    {
        try
        {
            renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplatesNullRenderMode()
    {
        try
        {
            renderer.getTranslatedTemplates(anonymousCtx, null, RenderMode.EDIT);
            fail("This should throw an exception.");
        }
        catch (IllegalArgumentException expected)
        {

        }
    }

    @Test
    public void testGetTranslatedTemplates()
    {
        Map actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, SearchRequest.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_group_display", "share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.EDIT);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_group_display", "share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, SearchRequest.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_group_description"), actualTemplates);

        actualTemplates = renderer.getTranslatedTemplates(anonymousCtx, PortalPage.ENTITY_TYPE, RenderMode.SEARCH);
        assertNotNull(actualTemplates);
        assertTemplates(EasyList.build("share_group_description"), actualTemplates);
    }

    private void assertTemplates(final List expectedKeys, final Map actualTemplates)
    {
        assertEquals(expectedKeys.size(), actualTemplates.size());
        assertTrue(actualTemplates.keySet().containsAll(expectedKeys));
        for (Iterator iterator = actualTemplates.entrySet().iterator(); iterator.hasNext();)
        {
            final Map.Entry currentEntry = (Map.Entry) iterator.next();
            assertTrue("Template for key '" + currentEntry.getKey() + "' is blank.", StringUtils.isNotBlank((String) currentEntry.getValue()));
        }
    }

    private JiraAuthenticationContext createAuthenticationContext(final User user, final I18nBean i18n)
    {
        return new MockAuthenticationContext(user, new MockOutlookManager(), i18n);
    }
}
