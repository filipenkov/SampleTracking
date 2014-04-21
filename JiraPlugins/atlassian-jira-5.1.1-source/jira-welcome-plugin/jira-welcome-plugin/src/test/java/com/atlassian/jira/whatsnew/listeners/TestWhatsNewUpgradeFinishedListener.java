package com.atlassian.jira.whatsnew.listeners;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.ApplicationProperties;
import junit.framework.TestCase;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.atlassian.jira.whatsnew.listeners.WhatsNewUpgradeFinishedListenerImpl.WHATSNEW_ENABLE_PROPERTY;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestWhatsNewUpgradeFinishedListener extends TestCase
{
    private static final String WHATSNEW_SHOW_WHATS_NEW_FLAG = "com.atlassian.jira.whatsnew.jira-whatsnew-plugin:show-whats-new-flag";

    private WhatsNewUpgradeFinishedListenerImpl listener;
    @Mock private PluginController mockPluginController;
    @Mock private ApplicationProperties mockApplicationProperties;
    @Mock private EventPublisher mockEventPublisher;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        listener = new WhatsNewUpgradeFinishedListenerImpl(mockPluginController, mockApplicationProperties, mockEventPublisher);
    }

    @Override
    protected void tearDown() throws Exception
    {
        listener = null;
        super.tearDown();
    }

    public void testEnableOnMajorVersion() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("4.0");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testEnableOnMinorVersion() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testEnableOnPatchVersion() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5.1");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testEnableOnPatchedPatchVersion() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5.1_01");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testEnableOnRc() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5-rc2");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testDisableOnSnapshot() throws Exception
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5-SNAPSHOT");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testDisableOnMilestone()
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5-m5");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testDisableOnBeta()
    {
        when(mockApplicationProperties.getVersion()).thenReturn("3.5-beta1");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testDisableOnChangeset()
    {
        when(mockApplicationProperties.getVersion()).thenReturn("4.0-CDOG-71-r148413");
        listener.enableAfterUpgrade(null);
        verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
    }

    public void testDisableOnSystemProperty() throws Exception
    {
        try
        {
            System.setProperty(WHATSNEW_ENABLE_PROPERTY, "false");
            when(mockApplicationProperties.getVersion()).thenReturn("4.0");
            listener.enableAfterUpgrade(null);
            verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
        finally
        {
            System.clearProperty(WHATSNEW_ENABLE_PROPERTY);
        }
    }

    public void testDisableOnSystemPropertyOverrideNonDevMode() throws Exception
    {
        try
        {
            System.setProperty(WHATSNEW_ENABLE_PROPERTY, "false");
            System.setProperty("atlassian.dev.mode", "false");
            when(mockApplicationProperties.getVersion()).thenReturn("4.0");
            listener.enableAfterUpgrade(null);
            verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
        finally
        {
            System.clearProperty(WHATSNEW_ENABLE_PROPERTY);
            System.clearProperty("atlassian.dev.mode");
        }
    }

    public void testDisableOnDevMode() throws Exception
    {
        try
        {
            System.setProperty("atlassian.dev.mode", "true");
            when(mockApplicationProperties.getVersion()).thenReturn("4.0");
            listener.enableAfterUpgrade(null);
            verify(mockPluginController).disablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
        finally
        {
            System.clearProperty("atlassian.dev.mode");
        }
    }

    public void testEnableOnSystemPropertyOverrideDevMode() throws Exception
    {
        try
        {
            System.setProperty(WHATSNEW_ENABLE_PROPERTY, "true");
            System.setProperty("atlassian.dev.mode", "true");
            when(mockApplicationProperties.getVersion()).thenReturn("3.5-beta1");
            listener.enableAfterUpgrade(null);
            verify(mockPluginController).enablePluginModule(WHATSNEW_SHOW_WHATS_NEW_FLAG);
        }
        finally
        {
            System.clearProperty(WHATSNEW_ENABLE_PROPERTY);
            System.clearProperty("atlassian.dev.mode");
        }
    }
}
