package com.atlassian.jira.plugin.searchrequestview;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * An search request view allows you to view a search request in different ways (eg XML, Word, PDF, Excel)
 *
 * @see com.atlassian.jira.plugin.searchrequestview.SearchRequestView
 */
public class SearchRequestViewModuleDescriptorImpl extends AbstractJiraModuleDescriptor<SearchRequestView> implements SearchRequestViewModuleDescriptor
{
    private String contentType;
    private String fileExtension;
    private final SearchRequestURLHandler urlHandler;
    private boolean basicAuthenticationRequired;
    private boolean excludeFromLimitFilter;

    public SearchRequestViewModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, SearchRequestURLHandler urlHandler, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.urlHandler = urlHandler;
    }

    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        fileExtension = element.attribute("fileExtension").getStringValue();
        contentType = element.attribute("contentType").getStringValue();

        Attribute basicAuthAttribute = element.attribute("basicAuthenticationRequired");
        if(basicAuthAttribute != null)
        {
            String basicAuthenticationRequired = basicAuthAttribute.getStringValue();
            //if attribute was provided, set requiresAuthentication.  Otherwise set it to false.
            this.basicAuthenticationRequired = StringUtils.isNotEmpty(basicAuthenticationRequired) && Boolean.valueOf(basicAuthenticationRequired);
        }
        else
        {
            this.basicAuthenticationRequired = false;
        }

        Attribute excludeFromLimitFilterAttr = element.attribute("excludeFromLimitFilter");
        if (excludeFromLimitFilterAttr != null)
        {
            excludeFromLimitFilter = Boolean.valueOf(excludeFromLimitFilterAttr.getStringValue());
        }
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(SearchRequestView.class);
    }

    public SearchRequestView getSearchRequestView()
    {
        return getModule();
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getFileExtension()
    {
        return fileExtension;
    }

    public String getURLWithoutContextPath(SearchRequest searchRequest)
    {
        return urlHandler.getURLWithoutContextPath(this, searchRequest);
    }

    public boolean isBasicAuthenticationRequired()
    {
        return basicAuthenticationRequired;
    }

    public boolean isExcludeFromLimitFilter()
    {
        return excludeFromLimitFilter;
    }
}
