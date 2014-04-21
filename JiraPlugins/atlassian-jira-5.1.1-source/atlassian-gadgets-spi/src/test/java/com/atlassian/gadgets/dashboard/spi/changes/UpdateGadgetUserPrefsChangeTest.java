package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UpdateGadgetUserPrefsChangeTest
{
    @Mock DashboardChange.Visitor visitor;
    
    @Test
    public void verifyThatAcceptCallsCorrectVisitMethod()
    {
        UpdateGadgetUserPrefsChange change = new UpdateGadgetUserPrefsChange(GadgetId.valueOf("1"), ImmutableMap.<String, String>of());
        change.accept(visitor);
        verify(visitor).visit(change);
    }
}
