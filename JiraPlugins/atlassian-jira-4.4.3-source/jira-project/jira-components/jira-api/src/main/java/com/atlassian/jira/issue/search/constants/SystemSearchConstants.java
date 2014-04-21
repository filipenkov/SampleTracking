package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.project.version.VersionManager;

import net.jcip.annotations.ThreadSafe;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Contains the constants used by systems fields for searching. It is designed to provide a safe link between all of
 * those string constants in JIRA.
 *
 * @since v4.0
 */
@ThreadSafe
public final class SystemSearchConstants
{
    /**
     * The ID of the query searcher.
     */
    public static final String QUERY_SEARCHER_ID = "query";

    public static final String FIX_FOR_VERSION = "fixversion";
    public static final String FIX_FOR_VERSION_CHANGEITEM = "Fix Version";

    //We don't want to create an instance of this class.
    private SystemSearchConstants()
    {}

    private static final SimpleFieldSearchConstants PRIORITY = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_PRIORITY,
        IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY, IssueFieldConstants.PRIORITY,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.PRIORITY);

    public static SimpleFieldSearchConstants forPriority()
    {
        return PRIORITY;
    }

    private static final SimpleFieldSearchConstants PROJECT = new SimpleFieldSearchConstants(DocumentConstants.PROJECT_ID,
        IssueFieldConstants.PROJECT, "pid", IssueFieldConstants.PROJECT, IssueFieldConstants.PROJECT, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
        JiraDataTypes.PROJECT);

    public static SimpleFieldSearchConstants forProject()
    {
        return PROJECT;
    }

    private static final SimpleFieldSearchConstants ISSUE_TYPE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_TYPE, new ClauseNames(
        IssueFieldConstants.ISSUE_TYPE, "type"), "type", IssueFieldConstants.ISSUE_TYPE, IssueFieldConstants.ISSUE_TYPE,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.ISSUE_TYPE);

    public static SimpleFieldSearchConstants forIssueType()
    {
        return ISSUE_TYPE;
    }

    private static final SimpleFieldSearchConstantsWithEmpty COMPONENT = new SimpleFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_COMPONENT,
        "component", "component", DocumentConstants.ISSUE_COMPONENT, "-1", "-1", IssueFieldConstants.COMPONENTS,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.COMPONENT);

    public static SimpleFieldSearchConstantsWithEmpty forComponent()
    {
        return COMPONENT;
    }

    /**
     * The "SearcherId" for affected version comes from the DocumentConstants as per 3.13.
     */
    private static final SimpleFieldSearchConstantsWithEmpty AFFECTED_VERSION = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_VERSION, "affectedVersion", "version", DocumentConstants.ISSUE_VERSION, VersionManager.NO_VERSIONS,
        FieldIndexer.NO_VALUE_INDEX_VALUE, IssueFieldConstants.AFFECTED_VERSIONS, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY,
        JiraDataTypes.VERSION);

    public static SimpleFieldSearchConstantsWithEmpty forAffectedVersion()
    {
        return AFFECTED_VERSION;
    }

    /**
     * The "SearcherId" for fixFor version comes from the DocumentConstants as per 3.13.
     */
    private static final SimpleFieldSearchConstantsWithEmpty FIXFOR_VERSION = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_FIXVERSION, "fixVersion", "fixfor", DocumentConstants.ISSUE_FIXVERSION, VersionManager.NO_VERSIONS,
        FieldIndexer.NO_VALUE_INDEX_VALUE, IssueFieldConstants.FIX_FOR_VERSIONS, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY,
        JiraDataTypes.VERSION);

    public static SimpleFieldSearchConstantsWithEmpty forFixForVersion()
    {
        return FIXFOR_VERSION;
    }

    private static final SimpleFieldSearchConstants RESOLUTION = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_RESOLUTION,
        IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION, IssueFieldConstants.RESOLUTION,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.RESOLUTION);

    public static SimpleFieldSearchConstants forResolution()
    {
        return RESOLUTION;
    }

    private static final SimpleFieldSearchConstants STATUS = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_STATUS,
        IssueFieldConstants.STATUS, IssueFieldConstants.STATUS, IssueFieldConstants.STATUS, IssueFieldConstants.STATUS,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.STATUS);

    public static SimpleFieldSearchConstants forStatus()
    {
        return STATUS;
    }

    private static final SimpleFieldSearchConstants SUMMARY = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_SUMMARY,
        IssueFieldConstants.SUMMARY, "summary", QUERY_SEARCHER_ID, IssueFieldConstants.SUMMARY, OperatorClasses.TEXT_OPERATORS, JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forSummary()
    {
        return SUMMARY;
    }

    private static final SimpleFieldSearchConstants DESCRIPTION = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_DESC,
        IssueFieldConstants.DESCRIPTION, "description", QUERY_SEARCHER_ID, IssueFieldConstants.DESCRIPTION, OperatorClasses.TEXT_OPERATORS,
        JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forDescription()
    {
        return DESCRIPTION;
    }

    private static final SimpleFieldSearchConstants ENVIRONMENT = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_ENV,
        IssueFieldConstants.ENVIRONMENT, "environment", QUERY_SEARCHER_ID, IssueFieldConstants.ENVIRONMENT, OperatorClasses.TEXT_OPERATORS,
        JiraDataTypes.TEXT);

    public static SimpleFieldSearchConstants forEnvironment()
    {
        return ENVIRONMENT;
    }

    private static final SimpleFieldSearchConstantsWithEmpty LABELS = new SimpleFieldSearchConstantsWithEmpty(
        DocumentConstants.ISSUE_LABELS, "labels", "labels", DocumentConstants.ISSUE_LABELS, FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE,
        FieldIndexer.LABELS_NO_VALUE_INDEX_VALUE, IssueFieldConstants.LABELS, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY,
        JiraDataTypes.LABEL);

    public static SimpleFieldSearchConstantsWithEmpty forLabels()
    {
        return LABELS;
    }

    public static CommentsFieldSearchConstants forComments()
    {
        return CommentsFieldSearchConstants.getInstance();
    }

    private static final SimpleFieldSearchConstants CREATED_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_CREATED, new ClauseNames(
        IssueFieldConstants.CREATED, "createdDate"), IssueFieldConstants.CREATED, IssueFieldConstants.CREATED, IssueFieldConstants.CREATED,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forCreatedDate()
    {
        return CREATED_DATE;
    }

    private static final SimpleFieldSearchConstants UPDATE_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_UPDATED, new ClauseNames(
        IssueFieldConstants.UPDATED, "updatedDate"), IssueFieldConstants.UPDATED, IssueFieldConstants.UPDATED, IssueFieldConstants.UPDATED,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forUpdatedDate()
    {
        return UPDATE_DATE;
    }

    private static final SimpleFieldSearchConstants DUE_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_DUEDATE, new ClauseNames("due",
        IssueFieldConstants.DUE_DATE), IssueFieldConstants.DUE_DATE, IssueFieldConstants.DUE_DATE, IssueFieldConstants.DUE_DATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forDueDate()
    {
        return DUE_DATE;
    }

    private static final SimpleFieldSearchConstants RESOLUTION_DATE = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_RESOLUTION_DATE,
        new ClauseNames("resolved", IssueFieldConstants.RESOLUTION_DATE), IssueFieldConstants.RESOLUTION_DATE, IssueFieldConstants.RESOLUTION_DATE,
        IssueFieldConstants.RESOLUTION_DATE, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DATE);

    public static SimpleFieldSearchConstants forResolutionDate()
    {
        return RESOLUTION_DATE;
    }

    private static final UserFieldSearchConstantsWithEmpty REPORTER = new UserFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_AUTHOR,
        IssueFieldConstants.REPORTER, "reporter", "reporterSelect", IssueFieldConstants.REPORTER, DocumentConstants.ISSUE_NO_AUTHOR,
        IssueFieldConstants.REPORTER, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    public static UserFieldSearchConstantsWithEmpty forReporter()
    {
        return REPORTER;
    }

    private static final UserFieldSearchConstantsWithEmpty ASSIGNEE = new UserFieldSearchConstantsWithEmpty(DocumentConstants.ISSUE_ASSIGNEE,
        IssueFieldConstants.ASSIGNEE, "assignee", "assigneeSelect", IssueFieldConstants.ASSIGNEE, DocumentConstants.ISSUE_UNASSIGNED,
        IssueFieldConstants.ASSIGNEE, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);

    public static UserFieldSearchConstantsWithEmpty forAssignee()
    {
        return ASSIGNEE;
    }

    public static SavedFilterSearchConstants forSavedFilter()
    {
        return SavedFilterSearchConstants.getInstance();
    }

    public static AllTextSearchConstants forAllText()
    {
        return AllTextSearchConstants.getInstance();
    }

    public static IssueIdConstants forIssueId()
    {
        return IssueIdConstants.getInstance();
    }

    public static IssueKeyConstants forIssueKey()
    {
        return IssueKeyConstants.getInstance();
    }

    public static IssueParentConstants forIssueParent()
    {
        return IssueParentConstants.getInstance();
    }

    private static final SimpleFieldSearchConstants WORK_RATIO = new SimpleFieldSearchConstants(DocumentConstants.ISSUE_WORKRATIO,
        IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO, IssueFieldConstants.WORKRATIO,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.NUMBER);

    public static SimpleFieldSearchConstants forWorkRatio()
    {
        return WORK_RATIO;
    }

    private static final DefaultClauseInformation CURRENT_ESTIMATE = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_ESTIMATE_CURR,
        new ClauseNames("remainingEstimate", IssueFieldConstants.TIME_ESTIMATE), IssueFieldConstants.TIME_ESTIMATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forCurrentEstimate()
    {
        return CURRENT_ESTIMATE;
    }

    private static final DefaultClauseInformation ORIGINAL_ESTIMATE = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_ESTIMATE_ORIG,
        new ClauseNames("originalEstimate", IssueFieldConstants.TIME_ORIGINAL_ESTIMATE), IssueFieldConstants.TIME_ORIGINAL_ESTIMATE,
        OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forOriginalEstimate()
    {
        return ORIGINAL_ESTIMATE;
    }

    private static final DefaultClauseInformation TIME_SPENT = new DefaultClauseInformation(DocumentConstants.ISSUE_TIME_SPENT,
        IssueFieldConstants.TIME_SPENT, IssueFieldConstants.TIME_SPENT, OperatorClasses.EQUALITY_AND_RELATIONAL_WITH_EMPTY, JiraDataTypes.DURATION);

    public static DefaultClauseInformation forTimeSpent()
    {
        return TIME_SPENT;
    }

    private static final DefaultClauseInformation SECURITY_LEVEL = new DefaultClauseInformation(DocumentConstants.ISSUE_SECURITY_LEVEL, "level",
        IssueFieldConstants.SECURITY, OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.ISSUE_SECURITY_LEVEL);

    public static DefaultClauseInformation forSecurityLevel()
    {
        return SECURITY_LEVEL;
    }

    private static final DefaultClauseInformation VOTES = new DefaultClauseInformation(DocumentConstants.ISSUE_VOTES, "votes",
        IssueFieldConstants.VOTES, OperatorClasses.EQUALITY_AND_RELATIONAL, JiraDataTypes.NUMBER);

    public static DefaultClauseInformation forVotes()
    {
        return VOTES;
    }

    private static final DefaultClauseInformation VOTERS = new DefaultClauseInformation(DocumentConstants.ISSUE_VOTERS, "voter",
        IssueFieldConstants.VOTERS, OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.USER);

    public static DefaultClauseInformation forVoters()
    {
        return VOTERS;
    }

    private static final DefaultClauseInformation WATCHES = new DefaultClauseInformation(DocumentConstants.ISSUE_WATCHES, "watchers",
        IssueFieldConstants.WATCHES, OperatorClasses.EQUALITY_AND_RELATIONAL, JiraDataTypes.NUMBER);

    public static DefaultClauseInformation forWatches()
    {
        return WATCHES;
    }

    private static final DefaultClauseInformation WATCHERS = new DefaultClauseInformation(DocumentConstants.ISSUE_WATCHERS, "watcher",
        IssueFieldConstants.WATCHERS, OperatorClasses.EQUALITY_OPERATORS, JiraDataTypes.USER);

    public static DefaultClauseInformation forWatchers()
    {
        return WATCHERS;
    }

    private static final DefaultClauseInformation PROJECT_CATEGORY = new DefaultClauseInformation(DocumentConstants.PROJECT_ID, "category", null,
        OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.PROJECT_CATEGORY);

    public static DefaultClauseInformation forProjectCategory()
    {
        return PROJECT_CATEGORY;
    }

    private static final Set<String> SYSTEM_NAMES;

    public static Set<String> getSystemNames()
    {
        return SYSTEM_NAMES;
    }

    public static boolean isSystemName(final String name)
    {
        return SYSTEM_NAMES.contains(name);
    }

    //NOTE: This code must be after all the static variable declarations that we need to access. Basically, make this
    //the last code in the file.
    static
    {
        Set<String> names = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try
        {
            for (final Method constantMethod : getConstantMethods())
            {
                names.addAll(getNames(constantMethod));
            }
        }
        catch (final RuntimeException e)
        {
            getLogger().error("Unable to calculate system JQL names: Unexpected Error.", e);
            names = Collections.emptySet();
        }
        SYSTEM_NAMES = Collections.unmodifiableSet(names);
    }

    private static Collection<String> getNames(final Method constantMethod)
    {
        try
        {
            final ClauseInformation information = (ClauseInformation) constantMethod.invoke(null);
            if (information == null)
            {
                logConstantError(constantMethod, "Clause information was not available.", null);
                return Collections.emptySet();
            }

            final ClauseNames names = information.getJqlClauseNames();
            if (names == null)
            {
                logConstantError(constantMethod, "The ClauseName was not available.", null);
                return Collections.emptySet();
            }

            final Set<String> strings = names.getJqlFieldNames();
            if (strings == null)
            {
                logConstantError(constantMethod, "The ClauseName returned no values.", null);
                return Collections.emptySet();
            }

            return strings;
        }
        catch (final InvocationTargetException e)
        {
            Throwable exception;
            if (e.getTargetException() != null)
            {
                exception = e.getTargetException();
            }
            else
            {
                exception = e;
            }
            logConstantError(constantMethod, null, exception);
        }
        catch (final IllegalAccessException e)
        {
            logConstantError(constantMethod, null, e);
        }
        catch (final SecurityException e)
        {
            logConstantError(constantMethod, "Security Error.", e);
        }
        catch (final RuntimeException e)
        {
            logConstantError(constantMethod, "Unexpected Error.", e);
        }
        return Collections.emptySet();
    }

    private static Collection<Method> getConstantMethods()
    {
        final Method[] methods;
        try
        {
            methods = SystemSearchConstants.class.getMethods();
        }
        catch (final SecurityException e)
        {
            getLogger().error("Unable to calculate system JQL names: " + e.getMessage(), e);
            return Collections.emptySet();
        }

        final List<Method> returnMethods = new ArrayList<Method>(methods.length);
        for (final Method method : methods)
        {
            final int modifiers = method.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers))
            {
                continue;
            }

            if (method.getParameterTypes().length != 0)
            {
                continue;
            }

            final Class<?> returnType = method.getReturnType();
            if (!ClauseInformation.class.isAssignableFrom(returnType))
            {
                continue;
            }

            returnMethods.add(method);
        }

        return returnMethods;
    }

    private static void logConstantError(final Method constantMethod, final String msg, final Throwable th)
    {
        String actualMessage = msg;
        if ((msg == null) && (th != null))
        {
            actualMessage = th.getMessage();
        }

        getLogger().error("Unable to calculate system JQL names for '" + constantMethod.getName() + "': " + actualMessage, th);
    }

    private static Logger getLogger()
    {
        return Logger.getLogger(SystemSearchConstants.class);
    }
}
