package com.atlassian.crowd.plugin.rest.entity;

import com.atlassian.plugins.rest.common.expand.*;

/**
 * Expands an <tt>MultiValuedAttributeEntityList</tt>. This expander simply returns the current attribute entity list since the
 * attribute list's parent entity expander should have expanded the attribute list.
 *
 * @since v2.1
 */
public class MultiValuedAttributeEntityListExpander extends AbstractRecursiveEntityExpander<MultiValuedAttributeEntityList>
{
    @Override
    protected MultiValuedAttributeEntityList expandInternal(final MultiValuedAttributeEntityList entity)
    {
        return entity;
    }
}
