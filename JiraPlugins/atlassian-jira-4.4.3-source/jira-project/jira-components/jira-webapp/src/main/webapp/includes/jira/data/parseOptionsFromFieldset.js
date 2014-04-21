(function() {

    JIRA.parseOptionsFromFieldset = function($fieldset) {
        var parsedValues = parseFieldset($fieldset, $fieldset);
        $fieldset.remove();
        return parsedValues;
    };

    function parseFieldset($fieldset, $parentFieldset) {
        var ret = {};
        $fieldset.children().each(function() {
            var itemValue,
                $item = jQuery(this);
            if ($item.is("input[type=hidden]")) {
                itemValue = parseValue($item);
                ret[itemValue.id] = itemValue.value;
            } else if ($item.is("fieldset")) {
                ret[$item.attr("title") || $item.attr("id")] = parseFieldset($item, $parentFieldset);
            } else {
                $item.insertBefore($parentFieldset);
            }
        });
        return ret;
    }

    function parseValue($item) {
        var itemValue = {},
            value = $item.val();
        itemValue.id = $item.attr("title") || $item.attr("id");
        itemValue.value = (value.match(/^(tru|fals)e$/i) ? value.toLowerCase() == "true" : value);
        return itemValue;
    }

})();
