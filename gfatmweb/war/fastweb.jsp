<%@ page import="com.ihsinformatics.gfatmweb.server.FastWebService"%>
<%@ page import="org.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	FastWebService web = new FastWebService();
	web.handleRequest(request, response);
	out.print(response);
%>
