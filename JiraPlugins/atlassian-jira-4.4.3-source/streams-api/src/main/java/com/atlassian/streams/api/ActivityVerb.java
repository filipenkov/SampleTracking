package com.atlassian.streams.api;

import java.net.URI;

import com.atlassian.streams.api.common.Option;

/**
 * An object that represents the activity:verb extension element defined in the activitystrea.ms spec
 */
public interface ActivityVerb
{
    /**
     * The content of the activity:verb. IRI doesn't have to be dereferencable, but it must be an absolute reference.
     *
     * @return the content of the activity:verb
     */
    URI iri();
    
    String key();
    
    Option<ActivityVerb> parent();
}
