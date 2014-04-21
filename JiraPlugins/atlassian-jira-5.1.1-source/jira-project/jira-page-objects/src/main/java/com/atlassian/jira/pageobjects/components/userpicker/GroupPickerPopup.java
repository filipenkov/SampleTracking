package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import static com.atlassian.jira.pageobjects.framework.elements.ExtendedElementFinder.forFinder;
import static com.atlassian.jira.pageobjects.framework.elements.PageElements.hasDataAttribute;

/**
 * Group picker popup.
 *
 * @since v5.0
 */
public class GroupPickerPopup extends PickerPopup<GroupPickerPopup.GroupPickerRow>
{

    public GroupPickerPopup(LegacyPicker parent)
    {
        super(parent, PickerType.GROUP_PICKER, GroupPickerRow.class);
    }

    public static class GroupPickerRow extends PickerPopup.PickerRow<GroupPickerRow>
    {
        private PageElement groupNameCell;

        public GroupPickerRow(GroupPickerPopup owner, PageElement rowElement)
        {
            super(owner, rowElement);
            initCells();
        }

        private void initCells()
        {
            groupNameCell = forFinder(rowElement).find(By.tagName("td"), hasDataAttribute("name"));
        }

        public String getGroupName()
        {
            return groupNameCell.getText();
        }
    }
}
