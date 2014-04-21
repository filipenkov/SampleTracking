package com.atlassian.streams.spi;

import com.atlassian.streams.api.common.Option;

import com.google.common.base.Function;

/**
 * <p>Used to resolve entity keys to their local entity.  This is needed by the AppLinks 3.x integration to convert
 * local entity keys to {@code EntityLink}s using the {@code EntityLinkService}.  The {@code EntityLink}s are used
 * to get the remote entity key when there are filters on the local entity key.</p>
 *
 * <p>The {@code EntityResolver} is used as follows:
 * <pre>
 *   String localKey = ... // extracted from stream filters
 *   Object entity = entityResolver.apply(localKey);
 *   Iterable<EntityLink> entityLinks = entityLinkService.getEntityLinks(entity)
 * </pre>
 */
public interface EntityResolver extends Function<String, Option<Object>> {}
