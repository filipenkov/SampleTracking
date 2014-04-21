package com.atlassian.applinks.api.application.generic;

import com.atlassian.applinks.api.EntityType;

/**
 * The generic entity type, can be used in combination with the generic application to create entity links
 * to this arbitrary application.
 * You might want to consider implementing your own application type.
 *
 * Consider implementing your own application type and entity types, rather than using the generic entity type.
 * Entity types are pluggable, see {@link com.atlassian.applinks.spi.application.NonAppLinksEntityType}.
 *
 * @since v3.3
 */
public interface GenericEntityType extends EntityType
{
}
