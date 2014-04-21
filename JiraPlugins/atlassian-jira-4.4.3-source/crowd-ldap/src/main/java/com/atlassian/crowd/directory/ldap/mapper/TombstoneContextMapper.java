package com.atlassian.crowd.directory.ldap.mapper;

import com.atlassian.crowd.directory.ldap.mapper.attribute.ObjectGUIDMapper;
import com.atlassian.crowd.directory.ldap.mapper.attribute.USNChangedMapper;
import com.atlassian.crowd.model.Tombstone;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;

/**
 * Specific to Active Directory to map deleted objects.
 */
public class TombstoneContextMapper implements ContextMapper
{
    private final ObjectGUIDMapper objectGUIDMapper = new ObjectGUIDMapper();
    private final USNChangedMapper usnChangedMapper = new USNChangedMapper();

    /**
     * Returns an object representation of a deleted object.
     *
     * @param ctx
     * @return tombstone object with objectGUID and uSNChanged.
     */
    public Object mapFromContext(Object ctx)
    {
        try
        {            
            DirContextAdapter context = (DirContextAdapter) ctx;

            // npe's get caught and rethrown as re
            String guid = objectGUIDMapper.getValues(context).iterator().next();
            String usnChanged = usnChangedMapper.getValues(context).iterator().next();

            return new Tombstone(guid, usnChanged);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not retrieve objectGUID/uSNChanged from object: " + ((DirContextAdapter) ctx).getDn());
        }
    }
}