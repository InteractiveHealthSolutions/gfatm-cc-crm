package com.ihsinformatics.gfatmweb.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.print.DocFlavor.STRING;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;

import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.ihs.emailer.EmailEngine;
import org.ihs.emailer.EmailException;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ihsinformatics.gfatmweb.shared.CustomMessage;
import com.ihsinformatics.gfatmweb.shared.DateTimeUtil;
import com.ihsinformatics.gfatmweb.shared.ErrorType;
import com.ihsinformatics.gfatmweb.shared.RequestType;
import com.ihsinformatics.tbreachapi.core.TBR;
import com.ihsinformatics.tbreachapi.core.service.impl.ServerService;
import com.ihsinformatics.tbreachapi.model.Element;
import com.ihsinformatics.tbreachapi.model.UserForm;
import com.ihsinformatics.tbreachapi.model.UserFormResult;
import com.ihsinformatics.tbreachapi.model.UserFormType;
import com.ihsinformatics.util.CommandType;

public class CallCenterWebService extends AbstractWebService {

	private static final long serialVersionUID = 2753102501559991028L;
	private Date dateEntered;
	private String lastDate = "";
	private String currentDate = "";
	DateFormat dformat = new SimpleDateFormat(DateTimeUtil.SQL_DATE); 
	SimpleDateFormat format = new SimpleDateFormat(DateTimeUtil.SQL_DATE);
	private int day = 0;
	private LocalDate currentDates;
	final static Logger logger = Logger.getLogger(CallCenterWebService.class);
	private int numberRowInsert = 0, numberRowNotInsert = 0;
	private String baseUrl = "", userForm = "", authKey = "";

	public CallCenterWebService() {
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
	
		ccUsername = prop.getProperty("callcenter.username");
		ccPassword = prop.getProperty("callcenter.password");
		baseUrl = prop.getProperty("callcenter.apibaseUrl");
		authKey = prop.getProperty("callcenter.authKey");
		userForm = prop.getProperty("callcenter.userForm");
	}

	@Override
	public void handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws IOException, JSONException {
		PrintWriter writer = response.getWriter();
		writer.println(precondition());
		writer.flush();
	}

	/**
	 * This method is used to apply the prerequiset conditions we apply for
	 * login after login we divide the total days into 7day. ..
	 */
	public JSONObject precondition() {

		JSONObject returnVal = new JSONObject();
		lastDate = getLastDate();
		// lastDate = "2017-10-17";
		currentDates = new LocalDate();
		day = Days.daysBetween(new LocalDate(lastDate),
				new LocalDate(currentDates)).getDays();
		// there is only one user for call center
		//boolean login = apiService.login(ccUsername, ccPassword);
		if (apiService.login(ccUsername, ccPassword)) {
			long numberIteration = day / 7;
			JSONArray jsonArray = new JSONArray();
			int exceedDays = 0;
			final String fixedCurrentDate = currentDates.toString();// fixed
			for (int i = 0; i <= numberIteration; i++) {

				if (day > 7) {
					// we need exclude the exceed days from current Date.
					exceedDays = day - 7;
					LocalDate fixedDate = currentDates.minusDays(exceedDays);
					currentDate = fixedDate.toString();
					// returnVal = processData(lastDate, currentDate);
					returnVal = processData(lastDate, currentDate);
					// updat the day and laste date
					lastDate = currentDate;
					day = exceedDays;

				} else {
					returnVal = processData(lastDate, fixedCurrentDate);
				}
				
				if (returnVal.length() > 0 || returnVal != null) {
					jsonArray.put(returnVal);
					returnVal = null;
				}
			}
			returnVal = new JSONObject();

			return returnVal;

		} else {
			returnVal.put("response", "ERROR");
			returnVal
					.put("details",
							"Detail: Unable to authenticate, Please provide a valid username and password to login");
			return returnVal;
		}
	}

	@Override
	public boolean login(String username, String password) {
		return apiService.getAuthenticationService().authenticate(username,
				password);
	}

	public JSONObject processData(String lastDate, String currentDate) {

		JSONObject jsonResponse = new JSONObject();
		JSONObject returnResponse = new JSONObject();

		JSONArray elementValueList = getHttpRequest(baseUrl + "?start_date="
				+ lastDate + "&end_date=" + currentDate, authKey);
		// In case when 'No record found' against the date rang, then we return
		if (elementValueList == null || elementValueList.length() == 0) {
			try {
				jsonResponse.put("response", "Empty:[]");
				jsonResponse.put("details", "Detail: No Record Found");
				returnResponse = jsonResponse;

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			returnResponse = importData(elementValueList);
		}
		return returnResponse;
	}

	/**
	 * HTTP GET request
	 * @param url
	 * @param authKey
	 * @return {@link JSONArray}
	 */
	public JSONArray getHttpRequest(String url, String authKey) {
		JSONArray returnJsonArray = new JSONArray();
   
        ///sef signed certificate used
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs,
					String authType) {
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc;
		try {
				sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection
						.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException e2) {
			e2.printStackTrace();
		}
		// Create all-trusting host name verifier
		  HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		
		
		try {
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
			con.setRequestProperty("Authorization", "Basic " + authKey);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
			BufferedReader bufferedData = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String responseData;
			StringBuffer response = new StringBuffer();
			while ((responseData = bufferedData.readLine()) != null) {
				response.append(responseData);
			}
			bufferedData.close();

			try {
				JSONObject jsonObj = new JSONObject(response.toString());
				returnJsonArray = jsonObj.getJSONArray("data");

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return returnJsonArray;
	}

	/**
	 * Iterate the JSONArray and send the JSONObject to saveForm result . append
	 * each result and then return.
	 * @param data
	 * @return {@link JSONArray}
	 */
	public JSONObject importData(JSONArray data) {

		String formType = RequestType.CC_CRM_FORM;
		JSONArray arrayResponse = new JSONArray();
		JSONObject returnResponse = new JSONObject();
		String result = "";
		JSONObject object = null;
		for (int i = 0; i < data.length(); i++) {
			try {

				object = (JSONObject) data.get(i);
				result = saveForm(formType, object);
			} catch (JSONException e) {
				result = e.getMessage();
				e.printStackTrace();
			} catch (PatternSyntaxException e) {
				result = e.getMessage();
				e.printStackTrace();
			} catch (HibernateException e) {
				result = e.getMessage();
				e.printStackTrace();
			} catch (ValidationException e) {
				result = e.getMessage();
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				result = e.getMessage();
				e.printStackTrace();
			} finally {

				try {
					JSONObject jsonResponse = new JSONObject();
					if (result.equalsIgnoreCase("SUCCESS")) {
						numberRowInsert++;
						jsonResponse.put("Index", i);
						jsonResponse
								.put("Lead ID", object.getString("lead_id"));
						jsonResponse.put("uuid", object.isNull("uuid") ? "NULL"
								: object.getString("uuid"));
						jsonResponse.put("Status", result);
						arrayResponse.put(jsonResponse);
					} else {
						numberRowNotInsert++;
						jsonResponse.put("Index", i);
						jsonResponse
								.put("Lead ID", object.getString("lead_id"));
						jsonResponse.put("uuid", object.isNull("uuid") ? "NULL"
								: object.getString("uuid"));
						jsonResponse.put("Status", result);
						datalog(object, result);
						logger.error(jsonResponse);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			returnResponse.put("Number Of Row Inserted", numberRowInsert);
			returnResponse
					.put("Number Of Row Not Inserted", numberRowNotInsert);
			// returnResponse.put("Return Data", arrayResponse);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return returnResponse;
	}

	/**
	 * Responsible for save the User Form and User Result Form
	 * 
	 * @param formType
	 * @param result
	 * @return {@link STRING}
	 * @throws HibernateException
	 * @throws JSONException
	 * @throws PatternSyntaxException
	 * @throws ValidationException
	 * @throws ClassNotFoundException
	 */
	public String saveForm(String formType, JSONObject result)
			throws HibernateException, JSONException, PatternSyntaxException,
			ValidationException, ClassNotFoundException {
		String responseDetail = "";
		String errorMessage = "";
		UserForm userForm = null;
		UserFormType userFormType = apiService.getUserFormService()
				.getUserFormTypeByName(formType.toString());
		// this line of code used to convert the timestamp to date.
		//long dates = Long.valueOf(result.getString("date_entered"));
		//Date d = new Date(dates * 1000);
		
		try {
			Date d = dformat.parse(result.getString("date_entered"));
			String datets = format.format(d);
			dateEntered = format.parse(datets);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			userForm = new UserForm(userFormType,
					ServerService.getCurrentUser(), dateEntered,
					ServerService.getCurrentUser(),
					ServerService.getCurrentLocation(), new Date(), null);
			userForm.setDateEntered(dateEntered);
			userForm.setUuid(result.getString("uuid"));

			List<UserFormResult> userFormResults = new ArrayList<UserFormResult>();
			JSONArray names = result.names();
			for (int i = 0; i < names.length(); i++) {
				String value;
				Element element = apiService.getEncounterService()
						.getElementByName(names.getString(i));
				String name = names.getString(i);
				
				if(!name.equals("source_of_referral"))
					value = result.isNull(name) ? "Null" : result.getString(name).toString();
				else
					value =  result.isNull(name) ? "Null" : result.getJSONArray("source_of_referral").toString();
				//value = result.isNull(naam) ? "Null" : result.getString(naam);
				// this check is because we don't need to insert some data who
				// don't have element id // result form just like uuid
				if (element != null) {
					UserFormResult userFormResult = new UserFormResult(element,
							value, userForm);
					userFormResults.add(userFormResult);
				} else {
					logger.error("Not saved :" + name + ": " + value);
				}
			}
			// save the user form result with respect to user form .
			userForm = apiService.getUserFormService().saveUserForm(userForm,
					userFormResults);
		} catch (PatternSyntaxException e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
		} catch (HibernateException e) {
			errorMessage = CustomMessage
					.getErrorMessage(ErrorType.DUPLICATION_ERROR);
			e.printStackTrace();
		} catch (ValidationException e) {
			errorMessage = CustomMessage
					.getErrorMessage(ErrorType.INVALID_VALUE);
		} catch (ClassNotFoundException e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
		} catch (JSONException e) {
			errorMessage = CustomMessage.getErrorMessage(ErrorType.NULL_VALUE);
		} catch (NullPointerException e) {
			e.getMessage();
			e.getStackTrace();
			e.printStackTrace();
		} finally {

			if (userForm.getUserFormId() != null) {
				responseDetail = "SUCCESS";
			} else {
				responseDetail = "ERROR:" + errorMessage;
			}
		}
		return responseDetail;
	}

	/**
	 * Get the last date from gfatm database, get the maximum date_created.
	 * @return
	 */
	public String getLastDate() {
		String query = "SELECT MAX(date_created) FROM user_form where user_form_type_id =9";
		String lastDate = "";
		List<List<Object>> list;
		try {
			list = apiService.getMetadataService().executeSQL(query, true);
			Object lastDateobj = list.get(0).get(0); // /previouse inserted date
			LocalDate lastDates = new LocalDate(lastDateobj);
			lastDate = lastDates.toString();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return lastDate;
	}

	/**
	 * datalog method is used here to save the error data into gfatm database.
	 * this errors are insert into dataLog table.
	 * @param obj
	 * @param description
	 */
	public void datalog(JSONObject obj, String description) {
		String query = " insert into data_log (log_id,log_type,entity_name,record,description,date_created,created_by,created_at,uuid)"
				+ "VALUES (0,'"
				+ CommandType.ALTER
				+ "','"
				+ userForm
				+ "','"
				+ obj.toString()
				+ "','"
				+ description
				+ "','"
				+ new LocalDate()
				+ "',131,2,'"
				+ UUID.randomUUID().toString() + "')";
		try {

			List<List<Object>> list = apiService.getMetadataService()
					.executeSQL(query, false);

			System.out.print(list.toString());

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
