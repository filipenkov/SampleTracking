AJS.test.require("jira.webresources:jira-global");
AJS.test.require("com.atlassian.jira.dev.func-test-plugin:sinon");

(function ($) {

    module("Generic Util Tests", {
        setup: function() {
            this.$element = AJS.$("<input />");
            $("body").append(this.$element);
        },
        teardown: function() {
            this.$element.remove();
            this.$element = null;
        }
    });

    test("jQuery element is focused", function () {
        this.$element.focus();
        ok(AJS.elementIsFocused(this.$element), "jQuery element is focused.");
    });

    test("non jQuery element is focused", function () {
        this.$element.focus();
        ok(AJS.elementIsFocused(this.$element.get(0)), "non jQuery element is focused.");
    });

    test("jQuery element is not focused when blurred", function () {
        this.$element.focus();
        this.$element.blur();
        ok(!AJS.elementIsFocused(this.$element.get(0)), "non jQuery element is focused.");
    });

    (function(){
        module("Test reloadViaWindowLocation", {
            setup: function() {
                var sandbox = this.sandbox = sinon.sandbox.create();
                this.location = {
                    href: "http://localhost/somerandom/url",
                    replace: sandbox.stub(),
                    assign: sandbox.stub()
                };
            },
            teardown: function() {
                this.sandbox.restore();
            }
        });

        test("reloading should replace the URL", function() {
            AJS.reloadViaWindowLocation._delegate(null, this.location);
            ok(this.location.replace.calledWith(this.location.href), "Replaced called with correct URL on reload.");
            ok(!this.location.assign.called, "Assign should not be called.");
        });

        test("redirect should assign the URL", function() {
            var url = "http://somethingelse.com";
            AJS.reloadViaWindowLocation._delegate(url, this.location);
            ok(this.location.assign.calledWith(url), "Assign called with the correct URL.");
            ok(!this.location.replace.called, "Replace should not have been called.");
        });

        function testAddCacheBusterWithHash(url, location, expectedRegex, callfn, nocalledfn){
            AJS.reloadViaWindowLocation._delegate(url, location);
            ok(callfn.calledOnce, "Assign called with the correct URL.");
            var call = callfn.getCall(0);
            if (expectedRegex.test(call.args[0])) {
                ok(true, "Redirected URL matches '" + expectedRegex + "'.");
            } else {
                ok(false, "Redirected URL '" + call.args[0] + "' does not match '" + expectedRegex + "'.");
            }
            ok(!nocalledfn.called, "Replace should not have been called.");
        }

        test("redirect should assign the URL and and add cache buster", function() {
            var url = "http://somethingelse.com#jsks";
            testAddCacheBusterWithHash(url, this.location,
                    /http:\/\/somethingelse\.com\?jwupdated=\d+#jsks/,
                    this.location.assign, this.location.replace);
        });

        test("reload should replace the URL and and add cache buster", function() {
            this.location.href = "http://somethingelse.com#abc";
            testAddCacheBusterWithHash(null, this.location,
                    /http:\/\/somethingelse\.com\?jwupdated=\d+#abc/,
                    this.location.replace, this.location.assign);
        });

        test("reload should replace the URL and and add cache buster with parameter", function() {
            this.location.href = "http://somethingelse.com?jack=two#abc";
            testAddCacheBusterWithHash(null, this.location,
                    /http:\/\/somethingelse\.com\?jwupdated=\d+&jack=two#abc/,
                    this.location.replace, this.location.assign);
        });

        test("redirect should assign the URL and and add cache buster with parameter", function() {
            var url = "http://somethingelse.com?def=abc#jsks";
            testAddCacheBusterWithHash(url, this.location,
                    /http:\/\/somethingelse\.com\?jwupdated=\d+&def=abc#jsks/,
                    this.location.assign, this.location.replace);
        });

        function testUpdateCacheBusterWithHash(url, location, expectedRegex, oldNumber, callfn, nocalledfn){
            AJS.reloadViaWindowLocation._delegate(url, location);
            ok(callfn.calledOnce, "Called with the correct URL.");
            var call = callfn.getCall(0);
            var match = expectedRegex.exec(call.args[0]);
            if (match) {
                var newBusterNumber = parseInt(match[1]);
                notEqual(newBusterNumber, oldNumber, "Buster number has been updated.");
            } else {
                ok(false, "Redirected URL '" + call.args[0] + "' does not match '" + expectedRegex + "'.");
            }
            ok(!nocalledfn.called, "Did not call other function?");
        }

        test("redirect should assign the URL and and update cache buster", function() {
            var url = "http://somethingelse.com?def=abc&jwupdated=1#jsks";
            testUpdateCacheBusterWithHash(url, this.location,
                    /http:\/\/somethingelse\.com\?def=abc&jwupdated=(\d+)#jsks/, 1,
                    this.location.assign, this.location.replace);
        });

        test("reload should replace the URL and and update cache buster", function() {
            this.location.href = "http://somethingelse.com?def=abc&jwupdated=1#jsks";
            testUpdateCacheBusterWithHash(null, this.location,
                    /http:\/\/somethingelse\.com\?def=abc&jwupdated=(\d+)#jsks/, 1,
                    this.location.replace, this.location.assign);
        });
    })();
})(AJS.$);