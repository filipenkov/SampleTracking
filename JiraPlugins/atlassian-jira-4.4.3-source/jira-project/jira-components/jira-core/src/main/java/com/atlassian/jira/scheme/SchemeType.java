package com.atlassian.jira.scheme;

import com.atlassian.jira.bc.JiraServiceContext;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

public interface SchemeType
{
    public String getDisplayName();

    public String getType();

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext);

    /**
     * Interface for determining if a permission type has the permission.
     * <p/>
     * This method is called if there is no Remote User (ie anonymous)
     *
     * @param entity   This is the issue or the project that the security is being checked for
     * @param argument If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @return true if anonymous Users have this permission.
     */
    public boolean hasPermission(GenericValue entity, String argument);

    /**
     * Interface for determining if a permission type has the permission
     *
     * @param entity        This is the issue or the project that the security is being checked for
     * @param argument      If this particular SchemeType has been configured with a parameter, then this parameter is passed (eg. Group Name for {@link com.atlassian.jira.security.type.GroupDropdown})
     * @param user          The user for whom the permission is being checked
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the given User has this permission.
     */
    public boolean hasPermission(GenericValue entity, String argument, com.atlassian.crowd.embedded.api.User user, boolean issueCreation);

    /**
     * This method determines if this SchemeType is valid for the given permissionId.
     * <p>
     * The default behaviour is for SchemeTypes to be valid for all permission functions, but some scheme types may
     * choose to override this behaviour.
     * eg the CurrentReporterHasCreatePermission scheme is invalid to be added to the "Create Issue" function.
     * Also see JRA-13315.
     * </p>
     *
     * @param permissionId ID of the permission in question
     * @return true if this SchemeType is valid for the given permissionId.
     * @see com.atlassian.jira.security.type.CurrentReporterHasCreatePermission
     */
    public boolean isValidForPermission(int permissionId);
}
