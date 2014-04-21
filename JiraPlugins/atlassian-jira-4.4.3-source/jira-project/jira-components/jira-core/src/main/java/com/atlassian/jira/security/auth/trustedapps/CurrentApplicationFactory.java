package com.atlassian.jira.security.auth.trustedapps;

import com.atlassian.security.auth.trustedapps.CurrentApplication;

/**
 * Responsible for getting the Current Application.
 *
 * @since v3.12
 */
public interface CurrentApplicationFactory
{
    CurrentApplication getCurrentApplication();
}
