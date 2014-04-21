// Include all test files
eval(jsUtil.include(basedir + "lib/jquery.js"));
eval(jsUtil.include(basedir + "lib/ajs.js"));
eval(jsUtil.include(jsBaseDir + "html-sanitizer.js"));
eval(jsUtil.include(jsBaseDir + "json.js"));
eval(jsUtil.include(jsBaseDir + "upm.js"));
eval(jsUtil.include(basedir + "unit/upm.test.js"));

/**
 * Constructor
 */
function AllTests()
{
    TestSuite.call(this, "AllTests");
}

/**
 * returns all test classes
 */
function AllTests_suite()
{
    var suite = new AllTests();
    suite.addTestSuite(UpmTest);
    return suite;
}
AllTests.prototype = new TestSuite();
AllTests.prototype.suite = AllTests_suite;
