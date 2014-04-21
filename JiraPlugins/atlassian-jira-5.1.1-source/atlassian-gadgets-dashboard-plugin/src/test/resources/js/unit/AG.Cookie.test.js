
function CookieTest(name)
{
    TestCase.call(this, name);
}

CookieTest.prototype = new TestCase();
CookieTest.glue();

function CookieTest_testValueSaveReadSucceed()
{
    AG.Cookie.save("key1", "value1");
    AG.Cookie.save("key2", "value2");
    this.assertEquals("value1", AG.Cookie.read("key1", "default"));
    this.assertEquals("value2", AG.Cookie.read("key2", "default"));
}

function CookieTest_testChangeSavedValueSucceed()
{
    AG.Cookie.save("key3", "value3");
    this.assertEquals("value3", AG.Cookie.read("key3", "default"));

    AG.Cookie.save("key3", "updatedKey3Value");
    this.assertEquals("updatedKey3Value", AG.Cookie.read("key3", "defaultvalue"));
}

function CookieTest_testValueReadUndefinedCookieGetDefaultValue()
{
    AG.Cookie.save("key4", "value4");
    this.assertEquals("defaultvalue", AG.Cookie.read("undefinedkey", "defaultvalue"));
}

function CookieTest_testEraseDefinedCookieSucceed()
{
    AG.Cookie.save("key5", "value5");
    this.assertEquals("value5", AG.Cookie.read("key5", "defaultvalue"));

    AG.Cookie.erase("key5");
    this.assertEquals("defaultvalue", AG.Cookie.read("key5", "defaultvalue"));
}

