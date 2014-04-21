/**
 * This class aims simply to remove the text in the DownloadProgress dialog
 * to save i18n problems.
 */
package com.sysbliss.jira.workflow.ui.dialog {
import mx.preloaders.DownloadProgressBar;

public class JiraDownloadProgressBar extends DownloadProgressBar {
    public function JiraDownloadProgressBar() {
        super();
        DownloadProgressBar.initializingLabel = "";
        this.showLabel = false;

    }
}
}
