package com.sysbliss.jira.plugins.workflow.servlet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

public class JWDSendRedirectResponseWrapper extends HttpServletResponseWrapper
{
    private static final Logger log = LoggerFactory.getLogger(JWDSendRedirectResponseWrapper.class);
    private HttpServletRequest servletRequest;

    public JWDSendRedirectResponseWrapper(HttpServletRequest inRequest, HttpServletResponse response)
    {
        super(response);
        servletRequest = inRequest;
        log.info("response wrapper created");
    }


    public void sendRedirect(String location) throws IOException
    {
        log.info("Going originally to:" + location);
        String finalurl;

        if (StringUtils.isNotBlank(servletRequest.getParameter("wfDesigner")))
        {
            log.info("adding jwd decorator param");
            if (location.contains("?"))
            {
                finalurl = location + "&decorator=inline&wfDesigner=true";
            }
            else
            {
                finalurl = location + "?decorator=inline&wfDesigner=true";
            }
        }
        else
        {
            finalurl = location;
        }
        super.sendRedirect(finalurl);
    }

}
