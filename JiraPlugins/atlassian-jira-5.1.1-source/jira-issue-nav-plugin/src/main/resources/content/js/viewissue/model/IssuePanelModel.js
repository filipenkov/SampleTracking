AJS.namespace("JIRA.Issues.IssuePanelModel");

JIRA.Issues.IssuePanelModel = JIRA.Issues.BaseModel.extend({

    properties: [
    /**
     * @type Object
     */
        "entity",
    /**
     * Prevent panel from being updated
     * @type Boolean
     */
        "updateLocked"
    ],
    namedEvents: [
    /**
     * Triggered when a panel entity has been updated
     */
        "updated"
    ],

    initialize: function () {
        // Allowing panels to opt out of refreshing (for example a comment might be half written, we don't want to replace it.)
        JIRA.bind(JIRA.Events.LOCK_PANEL_REFRESHING, _.bind(function (e, id) {
            if (id === this.getEntity().id) {
                this.setUpdateLocked(true);
            }
        }, this));
        JIRA.bind(JIRA.Events.UNLOCK_PANEL_REFRESHING, _.bind(function (e, id) {
            if (id === this.getEntity().id) {
                this.setUpdateLocked(false);
            }
        }, this));
    },

    /**
     * If there are edits [inProgress] then will update all the saved edits with new html. Otherwise if it can
     * will replace the entire panel with the new html
     *
     * @param {jQuery} $new - new html fragment, after save
     * @param {jQuery} $existing - existing html fragment, before save
     * @param {Array} saved - ids of fields that have been saved
     * @param {Array} inProgress - ids of fields that still have edits in progress
     *
     * @return jQuery - updated fragment
     */
    applyUpdates: function ($new, $existing, saved, inProgress) {

        // Replacing panel content can be VERY expensive in ie8 (sometimes 7 seconds) so we only want to update
        // if the content has changed
        if ((AJS.$.browser.msie && parseInt(AJS.$.browser.version, 10) < 9) && $new.text() === $existing.text()) {
            return false;
        }

        var type,
            $updated,
            updates = [];

        var foundInProgress = _.any(inProgress, function (id) {
            return $existing.find(JIRA.Issues.IssueFieldUtil.getFieldSelector(id)).length === 1;
        });
        if (!foundInProgress) {
            $updated = $new;
            type = "replace";
            updates.push($new);
        } else {
            _.each(saved, function (id) {
                var $toReplace = $existing.find(JIRA.Issues.IssueFieldUtil.getFieldSelector(id));
                if ($toReplace.length === 1) {
                    var $replaceWith = $new.find(JIRA.Issues.IssueFieldUtil.getFieldSelector(id));
                    if ($replaceWith.length === 1) {
                        $toReplace.replaceWith($replaceWith);
                        updates.push($replaceWith);
                    } else {
                        // Remove field if it's not present in the new panel
                        // Assumes the field's container in the panel - may not work properly with plugin panels.
                        // TODO: add a class to the container to find it more reliably.
                        $toReplace.closest('li, dl').remove();
                    }
                }
            });
            type = "update";
            $updated = $existing;
        }

        return {
            type: type,
            $existing: $existing,
            $el: $updated,
            updates: updates
        };
    },

    /**
     * Updates panel entity from data
     *
     * @param {Object} entity
     * @param {Object} props
     * ... {Array<String>} fieldsSaved - The update may come as the result of a save. This array includes the ids of any fields that may have been saved before hand.
     * ... {Array<String>} fieldsInProgress - Array of fields that are still in edit mode or still saving.
     * ... {Boolean}initialize - parameter indicating if it is the first time the update has been called.
     */
    update: function (entity, props) {
        if (!this.getUpdateLocked()) {
            this.setEntity(entity);
            this.triggerUpdated(props);
        }

    },

    getPanelId:function() {
        return (this.getEntity().prefix || '') + this.getEntity().id;
    }
});