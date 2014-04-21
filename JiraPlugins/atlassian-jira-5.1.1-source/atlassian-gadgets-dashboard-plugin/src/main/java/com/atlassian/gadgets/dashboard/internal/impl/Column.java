package com.atlassian.gadgets.dashboard.internal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.gadgets.GadgetId;
import com.atlassian.gadgets.dashboard.internal.Gadget;

class Column
{
    private final List<Gadget> gadgets = new ArrayList<Gadget>();

    Iterable<Gadget> getGadgets()
    {
        return gadgets;
    }
    
    void appendGadget(Gadget gadget)
    {
        gadgets.add(gadget);
    }

    void addGadget(Gadget gadget)
    {
        gadgets.add(0, gadget);
    }
    
    boolean containsGadget(GadgetId gadgetId)
    {
        for (Gadget gadget : gadgets)
        {
            if (gadget.getId().equals(gadgetId))
            {
                return true;
            }
        }
        return false;
    }

    void removeGadget(GadgetId gadgetId)
    {
        for (Iterator<Gadget> it = gadgets.iterator(); it.hasNext(); )
        {
            Gadget gadget = it.next();
            if (gadget.getId().equals(gadgetId))
            {
                it.remove();
            }
        }
    }

    Map<GadgetId, Gadget> getGadgetMap()
    {
        Map<GadgetId, Gadget> gadgets = new HashMap<GadgetId, Gadget>();
        for (Gadget gadget : getGadgets())
        {
            gadgets.put(gadget.getId(), gadget);
        }
        return gadgets;
    }

    void clear()
    {
        gadgets.clear();
    }
}
