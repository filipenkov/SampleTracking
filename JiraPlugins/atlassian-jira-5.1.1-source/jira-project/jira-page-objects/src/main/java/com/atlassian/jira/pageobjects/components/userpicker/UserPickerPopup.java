package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder.forFinder;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.hasDataAttribute;

/**
 * User picker popup.
 *
 * @since v5.0
 */
public class UserPickerPopup extends PickerPopup<UserPickerPopup.UserPickerRow>
{

    public UserPickerPopup(LegacyPicker parent)
    {
        super(parent, PickerType.USER_PICKER, UserPickerRow.class);
    }

    public static class UserPickerRow extends PickerPopup.PickerRow<UserPickerRow>
    {

        private PageElement usernameCell;
        // TODO rest

        public UserPickerRow(UserPickerPopup owner, PageElement rowElement)
        {
            super(owner, rowElement);
            initCells();
        }

        private void initCells()
        {
            usernameCell = forFinder(rowElement).find(By.tagName("td"), hasDataAttribute("name"));
        }

        public String getUsername()
        {
            return usernameCell.getText();
        }
    }
}
