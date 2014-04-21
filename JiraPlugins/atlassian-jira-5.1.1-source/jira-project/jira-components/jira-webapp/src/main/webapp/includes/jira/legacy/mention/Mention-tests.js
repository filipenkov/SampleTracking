(function($) {

    module("Mentions");

    test("regular expressions", function() {

        function match(text) {
            return JIRA.LegacyMention.Matcher.getUserNameFromCurrentWord(text, text.length);
        }
        ok(!match("@"), "matching @");
        equal(match("@a"), "a", "matching @a");

        ok(!match("["), "matching [");
        ok(!match("[a"), "matching [a");
        ok(!match("[@"), "matching [@");
        equal(match("[@a"), "a", "matching [@a");
        equal(match("[@~"), "~", "matching [@~");
        equal(match("[@~a"), "~a", "matching [@~a");
        ok(!match("[~"), "matching [~");
        equal(match("[~a"), "a", "matching [~a");
        ok(!match("[~@"), "matching [~@");
        equal(match("[~@a"), "a", "matching [~@a");

        equal(match("test@a"), "a", "matching test@a");
        equal(match("test[@a"), "a", "matching test[@a");
        equal(match("test[@~a"), "~a", "matching test[@~a");
        equal(match("test[~a"), "a", "matching test[~a");
        equal(match("test[~@a"), "a", "matching test[~@a");

        equal(match("a test@a"), "a", "matching a test@a");
        equal(match("a test[@a"), "a", "matching a test[@a");
        equal(match("a test[@~a"), "~a", "matching a test[@~a");
        equal(match("a test[~a"), "a", "matching a test[~a");
        equal(match("a test[~@a"), "a", "matching a test[~@a");
    });

    module("_isTrailingWhitespaceNotGreaterThan", {
        setup: function() {
            this.mention = new JIRA.LegacyMention();
            this.mention.textarea(jQuery("<textarea></textarea>")[0]);
            this.mention.lastValidUsername = "";
        },
        pretendToType: function(value) {
            var len = value.length;
            var el = this.mention.textarea().get(0);
            if (el.hasOwnProperty("innerText")) {
                el.innerText = value; // So it'll appear in webkit's console.
            } else {
                this.mention.textarea().val(value);
            }
            this.mention._getCaretPosition = function() { return len; };
        }
    });

    test("true if current input for username is empty", function() {
        this.pretendToType("");
        ok(this.mention._isTrailingWhitespaceNotGreaterThan(10));
    });

    test("true if current input was deleted-by-word", function() {
        this.mention.lastValidUsername = "admin";
        this.pretendToType("@");
        ok(this.mention._isTrailingWhitespaceNotGreaterThan(10));
    });

    test("false when whitespace limit exceeded", function() {
        this.mention.lastValidUsername = "admin";
        this.pretendToType("@admin    ");
        ok(this.mention._isTrailingWhitespaceNotGreaterThan(5), "there's only four spaces after 'admin', which is less than 5");
        ok(!this.mention._isTrailingWhitespaceNotGreaterThan(4), "there are four spaces after 'admin', so it shouldn't be greater than 4");
    });

})(AJS.$);
