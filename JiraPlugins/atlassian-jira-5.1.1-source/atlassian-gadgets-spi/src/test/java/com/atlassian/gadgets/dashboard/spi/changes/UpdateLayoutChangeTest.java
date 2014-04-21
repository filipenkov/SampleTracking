package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.Layout;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateLayoutChangeTest
{
    @Mock DashboardChange.Visitor visitor;
    
    @Test
    public void verifyThatAcceptCallsCorrectVisitMethod()
    {
        UpdateLayoutChange change = new UpdateLayoutChange(Layout.AAA, new GadgetLayout(ImmutableList.<Iterable<GadgetId>>of()));
        change.accept(visitor);
        verify(visitor).visit(change);
    }

}
