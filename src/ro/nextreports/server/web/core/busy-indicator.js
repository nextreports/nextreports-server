window.onload = setup;
var isBusy = false;

function setup() {
    document.getElementsByTagName('body')[0].onclick = clickHandler;
    hideBusy();

//    Wicket.Ajax.registerPreCallHandler(showBusy);
    Wicket.Event.subscribe('/ajax/call/before', showBusy);        
//    Wicket.Ajax.registerPostCallHandler(hideBusy);
    Wicket.Event.subscribe('/ajax/call/complete', hideBusy);
//    Wicket.Ajax.registerFailureHandler(hideBusy);    
    Wicket.Event.subscribe('/ajax/call/failure', hideBusy);
}

function hideBusy() {
    isBusy = false;
	$(".wicket-ajax-indicator").hide();
}

function showBusy() {
    isBusy = true;
    setTimeout("doShowBusy();", 500);
}

function doShowBusy() {
    if (!isBusy) {
    	return;
    }
	$(".wicket-ajax-indicator").show();
}

function clickHandler(eventData) {
    var clickedElement = (window.event) ? event.srcElement : eventData.target;
    if (//clickedElement.tagName.toUpperCase() == 'BUTTON' ||
        (clickedElement.tagName.toUpperCase() == 'A' &&  !ignoreBusyIndicator(clickedElement.className)) ||
        (clickedElement.parentNode.tagName.toUpperCase() == 'A' &&  !ignoreBusyIndicator(clickedElement.parentNode.className)) ||
        (clickedElement.tagName.toUpperCase() == 'INPUT' &&
         (//clickedElement.type.toUpperCase() == 'BUTTON' ||
          clickedElement.type.toUpperCase() == 'SUBMIT'))) {
        showBusy();
    }
}

// ignore all button links from DatePicker
// ignore all button links with a special css class noBusyIndicator
function ignoreBusyIndicator(className) {
	return isInArray(className, ['calnav', 'calnavleft', 'calnavright', 'link-close', 'noBusyIndicator']);		    
}

function isInArray(value, array) {
	return array.indexOf(value) > -1;
}
