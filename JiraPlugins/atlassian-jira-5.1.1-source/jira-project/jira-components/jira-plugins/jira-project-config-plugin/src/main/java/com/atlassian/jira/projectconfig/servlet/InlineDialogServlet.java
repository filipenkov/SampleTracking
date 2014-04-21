package com.atlassian.jira.projectconfig.servlet;

import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.projectconfig.order.ComparatorFactory;
import com.atlassian.jira.projectconfig.util.VelocityContextFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.http.JiraHttpUtils;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @since v4.4
 */
public class InlineDialogServlet extends HttpServlet
{
    private static final String PARAM_FIELD_ID = "fieldId";
    private static final String PARAM_PROJECT_KEY = "projectKey";
    private static final String DIALOG_VM = "screens/screensdialog.vm";
    private static final String WARNING_VM = "screens/warningpanel.vm";

    private static final String CONTEXT_SCREENS = "screens";
    private static final String CONTEXT_ERROR = "message";

    private final FieldScreenManager fieldScreenManager;
    private final TemplateRenderer templateRenderer;
    private final JiraAuthenticationContext authenticationContext;
    private final ProjectService projectService;
    private final ComparatorFactory comparatorFactory;
    private final VelocityContextFactory velocityContextFactory;

    public InlineDialogServlet(ComparatorFactory comparatorFactory, FieldScreenManager fieldScreenManager,
            TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, ProjectService projectService,
            VelocityContextFactory velocityContextFactory)
    {
        this.comparatorFactory = comparatorFactory;
        this.fieldScreenManager = fieldScreenManager;
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.projectService = projectService;
        this.velocityContextFactory = velocityContextFactory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        if (!checkPermission(getKey(req)))
        {
            renderWarning(resp, authenticationContext.getI18nHelper().getText("admin.project.fields.screens.perm.error"));
        }
        else
        {
            String fieldId = getFieldId(req);
            if (fieldId == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No fieldId specified");
                resp.setContentLength(0);
            }
            else
            {
                renderScreens(resp, fieldId);
            }
        }
    }

    private void renderScreens(HttpServletResponse response, String fieldId) throws IOException
    {
        final Collection<FieldScreenTab> fieldScreenTabs = fieldScreenManager.getFieldScreenTabs(fieldId);
        final List<FieldScreen> beans = Lists.newArrayListWithExpectedSize(fieldScreenTabs.size());
        for (FieldScreenTab tab : fieldScreenTabs)
        {
            FieldScreen fieldScreen = tab.getFieldScreen();
            beans.add(fieldScreen);
        }

        final Comparator<String> comparator = comparatorFactory.createStringComparator();
        Collections.sort(beans, new Comparator<FieldScreen>()
        {
            @Override
            public int compare(FieldScreen o1, FieldScreen o2)
            {
                return comparator.compare(o1.getName(), o2.getName());
            }
        });

        writeTemplate(response, DIALOG_VM,
                velocityContextFactory.createVelocityContext(ImmutableMap.<String, Object>of(CONTEXT_SCREENS, beans)));
    }

    private void renderWarning(HttpServletResponse response, String error) throws IOException
    {
        writeTemplate(response, WARNING_VM,
                velocityContextFactory.createVelocityContext(ImmutableMap.<String, Object>of(CONTEXT_ERROR, error)));
    }

    private void writeTemplate(HttpServletResponse reponse, String template, Map<String, Object> context) throws IOException
    {
        JiraHttpUtils.setNoCacheHeaders(reponse);
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

    private boolean checkPermission(String key)
    {
        if (key == null)
        {
            return false;
        }

        ProjectService.GetProjectResult allProjectsForAction =
                projectService.getProjectByKeyForAction(authenticationContext.getLoggedInUser(), key,
                        ProjectAction.EDIT_PROJECT_CONFIG);

        return allProjectsForAction.isValid() && allProjectsForAction.getProject() != null;
    }

    private static String getFieldId(HttpServletRequest req)
    {
        return getParameter(req, PARAM_FIELD_ID);
    }

    private static String getKey(HttpServletRequest req)
    {
        return getParameter(req, PARAM_PROJECT_KEY);
    }

    private static String getParameter(HttpServletRequest req, final String parameterName)
    {
        return StringUtils.stripToNull(req.getParameter(parameterName));
    }
}
