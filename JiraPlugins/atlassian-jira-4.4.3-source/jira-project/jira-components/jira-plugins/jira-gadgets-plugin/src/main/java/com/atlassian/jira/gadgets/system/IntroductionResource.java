package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.ProjectManager;
import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint to retrieve the introduction html.
 *
 * @since v4.0
 */
@Path("/intro")
@AnonymousAllowed
@Produces({MediaType.TEXT_HTML})
public class IntroductionResource
{
    private final ApplicationProperties applicationProperties;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;

    public IntroductionResource(final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext, final PermissionManager permissionManager, final ProjectManager projectManager, final VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.applicationProperties = applicationProperties;
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @GET
    public Response getIntro() throws Exception
    {
        String html = applicationProperties.getText(APKeys.JIRA_INTRODUCTION);

        if (StringUtils.isBlank(html))
        {
            final String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();

            StringBuilder builder = new StringBuilder();
            final String image = String.format("<img class=\"intro-logo\" height=\"64\" width=\"64\" src=\"%s\"/>", baseUrl + "/images/intro-gadget.png");
            builder.append("<div class=\"intro\">");
            builder.append(image);
            final boolean isAdmin = permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser());
            final boolean projectsExist = projectManager.getProjectObjects().size() > 0;
            final I18nHelper i18n = authenticationContext.getI18nHelper();
            if (isAdmin)
            {
                final HelpUtil.HelpPath jira101HelpPath = HelpUtil.getInstance().getHelpPath("jira101");
                final String jira101 = String.format("<a id=\"jira101\" alt=\"%s\" title=\"%s\" href=\"%s\">", jira101HelpPath.getAlt(), jira101HelpPath.getTitle(), jira101HelpPath.getUrl());
                final String jiraTraining = String.format("<a id=\"jiraTraining\" title=\"%s\" href=\"%s\">", i18n.getText("gadget.introduction.jira.training.title"), "http://www.atlassian.com/training/");
                final String gh101 = String.format("<a id=\"gh101\" title=\"%s\" href=\"%s\">", i18n.getText("gadget.introduction.gh101.title"), "http://confluence.atlassian.com/display/GH/GreenHopper+101/?utm_source=JIRA&utm_medium=Introduction%2BGadget&utm_campaign=CAC%2BGreenHopper%20101");
                final String ghWebsite = String.format("<a id=\"gh-website\" title=\"%s\" href=\"%s\">", i18n.getText("gadget.introduction.gh.website.title"), "http://www.atlassian.com/software/greenhopper/?utm_source=JIRA&utm_medium=Introduction%2BGadget&utm_campaign=WAC%2BGreenHopper%20Homepage");

                if (projectsExist)
                {
                    final String editIntroHref = baseUrl + "/secure/admin/jira/EditApplicationProperties!default.jspa";

                    builder.append(i18n.getText("gadget.introduction.thanksforchoosing.title", "<h1>", "</h1>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.thanksforchoosing.text"));
                    builder.append("</p>");

                    builder.append(i18n.getText("gadget.introduction.wheredoistart", "<h2>", "</h2>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.jira101", jira101, "</a>"));
                    builder.append("</p>");

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.jira.training", jiraTraining, "</a>"));
                    builder.append("</p>");

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.editintro", "<a id=\"edit-introduction\" href=\"" + editIntroHref + "\">", "</a>"));
                    builder.append("</p>");

                    builder.append(i18n.getText("gadget.introduction.agile.management", "<h2>", "</h2>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.gh101", ghWebsite, "</a>", gh101, "</a>"));
                    builder.append("</p>");

                }
                else
                {
                    final String createProjectHref = baseUrl + "/secure/admin/AddProject!default.jspa";
                    builder.append(i18n.getText("gadget.introduction.thanksforchoosing.title", "<h1>", "</h1>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.thanksforchoosing.text"));
                    builder.append("</p>");

                    builder.append(i18n.getText("gadget.introduction.wheredoistart", "<h2>", "</h2>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.jira101", jira101, "</a>"));
                    builder.append("</p>");

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.jira.training", jiraTraining, "</a>"));
                    builder.append("</p>");

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.createprojects", "<a id=\"create-project\" href=\"" + createProjectHref + "\">", "</a>"));
                    builder.append("</p>");

                    builder.append(i18n.getText("gadget.introduction.agile.management", "<h2>", "</h2>"));

                    builder.append("<p>");
                    builder.append(i18n.getText("gadget.introduction.gh101", ghWebsite, "</a>", gh101, "</a>"));
                    builder.append("</p>");
                    
                }
            }
            else
            {
                final HelpUtil.HelpPath introHelpPath = HelpUtil.getInstance().getHelpPath("introduction");
                final String link = String.format("<a id=\"%s\" href=\"%s\" alt=\"%s\" title=\"%s\">", "user-docs", introHelpPath.getUrl(), introHelpPath.getAlt(), introHelpPath.getTitle());
                builder.append(i18n.getText("gadget.introduction.thanksforchoosing.title", "<h1>", "</h1>"));

                builder.append("<p>");
                builder.append(i18n.getText("gadget.introduction.thanksforchoosing.text"));
                builder.append("</p>");

                builder.append(i18n.getText("gadget.introduction.wheredoistart", "<h2>", "</h2>"));

                builder.append("<p>");
                builder.append(i18n.getText("gadget.introduction.userguide", link, "</a>"));
                builder.append("</p>");
            }

            html = builder.toString();
        }

        return Response.ok(html).cacheControl(NO_CACHE).build();
    }

}
