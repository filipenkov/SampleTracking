package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default the JIRA instance will be set up for the test if not already set up. The set up process will use default
 * data (JIRA instance name, sysadmin username and password) for the setup process. Use this annotation to change the
 * data used to set up JIRA.
 *
 * @since v4.4
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ( { ElementType.TYPE, ElementType.METHOD })
public @interface Setup
{

    // TODO not useful really? if so, needs another annotation: CustomSetup
//    String name();
//
//    String username();
//
//    String password();
}
