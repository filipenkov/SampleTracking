package com.atlassian.crowd.directory.ldap.mapper.attribute;

import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import java.util.Collections;
import java.util.Set;

/**
 * Maps the objectGUID on an entity.
 *
 * This concept only applies to Active Directory.
 */
public class ObjectGUIDMapper implements AttributeMapper
{
    /**
     * Object GUID attribute name.
     */
    public static final String ATTRIBUTE_KEY = "objectGUID";

    public String getKey()
    {
        return ATTRIBUTE_KEY;
    }

    public Set<String> getValues(DirContextAdapter ctx) throws NamingException
    {
        byte[] guidBytes = (byte[]) ctx.getAttributes().get(getKey()).get();
        return Collections.singleton(getGUIDAsString(guidBytes));
    }

    /**
     * The returned representation doesn't match AD's string representation,
     * but it doesn't matter as the GUID should be treated as an opaque
     * identifier. Basically, the method is a byte array to hex string.
     * AD chooses to order the hex string in partial reverse, eg.
     *
     * Normal Hex String:     6797e1e5ecb5154f960f865c28c015fa
     * AD Formatted String:   e5e19767-b5ec-4f15-960f-865c28c015fa
     *
     * This method returns the "normal" hex string.
     *
     * @param inArr
     * @return
     */
    private String getGUIDAsString(byte[] inArr)
    {
        StringBuffer guid = new StringBuffer();
        for (int i = 0; i < inArr.length; i++)
        {
            StringBuffer dblByte = new StringBuffer(Integer.toHexString(inArr[i] & 0xff));
            if (dblByte.length() == 1)
            {
                guid.append("0");
            }
            guid.append(dblByte);
        }
        return guid.toString();
    }
}
