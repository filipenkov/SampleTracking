var NEW_CUSTOM_FIELD = 'newCustomField';
var EXISTING_CUSTOM_FIELD = 'existingCustomField';

var NEW_PROJECT = 'importsingleproject';
var EXISTING_PROJECT = 'importexistingproject';

function getElementsByClass(needle)
{
  var allElements = document.getElementsByTagName("TR");
  var retvalue = new Array();

  for (i = 0, j = 0; i < allElements.length; i++)
  {
    var c = " " + allElements[i].className + " ";
    if (c == (" " + needle + " "))
    {
      retvalue[j] = allElements[i];
      j++;
    }
  }

  return retvalue;
}


function a(elem)
{
    /* [alert] */
    if (elem == null)
    {
        alert("Nothing!");
    }
    else
    {
        alert(
          "name: " + elem.getAttribute("name") + "\n" +
          "id: " + elem.id + "\n" +
          "nodeType: " + elem.nodeType + "\n" +
          "nodeName: " + elem.nodeName + "\n" +
          "nodeValue: " + elem.nodeValue + "\n" +
          "tagName: " + elem.tagName + "\n" +
          "innerHTML: " + elem.innerHTML + "\n"
        );
    } // end-if
    /* [alert] end */
}

function hideObject(elem)
{
    elem.style.display = 'none';
}

function showObject(elem)
{
    elem.style.display = '';
}

function hideClass(className)
{
    var list = getElementsByClass(className);
    for (nIndex = 0; nIndex < list.length; nIndex++)
    {
        hideObject(list[nIndex]);
    } // end for
}

function showClass(className)
{
    var list = getElementsByClass(className);
    for (nIndex = 0; nIndex < list.length; nIndex++)
    {
        showObject(list[nIndex]);
    } // end for
}

function handleCustomFieldDisplay(selectListId)
{
    var selectList = document.getElementById(selectListId);
    var options = selectList.options;
    var selectedValue = options[selectList.selectedIndex];
    if (selectedValue)
    {

        if (selectedValue.value == NEW_CUSTOM_FIELD)
        {
            showClass(selectListId + NEW_CUSTOM_FIELD);
            hideClass(selectListId + EXISTING_CUSTOM_FIELD);
        }
        else if (selectedValue.value == EXISTING_CUSTOM_FIELD)
        {
            showClass(selectListId + EXISTING_CUSTOM_FIELD);
            hideClass(selectListId + NEW_CUSTOM_FIELD);
        }
        else
        {
            hideClass(selectListId + NEW_CUSTOM_FIELD);
            hideClass(selectListId + EXISTING_CUSTOM_FIELD);
        } // end-if
    }
}



function handleProjectDisplay()
{
    hideClass(NEW_PROJECT);
    hideClass(EXISTING_PROJECT);

    var radio = document.forms.jiraform['/bean/selectedProjectOption'];

    var selectedValue = radio.value;
    debug(radio);

        selectedValue = findSelectedValue(radio);
        debug("found selectedValue: " + selectedValue);    
    

    if (selectedValue)
    {
        debug("selectedValue: " + selectedValue);    
        if (selectedValue == NEW_PROJECT)
        {
            showClass(NEW_PROJECT);
            debug("Showing new project");
        }
        else if (selectedValue == EXISTING_PROJECT)
        {
            showClass(EXISTING_PROJECT);
            debug("Showing existing project");
        }
        else
        {
            debug("Showing nothing");
        }
    }    

    return true;
}

function findSelectedValue(radioList)
{
    for (nIndex = 0; nIndex < radioList.length; nIndex++)
    {
        if(radioList[nIndex].checked == true) 
            return radioList[nIndex].value;
    } // end for
}

function debugClass(className)
{
    var list = getElementsByClass(className);
    for (nIndex = 0; nIndex < list.length; nIndex++)
    {
        /* [alert] */
        alert(list[0]);
        /* [alert] end */
    } // end for
}


function debug(text)
{
    try
    {
        var testDiv = document.getElementById("testDiv");
        testDiv.innerHTML = testDiv.innerHTML + text + "<br/>";     	
    }
    catch (eException)
    {
    } // end try
}

function getHtml(obj)
{
    var text = obj.innerHTML;
    return text;
}

function duplicateRow(obj)
{
    var mappingRow  = findMappingRowParent(obj);
    var parentTable = findParentTable(obj);
    // var mappingRowHtml = getHtml(mappingRow);
    var newRow = createRow(mappingRow);
    
    a(newRow);

    parentTable.appendChild(newRow);
}

function createRow(mappingRow)
{
    return mappingRow.cloneNode(true);
}
//function createRow(mappingRow)
//{
//    var newRow = document.createElement("TR");
//    newRow.className = "mappingrow";
//    newRow.innerHTML = mappingRow.innerHTML;
//    return newRow;
//}

function findMappingRowParent(elem)
{
    if (elem == null)
    {
        return null;
    }
    else if (elem.className == 'mappingrow')
    {
        return elem;
    }
    else
    {
        return findMappingRowParent(elem.parentNode);
    } // end-if    
}

function findParentTable(elem)
{
    if (elem == null)
    {
        return null;
    }
    else if (elem.tagName == 'TABLE')
    {
        return elem;
    }
    else
    {
        return findParentTable(elem.parentNode);
    } // end-if    
}
