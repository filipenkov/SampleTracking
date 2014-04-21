package com.atlassian.jira.plugin;

import com.atlassian.crowd.plugin.descriptors.PasswordEncoderModuleDescriptor;
import com.atlassian.jira.plugin.component.ComponentModuleDescriptor;
import com.atlassian.jira.plugin.componentpanel.ComponentTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.contentlinkresolver.ContentLinkResolverDescriptor;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptorImpl;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.plugin.decorator.DecoratorMapperModuleDescriptor;
import com.atlassian.jira.plugin.decorator.DecoratorModuleDescriptor;
import com.atlassian.jira.plugin.issueoperation.IssueOperationModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.issueview.IssueViewModuleDescriptor;
import com.atlassian.jira.plugin.jql.function.JqlFunctionModuleDescriptorImpl;
import com.atlassian.jira.plugin.keyboardshortcut.KeyboardShortcutModuleDescriptor;
import com.atlassian.jira.plugin.language.LanguageModuleDescriptor;
import com.atlassian.jira.plugin.navigation.FooterModuleDescriptor;
import com.atlassian.jira.plugin.navigation.TopNavigationModuleDescriptor;
import com.atlassian.jira.plugin.portlet.PortletModuleDescriptor;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.plugin.projectoperation.ProjectOperationModuleDescriptor;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.renderer.JiraRendererModuleDescriptor;
import com.atlassian.jira.plugin.renderer.MacroModuleDescriptor;
import com.atlassian.jira.plugin.renderercomponent.RendererComponentFactoryDescriptor;
import com.atlassian.jira.plugin.report.ReportModuleDescriptor;
import com.atlassian.jira.plugin.roles.ProjectRoleActorModuleDescriptor;
import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import com.atlassian.jira.plugin.rpc.XmlRpcModuleDescriptor;
import com.atlassian.jira.plugin.searchrequestview.SearchRequestViewModuleDescriptor;
import com.atlassian.jira.plugin.userformat.descriptors.DefaultUserFormatModuleDescriptor;
import com.atlassian.jira.plugin.versionpanel.VersionTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.DefaultSimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebItemModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.descriptors.JiraWebSectionModuleDescriptor;
import com.atlassian.jira.plugin.webwork.WebworkModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowConditionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowFunctionModuleDescriptor;
import com.atlassian.jira.plugin.workflow.WorkflowValidatorModuleDescriptor;
import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.servlet.descriptors.ServletContextListenerModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletContextParamModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
import com.atlassian.plugin.web.descriptors.DefaultWebPanelModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebPanelRendererModuleDescriptor;
import com.atlassian.plugin.webresource.WebResourceModuleDescriptor;
import com.atlassian.plugin.webresource.transformer.WebResourceTransformerModuleDescriptor;

public class JiraModuleDescriptorFactory extends DefaultModuleDescriptorFactory
{
    public JiraModuleDescriptorFactory(final HostContainer hostContainer)
    {
        super(hostContainer);
        addModuleDescriptor("workflow-condition", WorkflowConditionModuleDescriptor.class);
        addModuleDescriptor("workflow-validator", WorkflowValidatorModuleDescriptor.class);
        addModuleDescriptor("workflow-function", WorkflowFunctionModuleDescriptor.class);
        addModuleDescriptor("customfield-type", CustomFieldTypeModuleDescriptorImpl.class);
        addModuleDescriptor("customfield-searcher", CustomFieldSearcherModuleDescriptorImpl.class);
        addModuleDescriptor("issue-tabpanel", IssueTabPanelModuleDescriptor.class);
        addModuleDescriptor("issue-operation", IssueOperationModuleDescriptor.class);
        addModuleDescriptor("project-operation", ProjectOperationModuleDescriptor.class);
        addModuleDescriptor("web-section", JiraWebSectionModuleDescriptor.class);
        addModuleDescriptor("web-item", JiraWebItemModuleDescriptor.class);
        addModuleDescriptor("simple-link-factory", DefaultSimpleLinkFactoryModuleDescriptor.class);
        addModuleDescriptor("single-issue-view", IssueViewModuleDescriptor.class);
        addModuleDescriptor("search-request-view", SearchRequestViewModuleDescriptor.class);
        addModuleDescriptor("project-tabpanel", ProjectTabPanelModuleDescriptor.class);
        addModuleDescriptor("version-tabpanel", VersionTabPanelModuleDescriptor.class);
        addModuleDescriptor("component-tabpanel", ComponentTabPanelModuleDescriptor.class);
        addModuleDescriptor("project-roleactor", ProjectRoleActorModuleDescriptor.class);
        addModuleDescriptor("report", ReportModuleDescriptor.class);
        addModuleDescriptor("web-resource", WebResourceModuleDescriptor.class);
        addModuleDescriptor("web-resource-transformer", WebResourceTransformerModuleDescriptor.class);

        addModuleDescriptor("portlet", PortletModuleDescriptor.class);
        addModuleDescriptor("rpc-soap", SoapModuleDescriptor.class);
        addModuleDescriptor("rpc-xmlrpc", XmlRpcModuleDescriptor.class);
        addModuleDescriptor("component", ComponentModuleDescriptor.class);
        addModuleDescriptor("webwork1", WebworkModuleDescriptor.class);

        addModuleDescriptor("jira-renderer", JiraRendererModuleDescriptor.class);
        addModuleDescriptor("macro", MacroModuleDescriptor.class);
        addModuleDescriptor("renderer-component-factory", RendererComponentFactoryDescriptor.class);
        addModuleDescriptor("content-link-resolver", ContentLinkResolverDescriptor.class);
        addModuleDescriptor("top-navigation", TopNavigationModuleDescriptor.class);
        addModuleDescriptor("jira-footer", FooterModuleDescriptor.class);
        addModuleDescriptor("view-profile-panel", ViewProfilePanelModuleDescriptor.class);
        addModuleDescriptor("user-format", DefaultUserFormatModuleDescriptor.class);
        addModuleDescriptor("jql-function", JqlFunctionModuleDescriptorImpl.class);
        addModuleDescriptor("keyboard-shortcut", KeyboardShortcutModuleDescriptor.class);

        // Crowd integration
        addModuleDescriptor("encoder", PasswordEncoderModuleDescriptor.class);

        // descriptors required by Plugins-2
        addModuleDescriptor("servlet-context-param", ServletContextParamModuleDescriptor.class);
        addModuleDescriptor("servlet", ServletModuleDescriptor.class);
        addModuleDescriptor("servlet-filter", ServletFilterModuleDescriptor.class);
        addModuleDescriptor("servlet-context-listener", ServletContextListenerModuleDescriptor.class);
        addModuleDescriptor("web-panel", DefaultWebPanelModuleDescriptor.class);
        addModuleDescriptor("web-panel-renderer", WebPanelRendererModuleDescriptor.class);

        // Sitemesh decorators
        addModuleDescriptor("decorator", DecoratorModuleDescriptor.class);
        addModuleDescriptor("decorator-mapper", DecoratorMapperModuleDescriptor.class);

        // language
        addModuleDescriptor("language", LanguageModuleDescriptor.class);
    }
}
