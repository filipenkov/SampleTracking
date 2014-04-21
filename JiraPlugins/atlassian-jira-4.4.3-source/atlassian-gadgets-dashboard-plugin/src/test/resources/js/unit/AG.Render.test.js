function RenderTest(name)
{
    TestCase.call(this, name);
}

RenderTest.prototype = new TestCase();
RenderTest.glue();

function RenderTest_testInitialize()
{
    var initialized = false;

    AG.render.ready(function() {
        initialized = true;
    });

    // testVar value should not have been changed yet
    this.assertFalse(initialized);

    AG.render.initialize();

    // verify that testVar changes after calling AG.render.initialize()
    this.assertTrue("boolean variable was not initialized", initialized);
}

function RenderTest_testZParseRenderTemplateByDescriptor()
{
    // test AG.render, passing a descriptor
    var item = AG.render({
        id: "item1_id",
        value:"my_name_1",
        useTemplate: "testTemplate"
    });

    this.assertEquals("my_name_1", $(item).find("#item1_id").text());
}

function RenderTest_testZParseRenderTemplateIfElseTag()
{
    var item = AG.render({
        value: "This is the value!",
        render: true,
        useTemplate: "testTemplateIf"
    });

    // check that the value has been rendered when render is true
    this.assertEquals("This is the value!", $(item).find("#test_id").text());

    item = AG.render({
        value: "This is the value!",
        render: false,
        useTemplate: "testTemplateIf"
    });

    // renderer should execute the "else" tag if render is false
    this.assertEquals("default value!", $(item).find("#test_id").text());
}

function RenderTest_testZParseRenderTemplateForTag()
{
    var item = AG.render({
        items: [
            {id: "id1", value: "item1"},
            {id: "id2", value: "item2"},
            {id: "id3", value: "item3"}
        ],
        useTemplate: "testTemplateFor"
    });

    // check that all items was rendered
    this.assertEquals("item1", $(item).find("#id1").text());
    this.assertEquals("item2", $(item).find("#id2").text());
    this.assertEquals("item3", $(item).find("#id3").text());
}

function RenderTest_testZParseRenderTemplateWithMacros()
{
    var item = AG.render({
        items: [
            {
                id: "add-gadget",
                text: "Add Gadget"
            },
            {
                id: "layout-changer",
                text: "Edit Layout"
            }
        ],
        useTemplate: "testTemplateWithMacro"
    });

    // check that all items was rendered
    this.assertEquals("Add Gadget", $(item).find("#add-gadget").text());
    this.assertEquals("Edit Layout", $(item).find("#layout-changer").text());
}

function RenderTest_testZParseRenderTemplateUsingDescriptorName()
{
    // AG.render only supports dashboardMenu, layoutDialog and gadget descriptor name,
    // so I created a template named dashboardMenu to test AG.render's support for
    // descriptor name
    var item = AG.render("dashboardMenu", {
        name: "Dashboard 1000"
    });

    this.assertEquals("Dashboard 1000", $(item).text());
}

