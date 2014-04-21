package com.atlassian.jira.pageobjects.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default the JIRA instance will be set up for the test if not already set up. Mark your test/method with this
 * annotation to acces not set up JIRA instance.
 *
 * @since v4.4
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ( { ElementType.TYPE, ElementType.METHOD })
public @interface NoSetup
{
}
