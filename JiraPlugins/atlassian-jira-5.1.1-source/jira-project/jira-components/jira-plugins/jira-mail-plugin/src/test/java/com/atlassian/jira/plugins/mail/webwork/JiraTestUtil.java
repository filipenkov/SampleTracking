/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.ServletActionContext;

import java.io.IOException;

public class JiraTestUtil
{
    public static MockHttpServletResponse setupExpectedRedirect(final String url) throws IOException
        {
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setExpectedRedirect(url);
            ServletActionContext.setResponse(response);
            return response;
        }

}
