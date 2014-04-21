/*
 * Atlassian Source Code Template.
 * User: owen
 * Date: Oct 15, 2002
 * Time: 11:07:22 AM
 * CVS Revision: $Revision: 1.4 $
 * Last CVS Commit: $Date: 2003/10/20 04:53:30 $
 * Author of last CVS Commit: $Author: amazkovoi $
 */
package com.atlassian.core.user.preferences;

import com.atlassian.core.AtlassianCoreException;

/**
 * An interface to represent preferences objects within JIRA.
 */
public interface Preferences
{
    long getLong(String key);

    void setLong(String key, long i) throws AtlassianCoreException;

    String getString(String key);

    void setString(String key, String value) throws AtlassianCoreException;

    boolean getBoolean(String key);

    void setBoolean(String key, boolean b) throws AtlassianCoreException;

    void remove(String key) throws AtlassianCoreException;
}
