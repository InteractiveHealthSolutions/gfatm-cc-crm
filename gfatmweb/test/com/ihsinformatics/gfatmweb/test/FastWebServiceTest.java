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
package com.ihsinformatics.gfatmweb.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ihsinformatics.gfatmweb.server.AicWebService;

/**
 * @author owais.hussain@ihsinformatics.com
 *
 */
public class FastWebServiceTest {

	private static final String URL = "http://127.0.0.1:8888/fastweb.jsp";

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new AicWebService();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * Test method for Login functionality. Takes form type, username, password
	 * as a queryString and authenticate login operation against provided
	 * credentials.
	 */
	@Test
	public final void testLoginRequest() {
		JSONObject params = new JSONObject();
		try {
			params.put("type", "login");
			params.put("username", "admin");
			params.put("password", "Admin123");
			params.put("location", "IHS");
			params.put("entereddate", "2016-08-29");

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String queryString = "?params=";
		String response = "";
		queryString = queryString + params;
		System.out.println(queryString);
		HttpURLConnection httpConnection = null;
		int responseCode = 0;
		URL url;
		try {
			url = new URL(URL + queryString);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
			httpConnection.setRequestProperty("Accept-Language",
					"en-US,en;q=0.5");
			httpConnection.setDoOutput(true);
			httpConnection.connect();
			responseCode = httpConnection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("GET parameters : " + queryString);
			System.out.println("Response Code : " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpConnection.getInputStream()));
				String inputLine;
				StringBuffer responseBuffer = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					responseBuffer.append(inputLine);
				}
				in.close();
				System.out.println("Response:" + responseBuffer.toString());
				
				JSONObject jsonObj = new JSONObject(responseBuffer.toString());
				response = jsonObj.getString("response");
			} else {
				response = "ERROR";
			}
			assertTrue("Login failed.", response.equals("SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpConnection.disconnect();
		}
	}

	/**
	 * Test method for Login functionality. Takes form type, username, password
	 * as a queryString and authenticate login operation against provided
	 * credentials.
	 */
	@Test
	public final void shouldNotLogin() {
		JSONObject params = new JSONObject();
		try {
			params.put("type", "login");
			params.put("username", "admin123");
			params.put("password", "Admin123");
			params.put("location", "IHS");
			params.put("entereddate", "2016-08-29");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String queryString = "?params=";
		String response = "";
		queryString = queryString + params;
		System.out.println(queryString);
		HttpURLConnection httpConnection = null;
		int responseCode = 0;
		URL url;
		try {
			url = new URL(URL + queryString);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
			httpConnection.setRequestProperty("Accept-Language",
					"en-US,en;q=0.5");
			httpConnection.setDoOutput(true);
			httpConnection.connect();
			responseCode = httpConnection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("GET parameters : " + queryString);
			System.out.println("Response Code : " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpConnection.getInputStream()));
				String inputLine;
				StringBuffer responseBuffer = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					responseBuffer.append(inputLine);
				}
				in.close();
				System.out.println(responseBuffer.toString());
				JSONObject jsonObj = new JSONObject(responseBuffer.toString());
				response = jsonObj.getString("response");
			} else {
				response = "ERROR";
			}
			assertTrue("Login successful on invalid credentials.",
					!response.equals("SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpConnection.disconnect();
		}
	}

	/**
	 * Test method for Installation form. elementArray contains keys for
	 * queryString while valueArray contains value for each value. Passes query
	 * string to Output stream and set request type to JSON. In response if form
	 * completely executed returned with a success response message.
	 */
	@Test
	public final void testScreeningFormRequest() {
		StringBuilder query = new StringBuilder();
		query.append("?params=");
		JSONObject params = new JSONObject();
		try {
			params.put("type", "fast_screening");
			params.put("username", "admin");
			params.put("password", "Admin123");
			params.put("location", "IRD");
			params.put("entereddate", "2016-08-29");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		JSONArray resultsArray = new JSONArray();
		String[] elements = { "starttime", "screening_location", "gender" };
		String[] values = { "2016-08-30 14:20:00", "IHS", "M" };
		for (int i = 0; i < elements.length; i++) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("name", elements[i]);
				obj.put("value", values[i]);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			resultsArray.put(obj);
		}
		try {
			params.put("results", resultsArray);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		query.append(params);
		String response = "";
		HttpURLConnection httpConnection = null;
		int responseCode = 0;
		URL url;
		try {
			url = new URL(URL + query);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
			httpConnection.setRequestProperty("Accept-Language",
					"en-US,en;q=0.5");
			httpConnection.setDoOutput(true);
			httpConnection.connect();
			DataOutputStream wr = new DataOutputStream(
					httpConnection.getOutputStream());
			wr.writeBytes(query.toString());
			wr.flush();
			wr.close();
			responseCode = httpConnection.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + query);
			System.out.println("Response Code : " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {

				BufferedReader in = new BufferedReader(new InputStreamReader(
						httpConnection.getInputStream()));
				String inputLine;
				StringBuffer responseBuffer = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					responseBuffer.append(inputLine);
				}
				in.close();
				System.out.println(responseBuffer.toString());
				JSONObject jsonObj = new JSONObject(responseBuffer.toString());
				response = jsonObj.getString("response");
			} else {
				response = "ERROR";
			}
			assertTrue("Installation Form failed.", response.equals("SUCCESS"));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpConnection.disconnect();
		}
	}
}
