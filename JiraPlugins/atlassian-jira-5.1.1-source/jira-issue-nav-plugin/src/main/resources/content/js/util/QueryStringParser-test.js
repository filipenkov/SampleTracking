AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");

module("JIRA.Issues.QueryStringParser");

test("QueryStringParser", function() {

    var parse = function(qs) { return JIRA.Issues.QueryStringParser.parse(qs); }

    deepEqual(parse(), {});
    deepEqual(parse(""), {});
    deepEqual(parse("a=b"), { a: "b" });
    deepEqual(parse("?a=b"), { a: "b" });
    deepEqual(parse("a=b&c=d"), { a: "b", c: "d" });
    deepEqual(parse("?a=b&c=d"), { a: "b", c: "d" });

    deepEqual(parse("?q=i%20like%20stuff"), { q: "i like stuff" });

    deepEqual(parse("?q%20uack=duck"), { "q uack": "duck" });
});
