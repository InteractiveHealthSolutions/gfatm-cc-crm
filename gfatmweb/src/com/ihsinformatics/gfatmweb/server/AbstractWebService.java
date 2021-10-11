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
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ihs.emailer.EmailEngine;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ihsinformatics.tbreachapi.core.service.impl.ServerService;
import com.ihsinformatics.tbreachapi.model.Location;
import com.ihsinformatics.util.DateTimeUtil;

/**
 * This class provides abstract functionality to core web service classes
 * 
 * @author owais.hussain@ihsinformatics.com
 *
 */
public abstract class AbstractWebService extends HttpServlet {

	private static final long serialVersionUID = -4482413651235453707L;
	public static final String PROP_FILE_NAME = "tbreach-api.properties";
	protected HttpServletRequest request;
	public static Properties prop;
	public static ServerService apiService = new ServerService();
	public static String guestUsername = "";
	public static String guestPassword = "";
	public static String ccUsername = "";
	public static String ccPassword = "";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handleRequest(req, resp);
			super.doGet(req, resp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			handleRequest(req, resp);
			super.doPost(req, resp);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected abstract void handleRequest(HttpServletRequest request,
			HttpServletResponse resp) throws IOException, JSONException;

	protected abstract boolean login(String username, String password)
			throws JSONException;

	/**
	 * Generates response in the same format as content-type
	 * 
	 * @param errorMessage
	 * @return
	 */
	protected String getError(String errorMessage) {
		return null;
	}

	/**
	 * This method parses parameters from HTTP request according to the
	 * content-type set in client request
	 * 
	 * @param contentType
	 * @return Map<String, Object>
	 * @throws JSONException
	 */
	public Map<String, Object> parseContent(JSONObject jsonObject)
			throws JSONException {
		Map<String, Object> params = new HashMap<>();
		JSONArray subJsonArray = jsonObject.getJSONArray("results");
		for (int j = 0; j < subJsonArray.length(); j++) {
			JSONObject jsonobject = subJsonArray.getJSONObject(j);
			String name = jsonobject.getString("name");
			String value = jsonobject.getString("value");
			params.put(name, value);
		}
		params.put("endtime", DateTimeUtil.getSqlDateTime(new Date()));
		return params;
	}

	/**
	 * Send email using EmailEngine
	 * 
	 * @param subject
	 * @param text
	 */
	public void sentEmail(String subject, String text) {
		try {
			EmailEngine.getInstance().emailReportToAdmin(subject, text);
			System.out.println("Email sent...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}
	
	/* Project-specific methods */

	/**
	 * Imports user along with properties from OpenMRS DB into the database
	 * @param username
	 * @return
	 */
	protected boolean importUser(String username) {
		try {
			StringBuilder query = new StringBuilder();
			// TODO: Complete query. Check importLocation method
			List<List<Object>> user = apiService.getMetadataService().executeSQL(query.toString(), true);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Imports location along with properties from OpenMRS DB into the database
	 * @param location
	 * @return
	 */
	protected boolean importLocation(String location) {
		try {
			StringBuilder query = new StringBuilder();
			query.append("select l.location_id, l.name as location_name, lt.value_reference as category, l.description, l.address1, l.address2, l.address3 as landmark, l.city_village, l.state_province, l.country, l.latitude, l.longitude, pc.value_reference as primary_contact, l.date_created, cr.username as created_by, l.uuid from openmrs.location as l ");
			query.append("left outer join openmrs.location_attribute as lt on lt.location_id = l.location_id and lt.attribute_type_id = 9 ");
			query.append("left outer join openmrs.location_attribute as pc on lt.location_id = l.location_id and lt.attribute_type_id = 2 ");
			query.append("inner join openmrs.users as cr on cr.user_id = l.creator ");
			query.append("where name = '" + location + "'");
			List<List<Object>> data = apiService.getMetadataService().executeSQL(query.toString(), true);
			if (data.size() == 0) {
				System.out.println("ERROR: No location found matching given name.");
				return false;
			}
			// We're interested only in first record
			List<Object> locationObj = data.get(0);
			Location newLocation = new Location(location);
			int counter = 2;
			newLocation.setCategory(locationObj.get(counter++).toString());
			newLocation.setAddress1(locationObj.get(counter++).toString());
			newLocation.setAddress2(locationObj.get(counter++).toString());
			newLocation.setLandmark1(locationObj.get(counter++).toString());
			newLocation.setCityVillage(locationObj.get(counter++).toString());
			newLocation.setStateProvince(locationObj.get(counter++).toString());
			newLocation.setCountry(locationObj.get(counter++).toString());
			newLocation.setLatitude(locationObj.get(counter++).toString());
			newLocation.setLongitude(locationObj.get(counter++).toString());
			newLocation.setPrimaryContact(locationObj.get(counter++).toString());
			try {
				newLocation.setDateCreated(DateTimeUtil.getDateFromString(locationObj.get(counter++).toString(), DateTimeUtil.SQL_DATETIME));
			} catch (ParseException e) {
			}
			newLocation.setUsersByCreatedBy(ServerService.getCurrentUser());
			newLocation.setUuid(locationObj.get(counter++).toString());
			newLocation = apiService.getLocationService().saveLocation(newLocation);
			return newLocation.getLocationId() != null;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
