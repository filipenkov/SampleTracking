package com.atlassian.jira.web.component;

import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Component that is used to render issue constants images.  This is the equiv of the macro "displayConstantIcon".
 *
 * @since v4.4
 */
public class IssueConstantWebComponent
{
    private String baseUrl;

    public IssueConstantWebComponent(VelocityRequestContextFactory velocityRequestContextFactory)
    {
        final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
        this.baseUrl = requestContext.getBaseUrl();
    }

    public IssueConstantWebComponent(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getHtml(IssueConstant issueConstant, String imgClass)
    {
        final String iconUrl = issueConstant.getIconUrl();
        if (StringUtils.isNotBlank(iconUrl))
        {
            final String fullIconUrl = (iconUrl.startsWith("http://") || iconUrl.startsWith("https://")) ? issueConstant.getIconUrlHtml() : baseUrl + issueConstant.getIconUrlHtml();
            final String title = StringUtils.isNotBlank(issueConstant.getDescTranslation()) ? TextUtils.htmlEncode(issueConstant.getNameTranslation(), false) + " - " + TextUtils.htmlEncode(issueConstant.getDescTranslation(), false) : TextUtils.htmlEncode(issueConstant.getNameTranslation(), false);
            final String fullImgClass = StringUtils.isBlank(imgClass) ? "" : "class=\"" + imgClass + "\"";
            return "<img " + fullImgClass + " alt=\"" + TextUtils.htmlEncode(issueConstant.getNameTranslation(), false) + "\" height=\"16\" src=\"" + fullIconUrl + "\" title=\"" + title + "\" width=\"16\" />";
        }
        return "";
    }
    public String getHtml(IssueConstant issueConstant)
    {
        return getHtml(issueConstant, null);
    }
}
