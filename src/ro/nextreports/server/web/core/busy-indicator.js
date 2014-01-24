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
        clickedElement.tagName.toUpperCase() == 'A' ||
        clickedElement.parentNode.tagName.toUpperCase() == 'A' ||
        (clickedElement.tagName.toUpperCase() == 'INPUT' &&
         (//clickedElement.type.toUpperCase() == 'BUTTON' ||
          clickedElement.type.toUpperCase() == 'SUBMIT'))) {
        showBusy();
    }
}
