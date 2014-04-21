package com.atlassian.jira.webtest.framework.impl.selenium.page.issue.move;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.page.ParentPage;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskChooseOperation;
import com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskOperationDetails;

/**
 * Selenium implementation of {@link com.atlassian.jira.webtest.framework.page.issue.move.MoveSubTaskChooseOperation}.
 *
 * @since v4.3
 */
public class SeleniumMoveSubtaskChooseOperation<P extends ParentPage> extends AbstractSeleniumMoveSubTask<P, MoveSubTaskOperationDetails<P>>
        implements MoveSubTaskChooseOperation<P>
{

    private final SeleniumLocator changeTypeRadioLocator;
    private final SeleniumLocator changeParentRadioLocator;



    public SeleniumMoveSubtaskChooseOperation(SeleniumContext ctx, P flowParent)
    {
        super(ctx, flowParent, 1);
        this.changeTypeRadioLocator = id("move.subtask.type.operation.name");
        this.changeParentRadioLocator = id("move.subtask.parent.operation.name_id");
    }

    @Override
    protected Class<MoveSubTaskOperationDetails<P>> nextStepType()
    {
        return nextClass();
    }

    @SuppressWarnings ({ "unchecked" })
    private Class<MoveSubTaskOperationDetails<P>> nextClass()
    {
        return (Class) MoveSubTaskOperationDetails.class;
    }

    private SeleniumLocator locatorForFlowType(FlowType flowType)
    {
        if (flowType == FlowType.CHANGE_TYPE)
        {
            return changeTypeRadioLocator;
        }
        else if (flowType == FlowType.CHANGE_PARENT)
        {
            return changeParentRadioLocator;
        }
        throw new AssertionError("Whaaat?");
    }

    /* ---------------------------------------------- QUERIES ---------------------------------------------------- */

    @Override
    public TimedCondition isSelectable(FlowType flowType)
    {
        return locatorForFlowType(flowType).element().isPresent();
    }

    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public MoveSubTaskChooseOperation<P> selectFlowType(FlowType flowType)
    {
        if (!isSelectable(flowType).now())
        {
            throw new IllegalStateException("Flow type <" + flowType + "> not selectable. isSelectable failed: "
                    + isSelectable(flowType));
        }
        locatorForFlowType(flowType).element().click();
        return this;
    }
}
