<%@page import="javax.naming.Context" %>
<%@page import="javax.naming.InitialContext" %>
<%@page import="com.vaavud.sensor.Tester"%>
<%@page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%><%

Context initCtx = new InitialContext();
Context envCtx = (Context) initCtx.lookup("java:comp/env");
Tester tester = (Tester) envCtx.lookup("bean/TesterFactory");
              
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
  <p>HelloTest</p>
</body>
</html>