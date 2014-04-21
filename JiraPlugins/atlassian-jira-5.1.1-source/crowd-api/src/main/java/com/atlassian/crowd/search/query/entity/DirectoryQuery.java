package com.atlassian.crowd.search.query.entity;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.search.EntityDescriptor;

public class DirectoryQuery extends EntityQuery<Directory>
{
    public DirectoryQuery(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
    {
        super(Directory.class, EntityDescriptor.directory(), searchRestriction, startIndex, maxResults);
    }
}
