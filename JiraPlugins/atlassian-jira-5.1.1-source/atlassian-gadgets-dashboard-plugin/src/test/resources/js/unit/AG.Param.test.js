
function ParamTest(name)
{
    TestCase.call(this, name);
}

ParamTest.prototype = new TestCase();
ParamTest.glue();

function ParamTest_testStandardMessage()
{
    AG.param.set("myTestMessage", "I am a test message");
    this.assertEquals(AG.param.get("myTestMessage"), "I am a test message");

    AG.param.set({
        foo: "This is foo message",
        bar: "This is bar message"
    });

    this.assertEquals(AG.param.get("foo"), "This is foo message");
    this.assertEquals(AG.param.get("bar"), "This is bar message");
}

function ParamTest_testMessageWithParameters()
{
    AG.param.set("foobar", "I am {0} message. I am a {1} message");
    this.assertEquals(AG.param.get("foobar", "foo", "bar"), "I am foo message. I am a bar message");
}

function ParamTest_testNullParamValue()
{
    // test AG-1026 (null param shouldn't take previous param's value)
    AG.param.set("bar", "bar message");
    AG.param.set("foo", null);
    this.assertNull(AG.param.get("foo"));
}

function ParamTest_testNullParamKey()
{
    AG.param.set(null, "bar message");
    this.assertNull(AG.param.get("null"));
    this.assertNull(AG.param.get(null));
}

function ParamTest_testNonExistingParamKey()
{
    this.assertNull(AG.param.get("nonexistingkey"));
}

