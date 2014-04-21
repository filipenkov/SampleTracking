package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.util.system.ExtendedSystemInfoUtilsImpl;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

/**
 * Action that manages the final results screen for a project import.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportResultsAction extends JiraWebActionSupport
{
    private static final Logger log = Logger.getLogger("com.atlassian.jira.imports.project.ProjectImportResultsAction");
    ProjectImportResults projectImportResults;

    protected void doValidation()
    {
        super.doValidation();

        if (log.isDebugEnabled())
        {
            System.gc();
            final Map stats = new ExtendedSystemInfoUtilsImpl(this).getJvmStats();
            for (final Iterator iterator = stats.keySet().iterator(); iterator.hasNext();)
            {
                final String key = (String) iterator.next();
                final String value = (String) stats.get(key);
                log.debug(key + " " + value);
            }
        }
        // Clean up the expensive session objects now that the import is over AND this stops the user from being able
        // to jump into the import to re-do it
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        projectImportBean.setBackupOverview(null);
        projectImportBean.setProjectImportData(null);
        projectImportBean.setProjectImportOptions(null);
        projectImportBean.setMappingResult(null);
        projectImportBean.getTaskProgressInformation().setTaskId(null);

        // We need to add any errors that may have been added to the error collection when performing the import
        if (projectImportBean.getTaskProgressInformation().getErrorCollection() != null)
        {
            addErrorCollection(projectImportBean.getTaskProgressInformation().getErrorCollection());
        }
    }

    /**
     * This action is when the Results have been shown, and then the user clicks on the "OK" button. It will redirect to
     * the View Project page for the new Project.
     *
     * @return redirect to the View Project page for the new Project.
     */
    public String doViewNewProject()
    {
        // Get the new project ID
        final Project importedProject = getProjectImportResults().getImportedProject();
        final Long projectID = importedProject.getId();
        // Redirect to the View Project page for the new Project.
        return getRedirect("/plugins/servlet/project-config/" + importedProject.getKey() + "/summary");
    }

    public ProjectImportResults getProjectImportResults()
    {
        if (projectImportResults == null)
        {
            projectImportResults = ProjectImportBean.getProjectImportBeanFromSession().getProjectImportResults();
        }
        return projectImportResults;
    }

    public String getAssigneeTypeString(final Long assigneeType)
    {
        return getText(ProjectAssigneeTypes.getPrettyAssigneeType(assigneeType));
    }

    public String getProjectEmail(final Project project) throws Exception
    {
        return OFBizPropertyUtils.getPropertySet(project.getGenericValue()).getString(ProjectKeys.EMAIL_SENDER);
    }

    public String getPrettyImportDuration()
    {
        // Convert from milliseconds to seconds
        final long durationInSeconds = getProjectImportResults().getImportDuration() / 1000;
        return DateUtils.getDurationPretty(durationInSeconds, getJiraServiceContext().getI18nBean().getDefaultResourceBundle());
    }

}
