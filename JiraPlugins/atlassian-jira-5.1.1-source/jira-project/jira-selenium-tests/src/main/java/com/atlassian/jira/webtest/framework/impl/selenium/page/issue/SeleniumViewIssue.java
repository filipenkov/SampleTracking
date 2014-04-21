package com.atlassian.jira.webtest.framework.impl.selenium.page.issue;

import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.ui.Shortcuts;
import com.atlassian.jira.webtest.framework.dialog.DotDialog;
import com.atlassian.jira.webtest.framework.dialog.issueaction.IssueActionDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.SeleniumDotDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction.IssueActionDialogOpener;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction.SeleniumLinkIssueDialog;
import com.atlassian.jira.webtest.framework.impl.selenium.dialog.issueaction.ViewIssueOpenMode;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.AbstractSeleniumPage;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issue.move.SeleniumMoveSubtaskChooseOperation;
import com.atlassian.jira.webtest.framework.impl.selenium.page.issue.move.SeleniumMoveSubtaskConfirmation;
import com.atlassian.jira.webtest.framework.model.IssueData;
import com.atlassian.jira.webtest.framework.model.IssueOperation;
import com.atlassian.jira.webtest.framework.page.IssueActionsParent;
import com.atlassian.jira.webtest.framework.page.issue.ConvertToSubTaskSelectTypes;
import com.atlassian.jira.webtest.framework.page.issue.IssueMenu;
import com.atlassian.jira.webtest.framework.page.issue.ViewIssue;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskChooseOperation;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskConfirmation;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskOperationDetails;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Selenium implementation of the {@link com.atlassian.jira.webtest.framework.page.issue.ViewIssue} interface.
 *
 * @since v4.3
 */
public class SeleniumViewIssue extends AbstractSeleniumPage implements ViewIssue
{
    private static final String DETECTOR = "div#stalker-placeholder";
    private static final String ISSUE_DATA_LINK_ID = "key-val";

    private final SeleniumLocator detector;
    private final SeleniumLocator issueDataLocator;
    private final IssueActionDialogOpener opener;

    private final SeleniumIssueMenu issueMenu;
    private final ViewIssueDD dotDialog;
    private final List<IssueActionDialog<?>> actionDialogs = new ArrayList<IssueActionDialog<?>>();


    public SeleniumViewIssue(SeleniumContext ctx)
    {
        super(ctx);
        this.detector = css(DETECTOR);
        this.issueDataLocator = id(ISSUE_DATA_LINK_ID);
        this.issueMenu = new SeleniumIssueMenu(this, context);
        this.dotDialog = new ViewIssueDD(context, issueData());
        this.opener = new VIOpener();
        initActionDialogs();
    }

    private void initActionDialogs()
    {
        actionDialogs.add(new SeleniumLinkIssueDialog(this, opener, context()));
        // TODO rest (or DI?)
    }

    /* ----------------------------------------------- QUERIES ------------------------------------------------------ */

    @Override
    public IssueData issueData()
    {
        return new VIIssueData();
    }

    /* ---------------------------------------------- LOCATORS ------------------------------------------------------ */

    @Override
    protected SeleniumLocator detector()
    {
        return detector;
    }

    /* ---------------------------------------------- COMPONENTS ---------------------------------------------------- */

    @Override
    public IssueMenu menu()
    {
        return issueMenu;
    }

    // TODO implement dialogs

    @Override
    public <D extends IssueActionDialog<D>> D dialog(Class<D> dialogType)
    {
        for (IssueActionDialog<?> dialog : actionDialogs)
        {
            if (dialogType.isInstance(dialog))
            {
                return uglyCast(dialog);
            }
        }
        throw new IllegalStateException(asString("No dialog of type <",dialogType.getName(),">"));
    }

    @SuppressWarnings ({ "unchecked" })
    private <D extends IssueActionDialog<D>> D uglyCast(IssueActionDialog<?> dialog)
    {
        return (D)dialog;
    }

    @Override
    public DotDialog dotDialog()
    {
        return dotDialog;
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public <T extends PageObject> T getChild(Class<T> childType)
    {
        // TODO neeeeds DI!!!!
        if (childType == ConvertToSubTaskSelectTypes.class)
        {
            return (T) new SeleniumConvertToSubtaskSelectTypes<ViewIssue>(context, this);
        }
        if (childType == MoveSubTaskChooseOperation.class)
        {
            return (T) new SeleniumMoveSubtaskChooseOperation<ViewIssue>(context, this);
        }
        if (childType == MoveSubTaskConfirmation.class)
        {
            return (T) new SeleniumMoveSubtaskConfirmation<ViewIssue>(context, this);
        }
        if (childType == MoveSubTaskOperationDetails.class)
        {
            // TODO if necessary
            return null;
        }
        throw new IllegalArgumentException("no support for child type: " + childType);
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public <D extends IssueActionDialog<D>> ViewIssueDialogOpenMode<D> openDialog(Class<D> dialogType)
    {
        return new ViewIssueOpenMode<D>(this, dialog(dialogType), context());
    }

    @Override
    public DotDialog openDotDialog()
    {
        context().ui().pressInBody(Shortcuts.DOT_DIALOG);
        return dotDialog;
    }


    /* ------------------------------------------------ STUFF ------------------------------------------------------- */

    private class ViewIssueDD extends SeleniumDotDialog implements DotDialog
    {
        public ViewIssueDD(SeleniumContext context, IssueData issueData)
        {
            super(context, issueData);
        }

        @Override
        public IssueActionsParent parentPage()
        {
            return SeleniumViewIssue.this;
        }
    }


    private class VIIssueData implements IssueData
    {
        @Override
        public long id()
        {
            String id = issueDataLocator.element().attribute("rel").byDefaultTimeout();
            return Long.parseLong(id);
        }

        @Override
        public String key()
        {
            return issueDataLocator.element().text().byDefaultTimeout();
        }
    }

    /**
     * By default, dialogs are opened via the issue ('ops-bar') menu.
     *
     */
    private class VIOpener implements IssueActionDialogOpener
    {
        @Override
        public void open(IssueOperation operation)
        {
            issueMenu.invoke(operation);
        }
    }
}
