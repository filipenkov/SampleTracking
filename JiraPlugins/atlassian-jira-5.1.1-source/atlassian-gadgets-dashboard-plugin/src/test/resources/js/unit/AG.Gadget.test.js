
// define test suite
module("AG.Gadget");

// define dependencies
var baseDir = "src/test/javascript/";
var jsBaseDir = "src/main/javascript/";
load(
    baseDir + "lib/jquery.js",
    baseDir + "lib/ajs.js",
    baseDir + "lib/ajs.clone.js",
    baseDir + "lib/jquery.aop.js",
    jsBaseDir + "/AG.Param.js",
    jsBaseDir + "/AG.DashboardManager.js",
    jsBaseDir + "/AG.Cookie.js",
    jsBaseDir + "/AG.LayoutManager.js",
    jsBaseDir + "/AG.Gadget.js"
);

AG.render = function () {
    return "<div id=\"gadget-1238-renderbox\" class=\"gadget color1\">\n"
    + "        <div class=\"gadget-menu\">\n"
    + "            <ul>\n"
    + "                <li class=\"aui-dd-parent\">\n"
    + "                    <a class=\"aui-dd-trigger standard \" href=\"#\"><span>Gadget menu</span></a>\n"
    + "                    <ul class=\"aui-dropdown standard hidden\"></ul>\n"
    + "                </li>\n"
    + "            </ul>\n"
    + "        </div>\n"
    + "    </div>";
};

// define tests
test("Initialization", function() {


    AG.Gadget({
        "id":"1238",
        "title":"All Hidden Prefs test",
        "gadgetSpecUrl":"rest/gadgets/1.0/g/com.atlassian.gadgets.samples/prefs/all-hidden-prefs.xml",
        "color":"color1",
        "column":0,
        "colorUrl":"https://dashboard-test.atlassian.com/rest/dashboards/1.0/1/gadget/1238/color",
        "gadgetUrl":"https://dashboard-test.atlassian.com/rest/dashboards/1.0/1/gadget/1238",
        "isMaximizable":false,
        "renderedGadgetUrl":"https://dashboard-test.atlassian.com/plugins/servlet/gadgets/ifr?container=atlassian&amp;mid=1238&amp;country=US&amp;lang=en&amp;view=default&amp;view-params=%7B%22writable%22%3A%22true%22%7D&amp;st=atlassian%3AhwC1eJrW4M9uLNfH57rDpyAGcdZIs5Fcvxx0odts60Y9LkFxac5ySomwsqzoCri52BxFt%2B6%2B4iMvlC83J0NrV1ut1gXhPRE%2B13SMPfI4NbzGX01Qjsr%2BW2%2FXfvWjRyp0hq%2BZG8J11YGE5jvArcd16UPEzZsZWndwMI9iJxdPzhTKwlZEOKh5G7OpaXnHuIxe%2FE7F1JWqeA21mkw5tSVNFPxOScDqEgBqwtFZd0NtN%2BvbjIeW0K5G1cQ92bYwgfDqrtw%2FQOoG4LJfQQz%2FKoQn%2FKbrvy4%3D&amp;up_name=fakename&amp;up_testbool=false&amp;url=https%3A%2F%2Fdashboard-test.atlassian.com%2Frest%2Fgadgets%2F1.0%2Fg%2Fcom.atlassian.gadgets.samples%2Fprefs%2Fall-hidden-prefs.xml#rpctoken=1600457133",
        "hasNonHiddenUserPrefs":false,
        "userPrefs":{
            "action":"https://dashboard-test.atlassian.com/rest/dashboards/1.0/1/gadget/1238/prefs",
            "fields":[
                {"name":"name","value":"fakename","type":"hidden","displayName":"name","required":false},
                {"name":"testbool","value":"false","type":"hidden","displayName":"testbool","required":false}
            ]
        },
        "loaded":true
    });
   
});
