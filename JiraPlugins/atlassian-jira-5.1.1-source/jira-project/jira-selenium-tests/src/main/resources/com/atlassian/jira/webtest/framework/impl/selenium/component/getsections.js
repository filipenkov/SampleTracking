var result = new Array();
jQuery("%s ul.aui-list-section").each(function(index, elem) {
    var $elem = jQuery(elem),
            newOne = {},
            $header = $elem.prev("h5");

    newOne.id = $elem.attr("id");
    if ($header.length === 1) {
        newOne.header = $header.text();
    }
    result.push(newOne);
});
return result;
