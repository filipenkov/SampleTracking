package com.atlassian.jira.plugin.issueview;

import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

/**
 * An issue view allows you to view an issue in different ways  (eg XML, Word, PDF)
 *
 * @see IssueView
 */
public class IssueViewModuleDescriptor extends JiraResourcedModuleDescriptor<IssueView>
{
    private String fileExtension;
    private String contentType;
    private final IssueViewURLHandler urlHandler;

    public IssueViewModuleDescriptor(final JiraAuthenticationContext authenticationContext,
            final IssueViewURLHandler urlHandler, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.urlHandler = urlHandler;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        fileExtension = element.attribute("fileExtension").getStringValue();
        contentType = element.attribute("contentType").getStringValue();
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(IssueView.class);
    }

    public IssueView getIssueView()
    {
        return getModule();
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getURLWithoutContextPath(String issueKey)
    {
        return urlHandler.getURLWithoutContextPath(this, issueKey);
    }
}
