var result = new Array();
jQuery("%s a.aui-list-item-link").each(function(index, elem) {
    var $elem = jQuery(elem),
            newOne = {};

    newOne.name = $elem.text();
    result.push(newOne);
});
return result;
