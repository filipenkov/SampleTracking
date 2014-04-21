package com.atlassian.jira.issue.search.searchers.renderer;

import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.search.MockSearchContext;
import com.atlassian.jira.mock.security.MockAuthenticationContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.easymock.classextension.EasyMock;

/**
 * @since v4.0
 */
public class TestAssigneeSearchRenderer extends LegacyJiraMockTestCase
{
    public void testIsShown() throws Exception
    {
        JiraAuthenticationContext context = new MockAuthenticationContext(null);
        FieldVisibilityManager fVB = EasyMock.createMock(FieldVisibilityBean.class);
        SearchContext searchContext = new MockSearchContext();

        EasyMock.expect(fVB.isFieldHiddenInAllSchemes(SystemSearchConstants.forAssignee().getFieldId(), searchContext, null)).andReturn(true).andReturn(false);

        AssigneeSearchRenderer assigneeSearchRenderer = new AssigneeSearchRenderer("nameKey", null, null, null, null, fVB);
        EasyMock.replay(fVB);

        assertFalse(assigneeSearchRenderer.isShown(null, searchContext));
        assertTrue(assigneeSearchRenderer.isShown(null, searchContext));

        EasyMock.verify(fVB);
    }
}
