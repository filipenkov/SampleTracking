AJS.test.require("jira.webresources:jira-global");

QUnit.testStart = function () {
    jQuery("html").attr("className", "");
};


test("Internet Explorer 9 Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))");

    ok(jQuery("html").hasClass("msie"), "has class 'msie'");

    ok(jQuery("html").hasClass("msie-9"), "has class 'msie-9'");
    ok(!jQuery("html").hasClass("msie-8"), "no class 'msie-8'");
    ok(!jQuery("html").hasClass("msie-7"), "no class 'msie-7'");
    ok(!jQuery("html").hasClass("msie-6"), "no class 'msie-6'");

    ok(!jQuery("html").hasClass("msie-gt-9"), "no class 'msie-gt-9'");
    ok(jQuery("html").hasClass("msie-gt-8"), "has class 'msie-gt-8'");
    ok(jQuery("html").hasClass("msie-gt-7"), "has class 'msie-gt-7'");
    ok(jQuery("html").hasClass("msie-gt-6"), "has class 'msie-gt-6'");
    ok(!jQuery("html").hasClass("msie-gt-5"), "no class 'msie-gt-5'");

});


test("Internet Explorer 8 Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; Media Center PC 4.0; SLCC1; .NET CLR 3.0.04320)");

    ok(jQuery("html").hasClass("msie"), "has class 'msie'");

    ok(!jQuery("html").hasClass("msie-9"), "no class 'msie-9'");
    ok(jQuery("html").hasClass("msie-8"), "has class 'msie-8'");
    ok(!jQuery("html").hasClass("msie-7"), "no class 'msie-7'");
    ok(!jQuery("html").hasClass("msie-6"), "no class 'msie-6'");

    ok(!jQuery("html").hasClass("msie-gt-9"), "no class 'msie-gt-9'");
    ok(!jQuery("html").hasClass("msie-gt-8"), "no class 'msie-gt-8'");
    ok(jQuery("html").hasClass("msie-gt-7"), "has class 'msie-gt-7'");
    ok(jQuery("html").hasClass("msie-gt-6"), "has class 'msie-gt-6'");
    ok(!jQuery("html").hasClass("msie-gt-5"), "no class 'msie-gt-5'");

});

test("Internet Explorer 7 Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)");

    ok(jQuery("html").hasClass("msie"), "has class 'msie'");

    ok(!jQuery("html").hasClass("msie-9"), "has class 'msie-9'");
    ok(!jQuery("html").hasClass("msie-8"), "no class 'msie-8'");
    ok(jQuery("html").hasClass("msie-7"), "no class 'msie-7'");
    ok(!jQuery("html").hasClass("msie-6"), "no class 'msie-6'");

    ok(!jQuery("html").hasClass("msie-gt-9"), "no class 'msie-gt-9'");
    ok(!jQuery("html").hasClass("msie-gt-8"), "has class 'msie-gt-8'");
    ok(!jQuery("html").hasClass("msie-gt-7"), "has class 'msie-gt-7'");
    ok(jQuery("html").hasClass("msie-gt-6"), "has class 'msie-gt-6'");
    ok(!jQuery("html").hasClass("msie-gt-5"), "no class 'msie-gt-5'");
});

test("Firefox Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (X11; U; Linux x86_64; it; rv:1.9.0.3) Gecko/2008092813 Gentoo Firefox/3.0.3");
    ok(jQuery("html").hasClass("mozilla"));
    ok(!/-gt-/gi.test(jQuery("html").attr("className")), "Expected no version greater than classes expected (IE only)");
});

test("Safari Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-HK) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5");
    ok(jQuery("html").hasClass("webkit"));
    ok(!/-gt-/gi.test(jQuery("html").attr("className")), "Expected no version greater than classes expected (IE only)");
});

test("Chrome Agent Strings", function () {

    AJS.describeBrowser("Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/540.0 (KHTML, like Gecko) Ubuntu/10.10 Chrome/8.1.0.0 Safari/540.0");
    ok(jQuery("html").hasClass("webkit"));
    ok(!/-gt-/gi.test(jQuery("html").attr("className")), "Expected no version greater than classes expected (IE only)");
});

test("Opera Agent Strings", function () {

    AJS.describeBrowser("Opera/9.99 (Windows NT 5.1; U; pl) Presto/9.9.9");
    ok(jQuery("html").hasClass("opera"));
    ok(!/-gt-/gi.test(jQuery("html").attr("className")), "Expected no version greater than classes expected (IE only)");
});

test("No browser string supplied", function () {
    AJS.describeBrowser();
    ok(/opera|webkit|mozilla|msie/gi.test(jQuery("html").attr("className")), "Expected to fall back to running browser string if not supplied");
});