package com.atlassian.jira.webtests.ztests.tpm.ldap;

import com.atlassian.jira.functest.framework.FuncTestCase;

/**
 * Provides some shared functionality for the Lab Manager LDAP func tests.
 *
 * @since v4.3
 */
public abstract class AbstractTpmLdapTest extends FuncTestCase
{

    protected boolean isActiveDirectory()
    {
        String ldapType = getConfigurationOption("ldap.type");
        if (ldapType == null)
        {
            throw new IllegalStateException("Missing configuration for 'ldap.type'.");
        }
        if (ldapType.equals("ActiveDirectory"))
        {
            return true;
        }
        if (ldapType.equals("OpenLdap"))
        {
            return false;
        }
        throw new IllegalStateException("Unknown LDAP type '" + ldapType + "'");
    }


    /**
     * Will return "OpenLDAP" or "Microsoft Active Directory" depending on the LDAP server under test.
     *
     * @return "OpenLDAP" or "Microsoft Active Directory" depending on the LDAP server under test.
     */
    protected String getTypeDisplayName()
    {
        String ldapType = getConfigurationOption("ldap.type");
        if (ldapType == null)
        {
            throw new IllegalStateException("Missing configuration for 'ldap.type'.");
        }
        if (ldapType.equals("ActiveDirectory"))
        {
            return "Microsoft Active Directory";
        }
        if (ldapType.equals("OpenLdap"))
        {
            return "OpenLDAP";
        }
        throw new IllegalStateException("Unknown LDAP type '" + ldapType + "'");
    }

    protected String getUserDn()
    {
        if (isActiveDirectory())
        {
            if (getLdapServer().equals("crowd-ad1"))
            {
                // Running the test locally against crowd-ad1:
                return "cn=Administrator,cn=Users,dc=sydney,dc=atlassian,dc=com";
            }
            else
            {
                // Running a proper Lab manager test:
                return "cn=Administrator,cn=Users,dc=tpm,dc=atlassian,dc=com";
            }
        } else {
            // Open LDAP
            if (getLdapServer().equals("crowd-op23"))
            {
                // Running the test locally against crowd-op23:
                return "o=sgi,c=us";
            }
            else
            {
                // Running a proper Lab manager test:
                return "cn=Manager,o=tpm";
            }
        }
    }

    protected String getPassword()
    {
        if (isActiveDirectory())
        {
            if (getLdapServer().equals("crowd-ad1"))
            {
                // Running the test locally against crowd-ad1:
                return "atlassian";
            }
            else
            {
                // Running a proper Lab manager test:
                return "5P3rtaaah";
            }
        } else {
            // Open LDAP
            return "secret";
        }
    }

    protected String getBaseDn()
    {
        if (isActiveDirectory())
        {
            if (getLdapServer().equals("crowd-ad1"))
            {
                // Running the test locally against crowd-ad1:
                return "dc=sydney,dc=atlassian,dc=com";
            }
            else
            {
                // Running a proper Lab manager test:
                return "dc=tpm,dc=atlassian,dc=com";
            }
        } else {
            // Open LDAP
            if (getLdapServer().equals("crowd-op23"))
            {
                // Running the test locally against crowd-op23:
                return "ou=JIRA-TPM,o=sgi,c=us";
            }
            else
            {
                // Running a proper Lab manager test:
                return "o=tpm";
            }
        }
    }

    private String getConfigurationOption(final String key)
    {
        String value = System.getProperty(key);
        if (value != null)
        {
            return value;
        }
        return environmentData.getProperty(key);
    }

    protected String getLdapServer()
    {
        // this allows us to connect to a remote ldap server for running inside IDE (set [ldap.server = crowd-op23] in localtest.properties)
        String server = environmentData.getProperty("ldap.server");
        if (server == null)
        {
            if (isActiveDirectory())
            {
                // Use the actual hostname because of checks against the SSL certificate
                return "atlas-win32ie9.tpm.atlassian.com";
            }
            else
            {
                return "localhost";
            }
        }
        else
        {
            return server;
        }
    }

    protected void assertExtendedTestPageAndReturnToDirectoryList()
    {
        // Assert we are on the "Extended test" page
        tester.assertTextPresent("Test Remote Directory Connection");
        tester.assertTextPresent("For extended testing enter the credentials of a user in the remote directory");
        // click the link to go back to directory list
        tester.clickLinkWithText("Back to directory list");
    }


}
