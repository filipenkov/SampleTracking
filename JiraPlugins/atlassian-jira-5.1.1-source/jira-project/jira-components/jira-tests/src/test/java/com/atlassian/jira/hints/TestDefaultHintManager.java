package com.atlassian.jira.hints;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.plugin.webfragments.MockJiraWebItemDescriptorBuilder;
import com.atlassian.jira.plugin.webfragment.JiraWebInterfaceManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.JiraWebLabel;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link DefaultHintManager}.
 *
 * @since v4.2
 */
public class TestDefaultHintManager extends ListeningTestCase
{

    private DefaultHintManager hintManager;

    private MockJiraWebInterfaceManager mockWebInterfaceManager;
    private JiraHelper jiraHelper;
    private JiraAuthenticationContext mockAuthenticationContext;

    private static class Pair
    {
        String hint;
        String tooltip;

        private Pair(final String hint, final String tooltip)
        {
            this.hint = hint;
            this.tooltip = tooltip;
        }
        private Pair(final String hint)
        {
            this(hint, hint);
        }
    }

    private class MockJiraWebInterfaceManager extends JiraWebInterfaceManager
    {
        private final List<Pair> hints;

        private MockJiraWebInterfaceManager()
        {
            super(null);
            hints = new ArrayList<Pair>();
        }
        public MockJiraWebInterfaceManager addHint(String hint, String tooltip)
        {
            this.hints.add(new Pair(hint, tooltip));
            return this;
        }
        public MockJiraWebInterfaceManager addHint(String hint)
        {
            this.hints.add(new Pair(hint));
            return this;
        }
        public MockJiraWebInterfaceManager addHints(String... hints)
        {
            this.hints.addAll(CollectionUtil.transform(Arrays.asList(hints),new Function<String,Pair>() {
                public Pair get(final String input)
                {
                    return new Pair(input);
                }
            }));
            return this;
        }
        public MockJiraWebInterfaceManager addNullHint()
        {
            this.hints.add(null);
            return this;
        }

        public List getDisplayableItems(final String section, final User remoteUser, final JiraHelper jiraHelper)
        {
            assertEquals("jira.hints/all", section);
            assertSame(TestDefaultHintManager.this.jiraHelper, jiraHelper);
            return newMockLinkList(hints);
        }
    }

    @Before
    public void setUp() throws Exception
    {
        mockWebInterfaceManager = new MockJiraWebInterfaceManager();
        mockAuthenticationContext = createNiceMock(JiraAuthenticationContext.class);
        jiraHelper = new JiraHelper();
        hintManager = new DefaultHintManager(mockWebInterfaceManager);
        replay(mockAuthenticationContext);
    }

    @Test
    public void testGetAll()
    {
        mockWebInterfaceManager.addHints("one", "two", "three");
        List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertEquals(Arrays.asList("one", "two", "three"), toStringList(result));
    }

    @Test
    public void testGetRandom()
    {
        mockWebInterfaceManager.addHints("one", "two", "three", "four", "five");
        Hint result = hintManager.getRandomHint(null, jiraHelper);
        assertTrue(Arrays.asList("one", "two", "three", "four", "five").contains(result.getText()));
    }

    @Test
    public void testGetWithNullWebItems()
    {
        mockWebInterfaceManager.addHints("one", "two", null, "four", "five");
        List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertEquals(Arrays.asList("one", "two", "", "four", "five"), toStringList(result));
    }

    @Test
    public void testGetWithNoWebItems()
    {
        List<Hint> result = hintManager.getAllHints(null, jiraHelper);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testHintsWithTooltips()
    {
        mockWebInterfaceManager.addHint("one", "tooltipone");
        Hint result = hintManager.getRandomHint(null, jiraHelper);
        assertEquals("one", result.getText());
        assertEquals("tooltipone", result.getTooltip());
    }

    private List<String> toStringList(List<Hint> hints)
    {
        return CollectionUtil.transform(hints, new Function<Hint, String>(){
            public String get(final Hint input)
            {
                return input.getText();
            }
        });
    }

    private List<WebItemModuleDescriptor> newMockLinkList(List<Pair> hints)
    {
        return CollectionUtil.transform(hints, new Function<Pair,WebItemModuleDescriptor>(){
            public WebItemModuleDescriptor get(final Pair input)
            {
                if (input == null)
                {
                    return newItemWithNullLabel();
                }
                return newItem(input.hint, input.tooltip);
            }
        });
    }

    private WebItemModuleDescriptor newItemWithNullLabel()
    {
        return new MockJiraWebItemDescriptorBuilder().itemKey("null").build();
    }

    private WebItemModuleDescriptor newItem(String label, String tooltip)
    {
        return new MockJiraWebItemDescriptorBuilder().itemKey("notnull").label(label).tooltip(tooltip).build();
    }

    private WebLabel newJiraWebLabel(final String label)
    {
        return new JiraWebLabel(newMockWebLabel(label), mockAuthenticationContext);
    }

    @SuppressWarnings ({ "unchecked" })
    private WebLabel newMockWebLabel(final String label)
    {
        WebLabel answer = createMock(WebLabel.class);
        expect(answer.getDescriptor()).andReturn(null).anyTimes();
        expect(answer.getDisplayableLabel(any(HttpServletRequest.class), isA(Map.class))).andReturn(label).anyTimes();
        replay(answer);
        return answer;
    }
}
