package com.atlassian.jira.config.component;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.defaults.DefaultPicoContainer;

public class TestSwitchingInvocationAdapter extends ListeningTestCase
{
    MyApplicationProperties applicationProperties = new MyApplicationProperties();

    @Test
    public void testSwap()
    {
        MutablePicoContainer container = getContainer();
        applicationProperties.setEnabled(true);

        AppPropertiesComponentAdaptor componentAdaptor = new AppPropertiesComponentAdaptor(IntX.class, EnabledClass.class, DisabledClass.class, "key");
        componentAdaptor.setContainer(container);

        assertEquals(IntX.class, componentAdaptor.getComponentKey());
        // Get the component Instance
        IntX componentInstance = (IntX) componentAdaptor.getComponentInstance();
        // Should start life as enabled.
        assertEquals("enabled", componentInstance.getName());

        // Change to disabled
        applicationProperties.setEnabled(false);
        // Should dynamically switch
        assertEquals("disabled", componentInstance.getName());

        // Change back to enabled just to be sure...
        applicationProperties.setEnabled(true);
        // and assert teh switch back.
        assertEquals("enabled", componentInstance.getName());
    }

    private MutablePicoContainer getContainer()
    {
        MutablePicoContainer container = new DefaultPicoContainer();
        container.registerComponentImplementation(EnabledClass.class);
        container.registerComponentImplementation(DisabledClass.class);

        container.registerComponentInstance(ApplicationProperties.class, applicationProperties);

        return container;
    }

    public static interface IntX
    {
        public String getName();
    }

    public static class EnabledClass implements IntX
    {
        public String getName()
        {
            return "enabled";
        }
    }

    public static class DisabledClass implements IntX
    {
        public String getName()
        {
            return "disabled";
        }
    }

    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private boolean enabled;

        private MyApplicationProperties() {super(null);}

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public boolean getOption(String key)
        {
            if ("key".equals(key))
                return enabled;
            throw new IllegalArgumentException("Can only call this with 'key'");
        }
    }

}
