package com.atlassian.streams.api;

import java.net.URI;

import com.atlassian.streams.api.common.Option;

/**
 * An object that represents the activity:object-type extension element defined in the activitystrea.ms spec
 */
public interface ActivityObjectType
{
    /**
     * The content of the activity:object-type. IRI doesn't have to be dereferencable, but it must be an absolute
     * reference.
     *
     * @return the content of the activity:object-type
     */
    URI iri();
    
    String key();

    /**
     * The optional parent of the activity:object-type.
     * @return the optional parent of the activity:object-type
     */
    Option<ActivityObjectType> parent();
}
