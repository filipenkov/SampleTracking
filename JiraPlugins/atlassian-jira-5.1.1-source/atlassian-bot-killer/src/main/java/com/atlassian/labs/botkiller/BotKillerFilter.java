package com.atlassian.labs.botkiller;

import com.atlassian.sal.api.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * The main filter for killing bots.  This is where the carnage happens!
 */
public class BotKillerFilter implements Filter
{
    private final BotKiller botKiller;
    private static final Logger log = LoggerFactory.getLogger(BotKillerFilter.class);

    public BotKillerFilter(final UserManager userManager)
    {
        botKiller = new BotKiller(userManager);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        if (httpServletRequest.getAttribute(BotKillerFilter.class.getName()) != null)
        {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        httpServletRequest.setAttribute(BotKillerFilter.class.getName(), Boolean.TRUE);

        // let the request go past.  we done do our killin' on thr udder side!
        filterChain.doFilter(servletRequest, servletResponse);

        botKiller.processRequest(httpServletRequest);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        log.info("The Atlassian BotKiller plugin has been started.  The hunt is afoot!");
    }

    @Override
    public void destroy()
    {
        log.info("The Atlassian BotKiller plugin has stopped hunting.");
    }
}
