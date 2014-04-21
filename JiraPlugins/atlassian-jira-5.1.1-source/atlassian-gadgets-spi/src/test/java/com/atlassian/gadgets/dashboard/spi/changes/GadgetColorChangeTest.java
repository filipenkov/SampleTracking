package com.atlassian.gadgets.dashboard.spi.changes;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.Color;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GadgetColorChangeTest
{
    @Mock DashboardChange.Visitor visitor;
    
    @Test
    public void verifyThatAcceptCallsCorrectVisitMethod()
    {
        GadgetColorChange change = new GadgetColorChange(GadgetId.valueOf("1"), Color.color1);
        change.accept(visitor);
        verify(visitor).visit(change);
    }
}
