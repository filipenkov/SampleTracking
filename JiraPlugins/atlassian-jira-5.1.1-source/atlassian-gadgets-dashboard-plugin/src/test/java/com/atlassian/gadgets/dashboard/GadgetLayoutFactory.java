package com.atlassian.gadgets.dashboard;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.internal.Gadget;
import com.atlassian.gadgets.dashboard.spi.GadgetLayout;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import static com.google.common.collect.Iterables.transform;
import static java.util.Collections.nCopies;

final public class GadgetLayoutFactory
{
    private GadgetLayoutFactory() {}

    public static GadgetLayout emptyGadgetLayout(final Layout dashboardLayout)
    {
        return newGadgetLayout(nCopies(dashboardLayout.getNumberOfColumns(), ImmutableList.<GadgetId>of()));
    }

    public static GadgetLayout newGadgetLayout(final List<? extends Iterable<GadgetId>> columns)
    {
        return new GadgetLayout(columns);
    }

    public static GadgetLayout newGadgetLayout(Iterable<GadgetId> column)
    {
        return newGadgetLayout(Collections.singletonList(column));
    }

    public
    static GadgetLayout newGadgetLayout(Iterable<GadgetId> column1, Iterable<GadgetId> column2)
    {
        return newGadgetLayout(ImmutableList.of(column1, column2));
    }

    public
    static GadgetLayout newGadgetLayout(Iterable<GadgetId> column1, Iterable<GadgetId> column2, Iterable<GadgetId> column3)
    {
        return newGadgetLayout(ImmutableList.of(column1, column2, column3));
    }

    public static Iterable<GadgetId> column(Gadget... gadgets)
    {
        return transform(Arrays.asList(gadgets), GadgetToGadgetId.FUNCTION);
    }
    
    public static Iterable<GadgetId> column(GadgetId... gadgetIds)
    {
        return ImmutableList.of(gadgetIds);
    }

    private static enum GadgetToGadgetId implements Function<Gadget, GadgetId> {
        FUNCTION;

        public GadgetId apply(Gadget gadget)
        {
            return gadget.getId();
        }
    }
}
