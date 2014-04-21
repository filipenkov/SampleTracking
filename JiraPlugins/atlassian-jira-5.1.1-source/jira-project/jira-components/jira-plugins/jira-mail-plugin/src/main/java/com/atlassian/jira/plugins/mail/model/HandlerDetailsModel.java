/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.model;

import com.atlassian.jira.plugins.mail.handlers.AbstractMessageHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateIssueHandler;
import com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler;
import com.atlassian.jira.plugins.mail.handlers.RegexCommentHandler;
import com.atlassian.jira.service.util.ServiceUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class HandlerDetailsModel
{
    private String projectKey;
    private String issueTypeId;
    private boolean stripquotes;
    private String reporterusername;
    private String catchemail;
    private String bulk;
    private String forwardEmail;
    private boolean createusers;
    private boolean notifyusers;
    private boolean ccwatcher;
    private boolean ccassignee = CreateIssueHandler.DEFAULT_CC_ASSIGNEE;
    private String splitregex;

    public HandlerDetailsModel() {
    }

    public HandlerDetailsModel(@Nullable String projectKey, @Nullable String issueTypeId,
            boolean stripquotes, @Nullable String reporterusername, @Nullable String catchemail, @Nullable String bulk,
            @Nullable String forwardEmail, boolean createusers, boolean notifyusers, boolean ccwatcher,
            boolean ccassignee, @Nullable String splitregex)
    {
        this.projectKey = projectKey;
        this.issueTypeId = issueTypeId;
        this.stripquotes = stripquotes;
        this.reporterusername = reporterusername;
        this.catchemail = catchemail;
        this.bulk = bulk;
        this.forwardEmail = forwardEmail;
        this.createusers = createusers;
        this.notifyusers = notifyusers;
        this.ccwatcher = ccwatcher;
        this.ccassignee = ccassignee;
        this.splitregex = splitregex;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public String getIssueTypeId()
    {
        return issueTypeId;
    }

    public void setIssueTypeId(String issueTypeId)
    {
        this.issueTypeId = issueTypeId;
    }

    public boolean isStripquotes()
    {
        return stripquotes;
    }

    public void setStripquotes(boolean stipquotes)
    {
        this.stripquotes = stipquotes;
    }

    public String getReporterusername()
    {
        return reporterusername;
    }

    public void setReporterusername(String reporterusername)
    {
        this.reporterusername = reporterusername;
    }

    public String getCatchemail()
    {
        return catchemail;
    }

    public void setCatchemail(String catchemail)
    {
        this.catchemail = catchemail;
    }

    public String getBulk()
    {
        return bulk;
    }

    public void setBulk(String bulk)
    {
        this.bulk = bulk;
    }

    public String getForwardEmail()
    {
        return forwardEmail;
    }

    public void setForwardEmail(String forwardEmail)
    {
        this.forwardEmail = forwardEmail;
    }

    public boolean isCreateusers()
    {
        return createusers;
    }

    public void setCreateusers(boolean createusers)
    {
        this.createusers = createusers;
    }

    public boolean isNotifyusers()
    {
        return notifyusers;
    }

    public void setNotifyusers(boolean notifyusers)
    {
        this.notifyusers = notifyusers;
    }

    public boolean isCcwatcher()
    {
        return ccwatcher;
    }

    public void setCcwatcher(boolean ccwatcher)
    {
        this.ccwatcher = ccwatcher;
    }

    public boolean isCcassignee()
    {
        return ccassignee;
    }

    public void setCcassignee(boolean ccassignee)
    {
        this.ccassignee = ccassignee;
    }

    public String getSplitregex()
    {
        return splitregex;
    }

    public void setSplitregex(String splitregex)
    {
        this.splitregex = splitregex;
    }

    public Map<String, String> toServiceParams()
    {
        Map<String, String> res = Maps.newLinkedHashMap();
        if (StringUtils.isNotBlank(projectKey))
        {
            res.put(CreateOrCommentHandler.KEY_PROJECT, projectKey);
        }
        if (StringUtils.isNotBlank(issueTypeId))
        {
            res.put(CreateOrCommentHandler.KEY_ISSUETYPE, issueTypeId);
        }
        if (StringUtils.isNotBlank(reporterusername))
        {
            res.put(AbstractMessageHandler.KEY_REPORTER, reporterusername);
        }
        if (StringUtils.isNotBlank(catchemail))
        {
            res.put(AbstractMessageHandler.KEY_CATCHEMAIL, catchemail);
        }
        if (StringUtils.isNotBlank(bulk))
        {
            res.put(AbstractMessageHandler.KEY_BULK, bulk);
        }
        if (StringUtils.isNotBlank(splitregex))
        {
            res.put(RegexCommentHandler.KEY_SPLITREGEX, splitregex);
        }
        res.put(AbstractMessageHandler.KEY_CREATEUSERS, String.valueOf(createusers));
        res.put(AbstractMessageHandler.KEY_NOTIFYUSERS, String.valueOf(notifyusers));
        res.put(CreateIssueHandler.CC_WATCHER, String.valueOf(ccwatcher));
        res.put(CreateIssueHandler.CC_ASSIGNEE, String.valueOf(ccassignee));
        res.put(CreateOrCommentHandler.KEY_QUOTES, String.valueOf(stripquotes));

        return res;
    }

    public void fromServiceParams(@Nullable String property) {
        final Map<String, String> params = ServiceUtils.getParameterMap(StringUtils.defaultString(property, ""));
        final Set<String> allowedFields = Sets.newHashSet(CreateOrCommentHandler.KEY_QUOTES,
                AbstractMessageHandler.KEY_REPORTER, AbstractMessageHandler.KEY_CATCHEMAIL,
                AbstractMessageHandler.KEY_BULK, AbstractMessageHandler.KEY_CREATEUSERS,
                AbstractMessageHandler.KEY_NOTIFYUSERS, CreateIssueHandler.CC_WATCHER,
                CreateIssueHandler.CC_ASSIGNEE, RegexCommentHandler.KEY_SPLITREGEX);

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                if (StringUtils.isNotBlank(param.getValue()) && allowedFields.contains(param.getKey())
                        && StringUtils.isNotBlank(param.getKey())) {
                    try
                    {
                        BeanUtils.copyProperty(this, param.getKey(), param.getValue());
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }

            final String project = params.get(CreateIssueHandler.KEY_PROJECT);
            if (StringUtils.isNotBlank(project)) {
                setProjectKey(project.toUpperCase(Locale.getDefault()));
            }

            if (params.containsKey(CreateIssueHandler.KEY_ISSUETYPE)) {
                setIssueTypeId(params.get(CreateIssueHandler.KEY_ISSUETYPE));
            }
        }
    }
}
