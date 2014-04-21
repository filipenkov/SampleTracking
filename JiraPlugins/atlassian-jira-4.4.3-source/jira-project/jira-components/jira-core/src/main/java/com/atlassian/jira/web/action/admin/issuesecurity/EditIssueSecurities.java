package com.atlassian.jira.web.action.admin.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.issue.security.ProjectIssueSecuritySchemeHelper;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.util.JiraEntityUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

@WebSudoRequired
public class EditIssueSecurities extends SchemeAwareIssueSecurityAction
{

    private String name;
    private String description;
    private Long levelId;

    private final ProjectIssueSecuritySchemeHelper helper;
    private List<Project> projects;

    public EditIssueSecurities(final ProjectIssueSecuritySchemeHelper helper)
    {
        this.helper = helper;
    }

    /**
     * Get a map of the permission events that can be part of a permission scheme. This map contains the permission id and the permission name
     *
     * @return Map containing the permissions
     * @see SchemePermissions
     */
    public Map getSchemeIssueSecurities()
    {
        IssueSecurityLevelManager secur = ManagerFactory.getIssueSecurityLevelManager();
        return JiraEntityUtils.createEntityMap(secur.getSchemeIssueSecurityLevels(getSchemeId()), "id", "name");
    }

    /**
     * Get all Generic Value issue security records for a particular scheme and security Id
     *
     * @param security The Id of the security
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException if fails retrieving issue securities
     * @see IssueSecuritySchemeManagerImpl
     */
    public List getSecurities(Long security) throws GenericEntityException
    {
        return ManagerFactory.getIssueSecuritySchemeManager().getEntities(getScheme(), security);
    }

    /**
     * Gets the description for the permission
     *
     * @param id Id of the permission that you want to get the description for
     * @return String containing the description
     * @see SchemePermissions
     */
    public String getIssueSecurityDescription(Long id)
    {
        IssueSecurityLevelManager secur = ManagerFactory.getIssueSecurityLevelManager();
        return secur.getIssueSecurityDescription(id);
    }

    public String doAddLevel() throws Exception
    {
        if (name == null || "".equals(name.trim()))
        {
            addError("name", getText("admin.errors.specify.name.for.security"));
        }

        if (CoreFactory.getGenericDelegator().findByAnd("SchemeIssueSecurityLevels", EasyMap.build("scheme", getSchemeId(), "name", name.trim())).size() > 0)
        {
            addError("name", getText("admin.errors.security.level.with.name.already.exists"));
        }

        if (getErrors().isEmpty())
        {
            EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", getSchemeId(), "name", name, "description", description));
        }

        return getRedirect(getRedirectURL());
    }

    public String doMakeDefaultLevel() throws Exception
    {
        GenericValue scheme = getScheme();

        if (scheme != null)
        {
            if ((new Long(-1).equals(levelId))) // -1 sets default to "none"
            {
                scheme.set("defaultlevel", null);
            }
            else
            {
                scheme.set("defaultlevel", levelId);
            }
            ManagerFactory.getIssueSecuritySchemeManager().updateScheme(scheme);
        }
        return getRedirect(getRedirectURL());
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Long getLevelId()
    {
        return levelId;
    }

    public void setLevelId(Long levelId)
    {
        this.levelId = levelId;
    }

    public String getRedirectURL()
    {
        return "EditIssueSecurities!default.jspa?schemeId=" + getSchemeId();
    }

    public List<Project> getUsedIn()
    {
        if (projects == null)
        {
            final Scheme issueSecurityScheme = getSchemeObject();
            projects = helper.getSharedProjects(issueSecurityScheme);
        }
        return projects;
    }
}
