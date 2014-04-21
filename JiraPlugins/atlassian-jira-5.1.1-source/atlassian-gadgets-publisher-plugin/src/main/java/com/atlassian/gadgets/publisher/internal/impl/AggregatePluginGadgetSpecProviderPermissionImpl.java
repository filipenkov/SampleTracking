package com.atlassian.gadgets.publisher.internal.impl;

import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.plugins.PluginGadgetSpec;
import com.atlassian.gadgets.publisher.spi.PluginGadgetSpecProviderPermission;

import org.springframework.osgi.service.ServiceUnavailableException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of the {@link PluginGadgetSpecProviderPermission} which aggregates all the imported services
 * and consults them to determine the outcome of the vote while dealing with all the vagaries of dynamic services coming
 * and going.  
 */
public class AggregatePluginGadgetSpecProviderPermissionImpl implements PluginGadgetSpecProviderPermission
{
    private final Iterable<PluginGadgetSpecProviderPermission> permissions;
    
    public AggregatePluginGadgetSpecProviderPermissionImpl(Iterable<PluginGadgetSpecProviderPermission> permissions)
    {
        this.permissions = checkNotNull(permissions, "permissions");
    }
    
    public Vote voteOn(final PluginGadgetSpec gadgetSpec)
    {
        return foldLeft(permissions, Vote.PASS, new FoldFunction<Vote, PluginGadgetSpecProviderPermission>()
        {
            public Vote apply(Vote a, PluginGadgetSpecProviderPermission b)
            {
                if (a == Vote.DENY)
                {
                    return Vote.DENY;
                }
                
                Vote voteB;
                try
                {
                    voteB = b.voteOn(gadgetSpec);
                }
                catch (ServiceUnavailableException e)
                {
                    // the permission service disappeared on us! just return what we know
                    return a;
                }
                
                if (voteB == Vote.DENY)
                {
                    return Vote.DENY;
                }
                if (a == Vote.ALLOW || voteB == Vote.ALLOW)
                {
                    return Vote.ALLOW;
                }
                return Vote.PASS;
            }
        });
    }
    
    static <A, B> A foldLeft(Iterable<B> xs, A z, FoldFunction<A, B> f)
    {
        A p = z;
        for (B x : xs)
        {
            p = f.apply(p, x);
        }
        return p;
    }

    interface FoldFunction<A, B>
    {
        A apply(A a, B b);
    }
}
