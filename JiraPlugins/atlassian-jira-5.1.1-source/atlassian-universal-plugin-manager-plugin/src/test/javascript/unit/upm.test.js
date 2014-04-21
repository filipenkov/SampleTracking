
function UpmTest(name)
{
    TestCase.call(this, name);
}

UpmTest.prototype = new TestCase();
UpmTest.glue();

UpmTest.prototype.setUp = function() {};

UpmTest.prototype.tearDown = function() {};

function UpmTest_testJqueryIsPresent()
{
    this.assertNotNull(AJS.$);
}
