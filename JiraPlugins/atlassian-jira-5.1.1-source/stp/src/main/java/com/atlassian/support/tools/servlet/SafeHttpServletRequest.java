package com.atlassian.support.tools.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * @author aatkins
 *
 * This is a marker interface to allow us to indicate that the servlet request has been sanitized.
 */
public interface SafeHttpServletRequest extends HttpServletRequest {

}
