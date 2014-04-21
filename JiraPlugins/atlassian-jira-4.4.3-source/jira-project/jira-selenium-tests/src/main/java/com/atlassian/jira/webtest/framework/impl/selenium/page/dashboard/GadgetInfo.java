package com.atlassian.jira.webtest.framework.impl.selenium.page.dashboard;

import com.atlassian.jira.webtest.framework.gadget.Gadget;
import com.atlassian.jira.webtest.framework.gadget.ReferenceGadget;
import com.atlassian.jira.webtest.framework.gadget.ReferencePortlet;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Gadget mappings to support the API.
 *
 * @since v4.3
 */
public final class GadgetInfo
{
    private GadgetInfo()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static final Map<Class<?>, Mapping> GADGET_MAPPINGS = ImmutableMap.<Class<?>, Mapping>builder()
            .put(ReferenceGadget.class, gadget(ReferenceGadget.class, "Reference Gadget"))
            .put(ReferencePortlet.class, portlet(ReferencePortlet.class, "Reference Portlet"))
            .build();

    public static String gadgetId(Class<?> gadgetClass)
    {
        return GADGET_MAPPINGS.get(gadgetClass).id();
    }

    public static String gadgetName(Class<?> gadgetClass)
    {
        return GADGET_MAPPINGS.get(gadgetClass).name();
    }

    private static Mapping portlet(Class<? extends Gadget> portletClass, String name)
    {
        return new PortletMapping(portletClass, name);
    }
    private static Mapping gadget(Class<? extends Gadget> gadgetClass, String name)
    {
        return new GadgetMapping(gadgetClass, name);
    }

    private static abstract class Mapping
    {
        final Class<? extends Gadget> gadgetClass;
        final String name;

        Mapping(Class<? extends Gadget> gadgetClass, String name)
        {
            this.gadgetClass = gadgetClass;
            this.name = name;
        }

        String name()
        {
            return name;
        }

        abstract String id();
    }

    private static class GadgetMapping extends Mapping
    {
        final String id;

        private GadgetMapping(Class<? extends Gadget> gadgetClass, String name)
        {
            super(gadgetClass, name);
            this.id = name.replaceAll(" ", "");
        }

        @Override
        String id()
        {
            return id;
        }
    }

    private static class PortletMapping extends Mapping
    {
        final String id;

        private PortletMapping(Class<? extends Gadget> gadgetClass, String name)
        {
            super(gadgetClass, name);
            this.id = name.replaceAll(" ", "") + "Legacy";
        }

        @Override
        String id()
        {
            return id;
        }
    }
}
