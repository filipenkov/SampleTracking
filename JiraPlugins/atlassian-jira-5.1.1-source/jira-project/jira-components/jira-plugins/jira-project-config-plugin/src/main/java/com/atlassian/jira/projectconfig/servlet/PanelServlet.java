package com.atlassian.jira.projectconfig.servlet;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.tab.DefaultTabRenderContext;
import com.atlassian.jira.projectconfig.tab.ProjectConfigTab;
import com.atlassian.jira.projectconfig.tab.ProjectConfigTabManager;
import com.atlassian.jira.projectconfig.tab.ProjectConfigTabRenderContext;
import com.atlassian.jira.projectconfig.tab.SummaryTab;
import com.atlassian.jira.projectconfig.util.ProjectConfigRequestCache;
import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.UserProjectHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.seraph.util.RedirectUtils;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet used to render the project configuration panels. It matches URLs of the form
 * "/project-config/(projectkey)/(pannel)/(other)" and tries to render the {@link ProjectConfigTab} with id "pannel"
 * for the project with key "projectkey". The "other" is passed to the pannel when rendering as the path info.
 *
 * @since v4.4
 */
public class PanelServlet extends HttpServlet
{
    /**
     * Matches "/project-config/(projectkey)/(pannel)/(other)"
     */
    static final Pattern PATTERN = Pattern.compile("/*project-config/+([^/]+)/*(?:(?<=/)([^/]+)/*(?:(?<=/)(.+))?)?");

    private static final String TEMPLATE_TAB = "global/tab.vm";
    private static final String TEMPLATE_ERROR = "global/taberror.vm";

    private final JiraAuthenticationContext authenticationContext;
    private final ProjectConfigTabManager tabManager;
    private final ProjectService service;
    private final TemplateRenderer templateRenderer;
    private final WebResourceManager webResourceManager;
    private final ApplicationProperties properties;
    private final VelocityContextFactory velocityContextFactory;
    private final UserProjectHistoryManager userProjectHistoryManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ProjectConfigRequestCache cache;

    public PanelServlet(JiraAuthenticationContext authenticationContext, ProjectConfigTabManager tabManager,
            ProjectService service, TemplateRenderer templateRenderer, WebResourceManager webResourceManager,
            ApplicationProperties properties, VelocityContextFactory velocityContextFactory, UserProjectHistoryManager userHistoryManager,
            VelocityRequestContextFactory velocityRequestContextFactory, ProjectConfigRequestCache cache)
    {
        this.authenticationContext = authenticationContext;
        this.tabManager = tabManager;
        this.service = service;
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;
        this.properties = properties;
        this.velocityContextFactory = velocityContextFactory;
        this.userProjectHistoryManager = userHistoryManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.cache = cache;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final Matcher matcher = PATTERN.matcher(req.getPathInfo());
        if (matcher.matches())
        {
            String projectKey = matcher.group(1);

            ProjectService.GetProjectResult result = service.getProjectByKeyForAction(authenticationContext.getLoggedInUser(),
                    projectKey, ProjectAction.EDIT_PROJECT_CONFIG);

            if (!result.isValid())
            {
                if (authenticationContext.getLoggedInUser() == null)
                {
                    redirectToLogin(req, resp);
                }
                else
                {
                    I18nHelper i18nHelper = authenticationContext.getI18nHelper();
                    outputError(resp, flattenMessages(result.getErrorCollection()),
                            i18nHelper.getText("common.words.error"));
                }
            }
            else
            {
                String projectPanel = matcher.group(2);
                if (projectPanel == null)
                {
                    projectPanel = SummaryTab.NAME;
                }

                ProjectConfigTab tab = tabManager.getTabForId(projectPanel);
                if (tab == null)
                {
                    I18nHelper i18nHelper = authenticationContext.getI18nHelper();
                    outputError(resp, i18nHelper.getText("admin.project.servlet.no.tab", projectPanel),
                            i18nHelper.getText("common.words.error"));
                }
                else
                {
                    userProjectHistoryManager.addProjectToHistory(authenticationContext.getLoggedInUser(),result.getProject());
                    // lets also place it in the users session so we can "Navigate back to project config"
                    final VelocityRequestContext requestContext = velocityRequestContextFactory.getJiraVelocityRequestContext();
                    final VelocityRequestSession session = requestContext.getSession();
                    session.setAttribute(SessionKeys.CURRENT_ADMIN_PROJECT, result.getProject().getKey());
                    session.setAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB, tab.getId());
                    outputTab(req, resp, result.getProject(), tab, matcher.group(3));
                }
            }
        }
        else
        {
            I18nHelper i18nHelper = authenticationContext.getI18nHelper();
            outputError(resp, i18nHelper.getText("admin.project.servlet.no.project"),
                    i18nHelper.getText("common.words.error"));
        }
    }

    void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.sendRedirect(RedirectUtils.getLoginUrl(req));
    }

    void outputTab(HttpServletRequest request, HttpServletResponse reponse,
            Project project, ProjectConfigTab tab, String extra) throws IOException
    {
        cache.setProject(project);

        JiraHttpUtils.setNoCacheHeaders(reponse);
        requireResources();

        ProjectConfigTabRenderContext ctx = new DefaultTabRenderContext(extra, project, authenticationContext, webResourceManager);
        tab.addResourceForProject(ctx);
        final Map<String, Object> context = velocityContextFactory.createVelocityContext(MapBuilder.<String, Object>newBuilder()
                .add("project", project)
                .add("tabHtml", tab.getTab(ctx))
                .add("req", request)
                .add("linkId", tab.getLinkId())
                .add("title", tab.getTitle(ctx))
                .add("dateFormat", CustomFieldUtils.getDateFormat())
                .add("timeFormat", CustomFieldUtils.getTimeFormat())
                .toMap());

        writeTemplate(reponse, TEMPLATE_TAB, context);
    }

    private void outputError(HttpServletResponse reponse, String errorMessage, String title) throws IOException
    {
        outputError(reponse, Collections.singleton(errorMessage), title);
    }

    void outputError(HttpServletResponse reponse, Collection<String> errorMessage, String title) throws IOException
    {
        JiraHttpUtils.setNoCacheHeaders(reponse);
        requireResources();

        final Map<String, Object> context = velocityContextFactory.createVelocityContext(MapBuilder.<String, Object>newBuilder()
                .add("errorMessages", errorMessage)
                .add("title", title)
                .toMap());

        writeTemplate(reponse, TEMPLATE_ERROR, context);
    }

    private void writeTemplate(HttpServletResponse reponse, String template, Map<String, Object> context) throws IOException
    {
        //We need to so this to ensure that site-mesh actually runs on the page.
        reponse.setContentType(getContentType());
        PrintWriter writer = reponse.getWriter();
        try
        {
            templateRenderer.render(template, context, writer);
        }
        catch (IOException e)
        {
            //Don't cause an error on close so we throw the original error.
            IOUtils.closeQuietly(writer);
            throw e;
        }
        finally
        {
            //Let this error be thrown if it happens.
            writer.close();
        }
    }

    private String getContentType()
    {
        try
        {
            return properties.getContentType();
        }
        catch (Exception e)
        {
            return "text/html; charset=UTF-8";
        }
    }

    private Collection<String> flattenMessages(ErrorCollection collection)
    {
        if (!collection.hasAnyErrors())
        {
            return Collections.emptyList();
        }

        final Set<String> messages = Sets.newLinkedHashSet();
        messages.addAll(collection.getErrorMessages());
        messages.addAll(collection.getErrors().values());

        return messages;
    }

    private void requireResources()
    {
        webResourceManager.requireResourcesForContext("jira.admin.conf");
        webResourceManager.requireResource("com.atlassian.jira.jira-project-config-plugin:project-config-global");
    }
}
