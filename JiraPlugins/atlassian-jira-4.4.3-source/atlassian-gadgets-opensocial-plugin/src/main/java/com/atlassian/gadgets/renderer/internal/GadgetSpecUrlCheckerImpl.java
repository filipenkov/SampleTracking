package com.atlassian.gadgets.renderer.internal;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.GadgetSpecUrlChecker;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.opensocial.spi.GadgetSpecUrlRenderPermission;

import com.google.common.collect.Iterables;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Default implementation which uses {@link com.atlassian.gadgets.opensocial.spi.GadgetSpecUrlRenderPermission}
 * implementations provided by OSGi to see if gadgets may be rendered.</p>
 *
 * <p>The permission handling scheme implemented here follows these rules:</p>
 *
 * <ul>
 *   <li>Any one {@code DENY} is cause for refusing to render the gadget.</li>
 *   <li>At least one permission must vote to {@code ALLOW} the gadget to
 *       render. If all permissions {@code PASS} on the decision, rendering
 *       will be refused.</li>
 *   <li>A vote to {@code DENY} overrides any number of votes to {@code ALLOW}.</li>
 *   <li>If no permissions are defined, no gadget may render.</li>
 * </ul>
 */
public class GadgetSpecUrlCheckerImpl implements GadgetSpecUrlChecker
{
    private final Log log = LogFactory.getLog(GadgetSpecUrlCheckerImpl.class);

    private final Iterable<GadgetSpecUrlRenderPermission> permissions;

    /**
     * Constructor. Empty {@code permissions} are permitted, but they must point to
     * instances or proxies when {@code assertRenderable()} is called. Null
     * {@code permissions} are an error.
     * @param permissions an {@code Iterable} of permissions to use for accepting or
     * rejecting a request to render a gadget
     * @throws NullPointerException if {@code permissions} is null
     */
    public GadgetSpecUrlCheckerImpl(Iterable<GadgetSpecUrlRenderPermission> permissions)
    {
        checkNotNull(permissions);
        this.permissions = permissions;
    }

    public void assertRenderable(final String gadgetSpecUri)
    {
        checkNotNull(gadgetSpecUri);
        if (Iterables.isEmpty(permissions))
        {
            throw new GadgetSpecUriNotAllowedException("no permissions defined: all rendering " +
                    "rejected by default");
        }

        int passes = 0;
        int totalPermissions = 0;

        for (GadgetSpecUrlRenderPermission permission : permissions)
        {
            Vote lastVote;            
            try
            {
                lastVote = permission.voteOn(gadgetSpecUri);
            }
            catch (RuntimeException re)
            {
                if (log.isDebugEnabled())
                {
                    log.warn("Could not check gadget render permission with " + permission, re);
                }
                else if (log.isWarnEnabled())
                {
                    log.warn("Could not check gadget render permission with "
                            + permission + ": " + re.getMessage());
                }
                throw new GadgetSpecUriNotAllowedException("exception while checking permission " +
                        permission + ": " + re.getMessage());
            }
            switch (lastVote)
            {
                case DENY:
                    throw new GadgetSpecUriNotAllowedException("permission '" +
                            permission + "' vetoed render of gadget at " + gadgetSpecUri);

                case PASS:
                    passes++;
                    break;
            }
            totalPermissions++;
        }

        // must have one explicit ALLOW vote to consider permission to have
        // been granted
        if (passes == totalPermissions)
        {
            throw new GadgetSpecUriNotAllowedException("no ALLOW permission for gadget at " +
                    gadgetSpecUri);
        }
    }
}
