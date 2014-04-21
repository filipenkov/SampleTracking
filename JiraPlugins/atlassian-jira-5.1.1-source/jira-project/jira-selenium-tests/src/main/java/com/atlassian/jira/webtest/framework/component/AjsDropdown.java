package com.atlassian.jira.webtest.framework.component;

import com.atlassian.jira.webtest.framework.core.Localizable;
import com.atlassian.jira.webtest.framework.core.Openable;
import com.atlassian.jira.webtest.framework.core.PageObject;
import com.atlassian.jira.webtest.framework.core.component.Component;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

import java.util.List;

/**
 * <p>
 * Represents a AJS.DropdownSelect component used in JIRA dialogs and pickers as an auto-complete suggestions container.
 *
 * <p>
 * The <tt>AjsDropdown</tt> contains a list of items grouped into one, or more sections. Occasionally it may contain
 * first-class items, i.e. items not grouped in any section. To retrieve a particular section or item, usage of
 * {@link #section(String)} and {@link #item(String)} is preferred to {@link #allSections()}. Additionally, {@link #item(String)}
 * is the only method capable of retrieving first-class items (as explained above).
 *
 * @param <P> type of the parent pag object
 * @since v4.3
 */
public interface AjsDropdown<P extends PageObject> extends Component<P>, Openable<AjsDropdown<P>>, Localizable
{
    /* ----------------------------------------- NESTED INTERFACES -------------------------------------------------- */

    /**
     * Section of the drop-down list.
     *
     */
    interface Section<Q extends PageObject> extends Localizable, Component<AjsDropdown<Q>>
    {
        /**
         * Unique ID of the section
         *
         * @return ID of the section
         */
        String id();

        /**
         * Header of the section
         *
         * @return header of the section
         */
        String header();

        /**
         * Check whether this section has header.
         *
         * @return <code>true</code>, if this section has header, <code>false</code> otherwise
         */
        boolean hasHeader();

        /**
         * All items of this section.
         *
         * @return all items of this section
         */
        TimedQuery<List<Item<Q>>> items();
    }

    /**
     * A single position in the drop-down list.
     *
     */
    interface Item<R extends PageObject> extends Localizable, Component<Section<R>>
    {
        /**
         * Dropdown that this position belongs to (section may be retrieved via {@link #parent()}).
         *
         * @return dropdown of this position
         */
        AjsDropdown<R> dropDown();

        /**
         * Name of the position as visible in the UI.
         *
         * @return name of this position
         */
        String name();

        /**
         * Check if this position is currently selected.
         *
         * @return condition checking whether this position is currently selected
         */
        TimedCondition isSelected();

        /**
         * Check if this position is currently <i>NOT</i> selected. The returned condition will return <code>true</code>
         * as soon as this position element is not select, or <code>false<code>, if the timeout expires and this element
         * is still selected.
         *
         * @return condition checking whether this position is currently <i>not</i> selected
         */
        TimedCondition isNotSelected();

        /**
         * Select this position.
         *
         * @return this position instance
         */
        Item<R> select();

        /**
         * If this position is selected, this action will move selection to the next position and return it.
         *
         * @return next position, which is also going to be selected 
         * @throws IllegalStateException if this position is not selected before invoking this action
         * @see #isSelected()
         * @see #select() 
         */
        Item<R> down();

        /**
         * If this position is selected, this action will move selection to the previous position and return it.
         *
         * @return previous position, which is also going to be selected
         * @throws IllegalStateException if this position is not selected before invoking this action
         * @see #isSelected()
         * @see #select()
         */
        Item<R> up();
    }

    interface CloseMode<PP extends PageObject>
    {
        PP byEnter();

        PP byEscape();

        PP byClickIn(Item<PP> position);
    }


    /* --------------------------------------------------- QUERIES -------------------------------------------------- */


    /**
     * Check if this drop-down has item with given <tt>itemText</tt>.
     *
     * @param itemText text of the position to find
     * @return condition querying whether a position with given text exists in this drop-down
     */
    TimedCondition hasItem(String itemText);

    /**
     * Check if this drop-down has section with given <tt>id</tt>.
     *
     * @param id unique pag ID of the section to find
     * @return condition querying whether a section with given ID exists in this drop-down
     */
    TimedCondition hasSection(String id);

    /**
     * Number of items in this drop-down
     *
     * @return total number of items
     */
    TimedQuery<Integer> itemCount();

    
    /* ------------------------------------------------ COMPONENTS -------------------------------------------------- */

    /**
     * Find section with given <tt>id</tt>. Id of the section must be known by the client. Some sections do not have
     * headings, so retrieving sections by their id is impractical.
     *
     * @param id unique page ID of the section
     * @return section with given ID, or <code>null</code>,
     * if such section does not exist within this drop-down
     *
     * @see #hasItem(String)
     */
    TimedQuery<Section<P>> section(String id);

    /**
     * Get all sections of this drop-down.
     *
     * @return list of all sections
     */
    TimedQuery<List<Section<P>>> allSections();

    /**
     * Find first position matching <tt>test</tt>
     *
     * @param text text of the position to find
     * @return position matching <tt>text</tt>, or <code>null</code>,
     * if position with given text does not exist in this drop-down
     *
     * @see #hasItem(String)
     */
    TimedQuery<Item<P>> item(String text);

    /**
     * Get currently selected position of in the list.
     *
     * @return timed query for selected position
     */
    TimedQuery<Item<P>> selectedItem();



    /* --------------------------------------------------- ACTIONS -------------------------------------------------- */

    /**
     * Close this drop-down
     *
     * @return close mode with the target parent instance
     */
    CloseMode<P> close();
}
