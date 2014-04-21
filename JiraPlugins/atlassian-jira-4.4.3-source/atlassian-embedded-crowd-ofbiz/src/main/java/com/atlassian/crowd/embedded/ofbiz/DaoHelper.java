package com.atlassian.crowd.embedded.ofbiz;

import java.util.List;
import java.util.ArrayList;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 *
 * @since v4.2
 */
public class DaoHelper
{
    /**
     * Turn a list of names into a list of lower case names.
     * @param entityNames Original list of names.
     * @return List of names in lower case.
     */
    public static List<String> getLowerNames(final List<String> entityNames)
    {
        final List<String> lowerNames = new ArrayList<String>(entityNames.size());
        for (String entityName : entityNames)
        {
            lowerNames.add(toLowerCase(entityName));
        }
        return lowerNames;
    }

}
