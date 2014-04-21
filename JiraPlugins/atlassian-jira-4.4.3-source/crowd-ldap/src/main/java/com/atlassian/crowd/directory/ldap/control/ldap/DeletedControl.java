package com.atlassian.crowd.directory.ldap.control.ldap;

import javax.naming.ldap.Control;

/**
 * Active Directory control that allows for
 * the searching of deleted objects aka
 * 'tombstones'.
 *
 * For more information, consult:
 * - MSDN AD Documentation: http://msdn.microsoft.com/en-us/library/ms677927(VS.85).aspx
 * - Java/JNDI Source Code: http://forums.sun.com/thread.jspa?threadID=449782&tstart=225
 */

public class DeletedControl implements Control
{
    public byte[] getEncodedValue()
    {
        return new byte[]{};
    }

    public String getID()
    {
        return "1.2.840.113556.1.4.417";
    }

    public boolean isCritical()
    {
        return true;
    }
}
