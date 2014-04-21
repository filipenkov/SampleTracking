
function singleItem() {
    var $item = jQuery("%s"),
                answer = {};

    if ($item.length === 0) {
        return answer;
    }

    answer.item = {};
    answer.item.name = $item.text();

    var $section = $item.parents("ul.aui-list-section");
    if ($section.length === 1) {
        var $header = $item.prev("h5");
        answer.section = {};
        answer.section.id = $section.attr("id");
        if ($header.length === 1) {
            answer.section.header = $header.text();
        }
    }
    return answer;
}
return singleItem();


