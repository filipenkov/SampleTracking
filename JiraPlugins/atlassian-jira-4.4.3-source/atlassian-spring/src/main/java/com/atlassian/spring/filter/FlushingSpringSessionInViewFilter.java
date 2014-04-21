package com.atlassian.spring.filter;

import com.atlassian.spring.container.ContainerManager;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.apache.log4j.Category;
import org.springframework.orm.hibernate.support.OpenSessionInViewFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SessionInViewFilter that explicitly flushes the session if it's connected.
 */
public class FlushingSpringSessionInViewFilter extends OpenSessionInViewFilter
{
    public static final Category log = Category.getInstance(FlushingSpringSessionInViewFilter.class);

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        if (!ContainerManager.isContainerSetup() || !isDatabaseSetUp())
        {
            filterChain.doFilter(request, response);
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    protected boolean isDatabaseSetUp()
    {
        return true;
    }

    // We have to make sure that we throw NO exceptions from this method, or we just end up masking problems
    // in the Spring session filter (the terrible ClobStringType requires active txn sync bug..), and we also
    // interrupt the session cleanup and leak db connections. - cm
    protected void closeSession(Session session, SessionFactory sessionFactory)
    {
        if (session != null && session.isOpen() && session.isConnected())
        {
            try
            {
                session.flush();
            }
            catch (Exception e)
            {
                log.error("Unable to flush Hibernate session. Possible data loss: " + e.getMessage(), e);
            }
            finally
            {
                super.closeSession(session, sessionFactory);
            }
        }
    }
}