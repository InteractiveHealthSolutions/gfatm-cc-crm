<%@page import="com.ihsinformatics.gfatmweb.server.GfatmTasksWebService"%>
<%@page import="org.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%
	GfatmTasksWebService web = new GfatmTasksWebService();
	web.handleRequest(request, response);
	out.print(response);
%>
