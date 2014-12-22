$(document).ready(function() {
	onInit();
	
	// wicket ajax handlers
//	Wicket.Ajax.registerPreCallHandler(onStartAjax);
//	Wicket.Ajax.registerPostCallHandler(onInit);
	Wicket.Event.subscribe('/ajax/call/complete', onInit);
//	Wicket.Ajax.registerPostCallHandler(onStopAjax);
//	Wicket.Ajax.registerFailureHandler(onStopAjax);
});

$(window).resize(onResize);

function onInit() {
	onResize();
	
	/* --- Table --- */
	// styling	
	$("table tr:even").addClass("tr-even");
		
	// hover for the right table th
	$("table th").hover(function() {
		$(this).addClass("th-hover");
   },function() {
	   $(this).removeClass("th-hover");
   });
	$("table tbody tr").hover(function() {
		$(this).addClass("tr-hover");
	},function() {
		$(this).removeClass("tr-hover");
	});	
	
//	$("table.actions-grid tr td:first-child").addClass("name-col");
//	$("table.actions-grid tr td:nth-child(2)").addClass("actions-col");
	$("table.actions-grid tr:last-child").addClass("tr-last");

	// hover actions in the right table th
	$("table.actions-grid tr td.actions-col ul li").hover(function() {
		$(this).addClass("li-relative");
	},function() {
	   $(this).removeClass("li-relative");
   });	
	$("table.actions-grid tr td.actions-col").hover(function() {
		$(this).addClass("actions-col-hover");
	},function(){
		$(this).removeClass("actions-col-hover");
	});
	
	/* --- Actions bar --- */
	$(".actions-bar ul.topnav > li").hover(function() {
		$(this).addClass("topnav-hover");
	},function() {
		$(this).removeClass("topnav-hover");
	});
	$(".actions-bar ul.topnav li").hover(function() { // when trigger is clicked...  
		// following events are applied to the subnav itself (moving subnav up and down)  
        $(this).find("ul.subnav").slideDown('fast').show(); //drop down the subnav on click  
  
       	$(this).hover(function() {  
        }, function(){  
        	$(this).parent().find("ul.subnav").slideUp('fast'); // when the mouse hovers out of the subnav, move it back up  
        });  
   
        // following events are applied to the trigger (Hover events for the trigger)  
        }).hover(function() {  
        	$(this).addClass("subhover"); // on hover over, add class "subhover"  
        }, function() {  // on Hover Out  
            $(this).removeClass("subhover"); // on hover out, remove class "subhover"  
     });  
	
	 /* --- Table actions column --- */
	$(".actions-grid ul.topnav > li").hover(function() {
		$(this).addClass("topnav-hover");
	}, function() {
		$(this).removeClass("topnav-hover");
	});
	$(".actions-grid ul.topnav li").hover(showPopup, hidePopup);
	
//	applyTooltip();

//	splitter();
}

function onResize() {
	/* --- Dynamic height --- */
	var $height = $(window).height() - 240;
//	alert($height);
	$(".accordion-group").css({'height': $height});
	$(".table-right-container").css({'height': $height});
    $(".wizardScheduler").css({'height': $height});
    var url = window.location.href;
    if (url.indexOf("dashboards") !== -1) {
    	// dashboards integration url -> do not take header height
    	$(".dashboard").css({'height': $height + 238});
    	$(".dashboardNavigation").css({'height': $height + 205});
    } else {
    	$(".dashboard").css({'height': $height + 65});
    	$(".dashboardNavigation").css({'height': $height + 32});
    }
    $(".analysis").css({'height': $height + 65});
	$(".analysisNavigation").css({'height': $height + 65});
    $(".fieldset-panel-right").css({'height': $height - 20});
    
}

function showPopup() {
    x = $(this).find("ul.subnav");
//    var t = $(this).parents().filter("td.actions-col");
//    x.css({ "left":  + t.position().left + "px" });
    
    var table = $(this).parents().filter("div.table-right-container,div.table-container,div.dashboardNavigation,div.analysisNavigation,div.table-actions");
	var tt = table.position().top;
	var th = table.height();
	var tb = tt + th;
	
	var p = $(this).parent(); // ul.topnav
	var pos = p.position()
	
	var xt = pos.top;
	var xh = x.height();
	var xb = xt + xh;
	
	if (xb > tb) {
		x.css({ "top":  + (tb - xb + 5) + "px" });
	/*
	} else {
		x.css({ "top": pos.top + "px" });
		*/
	}	
    x.show();  
	$(this).addClass("subhover");
}

function hidePopup() {	
	$(this).parent().find("ul.subnav").hide();
    $(this).removeClass("subhover");
}

function splitter() {	
	 $("#splitter").splitter({type: 'v'});
}

function applyTooltip() {
//	$("#tooltip").easyTooltip();
	vtip();
}

/*
function onStartAjax() {
	$(".wicket-ajax-indicator").show();
}

function onStopAjax() {
	$(".wicket-ajax-indicator").hide();
}
*/

function log(message) {
	var console = window["console"];
	if (console && console.log) {
		console.log(message);
	}
}
