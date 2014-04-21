JIRA.MentionUserModel = Backbone.Model.extend({ idAttribute: "name" });

/**
 * Provides autocomplete for username mentions in textareas.
 *
 * @constructor AJS.Mention
 * @extends AJS.Control
 */
JIRA.Mention = AJS.Control.extend({

    CLASS_SIGNATURE: "AJS_MENTION",

    lastInvalidUsername: "",

    lastRequestMatch: true,

    lastValidUsername: "",

    init: function () {
        var instance = this;
        this.listController = new AJS.MentionGroup();

        this.dataSource = new JIRA.ProgressiveDataSet([], {
            model: JIRA.MentionUserModel,
            queryEndpoint: contextPath + "/rest/api/2/user/viewissue/search",
            queryParamKey: "username",
            queryData: _.bind(this._getQueryParams, this)
        });
        this.dataSource.matcher = function(model, query) {
            var matches = false;
            matches = matches || instance._stringPartStartsWith(model.get("name"), query);
            matches = matches || instance._stringPartStartsWith(model.get("displayName"), query);
            return matches;
        };
        this.dataSource.bind('respond', function(response) {
            var results = response.results;
            var username = response.query;

            if (!username) return;

            // Update the state of mentions matches
            if (results.length === 0) {
                if (username.length > 1 && instance.dataSource.hasQueryCache(username)) {
                    if (instance.lastInvalidUsername.length === 0 || username.length <= instance.lastInvalidUsername.length) {
                        instance.lastInvalidUsername = username;
                    }
                }
                instance.lastRequestMatch = false;
            }
            else {
                instance.lastInvalidUsername = "";
                instance.lastValidUsername = username;
                instance.lastRequestMatch = true;
            }

            // Set the results
            var $suggestions = instance.generateSuggestions(results, username);
            if (instance.layerController) {
                instance.layerController.content($suggestions);
                instance.layerController.show();
                instance.layerController.refreshContent();
            }
        });
        this.dataSource.bind('activity', function(response) {
            if (response.activity) {
                instance.layerController._showLoading();
            } else {
                instance.layerController._hideLoading();
            }
        });
    },

    _getQueryParams: function() {
        return this.restParams;
    },

    _setQueryParams: function() {
        var params = {
            issueKey: this.$textarea.attr("data-issuekey"),
            projectKey: this.$textarea.attr("data-projectkey"),
            maxResults: 10
        }

        if (JIRA.Dialog.current && JIRA.Dialog.current.options.id === "create-issue-dialog") {
            delete params.issueKey;
        }

        this.restParams = params;
    },

    textarea: function(textarea) {

        var instance = this;

        if (textarea) {
            this.$textarea = AJS.$(textarea);

            AJS.$("#mentionDropDown").remove();

            this.layerController = new AJS.UncomplicatedInlineLayer({
                offsetTarget: this.textarea(),
                allowDownsize: true,

                /**
                 * Allows for shared object between comment boxes.
                 *
                 * Closure returns the width of the focused comment form.
                 * This comes into effect on the View Issue page where the top and
                 * bottom comment textareas are the same element moved up and down.
                 */
                width: function() {
                    return instance.$textarea.width();
                }
            });

            this.layerController.bind("showLayer", function () {
                    // Binds events to handle list navigation
                    instance.listController.trigger("focus");
                }).bind("hideLayer", function () {
                    // Unbinds events to handle list navigation
                    instance.listController.trigger("blur");
                }).bind("contentChanged", function () {
                    if (!instance.layerController.$content) return;
                    instance.listController.removeAllItems();
                    instance.layerController.$content.find("li").each(function () {
                        var li = AJS.$(this);
                        li.click(function(event) {
                            instance._acceptSuggestion(li);
                            event.preventDefault();
                        });

                        instance.listController.addItem(new AJS.Dropdown.ListItem({
                            element: li,
                            autoScroll: true
                        }));
                    });
                    instance.listController.prepareForInput();
                    instance.listController.shiftFocus(0);
                }).bind("setLayerPosition", function(event, positioning) {
                    if (JIRA.Dialog.current && JIRA.Dialog.current.$form) {
                        var buttonRow = JIRA.Dialog.current.$popup.find(".buttons-container:visible");
                        if (buttonRow.length && positioning.top > buttonRow.offset().top) {
                            positioning.top = buttonRow.offset().top;
                        }
                    }
                });

            this.layerController.layer().attr("id", "mentionDropDown");

            this._assignEvents("textarea", instance.$textarea);

            this._setQueryParams();
        } else {
            return this.$textarea;
        }
    },

    /**
     * Generates autocomplete suggestions for usernames from the server response.
     * @param data The server response.
     */
    generateSuggestions: function (data, username) {
        var regex = new RegExp("(^|.*?(\\s+|\\())(" + RegExp.escape(username) + ")(.*)", "i");

        function highlight(text) {
            var result = {
                text: text
            };
            
            if (text.toLowerCase().indexOf(username.toLowerCase()) > -1) {
                text.replace(regex, function (_, prefix, spaceOrParenthesis, match, suffix) {
                    result = {
                        prefix: prefix,
                        match: match,
                        suffix: suffix
                    };
                });
            }
            return result;
        }

        var filteredData = _.map(data, function(model) {
            var user = model.toJSON();
            user.username = user.name;
            user.emailAddress = highlight(user.emailAddress);
            user.displayName = highlight(user.displayName);
            user.name = highlight(user.name);
            return user;
        });

        return AJS.$(JIRA.Templates.mentionsSuggestions({
            suggestions: filteredData,
            query: username,
            activity: (this.dataSource.activeQueryCount > 0)
        }));
    },

    /**
     * Triggered when a user clicks on or presses enter on a highlighted username entry.
     *
     * The username value is stored in the rel attribute
     *
     * @param li The selected element.
     */
    _acceptSuggestion: function(li) {
        this._hide();
        this._replaceCurrentUserName(li.find("a").attr("rel"));
        this.listController.removeAllItems();
    },

    /**
     * Heavy-handed method to insert the selected user's username.
     *
     * Replaces the keyword used to search for the selected user with the
     * selected user's username.
     *
     * If a user is searched for with wiki-markup, the wiki-markup is replaced
     * with the @format mention.
     *
     * @param selectedUserName The username of the selected user.
     */
    _replaceCurrentUserName: function(selectedUserName) {
        var value = this.$textarea.val(),
            caretPos = this._getCaretPosition(),
            beforeCaret = value.substr(0, caretPos),
            wordStartIndex = JIRA.Mention.Matcher.getLastWordBoundaryIndex(beforeCaret, true);

        this.$textarea.val([value.substr(0, wordStartIndex + 1), "[~", selectedUserName, "]", value.substr(caretPos)].join(""));
        // Add four - For the [~ and the space before.
        this._setCursorPosition((wordStartIndex + 4) + selectedUserName.length);
    },

    /**
     * Sets the cursor position to the specified index.
     *
     * @param index The index to move the cursor to.
     */
    _setCursorPosition: function(index) {
        var input = this.$textarea.get(0);
        if (input.setSelectionRange) {
            input.focus();
            input.setSelectionRange(index, index);
        } else if (input.createTextRange) {
            var range = input.createTextRange();
            range.collapse(true);
            range.moveEnd('character', index);
            range.moveStart('character', index);
            range.select();
        }
    },

    /**
     * Returns the index of the cursor in the textarea.
     *
     * IE method adapted from here:
     *  http://stackoverflow.com/questions/263743/how-to-get-cursor-position-in-textarea/3373056#3373056
     */
    _getCaretPosition: function() {
        var element = this.$textarea.get(0);
        if (typeof element.selectionStart == "number") {
            return element.selectionStart;
        } else if (document.selection && element.createTextRange) {

            var range = document.selection.createRange(),
                start = this.$textarea.val().length;

            if (range && range.parentElement() == element) {
                var len = element.value.length,
                    normalizedValue = element.value.replace(/\r\n/g, "\n"),
                    // Create a working TextRange that lives only in the input
                    textInputRange = element.createTextRange();
                textInputRange.moveToBookmark(range.getBookmark());

                // Check if the start and end of the selection are at the very end
                // of the input, since moveStart/moveEnd doesn't return what we want
                // in those cases
                var endRange = element.createTextRange();
                endRange.collapse(false);

                if (textInputRange.compareEndPoints("StartToEnd", endRange) > -1) {
                    start = len;
                } else {
                    start = -textInputRange.moveStart("character", -len);
                    start += normalizedValue.slice(0, start).split("\n").length - 1;
                }
            }

            return start;
        }
    },

    /**
     * Sets the current username and triggers a content refresh.
     */
    fetchUserNames: function(username) {
        this.dataSource.query(username);
    },

    /**
     * Returns the current username search key.
     */
    _getCurrentUserName: function() {
        return this.currentUserName;
    },

    /**
     * Hides the autocomplete dropdown.
     */
    _hide: function() {
        this.layerController.hide();
    },

    /**
     * Shows the autocomplete dropdown.
     */
    _show: function() {
        this.layerController.show();
    },

    /**
     * Key up listener.
     *
     * Figure out what our input is, then if we need to, get some suggestions.
     */
    _keyUp: function() {
        var caret = this._getCaretPosition();
        var username = this._getUserNameFromInput(caret);
        username = jQuery.trim(username || "");
        if (this._isNewRequestRequired(username)) {
            this.fetchUserNames(username);
        } else if (!this._keepSuggestWindowOpen(username)) {
            this._hide();
        }
        this.lastQuery = username;
        delete this.willCheck;
    },


    /**
     *  Checks if suggest window should be open
     * @return {Boolean}
     */
    _keepSuggestWindowOpen: function(username) {
        if (!username) return false;
        if (this.layerController.isVisible()) {
           return this.dataSource.activeQueryCount || this.lastRequestMatch;
        }
        return false;
    },

    /**
     * Checks if server pool for user names is needed
     * @param username
     * @return {Boolean}
     */
    _isNewRequestRequired:function (username) {
        if (!username || !username.length) {
            return false;
        }
        username = jQuery.trim(username);
        if (username === this.lastQuery) {
            return false;
        } else if (this.lastInvalidUsername) {
            // We use indexOf instead of stringPartStartsWith here, because we want to check the whole input, not parts.
            if (username.indexOf(this.lastInvalidUsername) === 0) {
                return false;
            }
        } else if (!this.lastRequestMatch && username === this.lastValidUsername) {
            return true;
        }

        return true;
    },

    _stringPartStartsWith: function(text,startsWith) {
        text = jQuery.trim(text || "").toLowerCase();
        startsWith = (startsWith || "").toLowerCase();
        var nameParts = text.split(/\s+/);

        if (!text.length || !startsWith.length) return false;
        if (text.indexOf(startsWith) === 0) return true;

        return _.any(nameParts, function(word) {
            return word.indexOf(startsWith) === 0;
        });
    },


    /**
     * Gets the username which the caret is currently next to from the textarea's value.
     *
     * WIKI markup form is matched, and then if nothing is found, @format.
     */
    _getUserNameFromInput: function(caret) {
        if (typeof caret != "number") caret = this._getCaretPosition();
        return this.currentUserName = JIRA.Mention.Matcher.getUserNameFromCurrentWord(this.$textarea.val(), caret);
    },

    _events: {
        textarea: {
            /**
             * Makes a check to update the suggestions after the field's value changes.
             *
             * Prevents the blurring of the field or closure of a dialog when the drop down is visible.
             *
             * Also takes into account IE removing text from an input when escape is pressed.
             *
             * When in a dialog, the general convention is that when escape is pressed when focused on an
             * input the dialog will close immediately rather then just unfocus the input. We follow this convetion
             * here.
             *
             * Please don't hurt me for using stopPropagation.
             *
             * @param e The key down event.
             */
            "keydown": function (e) {
                if (!this.willCheck) {
                    this.willCheck = _.defer(_.bind(this._keyUp, this));
                }

                if (e.keyCode === jQuery.ui.keyCode.ESCAPE) {
                    if (this.layerController.isVisible()) {

                        if (JIRA.Dialog.current) {
                            jQuery(AJS).one("Dialog.beforeHide", function(e) {
                                e.preventDefault();
                            });
                        }

                        this.$textarea.one("keyup", function(keyUpEvent) {
                            if (keyUpEvent.keyCode === jQuery.ui.keyCode.ESCAPE) {
                                keyUpEvent.stopPropagation(); // Prevent unfocusing the input when esc is pressed
                                JIRA.trigger("Mention.afterHide");
                            }
                        });
                    }

                    if (AJS.$.browser.msie) {
                        e.preventDefault();
                    }
                }
            },

            "focus": function() {
                this._keyUp();
            },

            "mouseup": function() {
                this._keyUp();
            },

            /**
             * Prevents a bug where another inline layer will focus on comment textarea when
             * an item in it is selected (quick admin search).
             */
            "blur": function() {
                this.listController.removeAllItems();
                this.lastQuery = this.lastValidUsername = this.lastInvalidUsername = "";
            }
        }
    }
});

JIRA.Mention.Matcher = {

    AT_USERNAME_START_REGEX: /^@(.+)/i,
    AT_USERNAME_REGEX: /[^\[]@(.+)/i,
    WIKI_MARKUP_REGEX: /\[[~@]+([^~@]+)/i,
    ACCEPTED_USER_REGEX: /\[~[^~\]]+\]/i,

    getUserNameFromCurrentWord: function(text, caretPosition) {
        var before = text.substr(0, caretPosition);
        var lastWordStartIndex = this.getLastWordBoundaryIndex(before, false);
        var prevChar = before.charAt(lastWordStartIndex-1);
        var currentWord, foundMatch;

        if (!prevChar.length || !/\w/i.test(prevChar)) {
            currentWord = this._removeAcceptedUsernames(before.substr(lastWordStartIndex));
            if (/[\r\n]/.test(currentWord)) return false;

            jQuery.each([this.AT_USERNAME_START_REGEX, this.AT_USERNAME_REGEX, this.WIKI_MARKUP_REGEX], function(i, regex) {
                var match = regex.exec(currentWord);
                if (match) {
                    foundMatch = match[1];
                    return false;
                }
            });
        }
        return (foundMatch && this.lengthWithinLimit(foundMatch, 3)) ? foundMatch : false;
    },

    lengthWithinLimit: function(input, length) {
        var parts = jQuery.trim(input).split(/\s+/);
        return parts.length <= ~~length;
    },

    getLastWordBoundaryIndex: function(text, strip) {
        var lastAt = text.lastIndexOf("@"),
            lastWiki = text.lastIndexOf("[~");

        if(strip) {
            lastAt = lastAt - 1;
            lastWiki = lastWiki - 1;
        }

        return (lastAt > lastWiki) ? lastAt : lastWiki;
    },

    _removeAcceptedUsernames: function(phrase) {
        var match = this.ACCEPTED_USER_REGEX.exec(phrase);

        if (match) {
            return phrase.split(match)[1];
        } else{
            return phrase;
        }
    }
};
