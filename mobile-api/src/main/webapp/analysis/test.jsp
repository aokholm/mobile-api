<%@page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html;charset=UTF-8">
  <title>Vaavud</title>
  <style type="text/css">
      html {height:100%}
      body {height:100%; margin:0; padding:0}
      table {min-width:400px;}
      td {border:1px solid #000000;}
      .left {text-align:left;}
      .right {text-align:right;}
      button {font-size: 140%;}
  </style>
</head>
<body>
  <h1>Vaavud Frequency Measurement</h1>

  <h3>Measurement Description</h3>
  <table>
    <tr>
      <td>Sample Id</td>
      <td><input id="sampleId" type="text"></td>
    </tr>
    <tr>
      <td>Wind speed (est)</td>
      <td><input id="windSpeed" type="text"></td>
    </tr>
    <tr>
      <td>Test description</td>
      <td><input id="testDescription" type="text"></td>
    </tr>
  </table>
  
  <h3>Controls</h3>
  <button onclick="start();">Start</button>
	<button onclick="stop();">Stop</button>
	<button onclick="resetClear();">Clear</button>
	
	<div style="display: none;" id="showResult"></div>
	
	<h3>Live View</h3>
	<p style="font-size: 140%;">
    Timer: <span id="stopwatch"></span>
  </p>

	

	<div id="dashboard">
		<div id="Frequency"></div>
		<div id="control"></div>
		<div id="jsAnalysis"></div>
	</div>

  <br />
  <br />
  <p>
    Sensor was active when page loaded:
    <c:out value="${active}" />
  </p>
  
  
	<script
		src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js'
		type='text/javascript'></script>
	<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	<script type="text/javascript" src="js/test.js"></script>
	<script type="text/javascript" src="js/stopwatch.js"></script>
	<script type="text/javascript" src="js/testControl.js"></script>

</body>
</html>