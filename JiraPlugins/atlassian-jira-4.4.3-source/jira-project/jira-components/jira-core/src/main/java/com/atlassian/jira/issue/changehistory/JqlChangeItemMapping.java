package com.atlassian.jira.issue.changehistory;

import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.util.concurrent.LazyReference;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * jql clause names do not always map well to the value stored in the change item table - fore instance fixVersions is stored as FIX Version
 * Although SystemSearchConstants provides these values, it's a little overkill for what we need.
 *
 * @since v5.0
 */
public class JqlChangeItemMapping
{

    private final LazyReference<Map<String, String>> mappings =  new LazyReference<Map<String, String>>()
    {

        @Override
        protected Map<String, String> create() throws Exception
        {
            return ImmutableMap.of(SystemSearchConstants.FIX_FOR_VERSION_CHANGEITEM,
                SystemSearchConstants.FIX_FOR_VERSION,
                SystemSearchConstants.FIX_FOR_VERSION, SystemSearchConstants.FIX_FOR_VERSION_CHANGEITEM);
        }
    };


    public String mapJqlClauseToFieldName(String jqlClauseName)
    {
        String mappedFieldName = mappings.get().get(jqlClauseName);
        return mappedFieldName != null ? mappedFieldName : jqlClauseName;
    }


}
