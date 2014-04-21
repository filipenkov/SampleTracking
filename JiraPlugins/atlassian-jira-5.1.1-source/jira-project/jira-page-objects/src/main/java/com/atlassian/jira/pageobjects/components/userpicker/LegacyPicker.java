package com.atlassian.jira.pageobjects.components.userpicker;

import com.atlassian.jira.pageobjects.framework.fields.CustomField;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * <p/>
 * The old user/group picker that shows up as a popup window. Should go away but it won't any time soon...
 *
 * @since v5.0
 */
public class LegacyPicker implements CustomField
{

    @Inject
    protected PageBinder pageBinder;

    @Inject
    protected PageElementFinder elementFinder;


    protected final String pickerId;

    protected final PageElement form;

    protected PageElement pickerRoot;
    protected PageElement target;
    protected PageElement trigger;


    public LegacyPicker(@Nullable PageElement form, String pickerId)
    {
        this.pickerId = pickerId;
        this.form = form;
    }

    public LegacyPicker(String pickerId)
    {
        this(null, pickerId);
    }

    @Init
    private void init()
    {
        pickerRoot = root().find(By.id(pickerId + "_container"));
        target = pickerRoot.find(By.id(pickerId));
        trigger = pickerRoot.find(By.className("popup-trigger"));
        assertTrue("Picker root should have 'ajax_autocomplete' CSS class", pickerRoot.hasClass("ajax_autocomplete"));
    }

    private PageElementFinder root()
    {
        return form != null ? form : elementFinder;
    }


    public PageElement getRoot()
    {
        return pickerRoot;
    }

    public PageElement getTarget()
    {
        return target;
    }

    /**
     * The selected value in the picker.
     *
     * @return value in the picker
     */
    public String getPickerValue()
    {
        return target.getValue();
    }

//    // TODO for multi-value mode
//    /**
//     * Set of selected values
//     *
//     * @return
//     */
//    public Set<String> getMultiValue()
//    {
//
//    }

    public PageElement getTrigger()
    {
        return trigger;
    }



    // TODO auto suggestions
}
