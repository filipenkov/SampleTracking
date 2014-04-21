package com.atlassian.jira.webtest.framework.core.locator.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default mappings among default locator types.
 *
 * @since v4.3
 */
public final class LocatorMappings
{
    private LocatorMappings()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static final List<LocatorMapping> ALL = createAll();

    private static List<LocatorMapping> createAll()
    {
        return join(IdMappings.ALL, ClassMappings.ALL, CssMappings.ALL, JQueryMappings.ALL, XPathMappings.ALL);
    }

    private static List<LocatorMapping> join(List<LocatorMapping>... lists)
    {
        List<LocatorMapping> result = new ArrayList<LocatorMapping>();
        for (List<LocatorMapping> list : lists)
        {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    public static List<LocatorMapping> all()
    {
        return ALL;
    }


    
}
