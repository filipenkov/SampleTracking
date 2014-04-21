package com.atlassian.jira.issue.fields.screen.issuetype;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DefaultIssueTypeScreenSchemeStore implements IssueTypeScreenSchemeStore
{
    private final OfBizDelegator ofBizDelegator;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;

    public DefaultIssueTypeScreenSchemeStore(OfBizDelegator ofBizDelegator)
    {
        this.ofBizDelegator = ofBizDelegator;
    }

    public Collection getIssueTypeScreenSchemes()
    {
        List issueTypeScreenSchemeGVs = ofBizDelegator.findAll(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, EasyList.build("name"));
        return buildIssueTypeScreenSchemes(issueTypeScreenSchemeGVs);
    }

    private Collection buildIssueTypeScreenSchemes(List issueTypeScreenSchemeGVs)
    {
        List issueTypeScreenSchemes = new LinkedList();
        for (Iterator iterator = issueTypeScreenSchemeGVs.iterator(); iterator.hasNext();)
        {
            issueTypeScreenSchemes.add(buildIssueTypeScreenScheme((GenericValue) iterator.next()));
        }

        return issueTypeScreenSchemes;
    }

    private IssueTypeScreenScheme buildIssueTypeScreenScheme(GenericValue genericValue)
    {
        return new IssueTypeScreenSchemeImpl(getIssueTypeScreenSchemeManager(), genericValue);
    }

    public IssueTypeScreenScheme getIssueTypeScreenScheme(Long id)
    {
        GenericValue issueTypeScreenSchemeGV = ofBizDelegator.findByPrimaryKey(ISSUE_TYPE_SCREEN_SCHEME_ENTITY_NAME, EasyMap.build("id", id));
        return buildIssueTypeScreenScheme(issueTypeScreenSchemeGV);
    }

    public IssueTypeScreenSchemeManager getIssueTypeScreenSchemeManager()
    {
        return issueTypeScreenSchemeManager;
    }

    public void setIssueTypeScreenSchemeManager(IssueTypeScreenSchemeManager issueTypeScreenSchemeManager)
    {
        this.issueTypeScreenSchemeManager = issueTypeScreenSchemeManager;
    }

}
