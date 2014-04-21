AG.MockXHR = function () {
    this.status = 0;
    this.aborted = false;
};

AG.MockXHR.prototype.abort = function () {
    this.aborted = true;
};

AG.MockXHR.prototype.updateFromMakeRequestResp = function (response) {
    this.status = response.rc;
    this.responseText = response.text;
    this.headers = response.headers || {};
};

AG.MockXHR.prototype.updateFromJQueryXHR = function (xhr) {
    this.status = xhr.status;
    this.responseText = xhr.responseText;
    this.headers = xhr.headers;
};