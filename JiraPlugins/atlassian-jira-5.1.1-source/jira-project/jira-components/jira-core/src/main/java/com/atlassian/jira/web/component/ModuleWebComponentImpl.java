package com.atlassian.jira.web.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSectionImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebPanelModuleDescriptor;
import com.atlassian.plugin.web.model.WebLabel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleWebComponentImpl implements ModuleWebComponent
{
    private static final Logger log = LoggerFactory.getLogger(ModuleWebComponentImpl.class);
    private static final String RENDER_PARAM_HEADLESS = "headless";
    private static final String RENDER_PARAM_CONTAINER_CLASS = "containerClass";
    private static final String RENDER_PARAM_PREFIX = "prefix";
    private final SimpleLinkManager simpleLinkManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WebInterfaceManager webInterfaceManager;

    public ModuleWebComponentImpl(SimpleLinkManager simpleLinkManager, JiraAuthenticationContext jiraAuthenticationContext, WebInterfaceManager webInterfaceManager)
    {
        this.simpleLinkManager = simpleLinkManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public String renderModules(User user, HttpServletRequest request, List<WebPanelModuleDescriptor> webPanelModuleDescriptors, Map<String, Object> params)
    {
        final StringBuilder sb = new StringBuilder();
        for (WebPanelModuleDescriptor webPanelModuleDescriptor : webPanelModuleDescriptors)
        {
            renderModuleAndLetNoThrowablesEscape(sb, user, request, webPanelModuleDescriptor, params);
        }
        return sb.toString();
    }

    @Override
    public String renderModule(User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params)
    {
        final StringBuilder sb = new StringBuilder();
        renderModuleAndLetNoThrowablesEscape(sb, user, request, webPanelModuleDescriptor, params);
        return sb.toString();

    }

    /**
     * Renders a web panel, catching and logging any Throwables that might try escape. These should not be propagated
     * because that would keep
     */
    private void renderModuleAndLetNoThrowablesEscape(StringBuilder module, User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params)
    {
        try
        {
            renderModule(module, user, request, webPanelModuleDescriptor, params);
        }
        catch (Throwable t)
        {
            log.error("Error rendering web panel: " + webPanelModuleDescriptor, t);
            module.append(buildRenderFailureMessage(webPanelModuleDescriptor));
        }
    }

    /*
     * Puts it all together and wraps it
     */
    private void renderModule(StringBuilder module, User user, HttpServletRequest request, WebPanelModuleDescriptor webPanelModuleDescriptor, Map<String, Object> params)
    {
        String html;
        try
        {
            html = webPanelModuleDescriptor.getModule().getHtml(params);
        }
        catch (Throwable t)
        {
            log.error("Error rendering web panel: " + webPanelModuleDescriptor, t);
            html = buildRenderFailureMessage(webPanelModuleDescriptor);
        }

        final JiraHelper helper = (JiraHelper) params.get("helper");


        // if there is not content, don't render the module
        if (StringUtils.isBlank(html))
        {
            return;
        }
        final Map<String, String> moduleParams = webPanelModuleDescriptor.getParams();
        if ((moduleParams.containsKey(RENDER_PARAM_HEADLESS) && moduleParams.get(RENDER_PARAM_HEADLESS).equals("true")) || (params.containsKey(RENDER_PARAM_HEADLESS) && params.get(RENDER_PARAM_HEADLESS).equals(true)))
        {
            module.append(html);
            return;
        }
        final String key = webPanelModuleDescriptor.getKey();

        String additionalContainerClass = moduleParams.containsKey(RENDER_PARAM_CONTAINER_CLASS) ? " " + moduleParams.get(RENDER_PARAM_CONTAINER_CLASS) : "";
        if (StringUtils.isBlank(additionalContainerClass))
        {
            additionalContainerClass = params.containsKey(RENDER_PARAM_CONTAINER_CLASS) ? " " +  params.get(RENDER_PARAM_CONTAINER_CLASS) : "";
        }

        String prefix = moduleParams.containsKey(RENDER_PARAM_PREFIX) ? "" + moduleParams.get(RENDER_PARAM_PREFIX) : "";
        if (StringUtils.isBlank(prefix))
        {
            prefix = params.containsKey(RENDER_PARAM_PREFIX) ? "" + params.get(RENDER_PARAM_PREFIX) : "";
        }

        module.append("<div class='module toggle-wrap").append(additionalContainerClass).append("' id='").append(prefix).append(key).append("'>");
            module.append("<div id='").append(key).append("_heading' class='mod-header'>");
                final List<SimpleLink> headerItems = getHeaderItems(user, webPanelModuleDescriptor.getCompleteKey() + "/header", helper);

                final List<SectionsAndLinks> dropDownSections = getSections(webPanelModuleDescriptor.getCompleteKey() + "/drop", user, helper);
                if (!(headerItems.isEmpty() && dropDownSections.isEmpty())){
                    module.append("<ul class='ops'>");
                    for (SimpleLink link : headerItems)
                    {
                        module.append("<li>");
                            renderLink(module, link, "");
                        module.append("</li>");
                    }
                    if (!dropDownSections.isEmpty())
                    {
                        module.append("<li class='drop'>");
                            module.append("<div class='aui-dd-parent'>");
                        final String optionText = jiraAuthenticationContext.getI18nHelper().getText("admin.common.words.options");
                        module.append("<a href='#' class='icon drop-menu js-default-dropdown' title='").append( optionText).append("'><span>").append(optionText).append("</span></a>");
                                module.append("<div class='aui-dropdown-content aui-list'>");

                        for (int i = 0; i < dropDownSections.size(); i++)
                        {
                            String additionalClass = "";
                            if (i == 0)
                            {
                                additionalClass = "aui-first";
                            }

                            if (i == dropDownSections.size() - 1)
                            {
                                additionalClass = additionalClass + " aui-last";
                            }
                            final SectionsAndLinks sectionAndLinks = dropDownSections.get(i);
                            renderSection(module, sectionAndLinks.getSection(), additionalClass, sectionAndLinks.getLinks());
                        }

                                module.append("</div>");
                            module.append("</div>");
                        module.append("</li>");

                    }
                    module.append("</ul>");
                }
                renderHeaderPanels(module, webPanelModuleDescriptor.getCompleteKey() + "/panels", params);

                module.append("<h3 class='toggle-title'>");
                    renderModHeading(module, request, webPanelModuleDescriptor, params);
                module.append("</h3>");
            module.append("</div>");
            module.append("<div class='mod-content'>");
                module.append(html);
            module.append("</div>");
        module.append("</div>");

    }

    private void renderHeaderPanels(StringBuilder module, String key, Map<String, Object> params)
    {
        final List<WebPanelModuleDescriptor> panels = webInterfaceManager.getDisplayableWebPanelDescriptors(key, params);
        if (!panels.isEmpty())
        {
            module.append("<div class='mod-header-panels'>");
            for (WebPanelModuleDescriptor panel : panels)
            {
                module.append("<div class='mod-header-panel'>");
                    module.append(panel.getModule().getHtml(params));
                module.append("</div>");
            }
            module.append("</div>");
        }
    }

    /*
     * Get the sections and links for the dropdown
     */
    private List<SectionsAndLinks> getSections(String key, User user, JiraHelper helper)
    {
        final List<SectionsAndLinks> sections = new ArrayList<SectionsAndLinks>();

        final List<SimpleLink> defaultLinks = simpleLinkManager.getLinksForSection(key + "/default", user, helper);

        if (!defaultLinks.isEmpty())
        {
            sections.add(new SectionsAndLinks(new SimpleLinkSectionImpl("", null, null, null, "module-drop-default-section", null),
                    defaultLinks));

        }

        final List<SimpleLinkSection> sectionsForLocation = simpleLinkManager.getSectionsForLocation(key, user, helper);
        for (SimpleLinkSection simpleLinkSection : sectionsForLocation)
        {
            final List<SimpleLink> linksForSection = simpleLinkManager.getLinksForSection(key + "/" + simpleLinkSection.getId(), user, helper);
            if (!linksForSection.isEmpty())
            {
                sections.add(new SectionsAndLinks(simpleLinkSection, linksForSection));
            }
        }

        return sections;
    }


    private List<SimpleLink> getHeaderItems(User user, String key, JiraHelper helper)
    {
        return simpleLinkManager.getLinksForSection(key, user, helper);
    }

    /*
     * Renders the sections in the drop down
     */
    private void renderSection(StringBuilder module, SimpleLinkSection section, String additionalClass, List<SimpleLink> links)
    {
        if (StringUtils.isNotBlank(section.getLabel()))
        {
            module.append("<h5>").append(section.getLabel()).append("</h5>");
        }
        module.append("<ul");
        if (StringUtils.isNotBlank(section.getId()))
        {
            module.append(" id='").append(section.getId()).append("'");
        }
        module.append(" class='aui-list-section");
        if (StringUtils.isNotBlank(additionalClass))
        {
            module.append(" ").append(additionalClass);
        }
        if (StringUtils.isNotBlank(section.getStyleClass()))
        {
            module.append(" ").append(section.getStyleClass());
        }
        module.append("'>");

        for (SimpleLink link : links)
        {
            module.append("<li class='aui-list-item'>");
                renderLink(module, link, "aui-list-item-link");
            module.append("</li>");
        }

        module.append("</ul>");

    }

    /*
     * Render the individual link (used for both the header links and drop links)
     */
    private void renderLink(StringBuilder moduleBuilder, SimpleLink link, String additionalClass)
    {
        moduleBuilder.append("<a");

        if (StringUtils.isNotBlank(link.getId()))
        {
            moduleBuilder.append(" id='").append(link.getId()).append("'");
        }

        moduleBuilder.append(" href='").append(link.getUrl()).append("'");

        if (StringUtils.isNotBlank(link.getStyleClass()) || StringUtils.isNotBlank(additionalClass))
        {
            moduleBuilder.append(" class='");
            if (StringUtils.isNotBlank(link.getStyleClass()))
            {
                moduleBuilder.append(link.getStyleClass());
            }

            moduleBuilder.append(" ").append(additionalClass);

            moduleBuilder.append("'");

        }


        if (StringUtils.isNotBlank(link.getTitle()))
        {
            moduleBuilder.append(" title='").append(link.getTitle()).append("'");
        }
        moduleBuilder.append("><span>");

        moduleBuilder.append(link.getLabel());
        moduleBuilder.append("</span></a>");
    }

    private void renderModHeading(StringBuilder moduleBuilder, HttpServletRequest request, WebPanelModuleDescriptor webPanel, Map<String, Object> params)
    {
        try
        {
            final WebLabel webLabel = webPanel.getWebLabel();
            if (webLabel != null)
            {
                moduleBuilder.append(webLabel.getDisplayableLabel(request, params));
            }
        }
        catch (Throwable t)
        {
            if (webPanel.getI18nNameKey() != null)
            {
                moduleBuilder.append(jiraAuthenticationContext.getI18nHelper().getText(webPanel.getI18nNameKey()));
            }
            else
            {
                moduleBuilder.append(webPanel.getKey());
            }
        }
    }

    private String buildRenderFailureMessage(WebPanelModuleDescriptor webPanelModuleDescriptor)
    {
        return jiraAuthenticationContext.getI18nHelper().getText("modulewebcomponent.exception", webPanelModuleDescriptor.getCompleteKey());
    }

    private class SectionsAndLinks
    {
        private final SimpleLinkSection section;
        private final List<SimpleLink> links;

        public SectionsAndLinks(SimpleLinkSection section, List<SimpleLink> links)
        {
            this.section = section;
            this.links = links;
        }

        public SimpleLinkSection getSection()
        {
            return section;
        }

        public List<SimpleLink> getLinks()
        {
            return links;
        }
    }
}
