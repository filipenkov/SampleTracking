package com.atlassian.jira.webtest.framework.core.ui;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.webtest.ui.keys.KeySequence;

/**
 * Common UI operations in the web tests.
 *
 * @since v4.3
 */
public interface WebTestUi
{
    interface TargetSelector
    {
        /**
         * Select main window as target.
         *
         * @return web test UI instance
         */
        WebTestUi mainWindow();

        /**
         * Select window with given name.
         *
         * @param name name of the window
         * @return web test UI instance
         */
        WebTestUi window(String name);

        /**
         * Select frame with given <tt>locator</tt>.
         *
         * @param locator locator of the frame
         * @return web test UI instance
         */
        WebTestUi frame(Locator locator);

        /**
         * Select top frame within the current window.
         *
         * @return web test UI instance
         */
        WebTestUi topFrame();
    }

    /**
     * Locator of the current document's body element
     *
     * @return body element locator on the current page
     */
    Locator body();

    /**
     * Press a key sequence in the current document's body. Mainly used for invoking keyboard shortcuts.
     *
     * @param sequence shortcut sequence to invoke
     * @return this UI instance
     */
    WebTestUi pressInBody(KeySequence sequence);


    /**
     * Switch to given window/frame.
     *
     * @return selector to select target to switch to.
     */
    TargetSelector switchTo();

    
        // TODO mouse/window
}
