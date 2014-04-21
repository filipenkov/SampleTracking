package com.atlassian.applinks.api.application.generic;

import com.atlassian.applinks.api.ApplicationType;

/**
 * The generic application type supports all "out-of-the-box" authentication types that UAL ships with.
 * This application type can be used to authenticate to an application for which UAL does not have a specific application type,
 * but the application supports one or more of UAL's authentication providers.
 *
 * Consider implementing your own application type, rather than using the generic application type.
 * Application types are pluggable, see {@link com.atlassian.applinks.spi.application.NonAppLinksApplicationType}.
 *
 * @since v3.3
 */
public interface GenericApplicationType extends ApplicationType
{
}
