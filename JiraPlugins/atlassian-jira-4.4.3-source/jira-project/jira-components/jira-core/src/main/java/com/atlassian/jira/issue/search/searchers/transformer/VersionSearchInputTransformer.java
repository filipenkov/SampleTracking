package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.issue.search.searchers.util.IndexedInputHelper;
import com.atlassian.jira.issue.search.searchers.util.VersionIndexedInputHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.IndexInfoResolver;
import com.atlassian.jira.jql.resolver.NameResolver;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A version-specific {@link IdIndexedSearchInputTransformer}.
 *
 * @since v4.0
 */
public class VersionSearchInputTransformer extends IdIndexedSearchInputTransformer<Version>
{
    private final NameResolver<Version> versionResolver;
    private final ClauseNames clauseNames;
    private final VersionManager versionManager;

    public VersionSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, IndexInfoResolver<Version> versionIndexInfoResolver, JqlOperandResolver operandResolver, FieldFlagOperandRegistry fieldFlagOperandRegistry, final SearchContextVisibilityChecker searchContextVisibilityChecker, final NameResolver<Version> versionResolver, final VersionManager versionManager)
    {
        super(clauseNames, urlParameterName, versionIndexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker);
        this.versionManager = versionManager;
        this.versionResolver = notNull("versionResolver", versionResolver);
        this.clauseNames = notNull("clauseNames", clauseNames);
    }

    ///CLOVER:OFF
    @Override
    IndexedInputHelper createIndexedInputHelper()
    {
        return new VersionIndexedInputHelper(indexInfoResolver, operandResolver, fieldFlagOperandRegistry, searchContextVisibilityChecker, versionResolver);
    }
    ///CLOVER:ON

    public Clause getSearchClause(final User searcher, final FieldValuesHolder fieldValuesHolder)
    {
        Set<String> versionIdsFromHolder = getValuesFromHolder(fieldValuesHolder);
        if (versionIdsFromHolder != null && versionIdsFromHolder.size() > 0)
        {
            // if the versions selected do not belong to the project selected, then we cannot reliably
            // use the name of the versions for the clause, since it might match some versions in another
            // project. hence, we will use the DefaultIndexInputHelper in that case to generate a clause
            // with ids for the operands
            final IndexedInputHelper inputHelper;
            //JRA-22109 It is always safewst to search by id rather than name except when multiple
            // projects have been picked.
            if (isVersionsRelatedToProjects(fieldValuesHolder))
            {
                inputHelper = getDefaultIndexedInputHelper();
            }
            else
            {
                inputHelper = getIndexedInputHelper();
            }
            return inputHelper.getClauseForNavigatorValues(clauseNames.getPrimaryName(), versionIdsFromHolder);        }
        return null;
    }

    @Override
    public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext)
    {
        final boolean structureIsCorrect = super.doRelevantClausesFitFilterForm(searcher, query, searchContext);
        if (!structureIsCorrect)
        {
            return structureIsCorrect;
        }
        // JRA-20046 - Now check that none of the versions are archived
        return !queryContainsArchivedVersions(searcher, query);
    }

    // Because this is only ever called AFTER we have done the structure check we can assume that there is one and only
    // one version in the query and that it contains a SingleValueOperand
    boolean queryContainsArchivedVersions(final User searcher, final Query query)
    {
        final SimpleNavigatorCollectorVisitor collector = new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
        if (query != null && query.getWhereClause() != null)
        {
            query.getWhereClause().accept(collector);
            if (collector.getClauses().size() == 1)
            {
                final TerminalClause terminalClause = collector.getClauses().get(0);

                final List<String> ids = new ArrayList<String>();
                final Operand operand = terminalClause.getOperand();
                // JRA-22109 Should not do this check for unreleasedversion and releasedVersion function.  so check if the operand is
                // a function and return false
                if (operand instanceof FunctionOperand) {
                    return false;
                }
                final List<QueryLiteral> queryLiteralList = operandResolver.getValues(searcher, operand, terminalClause);
                for (QueryLiteral queryLiteral : queryLiteralList)
                {
                    if (queryLiteral.getStringValue() != null)
                    {
                        ids.addAll(indexInfoResolver.getIndexedValues(queryLiteral.getStringValue()));
                    }
                    else if (queryLiteral.getLongValue() != null)
                    {
                        ids.addAll(indexInfoResolver.getIndexedValues(queryLiteral.getLongValue()));
                    }
                    // Don't need to worry about EMPTY query literals
                }

                for (String idStr : ids)
                {
                    Long lid = parseLong(idStr);
                    if (lid != null)
                    {
                        final Version version = versionManager.getVersion(lid);
                        if (version != null)
                        {
                            if (version.isArchived())
                            {
                                return true;
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    /**
       * Check that the selected versions will fit with the selected project.
       *
       * @param fieldValuesHolder the general field values holder
       * @return true if the search relates to only one project
       */
      private boolean isVersionsRelatedToProjects(final FieldValuesHolder fieldValuesHolder)
      {
          final List<String> projects = (List<String>) fieldValuesHolder.get(SystemSearchConstants.forProject().getUrlParameter());
          if (projects == null || projects.isEmpty())
          {
              return false;
          }
          else if (projects.size() == 1 && projects.contains("-1"))
          {
              return false;
          }
          else if (projects.size() > 1)
          {
              return false;
          }
          return true;
      }


    private Long parseLong(String str)
    {
        try
        {
            return Long.valueOf(str);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }

}
