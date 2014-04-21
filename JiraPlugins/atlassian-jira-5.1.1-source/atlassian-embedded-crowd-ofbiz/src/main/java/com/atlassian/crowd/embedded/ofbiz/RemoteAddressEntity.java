package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.application.RemoteAddress;
import com.google.common.collect.ImmutableSet;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.crowd.embedded.ofbiz.PrimitiveMap.builder;
import static java.util.Collections.emptySet;

class RemoteAddressEntity
{
    static final String ENTITY = "RemoteAddress";
    static final String APPLICATION_ID = "applicationId";
    static final String ADDRESS = "address";
    static final String ENCODED_ADDRESS_BYTES = "encodedAddressBytes";
    static final String MASK = "mask";

    private RemoteAddressEntity()
    {
    }

    static Map<String, Object> getData(final Long applicationId, final String remoteAddress, final String encodedAddressBytes, final int mask)
    {
        return builder().put(APPLICATION_ID, applicationId).put(ADDRESS, remoteAddress).put(ENCODED_ADDRESS_BYTES, encodedAddressBytes).put(MASK, Long.valueOf(mask)).build();
    }

    static Set<RemoteAddress> toRemoteAddresses(final List<GenericValue> remoteAddresses)
    {
        if (remoteAddresses == null)
        {
            return emptySet();
        }
        final ImmutableSet.Builder<RemoteAddress> addresses = ImmutableSet.builder();
        for (final GenericValue remoteAddressGv : remoteAddresses)
        {
            addresses.add(new RemoteAddress(remoteAddressGv.getString(ADDRESS)));
        }
        return addresses.build();
    }

}
