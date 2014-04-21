package com.atlassian.crowd.cql.parser;

import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.query.entity.PropertyTypeService;

/**
 * Used to parse Cql to a {@link com.atlassian.crowd.embedded.api.SearchRestriction}.
 *
 * @since 2.2
 */
public interface CqlQueryParser
{
    /**
     * Parses a query string and returns a <code>SearchRestriction</code>.
     *
     * @param restriction restriction query
     * @param propertyTypeService service that returns the type of the attribute
     * @return the equivalent SearchRestriction
     */
    SearchRestriction parseQuery(String restriction, PropertyTypeService propertyTypeService);
}
