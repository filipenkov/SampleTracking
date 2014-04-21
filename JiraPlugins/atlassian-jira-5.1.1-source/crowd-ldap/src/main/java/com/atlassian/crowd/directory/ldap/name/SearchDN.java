package com.atlassian.crowd.directory.ldap.name;

import com.atlassian.crowd.directory.ldap.LDAPPropertiesMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.naming.CompositeName;
import javax.naming.InvalidNameException;
import javax.naming.Name;

public class SearchDN
{
    protected final Logger logger = Logger.getLogger(this.getClass());

    private final LDAPPropertiesMapper propertiesMapper;
    private final Converter converter;


    public SearchDN(LDAPPropertiesMapper propertiesMapper, Converter converter)
    {
        this.propertiesMapper = propertiesMapper;
        this.converter = converter;
    }

    /**
     * Returns a Name representing the DN to anchor the search for groups. Reads directory configuration to build the
     * dn.
     * @return
     */
    public Name getGroup()
    {
        try
        {
            return getSearchDN(LDAPPropertiesMapper.GROUP_DN_ADDITION);
        }
        catch (InvalidNameException e)
        {
            logger.error("Group Search DN could not be parsed", e);
            return new CompositeName();
        }
    }

    /**
     * Returns a Name representing the DN form which to search for roles. Reads directory configuration to build the dn.
     * @return
     */
    public Name getRole()
    {
        try
        {
            return getSearchDN(LDAPPropertiesMapper.ROLE_DN_ADDITION);
        }
        catch (InvalidNameException e)
        {
            logger.error("Role Search DN could not be parsed", e);
            return new CompositeName();
        }
    }

    /**
     * Returns a Name representing the DN to search beneath for users. Reads directory configuration to build the dn.
     * @return
     */
    public Name getUser()
    {
        try
        {
            return getSearchDN(LDAPPropertiesMapper.USER_DN_ADDITION);
        }
        catch (InvalidNameException e)
        {
            logger.error("User Search DN could not be parsed", e);
            return new CompositeName();
        }
    }

    /**
     * Returns a Name representing the DN beneath which all objects should reside.
     * @return
     * @throws javax.naming.InvalidNameException
     */
    public Name getBase() throws InvalidNameException
    {
        return converter.getName(propertiesMapper.getAttribute(LDAPPropertiesMapper.LDAP_BASEDN_KEY));
    }

    /**
     * Returns a Name representing the DN beneath which all objects should reside OR
     * the blank root DSE if none is specified.
     * @return
     */
    public Name getNamingContext()
    {
        Name baseDN;
        try
        {
            baseDN = getBase();
        }
        catch (InvalidNameException e)
        {
            baseDN = new CompositeName();
        }

        return baseDN;
    }

    /**
     * Given an additional DN (eg. "ou=Groups") and a base search DN (eg. "dc=example, dc=org") builds a full search DN
     * "ou=Groups, dc=example, dc=org". Handles if there's no additional DN specified.
     * @param propertyName
     * @return
     * @throws javax.naming.InvalidNameException
     */
    protected Name getSearchDN(String propertyName) throws InvalidNameException
    {
        String searchDN = "";

        String additionalDN = propertiesMapper.getAttribute(propertyName);
        if (StringUtils.isNotBlank(additionalDN))
        {
            searchDN = additionalDN + ",";
        }
        searchDN += propertiesMapper.getAttribute(LDAPPropertiesMapper.LDAP_BASEDN_KEY);

        return converter.getName(searchDN);
    }

}
