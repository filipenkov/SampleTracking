package com.atlassian.jira.security.util;

import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: owenfellows
 * Date: 02-Aug-2004
 * Time: 15:28:51
 * To change this template use File | Settings | File Templates.
 */
public class GroupToIssueSecuritySchemeMapper extends AbstractGroupToSchemeMapper
{
    private IssueSecurityLevelManager issueSecurityLevelManager;

    public GroupToIssueSecuritySchemeMapper(IssueSecuritySchemeManager issueSecuritySchemeManager, IssueSecurityLevelManager issueSecurityLevelManager) throws GenericEntityException
    {
        super(issueSecuritySchemeManager);
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        setGroupMapping(init());
    }

    /**
     * Go through all the Issue Security Schemes and create a Map, where the key is the group name
     * and values are Sets of Schemes
     */
    protected Map init() throws GenericEntityException
    {
        Map mapping = new HashMap();

        // Need to do this as init get called before this is set, then we call it again after.
        if (getSchemeManager() != null && issueSecurityLevelManager != null)
        {
            // Get all Permission Schmes
            final List schemes = getSchemeManager().getSchemes();
            for (int i = 0; i < schemes.size(); i++)
            {
                GenericValue issueSecurityScheme = (GenericValue) schemes.get(i);

                List schemeIssueSecurityLevels = issueSecurityLevelManager.getSchemeIssueSecurityLevels(issueSecurityScheme.getLong("id"));
                for (int j = 0; j < schemeIssueSecurityLevels.size(); j++)
                {
                    GenericValue issueSecurityLevel = (GenericValue) schemeIssueSecurityLevels.get(j);
                    List levelPermissions = getSchemeManager().getEntities(issueSecurityScheme, "group", issueSecurityLevel.getLong("id"));
                    for (int k = 0; k < levelPermissions.size(); k++)
                    {
                        GenericValue levelPermission = (GenericValue) levelPermissions.get(k);
                        addEntry(mapping, levelPermission.getString("parameter"), issueSecurityScheme);
                    }
                }
            }
        }
        return mapping;
    }
}
