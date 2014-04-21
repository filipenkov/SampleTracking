AJS.test.require("jira.webresources:jqlautocomplete");

(function () {

    function getOption (value, selected) {
        return jQuery("<option />")
                .attr("value", value)
                .attr("selected", selected)
                .data("descriptor", new AJS.ItemDescriptor({
                    value: value,
                    label: "Does not matter",
                    selected: selected
                }))
    }


    function getOptGroup (label, items) {

        var groupDescriptor = new AJS.GroupDescriptor({
            label: label
        });

        var optgroup = jQuery("<optgroup />")
                .attr("label", label)
                .data("descriptor", new AJS.GroupDescriptor({
                    label: label
                }));

        jQuery.each(items, function () {
            this.appendTo(optgroup);
            groupDescriptor.addItem(this.data("descriptor"))
        });

        return optgroup;
    }


    test("AJS.ItemDescriptor", function () {

        var option = jQuery("<option />");

        var optionDescriptor = new AJS.ItemDescriptor({
            value: "test",
            title: "I am a test title",
            selected: true,
            label: "I am a test label",
            styleClass: "test-class",
            icon: "url()",
            model: option
        });

        equals(optionDescriptor.value(), "test");
        equals(optionDescriptor.title(), "I am a test title");
        equals(optionDescriptor.label(), "I am a test label");
        equals(optionDescriptor.icon(), "url()");
        equals(optionDescriptor.selected(), true);
        equals(optionDescriptor.styleClass(), "test-class");
        equals(optionDescriptor.model()[0], option[0], "Expected model() to be jQuery wrapped option element");

    });

    test("AJS.GroupDescriptor", function () {

        var optgroup = jQuery("<optgroup />");

        var optgroupDescriptor = new AJS.GroupDescriptor({
            label: "I am a test label",
            weight: 10,
            styleClass: "test-class",
            showLabel: true,
            replace: true,
            description: "I am a test description",
            model: optgroup
        });

        equals(optgroupDescriptor.description(), "I am a test description");
        equals(optgroupDescriptor.label(), "I am a test label");
        equals(optgroupDescriptor.weight(), 10);
        equals(optgroupDescriptor.showLabel(), true);
        equals(optgroupDescriptor.styleClass(), "test-class");
        equals(optgroupDescriptor.model()[0], optgroup[0], "Expected model() to be jQuery wrapped option element");
    });

    test("Setting Selected", function () {


        function getDescriptor (val, label, hasOption) {

            var $option,
                descriptor;

            if (hasOption) {
                $option = jQuery("<option />").attr({
                    value: val,
                    label: label
                });
            }

            descriptor = new AJS.ItemDescriptor({
                value: val,
                label: label,
                model: $option
            });

            if (hasOption) {
                $option.data("descriptor", descriptor);
            }

            return descriptor;
        }

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
            }
        });

        var select = new AJS.MockSelect();

        // Selecting a single element

        var kellySlaterDescriptor = getDescriptor("kelly-slator", "Kelly Slator", true);

        select.$element.append(kellySlaterDescriptor.model());
        select.setSelected(kellySlaterDescriptor);

        ok(kellySlaterDescriptor.model().attr("selected"), "Expected option [Kelly Slator] to be selected");
        ok(kellySlaterDescriptor.selected(), "Expected descriptor [kellySlaterDescriptor] to be selected");



        // Selecting two options

        var kdogDescriptor = getDescriptor("kelly-slator", "k-dog", true);

        select.$element.append(kdogDescriptor.model());
        select.setSelected(kellySlaterDescriptor); // should still set kdog also as they have the same value

        ok(kdogDescriptor.model().attr("selected") && kellySlaterDescriptor.model().attr("selected"), "Expected option [Kelly Slator] and [k-dog] to be selected");
        ok(kdogDescriptor.selected() && kellySlaterDescriptor.selected(), "Expected descriptor [kellySlaterDescriptor] and [kdogDescriptor] to be selected");


        // If an option matching the descriptor does not exist, it should create a new one an select it

        var aiDescriptor = getDescriptor("andy-irons", "Andy Irons", false);

        select.setSelected(aiDescriptor);

        ok(select.$element.find("option:contains(Andy Irons)").length === 1, "Expected option to be appended to <select>");
        ok(select.$element.find("option:contains(Andy Irons)").attr("selected"), "Expected option [Andy Irons] to be selected");

        ok(aiDescriptor.selected(), "Expected descriptor [aiDescriptor] to be selected");
    });

    test("Setting Unselected", function () {

        function getOption (removeOnUnselect) {
            return jQuery("<option />")
                    .attr("value", "foo")
                    .attr("selected", true)
                    .data("descriptor", {
                        value: function () {
                            return "foo"
                        },
                        selected: function() {
                            return true;
                        },
                        removeOnUnSelect: function () {
                            return removeOnUnselect;
                        }
                    })
        }

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption());
                this.$element.append(getOption(true));
            }
        });


        var select = new AJS.MockSelect();

        select.setUnSelected({
            value: function () {
                return "foo"
            }
        });

        ok(!select.$element.find("option").attr("selected"), "Expected option not to be selected");
        equals(select.$element.find("option").length, 1, "Expected option with value removeOnUnselect to be removed from DOM")



    });


    test("Getting All Descriptors", function () {
        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption("1", true));
                this.$element.append(getOption("2", false));
                this.$element.append(getOption("3", false));
                this.$element.append(getOption("4", true));
                this.$element.append(getOptGroup("group1", [
                    getOption("group1-1", true),
                    getOption("group1-2", true),
                    getOption("group1-3", false),
                    getOption("group1-4", false)
                ]));
            }
        });

        var select = new AJS.MockSelect(),
            unselectedDescriptors = select.getAllDescriptors();


        equals(unselectedDescriptors.length, 5, "Expected 5 items (4 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "1", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "2", "Expected [1] to be option 2");
        ok(unselectedDescriptors[2].value() === "3", "Expected [2] to be option 3");
        ok(unselectedDescriptors[3].value() === "4", "Expected [3] to be option 4");
        ok(unselectedDescriptors[4] instanceof AJS.GroupDescriptor, "Expected [4] to be optgroup");
        equals(unselectedDescriptors[4].items().length, 4, "Expected 1 option in optgroup");
        ok(unselectedDescriptors[4].items()[0].value() === "group1-1", "Expected [4][0] to be group1-1");
        ok(unselectedDescriptors[4].items()[1].value() === "group1-2", "Expected [4][1] to be group1-2");
        ok(unselectedDescriptors[4].items()[2].value() === "group1-3", "Expected [4][2] to be group1-3");
        ok(unselectedDescriptors[4].items()[3].value() === "group1-4", "Expected [4][3] to be group1-4");

        unselectedDescriptors = select.getAllDescriptors(false); // without showing groups

        equals(unselectedDescriptors.length, 8, "Expected 3 items (2 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "1", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "2", "Expected [1] to be option 2");
        ok(unselectedDescriptors[2].value() === "3", "Expected [2] to be option 3");
        ok(unselectedDescriptors[3].value() === "4", "Expected [3] to be option 4");
        ok(unselectedDescriptors[4].value() === "group1-1", "Expected [4] to be group1-1");
        ok(unselectedDescriptors[5].value() === "group1-2", "Expected [5] to be group1-2");
        ok(unselectedDescriptors[6].value() === "group1-3", "Expected [6] to be group1-3");
        ok(unselectedDescriptors[7].value() === "group1-4", "Expected [7] to be group1-4");

    });

    test("Getting Unselected Descriptors", function () {

        var copyObjectCalled;

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
                this.$element.append(getOption("1", true));
                this.$element.append(getOption("2", false));
                this.$element.append(getOption("3", false));
                this.$element.append(getOption("4", true));
                this.$element.append(getOptGroup("group1", [
                    getOption("group1-1", true),
                    getOption("group1-2", true),
                    getOption("group1-3", false),
                    getOption("group1-4", false)
                ]));
            }
        });

        var select = new AJS.MockSelect(),
            unselectedDescriptors = select.getUnSelectedDescriptors();

        equals(unselectedDescriptors.length, 3, "Expected 3 items (2 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "2", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "3", "Expected [1] to be option 4");
        ok(unselectedDescriptors[2] instanceof AJS.GroupDescriptor, "Expected [2] to be optgroup");
        equals(unselectedDescriptors[2].items().length, 2, "Expected 1 option in optgroup");
        ok(unselectedDescriptors[2].items()[0].value() === "group1-3", "Expected [2][0] to be group1-3");
        ok(unselectedDescriptors[2].items()[1].value() === "group1-4", "Expected [2][0] to be group1-4");

        unselectedDescriptors = select.getUnSelectedDescriptors(false); // without showing groups

        equals(unselectedDescriptors.length, 4, "Expected 3 items (2 options & 1 optgroup");
        ok(unselectedDescriptors[0].value() === "2", "Expected [0] to be option 1");
        ok(unselectedDescriptors[1].value() === "3", "Expected [1] to be option 4");
        ok(unselectedDescriptors[2].value() === "group1-3", "Expected [3] to be group1-3");
        ok(unselectedDescriptors[3].value() === "group1-4", "Expected [4] to be group1-4");
    });


    test("Parsing &lt;option&gt; to AJS.ItemDescriptor", function () {


        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
            }
        });

        var select = new AJS.MockSelect();

        var $option = jQuery("<option />");

        $option.attr({
            model: $option
        })
        .css({
            backgroundImage: "url(test.png)"
        });

        var optionDescriptor = select._parseOption($option);

        ok(optionDescriptor instanceof AJS.ItemDescriptor, "Expected _parseOption to return AJS.ItemDescriptor");
        equals($option.data("descriptor"), optionDescriptor, "Expected descriptor to be stored on element using jQuery.data");

    });

    test("Appending &lt;option&gt; and &lt;optgroup&gt; from JSON", function () {


        var optionsRendered = 0;
        var optgroupsRendered = 0;

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {};
                this.$element = jQuery("<select multiple='multiple' />");
            },

            _renders: {
                option: function (descriptor) {
                    return jQuery(new Option(descriptor.label(), descriptor.value()));
                },
                optgroup: function (descriptor) {
                    return jQuery("<optgroup />").attr("label", descriptor.label());
                }
            }
        });

        var select = new AJS.MockSelect();

        select.appendOptionsFromJSON([

            new AJS.ItemDescriptor({
                icon: "/jira/images/icons/issue_subtask.gif",
                label: "HSP-2 - test",
                value: "hsp-2"
            }),

            new AJS.ItemDescriptor({
                icon: "/jira/images/icons/issue_subtask.gif",
                value: "hsp-1",
                label: "HSP-1 - I am a iest Issue"
            })
        ]);


        equals(select.$element.find("option").length, 2, "Expected 4 options to be addeed to <select>");

        select.appendOptionsFromJSON([

            new AJS.ItemDescriptor({
                icon: "/jira/images/icons/issue_subtask.gif",
                label: "HSP-2 - test",
                value: "hsp-2"
            }),

            new AJS.ItemDescriptor({
                icon: "/jira/images/icons/issue_subtask.gif",
                label: "HSP-1 - I am a iest Issue",
                value: "hsp-1"
            })
        ]);

        equals(select.$element.find("option").length, 2, "Expected duplicates not to be added");

        select.appendOptionsFromJSON([
            new AJS.GroupDescriptor({
                label: "History Search",
                items: [
                    new AJS.ItemDescriptor({
                       icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-2 - test",
                        value: "hsp-2"
                    }),
                    new AJS.ItemDescriptor({
                        icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-1 - I am a iest Issue",
                        value: "hsp-1"
                    })
                ]
            })
        ]);

        equals(select.$element.find("optgroup").length, 1, "Expected opgroup to be added");

        equals(select.$element.find("optgroup option").length, 2, "Expected optgroup to contain 2 options");

        select.appendOptionsFromJSON([
            new AJS.GroupDescriptor({
                label: "History Search",
                items: [
                    new AJS.ItemDescriptor({
                       icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-2 - test",
                        value: "hsp-2"
                    }),
                    new AJS.ItemDescriptor({
                        icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-1 - I am a iest Issue",
                        value: "hsp-1"
                    })
                ]
            })
        ]);

        equals(select.$element.find("optgroup option").length, 2, "Expected optgroup to not allow duplicates");


        select.appendOptionsFromJSON([
            new AJS.GroupDescriptor({
                label: "Current Search",
                items: [
                    new AJS.ItemDescriptor({
                       icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-2 - test",
                        value: "hsp-2"
                    }),
                    new AJS.ItemDescriptor({
                        icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-1 - I am a iest Issue",
                        value: "hsp-1"
                    })
                ]
            })
        ]);

        equals(select.$element.find("optgroup[label='Current Search'] option").length, 2, "Expected 2 options to be added to "
                + "new group, even though option with same values exist in another group [History Search]");

        select.appendOptionsFromJSON([
            new AJS.GroupDescriptor({
                label: "Current Search",
                replace: true,
                items: [
                    new AJS.ItemDescriptor({
                        icon: "/jira/images/icons/issue_subtask.gif",
                        label: "HSP-1 - I am a iest Issue",
                        value: "hsp-1"
                    })
                ]
            })
        ]);

        ok(select.$element.find("optgroup[label='Current Search'] option").length === 1,
                "Expected that if group has replace flag optgroup options are cleared and replaced with new definition");

        select = new AJS.MockSelect();

        select.appendOptionsFromJSON([
            new AJS.GroupDescriptor({
                weight: 999,
                label: "History Search",
                items: []
            }),
            new AJS.GroupDescriptor({
                weight: 1,
                label: "Current Search",
                items: []
            }),
            new AJS.GroupDescriptor({
                weight: 555,
                label: "Custom Search",
                items: []
            })
        ]);

        ok(select.$element.find("optgroup:eq(0)") , "Expected Current Search to be first group");
        ok(select.$element.find("optgroup:eq(1)") , "Expected Custom Search to be second group");
        ok(select.$element.find("optgroup:eq(2)") , "Expected History Search to be third group");
    });


    test("Removes null option", function () {

        AJS.MockSelect = AJS.SelectModel.extend({
            init: function () {
                this.options = {
                    removeNullOptions: true
                };
            }
        });

        var select = new AJS.MockSelect();


        var $option = jQuery("<option value='0' />").appendTo("body");


        select._parseOption($option);

        ok($option.parent().length === 1, "Expected option to only be removed if the value is less than 0");

        $option.val("-1");

        select._parseOption($option);

        ok($option.parent().length === 0, "Expected option to be removed if the value is less than 0");

    });
})();