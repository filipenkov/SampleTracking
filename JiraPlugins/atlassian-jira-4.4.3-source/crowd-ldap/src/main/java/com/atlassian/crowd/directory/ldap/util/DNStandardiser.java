package com.atlassian.crowd.directory.ldap.util;

import org.springframework.ldap.core.DistinguishedName;

import java.util.Locale;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

public class DNStandardiser
{    
    /**
     * Converts a DN string into a "standard"
     * DN string representation consummable by
     * caches.
     *
     * This is particularly useful when matching
     * group memberDNs to entity DNs as the two
     * DN formats need to be equivalent.
     *
     * Unfortunately, DN parsing and standardisation
     * is an expensive process (if done over 100,000+
     * iterations). This is because it involves a
     * Java CC parse followed by a serialise. This is
     * 100 times slower than simply calling toLowerCase
     * on the DN!
     *
     * AD returns DN's that have mixed case attribute labels, eg. member attribute might look like: CN=user-1100,CN=Users,DC=sydney,DC=atlassian,DC=com for AD.
     *
     * If you call: new DistinguishedName("CN=user-1100,CN=Users,DC=sydney,DC=atlassian,DC=com").toString()
     * you get: "cn=user-1100,cn=Users,dc=sydney,dc=atlassian,dc=com"
     *
     * ie. SpringLDAP does some automagic foo to convert the DN attribute names into lower case, causing the DN
     * to be monged.
     *
     * Since everything goes through a dirContextAdapter (thanks for coming in: SpringLDAP), when we call context.getDn()
     * in the UserContextMapper/GroupContextMapper, the DN we get back is a SpringLDAP parsed DN (ie. forces lower-casing of
     * attribute names). However, when we get back memberDNs for a group, these attributes values are pure String representaions
     * of the DN returned from the server. This will cause the memberDN to not match the DN of any object previously seen as
     * the memberDN will not be forced toLowerCase or forced to compaction (",") or comma-space-delimmition (", ") for RDN
     * components of the memberDN.
     *
     * It is critical that the DN and memberDN are comparable based on a raw String comparison (String.equals)
     * for things like group membership determination and cache consistency.
     *
     * Therefore, if it is known that
     * the directory server always returns DNs
     * that do not have ", " but have "," separating
     * the naming components, then we do not need
     * to force "proper" (ie. complete + expensive)
     * standardisation. Thus if <code>forceProperStandard</code>
     * is <code>false</code>, then the compact (spaceless)
     * comma delimited DNs are transformed to lower case.
     * This method assumes that the DNs are case-insensitive.
     *
     * If it is not known whether the directory server
     * will return spaceless or spaced commas, we need
     * to use the full DN standardisation. Set
     * <code>forceProperStandard</code> to <code>true</code>
     * for the significantly slower, 100% effective,
     * standardisation. This method also assumes nothing
     * about the case of DNs.
     *
     * @param dn original DN.
     * @param forceProperStandard <code>true</code> if you
     * want to enforce the slow but effective standardisation process.
     * @return DN in standard form.
     */
    public static String standardise(String dn, boolean forceProperStandard)
    {
        if (forceProperStandard)
        {
            return new DistinguishedName(dn).toString();
        }
        else
        {
            return toLowerCase(dn);
        }
    }

    public static String standardise(DistinguishedName dn, boolean forceProperStandard)
    {
        if (forceProperStandard)
        {
            return dn.toString();
        }
        else
        {
            return standardise(dn.toString(), false);
        }
    }
}
