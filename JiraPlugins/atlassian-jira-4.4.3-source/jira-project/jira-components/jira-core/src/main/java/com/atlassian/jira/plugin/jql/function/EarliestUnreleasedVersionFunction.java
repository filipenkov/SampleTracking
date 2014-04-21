package com.atlassian.jira.plugin.jql.function;

import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.resolver.ProjectResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Function that produces the last released version for any specified projects.
 * The versions are sequenced in the the user specified order (not the release date).
 * <p/>
 * Projects are resolved by project key first, then name, then id. Only Versions from Projects which the current user
 * can browse will be returned.
 *
 * @since v4.3
 */
public class EarliestUnreleasedVersionFunction extends AbstractSingleVersionFunction
{
    @Override
    public List<QueryLiteral> getValues(final QueryCreationContext queryCreationContext, final FunctionOperand operand, final TerminalClause terminalClause)
    {
        return super.getValues(queryCreationContext, operand, terminalClause);
    }

    public static final String FUNCTION_EARLIEST_UNRELEASED_VERSION = "earliestUnreleasedVersion";
    private final VersionManager versionManager;

    public EarliestUnreleasedVersionFunction(final VersionManager versionManager, final ProjectResolver projectResolver, final PermissionManager permissionManager)
    {
        super(projectResolver, permissionManager);
        this.versionManager = notNull("versionManager", versionManager);
    }


    protected Version getVersionForProject(final Long projectId)
    {
        Collection<Version> allVersions = versionManager.getVersionsUnreleased(projectId, true);
        if (allVersions != null && allVersions.size() > 0)
        {
            TreeSet<Version> sortedVersions = new TreeSet<Version>(new Comparator<Version>()
            {
                public int compare(final Version version, final Version version1)
                {
                    return version.getSequence().compareTo(version1.getSequence());
                }
            });
            sortedVersions.addAll(allVersions);
            return sortedVersions.first();
        }
        return null;
    }
}
