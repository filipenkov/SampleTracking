package com.atlassian.jira.jql.validator;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.util.InjectableComponent;

/**
 * Original Estimate validator
 *
 * @since v4.0
 */
@InjectableComponent
public class OriginalEstimateValidator extends AbstractTimeTrackingValidator
{
    ///CLOVER:OFF

    public OriginalEstimateValidator(final JqlOperandResolver operandResolver, final ApplicationProperties applicationProperties)
    {
        super(operandResolver, applicationProperties);
    }

    ///CLOVER:ON
}
