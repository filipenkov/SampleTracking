// Include all test files
eval(jsUtil.include(basedir + "lib/jquery.js"));
eval(jsUtil.include(basedir + "lib/ajs.js"));
eval(jsUtil.include(basedir + "lib/ajs.clone.js"));
eval(jsUtil.include(basedir + "lib/jquery.aop.js"));
eval(jsUtil.include(basedir + "lib/zparse.js"));
eval(jsUtil.include(jsBaseDir + "AG.DashboardManager.js"));
eval(jsUtil.include(jsBaseDir + "AG.LayoutManager.js"));
eval(jsUtil.include(jsBaseDir + "AG.Param.js"));
eval(jsUtil.include(jsBaseDir + "AG.Cookie.js"));
eval(jsUtil.include(jsBaseDir + "AG.Render.js"));
eval(jsUtil.include(basedir + "unit/AG.Render.test.js"));
eval(jsUtil.include(basedir + "unit/AG.Param.test.js"));
eval(jsUtil.include(basedir + "unit/AG.Cookie.test.js"));

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
    suite.addTestSuite(RenderTest);
    suite.addTestSuite(ParamTest);
    suite.addTestSuite(CookieTest);
    return suite;
}
AllTests.prototype = new TestSuite();
AllTests.prototype.suite = AllTests_suite;
