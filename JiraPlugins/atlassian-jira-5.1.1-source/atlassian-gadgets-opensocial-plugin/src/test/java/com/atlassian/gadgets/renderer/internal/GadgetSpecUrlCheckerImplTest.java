package com.atlassian.gadgets.renderer.internal;

import java.util.Collections;

import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.Vote;
import com.atlassian.gadgets.opensocial.spi.GadgetSpecUrlRenderPermission;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GadgetSpecUrlCheckerImplTest
{
    @Mock GadgetSpecUrlRenderPermission permission;
    @Mock GadgetSpecUrlRenderPermission permission1;
    @Mock GadgetSpecUrlRenderPermission permission2;

    private GadgetSpecUrlCheckerImpl gadgetChecker;
    private GadgetSpecUrlCheckerImpl multiplePermissionGadgetChecker;

    @Before
    public void setup()
    {
        gadgetChecker = new GadgetSpecUrlCheckerImpl(Collections.singleton(permission));
        multiplePermissionGadgetChecker = new GadgetSpecUrlCheckerImpl(
                ImmutableList.of(permission1, permission2)
        );
    }

    @Test(expected=NullPointerException.class)
    public void constructingWithNullPermissionsCausesException()
    {
        new GadgetSpecUrlCheckerImpl(null);
    }

    @Test(expected=NullPointerException.class)
    public void assertRenderableWithNullParameterCausesException()
    {
        gadgetChecker.assertRenderable(null);
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void vetoFromSinglePermissionCausesException()
    {
        when(permission.voteOn((String) anyObject())).thenReturn(Vote.DENY);
        gadgetChecker.assertRenderable("");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void passFromSinglePermissionCausesException()
    {
        when(permission.voteOn((String) anyObject())).thenReturn(Vote.PASS);
        gadgetChecker.assertRenderable("");
    }

    @Test
    public void allowFromSinglePermissionCausesNoException()
    {
        when(permission.voteOn((String) anyObject())).thenReturn(Vote.ALLOW);
        gadgetChecker.assertRenderable("");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void oneAllowAndOneVetoCausesException()
    {
        when(permission1.voteOn((String) anyObject())).thenReturn(Vote.ALLOW);
        when(permission2.voteOn((String) anyObject())).thenReturn(Vote.DENY);
        multiplePermissionGadgetChecker.assertRenderable("");
    }

    @Test
    public void oneAllowAndOnePassCausesNoException()
    {
        when(permission1.voteOn((String) anyObject())).thenReturn(Vote.ALLOW);
        when(permission2.voteOn((String) anyObject())).thenReturn(Vote.PASS);
        multiplePermissionGadgetChecker.assertRenderable("");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void onePassAndOneVetoCausesException()
    {
        when(permission1.voteOn((String) anyObject())).thenReturn(Vote.PASS);
        when(permission2.voteOn((String) anyObject())).thenReturn(Vote.DENY);
        multiplePermissionGadgetChecker.assertRenderable("");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    public void twoPassesCausesException()
    {
        when(permission1.voteOn((String) anyObject())).thenReturn(Vote.PASS);
        when(permission2.voteOn((String) anyObject())).thenReturn(Vote.PASS);
        multiplePermissionGadgetChecker.assertRenderable("");
    }

    @Test(expected=GadgetSpecUriNotAllowedException.class)
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void strangeExceptionFromPermissionCausesException()
    {
        doThrow(new IllegalStateException()).when(permission).voteOn((String) anyObject());
        gadgetChecker.assertRenderable("");
    }
}
