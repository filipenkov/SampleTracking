package com.atlassian.applinks.api.auth.types;

import com.atlassian.applinks.api.auth.ImpersonatingAuthenticationProvider;
import com.atlassian.applinks.api.auth.NonImpersonatingAuthenticationProvider;

/**
 * @since 3.7
 */
public interface CorsAuthenticationProvider extends ImpersonatingAuthenticationProvider, NonImpersonatingAuthenticationProvider
{
}
