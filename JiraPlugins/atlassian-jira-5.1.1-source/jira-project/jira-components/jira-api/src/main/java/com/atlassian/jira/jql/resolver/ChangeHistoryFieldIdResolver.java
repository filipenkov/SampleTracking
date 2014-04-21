package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.changehistory.ChangeHistoryFieldConstants;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * As both WasClasueQueryFactor and ChangedClauseQueryuFactory nned to resolve ids this is a helper class
 * to accomplish this.
 *
 * @since v5.0
 */
public class ChangeHistoryFieldIdResolver
{
    private final ChangeHistoryFieldConstants changeHistoryFieldConstants;
    private final VersionResolver versionResolver;
    private final ConstantsManager constantsManager;

     private final LazyReference<Map<String, List<String>>> ref = new LazyReference<Map<String, List<String>>>()
    {
        @Override
        protected Map<String, List<String>> create() throws Exception
        {
            //giant hack for  unresolved
            return Maps.newHashMap();
        }
    };

    public ChangeHistoryFieldIdResolver(final ChangeHistoryFieldConstants changeHistoryFieldConstants,
            final VersionResolver versionResolver, ConstantsManager constantsManager) {
        this.changeHistoryFieldConstants = changeHistoryFieldConstants;
        this.versionResolver = versionResolver;
        this.constantsManager = constantsManager;
    }

    public Collection<String> resolveIdsForField(final String field, QueryLiteral literal, boolean emptyOperand)
    {
        // treat issue constants differently to other fields (because they can be renamed)
        // -1 is the generic empty indicator
        Collection<String> ids=getIdsForName(field, literal, emptyOperand);
        if (ids.isEmpty())
        {
            if (emptyOperand)
            {
                ids = Collections.singleton("-1");
            }
            else
            {
                ids = Collections.singleton(literal.getLongValue() != null ? literal.getLongValue().toString() : literal.getStringValue());
            }

        }
        return ids;
    }

    private Collection<String> getIdsForName(String field, QueryLiteral literal, boolean emptyOperand)
    {
        Collection<String> ids = Collections.emptySet();
        String value = literal.getLongValue() != null ? literal.getLongValue().toString() : literal.getStringValue();
        if (constantsManager.getConstantObjects(field) != null && !emptyOperand)
        {
            IssueConstant issueConstant = constantsManager.getConstantByNameIgnoreCase(field, value);
            if (issueConstant == null)
            {
                ids = changeHistoryFieldConstants.getIdsForField(field, literal);
            }
            else
            {
                ids = Collections.singletonList(issueConstant.getId());
            }
        }
        else
        {
            // JRADEV-8274 check if this name exists
            if (!emptyOperand)
            {
                ids = resolveIdsForVersion(field, Collections.singleton(value));
            }
            else
            {
                if (value != null)
                {
                    ids = Collections.singleton(value);
                }
            }
        }
        return ids;
     }

    private Collection<String> resolveIdsForVersion(String field, Collection<String> stringValues)
    {
        Set<String> ids =  Sets.newHashSet();
        if (field.toLowerCase().equals(SystemSearchConstants.FIX_FOR_VERSION))
        {

            for (String rawValue : stringValues)
            {
                List<String> idsPerVersion = ref.get().get(rawValue);
                if ( idsPerVersion == null)
                {
                    ref.get().put(rawValue, versionResolver.getIdsFromName(rawValue) );
                    idsPerVersion =  ref.get().get(rawValue);
                }
                ids.addAll(idsPerVersion);
            }
        }
        else
        {
            ids.addAll(stringValues);
        }
        return ids;
    }

}
