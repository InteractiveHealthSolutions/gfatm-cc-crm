<%@ page import="com.ihsinformatics.gfatmweb.server.CallCenterWebService"%>
<%@ page import="org.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%
	CallCenterWebService cal = new CallCenterWebService();
	cal.handleRequest(request, response);
	//out.print(response);
%>
