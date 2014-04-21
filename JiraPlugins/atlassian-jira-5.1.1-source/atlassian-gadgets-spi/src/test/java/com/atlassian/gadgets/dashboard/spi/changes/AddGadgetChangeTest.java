package com.atlassian.gadgets.dashboard.spi.changes;

import java.net.URI;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.GadgetState;
import com.atlassian.gadgets.dashboard.DashboardState.ColumnIndex;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AddGadgetChangeTest
{
    @Mock DashboardChange.Visitor visitor;
    
    @Test
    public void verifyThatAcceptCallsCorrectVisitMethod()
    {
        AddGadgetChange change = new AddGadgetChange(GadgetState.gadget(GadgetId.valueOf("1")).specUri(URI.create("http://gadget/spec.xml")).build(), ColumnIndex.ZERO, 0);
        change.accept(visitor);
        verify(visitor).visit(change);
    }
}
