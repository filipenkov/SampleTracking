package com.sysbliss.jira.plugins.workflow.servlet;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWDSendRedirectFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(JWDSendRedirectFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }


    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("send redirect filter running");
        //continue the request
        chain.doFilter(request, new JWDSendRedirectResponseWrapper((HttpServletRequest) request, (HttpServletResponse) response));
    }

}
