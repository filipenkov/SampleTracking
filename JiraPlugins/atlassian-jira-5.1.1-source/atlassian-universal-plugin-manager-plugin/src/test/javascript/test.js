// Init
var basedir = "src/test/javascript/";
var jsBaseDir = "src/main/resources/js/";

// load our libraries
load(basedir + "lib/env.rhino.js");
load(basedir + "lib/JsUtil.js" );
load(basedir + "lib/JsUnit.js" );

// define an instance of JsUtil to be used by all tests
var jsUtil = new JsUtil();

window.onload = function() {
    print("Loading tests.");

    // load all the tests defined in AllTests.js
    load(basedir + "AllTests.js");

    // define the runner
    var runner = new TextTestRunner();

    // we set our result printer (this gives us a more detailed prints)
    runner.setPrinter(new ClassicResultPrinter(runner.getPrinter().getWriter()));

    // execute the runner, passing our test suite
    var result = runner.doRun(AllTests.prototype.suite());

    // quit with statuscode=1 if there are failing tests
    jsUtil.quit(result.wasSuccessful() ? runner.SUCCESS_EXIT : runner.FAILURE_EXIT);
};

window.location = "src/test/resources/index.html";
