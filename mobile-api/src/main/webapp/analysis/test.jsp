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
  </style>
</head>
<body>
  <div id="stopwatch"></div>
  <p>IsActive:<c:out value="${active}"/></p>
  <button onclick="start();">Start</button>
  <button onclick="stop();">Stop</button>
  <button onclick="resetClear();">Clear</button>
  
  <div style="display: none;" id="showResult"></div>
  
  <div id="dashboard">
     <div id="Frequency"></div>
     <div id="control"></div>
     <div id="jsAnalysis"></div>
  </div>
    
    
  <script src='http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js' type='text/javascript'></script>
  <script type="text/javascript" src="https://www.google.com/jsapi"></script>
  <script type="text/javascript" src="js/test.js"></script>
  <script type="text/javascript" src="js/stopwatch.js"></script>
  <script type="text/javascript" src="js/testControl.js"></script>
  
</body>
</html>