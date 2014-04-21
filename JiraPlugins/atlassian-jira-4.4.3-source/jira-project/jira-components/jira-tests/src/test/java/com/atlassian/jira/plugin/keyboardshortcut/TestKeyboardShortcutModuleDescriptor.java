package com.atlassian.jira.plugin.keyboardshortcut;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.plugin.webwork.TestDefaultAutowireCapableWebworkActionRegistry;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.plugin.PluginParseException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class TestKeyboardShortcutModuleDescriptor extends ListeningTestCase
{
    @Test
    public void testInit() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>10</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>gd</shortcut>\n"
                        + "        <operation type=\"followLink\">#home_link</operation>\n"
                        + "    </keyboard-shortcut>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());

        final int order = moduleDescriptor.getOrder();
        assertEquals(10, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals("#home_link", keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = CollectionBuilder.newBuilder(Arrays.asList("g", "d")).asSet();
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
    }

    @Test
    public void testInitWithJsonString() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>10</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>\"return\"</shortcut>\n"
                        + "        <operation type=\"followLink\">#home_link</operation>\n"
                        + "    </keyboard-shortcut>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());

        final int order = moduleDescriptor.getOrder();
        assertEquals(10, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals("#home_link", keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = CollectionBuilder.newBuilder(Arrays.asList("return")).asSet();
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
    }

    @Test
    public void testInitWithJsonArray() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>10</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>[\"j\", \"tab\"]</shortcut>\n"
                        + "        <operation type=\"followLink\">#home_link</operation>\n"
                        + "    </keyboard-shortcut>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());

        final int order = moduleDescriptor.getOrder();
        assertEquals(10, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.followLink, keyboardShortcut.getOperation());
        assertEquals("#home_link", keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = CollectionBuilder.newBuilder(Arrays.asList("j", "tab")).asSet();
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
    }

    @Test
    public void testInitWithContext() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>20</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>issueaction</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());

        final int order = moduleDescriptor.getOrder();
        assertEquals(20, order);
        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.issueaction, keyboardShortcut.getContext());
        assertEquals(KeyboardShortcutManager.Operation.click, keyboardShortcut.getOperation());
        assertEquals("#create_link", keyboardShortcut.getParameter());
        final Set<List<String>> shortcuts = CollectionBuilder.newBuilder(Arrays.asList("c")).asSet();
        assertEquals(shortcuts, keyboardShortcut.getShortcuts());
    }

    @Test
    public void testInitWithInvalidContextDefaultsToGlobal() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        final Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <order>20</order>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>booyah</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");

        moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());

        final KeyboardShortcut keyboardShortcut = moduleDescriptor.getModule();
        assertEquals(KeyboardShortcutManager.Context.global, keyboardShortcut.getContext());
    }

    @Test
    public void testInitFailure() throws DocumentException
    {
        final KeyboardShortcutModuleDescriptor moduleDescriptor =
                new KeyboardShortcutModuleDescriptor(new MockSimpleAuthenticationContext(null), null, null);
        //shortcut missing
        Document document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <context>issue</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }

        //shortcut missing body
        document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut></shortcut>\n"
                        + "        <context>issue</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }

        //operation missing
        document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>issue</context>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }

        //invalid operation
        document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description key=\"admin.keyboard.shortcut.goto.dashboard.desc\">Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>global</context>\n"
                        + "        <operation type=\"booyah\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }
        
        //no i18n key for the description
        document = DocumentHelper.parseText(
                "<keyboard-shortcut key=\"goto.dashboard\" i18n-name=\"admin.keyboard.shortcut.goto.dashboard.name\"\n"
                        + "                       name=\"Goto Dashboard\" state='enabled'>\n"
                        + "        <description>Go to Dashboard</description>\n"
                        + "        <shortcut>c</shortcut>\n"
                        + "        <context>global</context>\n"
                        + "        <operation type=\"click\">#create_link</operation>\n"
                        + "    </keyboard-shortcut>");
        try
        {
            moduleDescriptor.init(new TestDefaultAutowireCapableWebworkActionRegistry.MockPlugin("keyboard-shortcuts"), document.getRootElement());
            fail("Should have thrown exception!");
        }
        catch (PluginParseException e)
        {
            //yay
        }

    }
}
