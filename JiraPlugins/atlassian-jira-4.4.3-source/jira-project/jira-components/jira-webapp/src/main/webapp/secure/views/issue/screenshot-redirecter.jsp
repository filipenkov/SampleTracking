<%@ taglib uri="webwork" prefix="ww" %>
<html>
<meta name="decorator" content="none">
<body>
<ul id="params" style="display:none">
    <li id="afterURL"><ww:property value="$afterURL" /></li>
    <li id="action"><ww:property value="$action" /></li>
</ul>

<script type="text/javascript">
    window.onload = function () {
        var afterURL = document.getElementById("afterURL").firstChild;
        var action = document.getElementById("action").firstChild;
        if (afterURL && action) {
            var actionTaken = action.nodeValue.replace(/\s/g, "");
            var replaced = afterURL.nodeValue.replace(/\s/g, "");
            var isPopup = false;
            //window.name will be set when we call window.open(), so we can use it here to detect if it's a popup or
            //if the user opened the screenshot applet in a new tab (JRADEV-3511,JRADEV-3512)
            if(window.name === "screenshot") {
                isPopup = true;                
            }
            if (actionTaken && actionTaken == "submit") {
                if (replaced && replaced != "") {
                    if (isPopup) {
                        window.opener.open(afterURL.nodeValue, '_top');
                        window.close();
                    } else {
                        window.location = afterURL.nodeValue;
                    }
                }
            }
            if (actionTaken && actionTaken == "cancel") {
                if (replaced && replaced != "") {
                    if (isPopup) {
                        window.close();
                    } else {
                        window.location = afterURL.nodeValue;
                    }
                }
            }
        } else {
            //should never really get here, but just in case!
            window.close();
        }
    }
</script>

</body>
</html>
