/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly.tag.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import electric.xml.Document;
import electric.xml.Element;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericValue;
import org.easymock.classextension.EasyMock;
import com.atlassian.jira.jelly.AbstractJellyTestCase;
import com.atlassian.jira.jelly.tag.util.JellyTagUtils;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;

import java.sql.Timestamp;

public class TestChangeIssue extends AbstractJellyTestCase
{
    private FieldVisibilityBean origFieldVisibilityBean;

    public TestChangeIssue(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        // Create test issue
        UtilsForTests.getTestEntity("Issue", EasyMap.build("key", "ABC-1", "updated", new Timestamp(1000)));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
    }

    public void testTheTag() throws Exception
    {
        final String scriptFilename = "change-issue-tag.test.the-tag.jelly";
        Document document = runScript(scriptFilename);

        final Element root = document.getRoot();
        assertEquals(0, root.getElements().size());

        GenericValue issue = EntityUtil.getOnly(CoreFactory.getGenericDelegator().findByAnd("Issue", EasyMap.build("key", "ABC-1")));
        assertNotNull(issue);
        assertEquals(JellyTagUtils.parseDate("2000-01-14 12:00:00.0"), issue.get("updated"));
    }

    protected String getRelativePath()
    {
        return "tag" + FS + "issue" + FS;
    }
}
