package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RemoveGadgetChangeTest
{
    @Mock DashboardChange.Visitor visitor;
    
    @Test
    public void verifyThatAcceptCallsCorrectVisitMethod()
    {
        RemoveGadgetChange change = new RemoveGadgetChange(GadgetId.valueOf("1"));
        change.accept(visitor);
        verify(visitor).visit(change);
    }
}
