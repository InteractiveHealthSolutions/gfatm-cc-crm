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
import java.sql.SQLException;
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
import org.json.JSONArray;

import com.google.gwt.dev.json.JsonObject;
import com.ihsinformatics.gfatmweb.shared.RequestType;
import com.ihsinformatics.tbreachapi.core.TBR;
import com.ihsinformatics.tbreachapi.core.service.impl.ServerService;
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
public class GfatmTasksWebService extends AbstractWebService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6109274182101835832L;
	private static final String encoding = "UTF-8";

	public GfatmTasksWebService() {
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
		ScheduledTask st = new ScheduledTask(); // Instantiate SheduledTask
												// class
		time.schedule(st, 0, 25200000); // run function for sql
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
		String[] fixedParameters = { "type", "username", "password", "location" };
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
					fixedParamsMap.get("password").toString(), fixedParamsMap
							.get("location").toString());
		} else if (requestType.equals(RequestType.GFATM_SEARCH)) {
			Map<String, Object> params = parseContent(jsonObj);

			jsonResponse = doSearch(fixedParamsMap.get("username").toString(),
					requestType, jsonObj.get("age_range").toString(),
					jsonObj.get("gender").toString(), params);

		} else if (requestType.equals(RequestType.GFATM_FEEDBACK)) {
		
			
			try {				
				dateEntered = DateTimeUtil.getDateFromString(
						jsonObj.get("entereddate").toString(), DateTimeUtil.SQL_DATE);
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
		Location locationByName = apiService.getLocationService()
				.getLocationByName(location);
		// If not, then import location with attributes from OpenMRS
		if (locationByName == null) {
			boolean created = importLocation(location);
			if (!created) {
				// TODO: ERROR
			}
		}
		// See if the user is present in the database
		Users userByUsername = apiService.getUserService().getUserByUsername(
				username);
		// If not, then import user with attributes from OpenMRS
		if (userByUsername == null) {
			boolean created = importUser(username);
			if (!created) {
				// TODO: ERROR
			}
		}
		return null;
	}

	public JSONObject doSearch(String username, String formType,
			String ageRange, String gender, Map<String, Object> params) {
		String response = "";
		String responseDetails = "";
		JSONObject jsonResponse = new JSONObject();
		StringBuilder query;
		List<List<Object>> data;
		String[] persons;
		JSONArray personDetailsArray = new JSONArray();

		String lowerAge = ageRange.split("-")[0].trim();
		String upperAge = ageRange.split("-")[1].trim();

		try {
			query = new StringBuilder();

			// search on age_range and gender is mandatory
			query.append("select p.person_id from openmrs.person as p ");
			query.append("where p.voided = 0 AND TIMESTAMPDIFF(YEAR,p.birthdate,CURDATE()) >= "
					+ lowerAge
					+ " AND TIMESTAMPDIFF(YEAR,p.birthdate,CURDATE()) <= "
					+ upperAge + " AND p.gender = '" + gender + "'");

			data = apiService.getMetadataService().executeSQL(query.toString(),
					true);
			persons = convertToArray(data);

			if (persons.length > 0) {

				// filter search
				if (params.containsKey("PATIENT_IDENTIFIER")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select pa.patient_id from openmrs.patient as pa ");
					query.append("INNER JOIN openmrs.patient_identifier as pi ON pi.patient_id = pa.patient_id ");
					query.append("where pi.identifier = '"
							+ params.get("PATIENT_IDENTIFIER")
							+ "' AND pi.identifier_type = 3 AND pi.voided = 0 AND pa.patient_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("CNIC")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select p.person_id from openmrs.person as p ");
					query.append("INNER JOIN openmrs.person_attribute as pa ON pa.person_id = p.person_id ");
					query.append("INNER JOIN openmrs.person_attribute_type as pat ON pat.person_attribute_type_id = pa.person_attribute_type_id ");
					query.append("where pat.name = 'National ID' AND pa.voided = 0 AND pa.value = '"
							+ params.get("CNIC")
							+ "' AND p.person_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("CONTACT_NUMBER")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select p.person_id from openmrs.person as p ");
					query.append("INNER JOIN openmrs.person_attribute as pa ON pa.person_id = p.person_id ");
					query.append("INNER JOIN openmrs.person_attribute_type as pat ON pat.person_attribute_type_id = pa.person_attribute_type_id ");
					query.append("where pat.name = 'Primary Contact' AND pa.voided = 0 AND pa.value = '"
							+ params.get("CONTACT_NUMBER")
							+ "' AND p.person_id IN ("
							+ getString(persons)
							+ ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("GUARDIAN_NAME")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select p.person_id from openmrs.person as p ");
					query.append("INNER JOIN openmrs.person_attribute as pa ON pa.person_id = p.person_id ");
					query.append("INNER JOIN openmrs.person_attribute_type as pat ON pat.person_attribute_type_id = pa.person_attribute_type_id ");
					query.append("where pat.name = 'Guardian Name' AND pa.voided = 0 AND pa.value LIKE '%"
							+ params.get("GUARDIAN_NAME")
							+ "%' AND p.person_id IN ("
							+ getString(persons)
							+ ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("MOTHER_NAME")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select p.person_id from openmrs.person as p ");
					query.append("INNER JOIN openmrs.person_attribute as pa ON pa.person_id = p.person_id ");
					query.append("INNER JOIN openmrs.person_attribute_type as pat ON pat.person_attribute_type_id = pa.person_attribute_type_id ");
					query.append("where pat.name = 'Mother Name' AND pa.voided = 0 AND pa.value LIKE '%"
							+ params.get("MOTHER_NAME")
							+ "%' AND p.person_id IN ("
							+ getString(persons)
							+ ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("PERSON_NAME")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select p.person_id from openmrs.person p ");
					query.append("INNER JOIN openmrs.person_name pn ON p.person_id = pn.person_id ");
					query.append("where concat('', pn.given_name, ' ', pn.family_name) LIKE '%"
							+ params.get("PERSON_NAME")
							+ "%' AND pn.voided = 0 AND p.person_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("HEALTH_CENTRE")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("select pi.patient_id from openmrs.patient_identifier as pi ");
					query.append("INNER JOIN openmrs.location as l ON pi.location_id = l.location_id ");
					query.append("where l.description = '"
							+ params.get("HEALTH_CENTRE")
							+ "' AND pi.identifier_type = 3 AND l.retired = 0 AND pi.voided = 0 AND p.person_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				} else if (params.containsKey("PROGRAM")) {

					query.setLength(0); // clearing query StringBuilder

					query.append("SELECT pp.patient_id from openmrs.patient_program as pp ");
					query.append("INNER JOIN openmrs.program as pg ON pp.program_id = pg.program_id  ");
					query.append("where pg.name = '"
							+ params.get("PROGRAM")
							+ "' AND pp.voided = 0 AND pg.retired = 0 AND pp.patient_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					persons = convertToArray(data);

				}

				if (persons.length > 0) {

					query.setLength(0); // clearing query StringBuilder

					// finally search for person details filtered in 'persons'
					// array
					query.append("select p.uuid, pi.identifier, concat('', pn.given_name, ' ' , pn.family_name) as full_name, p.gender, p.birthdate, TIMESTAMPDIFF(YEAR,p.birthdate,CURDATE()) as age from openmrs.person as p ");
					query.append("INNER JOIN openmrs.person_name as pn ON pn.person_id = p.person_id ");
					query.append("INNER JOIN openmrs.patient_identifier as pi ON p.person_id = pi.patient_id ");
					query.append("where p.voided = 0 AND pn.voided = 0 AND pi.voided = 0 AND pi.identifier_type = 3 AND p.person_id IN ("
							+ getString(persons) + ")");

					data = apiService.getMetadataService().executeSQL(
							query.toString(), true);
					personDetailsArray = getPersonDetailArray(data);

				}

				if (personDetailsArray != null
						&& personDetailsArray.length() > 0) {
					response = "SUCCESS";
					responseDetails = "Detail :  Data found";

					jsonResponse.put("response", response);
					jsonResponse.put("details", responseDetails);
					jsonResponse.put("personArray", personDetailsArray);
				} else {
					response = "ERROR";
					responseDetails = "Detail : No data found for matching criteria";
					jsonResponse.put("response", response);
					jsonResponse.put("details", responseDetails);
				}

			} else {
				response = "ERROR";
				responseDetails = "Detail : No data found for matching criteria";
				jsonResponse.put("response", response);
				jsonResponse.put("details", responseDetails);
			}

		} catch (SQLException | JSONException e) {
			e.printStackTrace();
		}

		return jsonResponse;
	}

	public String[] convertToArray(List<List<Object>> data) {

		ArrayList<String> id = new ArrayList<String>();

		for (List<Object> dataObj : data) {
			id.add(dataObj.get(0).toString());
		}

		return id.toArray(new String[0]);

	}

	public JSONArray getPersonDetailArray(List<List<Object>> data) {

		JSONArray personDetails = new JSONArray();
		JSONObject person;

		for (List<Object> dataObj : data) {
			person = new JSONObject();
			try {
				person.put("uuid", dataObj.get(0).toString());
				person.put("identifier", dataObj.get(1).toString());
				person.put("fullName", dataObj.get(2).toString());
				person.put("gender", dataObj.get(3).toString());
				person.put("dob", dataObj.get(4).toString());
				person.put("age", dataObj.get(5).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			personDetails.put(person);
		}

		return personDetails;

	}

	public String getString(String[] array) {

		StringBuilder strBuilder = new StringBuilder();

		for (int i = 0; i < array.length; i++) {

			if (i != 0)
				strBuilder.append(",");
			strBuilder.append(array[i]);

		}
		return strBuilder.toString();

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
	public JSONObject doScreening(String username, String formType,
			Date dateEntered, Map<String, Object> results /*
														 * TODO: define
														 * parameters
														 */)
			throws JSONException {

		String response = "";
		String responseDetail = "";
		JSONObject jsonResponse = new JSONObject();
		UserFormType userFormType = apiService.getUserFormService()
				.getUserFormTypeByName(formType);
		Users user = apiService.getUserService().getUserByUsername(username);
		UserForm userForm = new UserForm(userFormType, user, dateEntered, user,
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
				apiService.getMetadataService().executeSQL(
						"select person_id from gfatm.patient", true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
