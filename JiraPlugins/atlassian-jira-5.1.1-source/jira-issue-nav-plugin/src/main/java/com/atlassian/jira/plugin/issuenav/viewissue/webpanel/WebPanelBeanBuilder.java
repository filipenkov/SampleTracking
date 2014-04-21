package com.atlassian.jira.plugin.issuenav.viewissue.webpanel;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.rest.v2.common.SimpleLinkBean;
import com.atlassian.jira.rest.v2.issue.LinkGroupBean;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builder to create {@link WebPanelBean}. Copies a lot of logic from {@link com.atlassian.jira.web.component.ModuleWebComponentImpl}.
 *
 * @since v5.0
 */
public class WebPanelBeanBuilder
{
    private static final String RENDER_PARAM_HEADLESS = "headless";
    private static final String RENDER_PARAM_CONTAINER_CLASS = "containerClass";
    private static final String RENDER_PARAM_PREFIX = "prefix";
    
    private final WebInterfaceManager webInterfaceManager;
    private final SimpleLinkManager simpleLinkManager;
    private final IssueWebPanelRenderUtil renderUtil;
    private final I18nHelper i18n;
    private final User user;
    private final WebPanelModuleDescriptor panel;

    public WebPanelBeanBuilder(WebInterfaceManager webInterfaceManager, SimpleLinkManager simpleLinkManager,
            IssueWebPanelRenderUtil renderUtil, I18nHelper i18n, User user, WebPanelModuleDescriptor panel)
    {
        this.webInterfaceManager = webInterfaceManager;
        this.simpleLinkManager = simpleLinkManager;
        this.renderUtil = renderUtil;
        this.i18n = i18n;
        this.user = user;
        this.panel = panel;
    }

    public WebPanelBean build()
    {
        final String panelHtml = renderUtil.renderHeadlessPanel(panel);
        Map<String, String> moduleParams = panel.getParams();

        if (StringUtils.isNotBlank(panelHtml))
        {
            final boolean headless = moduleParams.containsKey(RENDER_PARAM_HEADLESS);
            final String styleClass = moduleParams.containsKey(RENDER_PARAM_CONTAINER_CLASS) ? moduleParams.get(RENDER_PARAM_CONTAINER_CLASS) : "";
            return new WebPanelBean.Builder()
                    .completeKey(panel.getCompleteKey())
                    .prefix(getPrefix())
                    .id(panel.getKey())
                    .styleClass(styleClass)
                    .label(getLabel())
                    .renderHeader(!headless)
                    .headerLinks(getHeaderLinks())
                    .subpanelHtmls(getSubpanelHtmls())
                    .html(panelHtml)
                    .build();
        }
        return null;
    }

    private String getLabel()
    {
        try
        {
            final WebLabel webLabel = panel.getWebLabel();
            if (webLabel != null)
            {
                return webLabel.getDisplayableLabel(ExecutingHttpRequest.get(), renderUtil.getWebPanelContext());
            }
        }
        catch (Throwable t)
        {
            //ignore the exception and use fallbacks below.
        }

        if (panel.getI18nNameKey() != null)
        {
            return i18n.getText(panel.getI18nNameKey());
        }
        else
        {
            return panel.getKey();
        }
    }

    private List<String> getSubpanelHtmls()
    {
        final List<String> ret = new ArrayList<String>();
        final List<WebPanelModuleDescriptor> panels = webInterfaceManager.getDisplayableWebPanelDescriptors(panel.getCompleteKey() + "/panels", renderUtil.getWebPanelContext());
        if (!panels.isEmpty())
        {
            for (WebPanelModuleDescriptor panel : panels)
            {
                String panelHtml = panel.getModule().getHtml(renderUtil.getWebPanelContext());
                if (StringUtils.isNotBlank(panelHtml))
                {
                    ret.add(panelHtml);
                }
            }
        }

        return ret;
    }

    private LinkGroupBean getHeaderLinks()
    {
        final List<SimpleLink> headerItems = getHeaderItems(user, panel.getCompleteKey() + "/header", renderUtil.getHelper());
        final List<LinkGroupBean> dropdownGroups = getGroups(panel.getCompleteKey() + "/drop");

        return new LinkGroupBean.Builder()
                .addLinks(toBeans(headerItems))
                .addGroups(dropdownGroups)
                .build();
    }


    /*
    * Get the sections and links for the dropdown
    */
    private List<LinkGroupBean> getGroups(String key)
    {
        final List<LinkGroupBean> groups = new ArrayList<LinkGroupBean>();
        final JiraHelper helper = renderUtil.getHelper();
        final List<SimpleLink> defaultLinks = simpleLinkManager.getLinksForSection(key + "/default", user, helper);

        if (!defaultLinks.isEmpty())
        {
            groups.add(new LinkGroupBean.Builder()
                    .styleClass("module-drop-default-section")
                    .addLinks(toBeans(defaultLinks))
                    .build());
        }

        final List<SimpleLinkSection> sectionsForLocation = simpleLinkManager.getSectionsForLocation(key, user, helper);
        for (SimpleLinkSection simpleLinkSection : sectionsForLocation)
        {
            final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(key + "/" + simpleLinkSection.getId(), user, helper);
            if (!linksForSection.isEmpty())
            {
                groups.add(new LinkGroupBean.Builder()
                        .header(toBean(simpleLinkSection))
                        .addLinks(toBeans(linksForSection))
                        .build());
            }
        }

        return groups;
    }

    private SimpleLinkBean toBean(final SimpleLinkSection simpleLinkSection)
    {
        return new SimpleLinkBean(simpleLinkSection.getId(), simpleLinkSection.getStyleClass(), simpleLinkSection.getLabel(), simpleLinkSection.getTitle(), null, simpleLinkSection.getStyleClass());
    }

    private List<SimpleLinkBean> toBeans(List<SimpleLink> input)
    {
        final List<SimpleLinkBean> ret = new ArrayList<SimpleLinkBean>();
        for (SimpleLink simpleLink : input)
        {
            ret.add(new SimpleLinkBean(simpleLink));
        }
        return ret;
    }

    private List<SimpleLink> getHeaderItems(User user, String key, JiraHelper helper)
    {
        return simpleLinkManager.getLinksForSection(key, user, helper);
    }
    
    private String getPrefix()
    {
        final String prefix = panel.getParams().get(RENDER_PARAM_PREFIX);
        return (prefix == null) ? "" : prefix;
    }
}
