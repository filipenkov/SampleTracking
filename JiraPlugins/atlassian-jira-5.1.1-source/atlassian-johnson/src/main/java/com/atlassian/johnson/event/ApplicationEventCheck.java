/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Aug 12, 2004
 * Time: 3:03:08 PM
 */
package com.atlassian.johnson.event;

import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.ServletContext;

/**
 * A check that is run every time the application is started.
 */
public interface ApplicationEventCheck extends EventCheck
{
    void check(JohnsonEventContainer eventContainer, ServletContext context);
}