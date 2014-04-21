/**
 * Provides autocomplete for username mentions in textareas.
 *
 * @deprecated
 * @constructor AJS.LegacyMention
 * @extends AJS.Control
 */
JIRA.LegacyMention = AJS.Control.extend({

    CLASS_SIGNATURE: "AJS_MENTION",

    listening: false,

    lastInvalidUsername: "",

    lastRequestMatch: true,

    lastValidUsername: "",

    init: function () {

        var instance = this;

        this.$suggestions = jQuery("<div />");

        this.contentRetreiver = new AJS.AjaxContentRetriever(function () {
            var params = instance._getQueryParams();
            return {
                url: contextPath + "/rest/api/2/user/viewissue/search",
                data: params,
                formatSuccess: function (data) {
                    return instance.generateSuggestions(data);
                },
                cache: false,
                requestDelay: 100
            }
        });

        this.listController = new AJS.MentionGroup();
    },

    _getQueryParams: function() {
        var queryParams = this.restParams;

        queryParams.username = this._getCurrentUserName();

        return queryParams;
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

            this.layerController = new AJS.InlineLayer({
                offsetTarget: this.textarea(),
                contentRetriever: this.contentRetreiver,
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
                    // Focus first item in list
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
    generateSuggestions: function (data) {

        var instance = this,
            $suggestions,
            username = this._getCurrentUserName(),
            filteredData = new Array();

        if (!username) {
            return;
        }

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

        this.listController.removeAllItems();

        for (var i = 0; i < data.length; i++) {
            if (instance._checkStringStartsWith(data[i].name,username) ||
                    instance._checkStringStartsWith(data[i].displayName,username)) {
                data[i].username = data[i].name;
                data[i].emailAddress = highlight(data[i].emailAddress);
                data[i].displayName = highlight(data[i].displayName);
                data[i].name = highlight(data[i].name);
                filteredData.push(data[i]);
            }
        }

        $suggestions =  AJS.$(JIRA.Legacy.Templates.mentionsSuggestions({
            suggestions: filteredData,
            hasSuggestions: filteredData && filteredData.length
        }));

        $suggestions.find("li").each(function () {
            var li = AJS.$(this);
            li.click(function(event) {
                instance._acceptSuggestion(li);
                instance.listening = false;
                event.preventDefault();
            });

            instance.listController.addItem(new AJS.Dropdown.ListItem({
                element: this,
                autoScroll: true
            }));
        });

        if (filteredData && filteredData.length === 0)   {
            if ((!instance.lastRequestMatch) ||
                (instance.lastInvalidUsername.length === 0 || username.length <= instance.lastInvalidUsername.length)) {
                instance.lastInvalidUsername = username;

            }
            instance.lastRequestMatch = false;
        }
        else {
            instance.lastValidUsername = username;
            instance.lastRequestMatch = true;
        }

        this.listController.prepareForInput();

        return $suggestions;
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
    fetchUserNames: function() {
        if (this.layerController.isVisible()) {
            this.layerController.refreshContent();
        } else {
            this._show(); // will request content also
        }
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

    onEdit: function () {
        if (this.listening) {
            var instance = this;
            this.$textarea.one("keyup", function(event) {
                instance._keyUp(event);
            });
        } else {
            this._hide();
        }
    },

    /**
     * Key up listener.
     *
     * Double check for listening as there could be a
     */
    _keyUp: function() {
        if (this.listening) {
            var username = this._getUserNameFromInput(),
                instance = this;
            if (instance._isNewRequestRequired(username)) {
                   instance.fetchUserNames(username);
            } else if (!instance._keepSuggestWindowOpen()) {
                this._hide();
            }
        }
    },


    /**
     *  Checks if suggest window should be open
     * @return {Boolean}
     */
    _keepSuggestWindowOpen: function() {
        var instance = this;
        if (this.layerController.isVisible()) {
           return instance.lastRequestMatch && instance._isTrailingWhitespaceNotGreaterThan(10);
        }
        return false;
    },

    /**
     * Checks if whitespace at the end of username is not longer than limit
     * @param limit - number of witespace characters
     * @return {Boolean}
     */
    _isTrailingWhitespaceNotGreaterThan: function(limit) {
        var instance = this;
        var username = instance._getUserNameFromInput();
        var trimmedUsername = jQuery.trim(instance.lastValidUsername);

        var result = (username || "").split(trimmedUsername);
        return (result.length > 1) ? result[1].length < limit : true;
    },

    /**
     * Checks if server pool for user names is needed
     * @param username
     * @return {Boolean}
     */
    _isNewRequestRequired:function (username) {
        var instance = this;
        if (username && username.length > 0) {
            if (jQuery.trim(username) === instance.lastValidUsername) {
                return false;
            }
             if (instance.lastInvalidUsername && instance.lastInvalidUsername.length > 0) {
                return (username.match(new RegExp("^" + RegExp.escape(instance.lastInvalidUsername))) === null);
            }
        } else {
            return false;
        }
        return true;
    },

    _checkStringStartsWith: function(text,startsWith) {
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
    _getUserNameFromInput: function() {
        this.currentUserName = JIRA.Mention.Matcher.getUserNameFromCurrentWord(this.$textarea.val(), this._getCaretPosition());

        return this.currentUserName;
    },

    /**
     * Determines whether the input should be listened to, selected and used as a search keyword.
     *
     * Return, Space and ] all prevent listening (when a user finishes entering a username or selects an autocompleted
     * option)
     *
     * @ and [ commence listening only if the current word is the first word or is preceeded by a space (to prevent
     * email addresses being matched)
     *
     * Attempt to determine whether the username will be empty when this event has taken place. This prevents other
     * aui:keypress / aui:keydown events from picking up the old username value (we set the new value on keyup).
     *
     * @param event The keypress/down event.
     */
    _setListeningState: function(event) {
        var key = event.key,
            username = this._getCurrentUserName();
        if (username && username.length === 1 && key === "Backspace") {
            this._hide();
        }
        if (this.listening && (key === "Return" ||  key === "]")) {
            this.listening = false;
        } else if (!this.listening && key === "@" || key === "[") {
            var caretPos = this._getCaretPosition(),
                prevChar = this.$textarea.val().charAt(caretPos - 1);

            if (prevChar === "" || prevChar === " " || prevChar === "\n") {
                this.listening = true;
            }
        }
    },

    _events: {
        textarea: {
            "aui:keydown aui:keypress": function (event) {
                this._setListeningState(event);
                this._handleKeyEvent(event);
            },


            /**
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
                if (e.keyCode === jQuery.ui.keyCode.ESCAPE) {
                    if (this.layerController.isVisible()) {
                        this.listening = false;

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

            /**
             * Prevents a bug where another inline layer will focus on comment textarea when
             * an item in it is selected (quick admin search).
             */
            "blur": function() {
                this.listController.removeAllItems();
            }
        }
    }
});

/**
 * @deprecated
 */
JIRA.LegacyMention.Matcher = {

    AT_USERNAME_START_REGEX: /^@(.+)/i,
    AT_USERNAME_REGEX: /[^\[]@(.+)/i,
    WIKI_MARKUP_REGEX: /\[[~@]+([^~@]+)/i,
    ACCEPTED_USER_REGEX: /\[~[^~\]]+\]/i,

    getUserNameFromCurrentWord: function(text, caretPosition) {
        var before = text.substr(0, caretPosition),
            lastWordStartIndex = this.getLastWordBoundaryIndex(before, false);

        var currentWord = this._removeAcceptedUsernames(before.substr(lastWordStartIndex));

        var foundMatch;
        jQuery.each([this.AT_USERNAME_START_REGEX, this.AT_USERNAME_REGEX, this.WIKI_MARKUP_REGEX], function(i, regex) {
            var match = regex.exec(currentWord);
            if (match) {
                foundMatch = match[1];
                return false;
            }
        });
        return foundMatch || false;
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
