package com.atlassian.johnson.event;

import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * A check that is run every HTTP request
 */
public interface RequestEventCheck extends EventCheck
{
    void check(JohnsonEventContainer eventContainer, HttpServletRequest request);
}
