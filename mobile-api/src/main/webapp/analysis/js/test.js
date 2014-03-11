    // Load the Visualization API and the piechart package.
    google.load('visualization', '1.1', {
        'packages' : [ 'corechart', 'controls', 'table' ]
    });
 
    // Set a callback to run when the Google Visualization API is loaded.
    google.setOnLoadCallback(drawVisualization);

  var dataTable;
  var control;
  var dashboard;
  
  function changeRange() {
       // 'slider' is the ControlWrapper instance describing the range slider.
       control.setState({'range': {'start': 10, 'end': 40}});
       control.draw();
     }
  
  function drawVisualization() {
    //
    // Chart settings
    //
    var chartAreaLeft = 80;
    var chartAreaHeight = '80%';
    var chartAreaWidth = 680;
    var chartWidth = 980;
    var chartHeight = 300;
    var controlChartHeight = 70;
    
    
    dashboard = new google.visualization.Dashboard(
            document.getElementById('dashboard'));
    
    //
    // Control chart
    // 
    control = new google.visualization.ControlWrapper({
      'controlType': 'ChartRangeFilter',
      'containerId': 'control',
      'options': {
        // Filter by the time axis.
        'filterColumnIndex': 0,
        'ui': {
          'chartType': 'LineChart',
          'chartOptions': {
            'chartArea': {'left':chartAreaLeft, 'width': chartAreaWidth},
            'hAxis': {'baselineColor': 'none', 'minValue': 0},
          'width': chartWidth,
          'height': controlChartHeight
          },
          // Display a single series that shows the closing value of the stock.
          // Thus, this view has two columns: the date (axis) and the stock value (line series).
          'chartView': {
            'columns': [0,1]
          },
          // 2 seconds
          'minRangeSize': 0.1
        }
      },
      'state': {'range': {'start': 0, 'end': 30}}
    });
    
    
    //
    // Settings for each chart 
    // 
    
    var Frequency = new google.visualization.ChartWrapper(
         {"chartType":"ScatterChart","view":{"columns":[0,1]},"containerId":"Frequency","options":{"pointSize":1,"vAxis":{"title":"Rotation Freq (Hz)/(m/s)"},"lineWidth":1}});
    Frequency.setOption('chartArea', {
      'left': chartAreaLeft, 
      'height': chartAreaHeight, 
      'width': chartAreaWidth});
    Frequency.setOption('width', chartWidth);
    Frequency.setOption('height', chartHeight);

    //var data = {"cols":[{"id":"TIME","label":"TIME","type":"number","pattern":""},{"id":"FREQ-FREQ-3.0 BLAC","label":"FREQ-FREQ-3.0 BLAC","type":"number","pattern":""}]};
    
    //var dataTable = new google.visualization.DataTable(data, 0.6);    
    
    dataTable = new google.visualization.DataTable();
    dataTable.addColumn("number", "Time");
    dataTable.addColumn("number", "Freq");
     
     //
     // Add charts to the dashboard
     //
     
       
     dashboard.bind(control, Frequency);
         
     google.visualization.events.addListener(dashboard, 'ready', function() {
       jsFreqAnalysis();
     });
     
     function jsFreqAnalysis() {
        controlCols = control.getOption('ui.chartView').columns;
        
        freqCols = controlCols.slice(1);
        rows = [];
          
        for (var i=0; i < freqCols.length; i++ ) {
          rows[i] = dataTable.getFilteredRows([
                 {column: 0, minValue: control.getState().range.start,
                   maxValue: control.getState().range.end},
                 {column: freqCols[i], minValue: 0} // remove null
               ]);
        }
        
        means = [];
        maxs = [];
        mins = [];
        stds = [];
        
        for (var i=0; i < freqCols.length; i++ ) {
            sum = 0;
            max = 0;
            min = dataTable.getValue(rows[i][0], freqCols[i]);
            SS = 0;
            
            for (var j=0; j < rows[i].length; j++) {
              val = dataTable.getValue(rows[i][j], freqCols[i]);
              // sum
              sum += val;
              // max
              if (val > max) {
                max = val;
              }
              
              if (val < min) {
                min = val;
              }
              
              SS += Math.pow(val,2);
              
            }
            
            means[i] = sum/rows[i].length;
            maxs[i] = max;
            mins[i] = min;
            stds[i] = Math.sqrt( 1 /  rows[i].length * SS - Math.pow(means[i],2));
        }
        
        analysis = "<table><tr><th>name</th><th>mean</th><th>max</th><th>min</th><th>std</th></tr>";
        
        for (var i=0; i < freqCols.length; i++ ) {
          name = dataTable.getColumnLabel(freqCols[i]);
          analysis += "<tr><td>" + name + "</td><td>" + means[i] + "</td><td>" + maxs[i] + "</td><td>" + mins[i] + "</td><td>" + stds[i] + "</td></tr>";
        }
        
        analysis += "</table>";
        
        $("#jsAnalysis").html(analysis);
     }
   } 
   