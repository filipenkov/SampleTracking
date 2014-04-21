if (typeof FLASHBROWSER == "undefined")
    {
        var FLASHBROWSER = new Object();
        FLASHBROWSER.loaded = false;
    }

// WebBrowser specific code
function verifyCreated(id) {
    var div = document.getElementById('div' + id);
    if (div == null) {
        div = document.createElement('div');
        div.setAttribute('id', 'div' + id);
        div.style.position = 'absolute';
        div.style.visibility = 'hidden';
        //div.style.overflow = 'hidden';
        var body = document.getElementById("jira");
        body.appendChild(div);
    }
    var iframe = document.getElementById('iframe' + id);
    if (iframe == null) {
        iframe = document.createElement('iframe');
        iframe.setAttribute('id', 'iframe' + id)
        iframe.style.position = 'relative';
        iframe.style.backgroundColor = 'white';
        iframe.style.borderStyle = 'none';
        iframe.setAttribute('frameborder', '0');
        iframe.addEventListener("load", showBrowserFrame, false);

        div.appendChild(iframe);
    }
    return iframe;
}

function showBrowserFrame() {
    FLASHBROWSER.loaded = true;
    var div = document.getElementById('divtransitionBrowser');
    div.style.visibility = 'visible';
    div.style.display = '';
}


function updateBrowser(id, x, y, width, height, clipX, clipY, clipWidth, clipHeight) {

    var iframe = verifyCreated(id);
    iframe.style.left = -clipX + 'px';
    iframe.style.top = -clipY + 'px';
    iframe.style.width = width + 'px';
    iframe.style.height = height + 'px';
    iframe.style.display = '';

    var div = document.getElementById('div' + id);
    div.style.left = x + clipX + 'px';
    div.style.top = y + clipY + 'px';
    div.style.width = clipWidth + 'px';
    div.style.height = clipHeight + 'px';

    if(FLASHBROWSER.loaded) {
        div.style.visibility = 'visible';
        div.style.display = '';
    }


}

function loadURL(id, url) {
    FLASHBROWSER.loaded = false;
    var iframe = verifyCreated(id);
    hideBrowser(id);
    
    iframe.src = url;

}

function hideBrowser(id) {
    var div = document.getElementById('div' + id);
    div.style.visibility = 'hidden';
    div.style.display = 'none';
}