AJS.test.require("com.atlassian.jira.jira-project-config-plugin:project-config-global");


JIRA.FormDialog =function () {};
JIRA.FormDialog.prototype.show = function () {};

function getGenericRestfulTable (options) {

    var entryArr = [];

    options = options || {};

    for (var i=0; i < options.entries; i++) {
        entryArr.push({id: Math.random()});
    }

    var modifiedOptions = {
        editable: typeof options.editable !== "undefined" ? options.editable : true,
        entries: entryArr
    };

    return new JIRA.RestfulTable({
        el: jQuery("<table><tbody></tbody></table>").appendTo("body"),
        editable: modifiedOptions.editable,
        views: {
            editRow: JIRA.RestfulTable.EditRow,
            row: JIRA.RestfulTable.Row
        },
        entries: modifiedOptions.entries
    });
}

function getGenericRestfulTableRow (options) {
    return new JIRA.RestfulTable.Row({
        el: jQuery("<div />"),
        model: new JIRA.RestfulTable.EntryModel({})
    });
}

module("JIRA.RestfulTable");

test("Construction", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 2
    });

    ok(restfulTable.getCreateRow().hasFocus(), "Expected create row to have focus");
    equals(restfulTable.getModels().length, 2, "Expected 2 models");
    equals(restfulTable.getTableBody().find(".jira-restfultable-row").length, 2);

    restfulTable = getGenericRestfulTable({
        entries: 2,
        editable: false
    });

    ok(!restfulTable.getCreateRow(), "Expected no create row");
});

test("New model instances are of type options.model", function () {

    var MyModel = JIRA.RestfulTable.EntryModel.extend({});

    var restfulTable = new JIRA.RestfulTable({
        el: jQuery("<table><tbody></tbody></table>"),
        model: MyModel,
        editable: true,
        views: {
            editRow: JIRA.RestfulTable.EditRow,
            row: JIRA.RestfulTable.Row
        },
        entries: [{}]
    });

    ok(restfulTable.getModels().at(0) instanceof MyModel, "Expected options.entries items to be added as provided model type");

    restfulTable.addRow({id: 1000});

    ok(restfulTable.getModels().at(1) instanceof MyModel, "Expected when adding a new model it to be provided model type");

});


test("JRADEV-5399 - When in modal mode. Edit rows are still modeless", function () {

    var editRowModal = false;

    var editRow = JIRA.RestfulTable.EditRow.extend({

        _cancel: function () {
            editRowModal = true;
        }

    });

    var $table = jQuery("<table><tbody></tbody></table>").appendTo("body");

    var restfulTable = new JIRA.RestfulTable({
        el: $table,
        editable: true,
        views: {
            editRow: editRow,
            row: JIRA.RestfulTable.Row
        },
        entries: [{}]
    });

    var row = restfulTable.getRows()[0];


    row.trigger("edit");
    row.trigger("modal");

    row.$(".cancel").click();

    ok(editRowModal, "expected edit row not to be modal");

    // cleanup
    restfulTable.getTable().remove();

});

test("row removal", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 2
    });

    var row = restfulTable.getRows()[1];

    restfulTable.getModels().remove(row.model);

    equals(restfulTable.getRows().length, 1, "Expected if I remove model from collection for it also to be removed from view");
    equals(row.el.parentNode, null, "Expected node to be remove from DOM");

    row = restfulTable.getRows()[0];

    restfulTable._removeRow(row);

    equals(restfulTable.getRows().length, 0, "Expected if I remove model from collection for it also to be removed from view");
    equals(row.el.parentNode, null, "Expected node to be remove from DOM");

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 1,
            "Expected no entries message to be appended to table body");
});

test("adding entry", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 0
    });

    restfulTable.addRow({id: 1000}, 0)

});

test("isEmpty", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 2
    });

    equals(restfulTable.isEmpty(), false);

    restfulTable = getGenericRestfulTable({
        entries: 0
    });

    equals(restfulTable.isEmpty(), true);
});


test("showNoEntriesMessage", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 0
    });

    equals(restfulTable.showNoEntriesMsg(), restfulTable, "Expected instance to be returned");

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 1,
            "Expected no entries message to be appended to table body");

    restfulTable.showNoEntriesMsg();

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 1,
            "Expected no duplicate messages to be appended to table body");

});

test("removeNoEntriesMsg", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 0
    });

    equals(restfulTable.removeNoEntriesMsg(), restfulTable, "Expected instance to be returned");

    restfulTable.showNoEntriesMsg();

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 1,
            "Expected no duplicate messages to be appended to table body");

    restfulTable.removeNoEntriesMsg();

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 1,
            "Expected no duplicate messages NOT to be removed from table body");

    restfulTable.addRow({id: 1000}, 1);

    restfulTable.removeNoEntriesMsg();

    equals(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length, 0,
            "Expected no duplicate messages to be removed from table body");
});

test("_renderRow", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 0,
        editable: false
    });

    restfulTable.getModels().add({}); // prevent no entries message


    ok(restfulTable._renderRow(new Backbone.Model()) instanceof jQuery, "Expected jQuery element to be returned");

    equals(restfulTable.getTableBody().find(".jira-restfultable-row").length, 1, "Expected row to be added");

    restfulTable._renderRow(new Backbone.Model());
    equals(restfulTable.getTableBody().find(".jira-restfultable-row").length, 2, "Expected row another row be added");


    var $row = restfulTable._renderRow(new Backbone.Model(), 0).addClass("myRow");

    console.log($row.parent());

    equals(restfulTable.getTableBody().find("tr:eq(0)")[0], $row[0], "Expected table row to be inserted at index 0");

    $row = restfulTable._renderRow(new Backbone.Model(), 2);

    equals(restfulTable.getTableBody().find("tr:eq(2)")[0], $row[0], "Expected table row to be inserted at index 2");
});

test("edit", function () {

    var savedCalled;
    var restfulTable = getGenericRestfulTable({
        entries: 3
    });
    var row = getGenericRestfulTableRow();
    var editRow = restfulTable.edit(row);

    ok(editRow instanceof JIRA.RestfulTable.EditRow, "Expected to be returned instance of [JIRA.RestfulTable.EditRow]");
    equals(editRow.el, row.el, "Expected edit row el to render to same element");
    ok(editRow.hasFocus(), "Expected edit row to have focus");

    editRow.hasUpdates = function () {
        return true;
    };

    editRow.bind("save", function () {
        savedCalled = true
    });

    editRow.trigger("blur");

    ok(savedCalled, "Expected blur event to call save event");

    editRow.hasUpdates = function () {
        return false;
    };

    savedCalled = false;

    editRow.trigger("blur");

    ok(!savedCalled, "Expected blur event to NOT call save event, there are no changes");

});

test("edit: test focus after update", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 3
    });
    var row = getGenericRestfulTableRow();
    var editRow = restfulTable.edit(row);

    editRow.trigger("updated");
    ok(restfulTable.getCreateRow().hasFocus(), "Expected create row to have focus");
});




// tests editing of other rows, while waiting for long running requests to come back
test("concurrent edit", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 3
    });

    var row1 = getGenericRestfulTableRow();
    var row2 = getGenericRestfulTableRow();
    var row3 = getGenericRestfulTableRow();

    var editRow1 = restfulTable.edit(row1);

    editRow1.hasUpdates = function () {
        return true;
    };

    editRow1.submit = function () {}; // stop submission, so we can control what & when the response comes back from server

    var editRow2 = restfulTable.edit(row2);

    editRow2.submit = function () {}; // stop submission, so we can control what & when the response comes back from server

    editRow2.hasUpdates = function () {
        return true;
    };

    var editRow3 = restfulTable.edit(row3);

    editRow3.hasUpdates = function () {
        return true;
    };

    editRow3.submit = function () {}; // stop submission, so we can control what & when the response comes back from server

    equals(restfulTable.editRows.length, 3, "Expected 3 rows to be editable");

    editRow1.trigger("validationError");
    editRow2.trigger("updated");
    editRow3.trigger("validationError");

    equals(restfulTable.editRows.length, 2, "Expected 2 rows to be editable");

    ok(!editRow1.hasFocus(), "Expected first row with validation error NOT to have focus");
    ok(editRow3.hasFocus(), "Expected last row with validation error to have focus");

    editRow3.trigger("cancel");

    ok(editRow1.hasFocus(), "Expected first row to have focus");

    editRow1.trigger("updated");

    ok(!editRow1.hasFocus(), "Expected first row with validation error NOT to have focus");

    ok(restfulTable.getCreateRow().hasFocus(), "Expected create row to have focus");
});

test("edit: test focus after cancel", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 3
    });
    var row = getGenericRestfulTableRow();
    var editRow = restfulTable.edit(row);

    editRow.trigger("cancel");
    ok(!row.hasFocus(), "Expected row to have focus");
});

test("edit: test focus after blurring", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 3
    });
    var row = getGenericRestfulTableRow();
    var editRow = restfulTable.edit(row);

    restfulTable.getCreateRow().trigger("focus");

    ok(restfulTable.getCreateRow().hasFocus(), "Expected create row to have focus");
    ok(!editRow.hasFocus(), "Expected edit row NOT to have focus");
    ok(!row.hasFocus(), "Expected row to have focus");
});

test("renderRows", function () {

    var restfulTable = getGenericRestfulTable({
        entries: 0
    });

    ok(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length === 1,
            "Expected no entries message to be appended when there are no entries");

    restfulTable.renderRows([{id: Math.random()}]);

    ok(restfulTable.getTableBody().find(".jira-restfultable-no-entires").length === 0,
            "Expected no entries message to be appended when there are no entries");


    restfulTable = getGenericRestfulTable({
        editable: false,
        entries: 0
    });

    restfulTable.renderRows([{id: Math.random()}, [{id: Math.random()}], [{id: Math.random()}]]);

    equals(restfulTable.getTableBody().find("tr").length, 3, "Expected 3 rows to be added");


    restfulTable.renderRows([{id: Math.random()}, [{id: Math.random()}], [{id: Math.random()}]]);

    equals(restfulTable.getTableBody().find("tr").length, 6, "Expected 6 rows to be added");
});

module("JIRA.RestfulTable.EntryModel");

test("save", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        syncArgs;

    Backbone.sync = function (method, model, success, error) {
        syncArgs = arguments;

        if (success) {
            success(model, {});
        }
    };

    // test model without id use create

    var model = new Model();

    model.save({val : "b"});

    equals(syncArgs[0], "create", "Expected to be [create] for model without id");

    // test model with id use update (PUT)

    model = new Model({
        id: 1000
    });

    model.save({val : "b"});

    equals(syncArgs[0], "update", "Expected to be [update]");

    // test only going to server for updated results
    var syncCalled = false;

    Backbone.sync = function (method, model, success, error) {
        syncCalled = true;
    };

    model.save({val : "c"});

    ok(syncCalled, "Expected to sync value as it HAS changed");


    // test generic error handling

    Backbone.sync = function (method, model, success, error) {
        if (error) {
            error({status: 500, responseText: "{}"});
        }
    };

    var serverErrorEventFired = false;

    jQuery(document).bind(JIRA.SERVER_ERROR_EVENT, function () {
         serverErrorEventFired = true;
    });



    model.save({fd : "a"});

    ok(serverErrorEventFired, "Expected server error to be fired");

});

test("fetch", function () {

    var model = new (JIRA.RestfulTable.EntryModel.extend({
        url: function () {
            return "/blah";
        }
    }));
    var customErrorCallbackCalled = false;
    var serverErrorEventFired = false;

    Backbone.sync = function (method, model, success, error) {

        if (error) {
            error({status: 500, responseText: "{}"});
        }
    };

    jQuery(document).bind(JIRA.SERVER_ERROR_EVENT, function () {
         serverErrorEventFired = true;
    });

    model.fetch({
        error: function () {
            customErrorCallbackCalled = true;
        }
    });

    ok(serverErrorEventFired, "Expected server error to be fired");
    ok(customErrorCallbackCalled, "Expected custom error handler to be called");

});

module("JIRA.RestfulTable.Row");

test("events", function () {

    var row = new JIRA.RestfulTable.Row();


    row.trigger("focus");

    ok(row.$el.hasClass("jira-restfultable-focused"), "expected row to have focus when focus event called");

    row.trigger("blur");

    ok(!row.$el.hasClass("jira-restfultable-focused"), "expected row NOT to have focus when blur event called");

    row.trigger("modal");

    ok(row.$el.hasClass("jira-restfultable-active"), "expected row to be in modal mode when modal event fired");

    row.trigger("modeless");

    ok(!row.$el.hasClass("jira-restfultable-active"), "expected row NOT to be in modal mode when modeless event fired");

    row.trigger("updated");

    ok(row.$el.hasClass("jira-restfultable-animate"), "expected row to show updated when updated event fired");
});

test("sync", function () {

    var RowClass = JIRA.RestfulTable.Row.extend({
        render: function () {
            this.el.innerHTML = "test";
        }
    });

    var row = new RowClass({
        el: jQuery("<div />"),
        model: new JIRA.RestfulTable.EntryModel({})
    });


    Backbone.sync = function (method, model, success) {
        if (success) {
            success(model, {});
        }
    };

    row.sync({test: "a"});

    ok(row.el.innerHTML == "test", "Expected sync to render view when successful");

    row.el.innerHTML = "";

    Backbone.sync = function (method, model, success, error) {
        error({status: 500, responseText: "{}"});
    };

    row.sync({blah: "a"});

    ok(row.el.innerHTML != "test", "Expected sync to NOT to render view when error");
});


test("refresh", function () {

    var RowClass = JIRA.RestfulTable.Row.extend({
        render: function () {
            this.el.innerHTML = "test";
        }
    });

    var row = new RowClass({
        el: jQuery("<div />"),
        model: new JIRA.RestfulTable.EntryModel({})
    });


    Backbone.sync = function (method, model, success) {
        if (success) {
            success(model, {});
        }
    };

    row.refresh();

    ok(row.el.innerHTML == "test", "Expected sync to render view when successful");

    row.el.innerHTML = "";

    Backbone.sync = function (method, model, success, error) {
        error({status: 500, responseText: "{}"});
    };

    row.refresh();

    ok(row.el.innerHTML != "test", "Expected sync to NOT to render view when error");
});

test("hasFocus", function () {

    var row = new JIRA.RestfulTable.Row();

    ok(!row.hasFocus(), "Expected row not to have focus my default");

    row.trigger("focus");

    ok(row.hasFocus(), "Epected row to have focus");

    row.trigger("blur");

    ok(!row.hasFocus(), "Epected row NOT to have focus");
});

test("focus & unfocus", function () {

    var row = new JIRA.RestfulTable.Row();

    row.focus();

    ok(row.$el.hasClass("jira-restfultable-focused"), "expected row to have focused class");

    row.unfocus();

    ok(!row.$el.hasClass("jira-restfultable-focused"), "expected row not to have focused class");
});


test("showLoading & hideLoading", function () {
    var row = new JIRA.RestfulTable.Row();

    row.showLoading();

    ok(row.$el.hasClass("loading"), "expected row to have loading class");

    row.hideLoading();

    ok(!row.$el.hasClass("loading"), "expected row not to have loading class");
});

test("edit", function() {

    var eventCalled = false,
        row = new JIRA.RestfulTable.Row();

    row.bind("edit", function () {
        eventCalled = true;
    });

    row.edit({
        target: document.createElement("span")
    });

    ok(eventCalled, "Expected edit event to be called");

});

module("JIRA.RestfulTable.EditRow");

test("constructor (update vs created mode)", function () {


    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        editRow = new JIRA.RestfulTable.EditRow({
            model: Model
        });

    ok(!editRow.isUpdateMode, "Expected to be in create mode");
    ok(_.isEmpty(editRow.model.toJSON()), "Expected an empty model to be created");


    var model = new Model({
        myVal: "scott"
    });

    editRow = new JIRA.RestfulTable.EditRow({
        isUpdateMode: true,
        model: model
    });


    ok(editRow.isUpdateMode, "Expected to be in update mode");
    equals(editRow.model.toJSON()["myVal"], "scott", "Expected my supplied model to be set on view");
});

test("constuctor (events)", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        editRow = new JIRA.RestfulTable.EditRow({
            model: Model
        });

    editRow.trigger("cancel");

    ok(editRow.disabled === true, "Expected edit row to be disabled");

    editRow.trigger("focus");

    ok(editRow.$el.hasClass("jira-restfultable-focused"), "expected row to have focus when focus event called");

    editRow.trigger("blur");

    ok(!editRow.$el.hasClass("jira-restfultable-focused"), "expected row NOT to have focus when blur event called");

});

test("hasFocus", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        row = new JIRA.RestfulTable.EditRow({
            model: Model
        });

    ok(!row.hasFocus(), "Expected row not to have focus my default");

    row.trigger("focus");

    ok(row.hasFocus(), "Epected row to have focus");

    row.trigger("blur");

    ok(!row.hasFocus(), "Epected row NOT to have focus");
});

test("focus", function () {

    var el = jQuery("<tr><td><input name='first' /></td><td><input /></td><td><input /></td></tr>").prependTo("body");

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        row = new JIRA.RestfulTable.EditRow({
            el: el,
            model: Model
        });

    row.focus();

    equals(el.find("input")[0], document.activeElement, "Expected first input to be focused");

    row.focus("first");

    equals(el.find("input")[0], document.activeElement, "Expected input specified in argument to be focused");

    el.css({
        position: "absolute",
        top: 8000
    });

    jQuery(document.activeElement).blur();

    jQuery(window).scrollTop = 0;

    row.focus("first");

    equals(document.activeElement, document.body, "Expected input element not to be focused as it is out of the viewport");
});


test("unfocus", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        row = new JIRA.RestfulTable.EditRow({
            model: Model
        });

    row.$el.addClass("jira-restfultable-focused");

    row.unfocus();

    ok(row.$el.hasClass("jira-resfultable-disabled"), "Expected row to be disabled on unfocus");
    ok(!row.$el.hasClass("jira-restfultable-focused"), "Expected row not to have focus");
});

test("disable & enable", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        row = new JIRA.RestfulTable.EditRow({
            el: jQuery("<tr><td><input /></td></tr>"),
            model: Model
        });

    row.disable();

    equals(row.$el.hasClass("jira-resfultable-disabled"), true, "Expected inputs to be disabled");

    row.enable();

    ok(!row.$el.hasClass("jira-resfultable-disabled"), "Expected inputs to be disabled");
});

test("showLoading & hideLoading", function () {

    var Model = JIRA.RestfulTable.EntryModel.extend({
            url: function () {
                return "/blah";
            }
        }),
        row = new JIRA.RestfulTable.EditRow({
            model: Model
        });

    row.showLoading();

    ok(row.$el.hasClass("loading"), "expected row to have loading class");

    row.hideLoading();

    ok(!row.$el.hasClass("loading"), "expected row not to have loading class");
});
