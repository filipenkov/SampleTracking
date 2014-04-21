AJS.gadgets.templater.Table = function (options) {

    var table, sortCol, sortOrd = [],

    sort = function (data, column, order) {
        data.cols[column].theme = order;
        // string/number sort
        if (!data.cols[column].sorted) {
           data.data.sort(function (item, compareItem) {
               var dataItem = item[column].value, dataItemCompare = compareItem[column].value;
               if (!isNaN(parseFloat(dataItem)) && !isNaN(parseFloat(dataItemCompare))) {
                   dataItem = parseFloat(dataItem);
                   dataItemCompare = parseFloat(dataItemCompare);
               }
               if (dataItem > dataItemCompare) {
                    return 1;
               } else if (dataItem < dataItemCompare) {
                    return -1;
               } else {
                    return 0;
               }
           });
        }
        if (order === "desc") {
            data.data.reverse();
        }
        return data;
    },

    templater =  AJS.gadgets.templater.Custom(AJS.$.extend({
        template: AJS.gadgets.templater.Table.template,
        baseUrl: AJS.gadget.getBaseUrl(),
        cache: false
    },options));

    if (options.sortable) {
        templater.addCallback = function (addCallback) {
            return function (callback) {
                addCallback(function (wrappedCallback) {
                    return function (newTable) {
                        AJS.$("th", newTable).click(function() {
                            sortCol = AJS.$.inArray(this, AJS.$("th", newTable));
                            if (sortOrd[sortCol]) {
                                sortOrd[sortCol].reverse();
                            }
                            templater.build();
                        });
                        if (table) {
                            table.replaceWith(newTable);
                        }
                        table = newTable;
                        wrappedCallback.call(templater, table);
                    };
                }(callback));
            };
        }(templater.addCallback);

        templater.build = function (build) {
            return function (data) {
                if (data) {
                    if (sortCol === undefined) {
                        AJS.$(data.cols).each(function(i){
                            if (this.sort) {
                                sortCol = i;
                                if (!sortOrd[sortCol]) {
                                    sortOrd[sortCol] = [this.sort, this.sort == "asc" ? "desc" : "asc"];
                                }
                            }   
                        });
                    }
                    if (sortCol !== undefined) {
                        if (!sortOrd[sortCol]) {
                            sortOrd[sortCol] = ["desc", "asc"];
                        }
                        data = sort(data, sortCol, sortOrd[sortCol][0]);
                    }
                }
                build.call(templater, data);
            };
        }(templater.build);

    }

    return templater;
};