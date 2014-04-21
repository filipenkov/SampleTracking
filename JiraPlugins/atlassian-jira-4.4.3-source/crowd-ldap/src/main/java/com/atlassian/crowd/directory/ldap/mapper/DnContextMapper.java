package com.atlassian.crowd.directory.ldap.mapper;

import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

public class DnContextMapper implements ContextMapper
{
    /**
     * Mapping DN LDAP / UID
     *
     * @param ctx
     * @return
     */
    public Object mapFromContext(Object ctx)
    {
        DirContextAdapter context = (DirContextAdapter) ctx;
        return context.getDn();
    }
}