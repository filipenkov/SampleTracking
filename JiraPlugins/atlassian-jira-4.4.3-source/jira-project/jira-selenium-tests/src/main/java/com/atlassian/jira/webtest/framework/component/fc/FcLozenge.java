package com.atlassian.jira.webtest.framework.component.fc;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.component.ValueHolder;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * Lozenge component of the Frother Control. Lozenge represent a single element picked by the user in the
 * given picker. Lozenges are created when the user types into this input and selects any suggestion,
 * or simply types in 'space' character. They may be removed from the input by typing a 'backspace'
 * character into it, or by clicking the 'x' link on the lozenge.
 *
 * @see com.atlassian.jira.webtest.framework.component.fc.FrotherControl
 * @see com.atlassian.jira.webtest.framework.component.fc.FcInput
 * @since v4.3
 */
public interface FcLozenge extends ValueHolder, Localizable
{

    /**
     * Check if this lozenge is selected (focused on). This may happen via keyboard operations (backspace key).
     *
     * @return timed condition verifying if this lozenge is focused on
     */
    TimedCondition isSelected();

    /**
     * Check if this lozenge is <i>not</i> selected.
     *
     * @see #isSelected()
     * @return timed condition verifying if this lozenge is <i>not</i> focused on
     */
    TimedCondition isNotSelected();


    /**
     * Click on the 'x' icon on the lozenge to remove it.
     *
     */
    void removeByClick();
    
}
