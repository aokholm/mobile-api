function ajax(sendData) {
  $.ajax({
        type: "POST",
    url: "serial",
    data: sendData,
    success: function(data) {console.log(data);} ,
    dataType: "json"
  })
  .fail(function() {
       console.log( "ajax error" );
       });
}

function start() {
  ajax({action: "start"});
  watch.start();
  $("#showResult").hide();
}

function stop() {
$.ajax({
    type: "POST",
    url: "serial",
    data: {action: "stop"},
    success: function(data) {
    	if (data.msId != null) {
    		link = "<a target='_blank' href='/analysis/measurement?pass=2gh7yJfJ6H&session_id=" + data.msId + "'>Result " + data.msId + "</a>";
    		$("#showResult").html(link);
    		$("#showResult").show();
    	} else {
    		console.log(data);
    	}
    } ,
    dataType: "json"
})
.fail(function() {
   console.log( "ajax error" );
   });
  watch.stop();
  watch.reset();
}



function resetClear() {
	if (confirm("clear")) {
		ajax({action: "clear"});
      watch.stop();
      watch.reset();
	}
}

function getFreqUpdate() {
	$.ajax({
        type: "POST",
	    url: "serial",
	    data: {status: "freq"},
	    success: function(data) {
	    	if (data.time != null) {
	    		dataTable.addRow([data.time, data.freq]);
	    		if (data.time > 30 ) {
	    			startTime = data.time - 30;
	    		} else {
	    			startTime = 0;
	    		}
	    		end = data.time+0.1;
	    		
	    		control.setState({'range': {'start': startTime, 'end': end}});
	    		dashboard.draw(dataTable);
	    	}
	    } ,
	    dataType: "json"
	})
	.fail(function() {
       console.log( "ajax error" );
       });
}

var watch;

$( document ).ready(function() {
	var stopwatchElement = document.getElementById("stopwatch");
	watch = new Stopwatch(stopwatchElement, {delay: 500, call: getFreqUpdate});
});