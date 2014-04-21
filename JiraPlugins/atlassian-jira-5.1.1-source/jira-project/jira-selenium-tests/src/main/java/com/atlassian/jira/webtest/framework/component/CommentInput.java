package com.atlassian.jira.webtest.framework.component;

import com.atlassian.jira.webtest.framework.core.component.Input;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

/**
 * <p>
 * Represents the comment input component in the JIRA UI. The comment control consists mainly of the text area input,
 * but also has additional switches to control comment visibility and to switch to wiki preview (if wiki markup
 * is enabled for comments).
 *
 *
 * @since v4.3
 */
public interface CommentInput extends Input
{

    static interface CommentVisibilityDropdown extends AjsDropdown<CommentInput>
    {
        TimedQuery<Section<CommentInput>> groupsSection();

        TimedQuery<Section<CommentInput>> rolesSection();

        TimedQuery<Item<CommentInput>> allUsersItem();
    }

    /**
     * Return the visibility drop-down of this comment input
     *
     * @return visibility selection dropdown
     */
    CommentVisibilityDropdown visibilityDropdown();

    /**
     * Check if this comment input is in edit-mode. If the wiki-rendering is disabled for this comment, this method
     * will always return <code>true</code> condition.
     *
     * @return timed condition verifying if this comment input is in edit-mode
     */
    TimedCondition isEditMode();

    /**
     * Check if this comment input is in preview-mode. If wiki-rendering is disabled for this comment, this method
     * will always return <code>false<code> condition.
     *
     * @return timed condition verifying if this comment input is in preview mode
     * @see #hasWikiRendering()
     */
    TimedCondition isPreviewMode();

    /**
     * Check if this comment input has wiki rendering.
     *
     * @return timed condition verifying, if wiki rendering is enabled for this comment input
     */
    TimedCondition hasWikiRendering();


    /**
     * Switch mode of this comment input. Results of this operation may be queried via {@link #isEditMode()}
     * and {@link #isPreviewMode()}. Wiki rendering <b>must</b> be enabled for this operation to succeed,
     *
     * @return this comment input
     * @throws IllegalStateException if wiki rendering is disabled for this comment input
     */
    CommentInput toggleMode();
}
