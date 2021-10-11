<%@ page import="com.ihsinformatics.gfatmweb.server.AicWebService"%>
<%@ page import="org.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%
	AicWebService web = new AicWebService();
	web.handleRequest(request, response);
	out.print(response);
%>
