
/**
 * function handleInput
 * @param {Object} e
 * @returns {Boolean}
 */
function handleInput(e)
{
    var o = jQuery(e.target).get(0);
    //debug('o:' + o.id);
    //debug('target:' + e.target.id);
    if (o.type == 'radio' || o.type == 'checkbox')
    {
        addToList("tester.checkCheckbox(\""+ o.name + "\", \""+ o.value+"\");");
    }
    else if (o.type == 'text')
    {
        // normal input
        addToList("tester.setFormElement(\""+ o.name + "\", \""+ o.value+"\");");
    }
    else if (o.tagName.toLowerCase() === "textarea")
    {
        addToList("tester.setFormElement(\""+ o.name + "\", \""+ o.value+"\");");
    }

    return true;
}

/**
 * function handleSubmit
 * @param {Object} e
 * @returns {Boolean}
 */
function handleSubmit(e)
{
    var o = jQuery(e.target).get(0);
    if (o.type == 'submit')
    {
        addToList("tester.submit(\""+ o.name + "\");");
    }
    return true;
}

/**
 * function handleSelect
 * @param {Object} e
 * @returns {Boolean}
 */
function handleSelect(e)
{
    var o = e.currentTarget;
    var label = getSelectedLabel(o) + '';
    addToList("// Select '"+ trim(label) + "' from select box '"+ o.name + "'.");
    addToList("tester.selectOption(\""+ o.name + "\", \""+ trim(label) + "\");");
    console.warn('handleselect');
    return true;
}

/**
 * function handleAnchor
 * @param {Object} e
 * @returns {Boolean}
 */
function handleAnchor(e)
{

    var o = jQuery(e.target).get(0);

    if (o.id)
    {
        addToList("// Click Link '"+ stripText(o) + "' (id='" + o.id + "').");
        addToList("tester.clickLink(\""+ o.id + "\");");
    }
    else
    {
        // TODO: maybe better to use stripText(o) rather than o.innerHTML
        addToList("tester.clickLinkWithText(\""+ o.innerHTML + "\");");
    }

    return true;
}

/**
 * function handleFormSubmit
 * @param {Object} e
 * @returns {Boolean}
 */
function handleFormSubmit(e)
{
    var o = jQuery(e.target).get(0);

    if (!o.action)
        o = o.form;
    addToList("tester.setWorkingForm(\""+ o.name + "\");");
    addToList("tester.submit();");
    return true;
}

