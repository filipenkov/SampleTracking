/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionImpl;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class TestIssueResolveFunction extends LegacyJiraMockTestCase
{
    public TestIssueResolveFunction(String s)
    {
        super(s);
    }

    public void testIssueResolveFunction() throws GenericEntityException
    {
        IssueResolveFunction irf = new IssueResolveFunction();

        String resolutionId = "1";
        GenericValue resolution = UtilsForTests.getTestEntity("Resolution", EasyMap.build("id", resolutionId, "name", "Test Resolution"));

        GenericValue issue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1)));
        Version fixVersion = new VersionImpl(null, new MockGenericValue("Version", EasyMap.build("id", new Long(2), "name", "new fix version")));
        Collection fixVersions = new ArrayList();
        fixVersions.add(fixVersion);

        GenericValue existingFixVersion = EntityUtils.createValue("Version", EasyMap.build("id", new Long(3), "name", "old fix version"));
        CoreFactory.getAssociationManager().createAssociation(issue, existingFixVersion, IssueRelationConstants.FIX_VERSION);

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectVoid("setFixVersions", P.args(new IsEqual(fixVersions)));
        mockIssue.expectVoid("setResolution", P.args(new IsEqual(resolution)));

        Map input = EasyMap.build("issue", mockIssue.proxy(), "fixVersions", fixVersions, "resolution", resolutionId);
        irf.execute(input, null, null);
        mockIssue.verify();
    }
}
