/*global ok */
/*global equals */
/**
 * Tests the Assignee Single Select on the Edit Screen.
 */

// Don't start the test until the control is initialized.
// HACK - should be able to bind on bubbled initialize event from SingleSelect.js ? dT
(function ($) {  $(document).one('ready.single-select.assignee', function (e, control) {

    var field;

    var SUGGESTIONS = 'Suggestions';

    module('Assignee Field Tests', {
        setup: function () {
            field = $('#assignee-field');
            control.clear();
            ok(noSuggestions(), "The dropdown should not be displayed");
        }
    });

    test("Assignee Control should be ready", function () {
        ok(field.length, "The constructed input field should be present");
    });

    test("Down arrow should show Assignee Suggestions", function () {
        control._handleDown();

        checkSuggestions();
    });

    test("Typing d should show Suggestions and Search results", function () {
        // The server should return the first 10 users matching the search term.
        var server = setupFakeServer([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]);

        var query = 'd';
        field.val(query);
        control._handleCharacterInput(true);
        server.respond();

        checkSuggestions([15, 16, 17, 18, 19], query);
        checkSearch([0, 1, 2, 3, 4, 5, 6, 7, 8, 9], query);
    });

    test("Typing d1 should show Suggestions and Search results based on username", function () {
        var server = setupFakeServer([1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19]);

        var query = 'd1';
        field.val(query);
        control._handleCharacterInput(true);
        server.respond();

        checkSuggestions([15, 16, 17, 18, 19], query);
        checkSearch([1, 10, 11, 12, 13, 14], query);
    });

    // JRADEV-7307
    test("Selected item shouldn't be shown in list, but shown after selection removed", function () {
        control._handleDown();
        checkSuggestions([15, 16, 17, 18, 19], '', 8);
        control.selectValue('d19');

        control._handleDown();
        checkSuggestions([15, 16, 17, 18], '', 7);
        control.selectValue('d18');

        control._handleDown();
        checkSuggestions([15, 16, 17, 19], '', 7);
    });

    // JRADEV-7306
    test("Assignee should be set to Automatic if the user clears the field", function () {
        control.clear();
        field.focus();
        var val = $('#assignee').val();
        equals(val, null, "No assignee should be selected");

        field.blur();
        val = $('#assignee').val();
        equals(val && val[0], '-1', "Automatic assignee should be selected");
    });

    // JRADEV-7779
    test("Error message should be displayed if invalid user is entered", function () {
        field.focus();
        field.val("baduser");
        field.blur();
        
        var errorText = field.closest('.field-group').find('.error:visible').text();
        equals(errorText, "User 'baduser' cannot be assigned this issue", "Bad user error message should be displayed");
    });

    function getSuggestionContainer() {
        return $('#assignee-suggestions');
    }

    function noSuggestions() {
        return !getSuggestionContainer().children(':visible').length;
    }

    function getRESTSearchJson(username, displayName, emailAddress) {
        return {
            self: "http://localhost:8090/jira/rest/api/latest/user?username=" + username,
            id: username,
            name: username,
            emailAddress: emailAddress, //"d0@example.com",
            avatarUrls: {
                "16x16": "http://localhost:8090/jira/secure/useravatar?size=small&avatarId=10062",
                "48x48": "http://localhost:8090/jira/secure/useravatar?avatarId=10062"
            },
            displayName: displayName,
            active: true,
            timeZone: "Australia/Sydney"
        };
    }

    function setupFakeServer(fakedIndices) {
        var server = sinon.sandbox.useFakeServer();
        var users = [];
        var username;
        var displayName;
        var emailAddress;
        var devIndex;
        for (var i = 0, len = fakedIndices.length; i < len; i++) {
            devIndex = fakedIndices[i];
            username = 'd' + devIndex;
            displayName = 'Dev ' + devIndex;
            emailAddress = 'ed' + devIndex + '@example.com';
            users.push(getRESTSearchJson(username, displayName, emailAddress));
        }
        server.respondWith(/\/user\/assignable\/search\?/,JSON.stringify(users));

        return server;
    }

    function checkSuggestions(expectedIndices, query, numResults) {
        var suggestionGroup = $('#assignee-group-suggested-view:visible');
        ok(suggestionGroup.length, "There should be a Suggestions section visible");
        equals(suggestionGroup.children('h5').text(), SUGGESTIONS, "The Suggestions section should have the correct heading");

        if (expectedIndices) {

            // There may be more results than just Dev users (e.g. admin, automatic)
            if (typeof numResults === 'undefined') {
                numResults = expectedIndices.length;
            }

            var items = suggestionGroup.find('li.aui-list-item');
            equals(items.length, numResults, "There should be " + numResults + " suggestion items");

            for (var i = 0, ii = expectedIndices.length; i < ii; i++) {
                checkDevItem(items, expectedIndices[i], query);
            }
        }
    }

    function checkSearch(expectedIndices, query) {
        var searchGroup = $('#assignee-group-search-view:visible');
        ok(searchGroup.length, "There should be a Search section visible");
        equals(searchGroup.children('h5').text(), 'All Users', "The Search section should have the correct heading");

        var numResults = expectedIndices.length;

        var items = searchGroup.find('li.aui-list-item');
        equals(items.length, numResults, "There should be " + numResults + " search items");

        for (var i = 0; i < numResults; i++) {
            checkDevItem(items, expectedIndices[i], query);
        }
    }

    function checkDevItem(items, devIndex, query) {
        var itemClass = '.aui-list-item-li-dev-' + devIndex;
        var item = items.filter(itemClass);
        var emailAddress = 'ed' + devIndex + '@example.com';

        // \u00A0 == &nbsp;
        var expectedText = "Dev " + devIndex + " - " + emailAddress + " (d" + devIndex + ")";

        ok(item.length, "Dropdown item should match selector: " + itemClass);

        equals(expectedText, item.text(), "Dropdown item should have correct text: " + expectedText);

        if (query) {
            equals(query, item.find('em').text().toLowerCase(), "Item should have query highlighted");
        }
    }

}); }(AJS.$));
