package com.atlassian.crowd.directory.ldap.control;

import com.atlassian.crowd.directory.ldap.control.ldap.DeletedControl;
import org.springframework.ldap.control.AbstractRequestControlDirContextProcessor;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.Control;

/**
 * Wrapper for the LDAPDeletedResultsControl so
 * that it "fits in" with the SpringLDAP templating
 * model.
 */
public class DeletedResultsControl extends AbstractRequestControlDirContextProcessor
{
    private static final DeletedControl DELETED_RESULTS_CONTROL = new DeletedControl();

    public Control createRequestControl()
    {
        return DELETED_RESULTS_CONTROL;
    }

    public void postProcess(DirContext ctx) throws NamingException
    {
        // nothing to do
    }
}
