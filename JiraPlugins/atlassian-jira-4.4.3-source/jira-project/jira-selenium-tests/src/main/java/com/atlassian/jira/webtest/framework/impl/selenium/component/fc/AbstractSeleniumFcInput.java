package com.atlassian.jira.webtest.framework.impl.selenium.component.fc;

import com.atlassian.jira.webtest.framework.component.AjsDropdown;
import com.atlassian.jira.webtest.framework.component.fc.FcInput;
import com.atlassian.jira.webtest.framework.component.fc.FcLozenge;
import com.atlassian.jira.webtest.framework.component.fc.FcSuggestions;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControl;
import com.atlassian.jira.webtest.framework.component.fc.FrotherControlComponent;
import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.Queries;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.component.SeleniumAutoCompleteInput;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;

/**
 * Abstract implementation of {@link com.atlassian.jira.webtest.framework.component.fc.FcSuggestions}
 *
 * @since 4.3
 */
public abstract class AbstractSeleniumFcInput<I extends FcInput<I,F,S>, F extends FrotherControl<F,S,I>, S extends FcSuggestions<F>>
        extends SeleniumAutoCompleteInput<I,F> implements FcInput<I,F,S>, FrotherControlComponent<F,S,I>
{

    private final SeleniumLocator inputLocator;
    private final SeleniumLocator iconLocator;

    protected AbstractSeleniumFcInput(String fieldId, F parent, SeleniumContext context)
    {
        super(parent, context);
        this.inputLocator = id(fieldId + "-textarea").withDefaultTimeout(Timeouts.COMPONENT_LOAD);
        this.iconLocator = (SeleniumLocator) parent.locator().combine(jQuery(".icon.drop-menu"));
    }


    /**
     * Create lozenge associated with this input for given <tt>label</tt>.
     *
     * @param label label of the lozenge to create
     * @return new lozenge instance
     */
    protected FcLozenge createLozenge(String label)
    {
        return new SeleniumLozenge(label, parent(), context);
    }

    /* --------------------------------------------------- LOCATORS ------------------------------------------------- */

    @Override
    protected final SeleniumLocator inputLocator()
    {
        return inputLocator;
    }

    @Override
    public Locator locator()
    {
        return inputLocator;
    }

    @Override
    protected final SeleniumLocator iconLocator()
    {
        return iconLocator;
    }

    /* ------------------------------------------------- COMPONENTS ------------------------------------------------- */

    @Override
    public final AjsDropdown<F> dropDown()
    {
        return suggestions();
    }

    @Override
    public TimedQuery<FcLozenge> lozenge(String label)
    {
        FcLozenge newLozenge = createLozenge(label);
        return Queries.conditionalQuery(newLozenge, newLozenge.isReady())
                .expirationHandler(ExpirationHandler.RETURN_NULL).build();

    }


    @Override
    public TimedCondition hasLozenge(String text)
    {
        // TODO may be nice to cache them
        return createLozenge(text).isReady();
    }


    @Override
    public final F fc()
    {
        return parent();
    }


    /* ----------------------------------------------- ACTIONS ------------------------------------------------------ */

    @Override
    public final S arrowDown()
    {
        super.arrowDown();
        return suggestions();
    }

    @Override
    public final S clickDropIcon()
    {
        super.clickDropIcon();
        return suggestions();
    }

}

