package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.ldap.mapper.attribute.ObjectGUIDMapper;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.LDAPDirectoryEntity;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class LDAPEntityNameMap<T extends LDAPDirectoryEntity>
{
    private Map<String, String> guidMap = new HashMap<String, String>();
    private Map<String, String> dnMap = new HashMap<String, String>();

    public void put(T ldapEntity)
    {
        guidMap.put(ldapEntity.getValue(ObjectGUIDMapper.ATTRIBUTE_KEY), ldapEntity.getName());
        dnMap.put(ldapEntity.getDn(), ldapEntity.getName());
    }

    public String getByDn(String dn)
    {
        return dnMap.get(dn);
    }

    public String getByGuid(String guid)
    {
        return guidMap.get(guid);
    }

    public void clear()
    {
        guidMap.clear();
        dnMap.clear();        
    }
}
