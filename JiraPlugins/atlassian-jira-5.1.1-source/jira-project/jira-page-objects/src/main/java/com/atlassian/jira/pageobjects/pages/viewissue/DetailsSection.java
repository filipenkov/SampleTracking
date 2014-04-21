package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v5.0
 */
public class DetailsSection
{
    @ElementBy (id = "type-val")
    PageElement issueType;

    @ElementBy (id = "wrap-labels")
    PageElement labelContainer;

    public String getIssueType()
    {
        return issueType.getText().trim();
    }

    public List<String> getLabels()
    {

        final List<String> labels = new ArrayList<String>();
        final List<PageElement> all = labelContainer.findAll(By.cssSelector("li .lozenge"));

        for (PageElement pageElement : all)
        {
            labels.add(pageElement.getText().trim());
        }
        return labels;
    }

}