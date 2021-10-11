/* Copyright(C) 2016 Interactive Health Solutions, Pvt. Ltd.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */
/**
 * 
 */
package com.ihsinformatics.gfatmweb.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;

import org.hibernate.HibernateException;
import org.ihs.emailer.EmailEngine;
import org.ihs.emailer.EmailException;
import org.json.JSONException;
import org.json.JSONObject;

import com.ihsinformatics.gfatmweb.shared.RequestType;
import com.ihsinformatics.tbreachapi.core.TBR;
import com.ihsinformatics.tbreachapi.core.service.impl.ServerService;
import com.ihsinformatics.tbreachapi.model.Definition;
import com.ihsinformatics.tbreachapi.model.DefinitionType;
import com.ihsinformatics.tbreachapi.model.Element;
import com.ihsinformatics.tbreachapi.model.Location;
import com.ihsinformatics.tbreachapi.model.UserForm;
import com.ihsinformatics.tbreachapi.model.UserFormResult;
import com.ihsinformatics.tbreachapi.model.UserFormType;
import com.ihsinformatics.tbreachapi.model.Users;
import com.ihsinformatics.util.DateTimeUtil;
import com.ihsinformatics.util.SecurityUtil;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class FastWebService extends AbstractWebService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109274182101835832L;
	private static final String encoding = "UTF-8";

	public FastWebService() {
		if (!ServerService.isRunning()) {
			try {
				InputStream inputStream = Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(PROP_FILE_NAME);
				prop = new Properties();
				prop.load(inputStream);
				TBR.readProperties(PROP_FILE_NAME);
				TBR.props = prop;
			} catch (Exception e) {
				e.printStackTrace();
			}
			guestUsername = prop.getProperty("guest.username");
			guestPassword = prop.getProperty("guest.password");
			apiService.startup();
			ServerService.isLoggedIn();
			try {
				// Start email engine
				EmailEngine.instantiateEmailEngine(prop);
			} catch (EmailException e) {
				e.printStackTrace();
			}
		}
		
		Timer time = new Timer(); // Instantiate Timer Object
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask class
		time.schedule(st, 0, 25200000); //run function for sql
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ihsinformatics.gfatmweb.server.AbstractWebService#handleRequest(javax
	 * .servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse resp) throws IOException, JSONException {
		
		ServerService serverService = apiService;
		PrintWriter out = resp.getWriter();
		JSONObject jsonResponse = new JSONObject();
		String response = null;
		String content = "";
		String responseDetail = "";
		Date dateEntered;
		Date endtime;
		Date starttime;
		long seconds = 0;
		setRequest(request);
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (request.getMethod().equalsIgnoreCase("GET")) {
			content = request.getQueryString();
			content = URLDecoder.decode(content, "UTF-8");
		} else if (request.getMethod().equalsIgnoreCase("POST")) {
			ServletInputStream inputStream = request.getInputStream();
			StringBuffer sb = new StringBuffer();
			int i;
			while ((i = inputStream.read()) != -1) {
				sb.append((char) i);
			}
			content = sb.toString();
		}
		content = content.replace("params=", "");
		JSONObject jsonObj = new JSONObject(content);
		if (!jsonObj.has("type")) {
			response = "ERROR";
			responseDetail = "Detail : form type not found";
			jsonResponse.put("response", response);
			jsonResponse.put("details", responseDetail);
			out.println(jsonResponse);
			out.flush();
			return;
		}
		String[] fixedParameters = { "type", "username", "password",
				"location", "entereddate" };
		for (String param : fixedParameters) {
			if (!jsonObj.has(param)) {
				response = "ERROR";
				responseDetail = "Detail : Mandatory parameter(s) not found.";
				jsonResponse.put("response", response);
				jsonResponse.put("details", responseDetail);
				out.println(jsonResponse);
				out.flush();
				return;
			}
		}
		String requestType = jsonObj.getString("type");
		String username = jsonObj.getString("username");
		Map<String, Object> fixedParamsMap = new HashMap<String, Object>();
		for (String eachparam : fixedParameters) {
			fixedParamsMap.put(eachparam, jsonObj.getString(eachparam));
			jsonObj.remove(eachparam);
		}
		try {
			dateEntered = DateTimeUtil.getDateFromString(
					fixedParamsMap.get("entereddate").toString(), DateTimeUtil.SQL_DATE);
		} catch (ParseException e) {
			e.printStackTrace();
			response = "ERROR";
			responseDetail = "Detail: Parse exception for Date, Please provide valid format";
			jsonResponse.put("response", response);
			jsonResponse.put("details", responseDetail);
			out.println(jsonResponse);
			out.flush();
			return;
		}
		
		boolean login1 = apiService.login(guestUsername, guestPassword,
				fixedParamsMap.get("location").toString());
		if (!login1) {
			jsonResponse.put("response", "ERROR");
			jsonResponse
					.put("details",
							"Detail: Unable to authenticate, Please provide a valid username and password to login");
			out.println(jsonResponse);
			out.flush();
			return;
		}
		
		boolean login = login(fixedParamsMap.get("username").toString(),
				fixedParamsMap.get("password").toString());
		if (!login) {
			jsonResponse.put("response", "ERROR");
			jsonResponse
					.put("details",
							"Detail: Unable to authenticate, Please provide a valid username and password to login");
			out.println(jsonResponse);
			out.flush();
			return;
		}
		if (requestType.equals(RequestType.LOGIN)) {
			jsonResponse = doLogin(fixedParamsMap.get("username").toString(),
					fixedParamsMap.get("password").toString(), fixedParamsMap.get("location").toString());
		} else if (requestType.equals(RequestType.FAST_SCREENING)) {
			Map<String, Object> params = parseContent(jsonObj);
			jsonResponse = doScreening(fixedParamsMap.get("username").toString(), requestType, dateEntered, params /* TODO: More parameters */);
		}
		out.println(jsonResponse);
		out.flush();
		// out.close();
		return;
	}

	/**
	 * This function expects the user to exist in the OpenMRS database. Once
	 * authenticated, the user is inserted into the other database Note! The
	 * database name for OpenMRS is hard-coded, i.e. "openmrs"
	 */
	@Override
	protected boolean login(String username, String password)
			throws JSONException {
		boolean result = false;
		try {
			if (username != null && password != null) {
				List<List<Object>> data = apiService
						.getMetadataService()
						.executeSQL(
								"select password, salt from openmrs.users inner join openmrs.user_role using (user_id) where username = '"
										+ username
										+ "' and retired = 0 and role in ('System Developer', 'Provider', 'Program Admin')",
								true);
				if (data != null) {
					String hash = data.get(0).get(0).toString();
					String salt = data.get(0).get(1).toString();
					if (SecurityUtil.hashMatches(hash, password + salt)) {
						result = true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	

	public JSONObject doLogin(String username, String password, String location)
			throws JSONException {
		// TODO: Check AicWebService doLogin to get an idea
		
		// Check if the location is present in the database
		Location locationByName = apiService.getLocationService().getLocationByName(location);
		// If not, then import location with attributes from OpenMRS
		if(locationByName == null) {
			boolean created = importLocation(location);
			if (!created) {
				// TODO: ERROR
			}
		}
		// See if the user is present in the database
		Users userByUsername = apiService.getUserService().getUserByUsername(username);
		// If not, then import user with attributes from OpenMRS
		if(userByUsername == null) {
			boolean created = importUser(username);
			if (!created) {
				// TODO: ERROR
			}
		}
		return null;
	}
	
	
	/**
	 * Saves screening form as UserForm with results
	 * 
	 * @param username
	 * @param formType
	 * @param dateEntered
	 * @param results
	 * @return
	 * @throws JSONException
	 */
	public JSONObject doScreening(String username, String formType, Date dateEntered, Map<String, Object> results /* TODO: define parameters */)
			throws JSONException {

		
		String response = "";
		String responseDetail = "";
		JSONObject jsonResponse = new JSONObject();
		UserFormType userFormType = apiService.getUserFormService()
				.getUserFormTypeByName(formType);
		Users user = apiService.getUserService().getUserByUsername(username);
		UserForm userForm = new UserForm(userFormType,
				user, dateEntered,
				user,
				ServerService.getCurrentLocation(), new Date(), null);
		userForm.setDateEntered(dateEntered);
		userForm.setDurationSeconds(20);
		ArrayList<UserFormResult> userFormResults = new ArrayList<UserFormResult>();
		for (String key : results.keySet()) {
			Element element = apiService.getEncounterService()
					.getElementByName(key);
			String value = results.get(key).toString();
			userFormResults.add(new UserFormResult(element, value, userForm));
		}
		try {
			userForm = apiService.getUserFormService().saveUserForm(userForm,
					userFormResults);
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (userForm.getUserFormId() != null) {
			response = "SUCCESS";
			responseDetail = "Detail: submitted installation form with id : "
					+ userForm.getUserFormId();
			jsonResponse.put("response", response);
			jsonResponse.put("details", responseDetail);
		} else {
			response = "ERROR";
			responseDetail = "Detail:  Unable to submit form";
			jsonResponse.put("response", response);
			jsonResponse.put("details", responseDetail);
		}
		return jsonResponse;
	}
	
	class ScheduledTask extends TimerTask {
		public void run() {
			try {
				apiService
				.getMetadataService()
				.executeSQL(
						"select person_id from gfatm.patient", true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
