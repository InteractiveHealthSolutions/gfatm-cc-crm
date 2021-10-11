package com.ihsinformatics.gfatmweb.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.ValidationException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.ClientProtocolException;
import org.hibernate.HibernateException;
import org.ihs.emailer.EmailEngine;
import org.ihs.emailer.EmailException;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import sun.net.www.protocol.http.HttpURLConnection;

import com.ihsinformatics.gfatmweb.server.AbstractWebService;
import com.ihsinformatics.gfatmweb.server.CallCenterWebService;
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

/**
 * @author Shujaat
 *
 */

public class CallCenterTestCases  extends AbstractWebService{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1244262619939218862L;

	
	static CallCenterWebService callcenter;
	protected static ServerService apiService = AbstractWebService.apiService;
	Date dateEntered;
	String lastDate = "";
	String currentDate = "";
	private static String baseUrl = "", ccUserForm = "", authKey = "";
	SimpleDateFormat format = new SimpleDateFormat(DateTimeUtil.SQL_DATE);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		callcenter = new CallCenterWebService();
	
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
			ccUsername = prop.getProperty("callcenter.username");
			ccPassword = prop.getProperty("callcenter.password");
			//baseUrl = prop.getProperty("callcenter.apibaseUrl");
			baseUrl = prop.getProperty("callcenter.baseUrl");
			authKey = prop.getProperty("callcenter.authKey");
			ccUserForm = prop.getProperty("callcenter.userForm");
			apiService.startup();
			ServerService.isLoggedIn();
			try {
				// Start email engine
				EmailEngine.instantiateEmailEngine(prop);
			} catch (EmailException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * case 1: When difference between lastDate and currentDate is greater then
	 * 7days then we need to fetch the data with slice/iterate. because api can
	 * fetch only the data of 7 day(s). we need currentDte and lastDate ....
	 */
	@Test
	public void sevenDaysTest() {

		String currentDate = "2017-10-12";
		String lastDate = "2017-09-25";
		LocalDate currentDates = new LocalDate();
		int day = Days.daysBetween(new LocalDate(lastDate),
				new LocalDate(currentDates)).getDays();
		long numberIteration = day / 7;
		int exceedDays = 0;
		for (int i = 0; i <= numberIteration; i++) {

			if (day > 7) {

				assertTrue(day > 7);
				// we need exclude the exceed days from current Date.
				exceedDays = day - 7;
				LocalDate fixedDate = currentDates.minusDays(exceedDays);
				currentDate = fixedDate.toString();
				// /updat the day and laste date
				lastDate = currentDate;
				day = exceedDays;

			} else {
				assertFalse(day > 7);
				// currentDate = currentDates.toString();
				// returnVal = processData(lastDate,currentDate);
			}

		}

	}

	/**
	 * Case 2:In this case we check the url response,i have set 10000 milisecond
	 * for the response time. we need lastDate and currentDate for this.
	 */
	//@Test(timeout = 10000)
	// set the thousand milisecond
	public void urlResponseTest() {
		String lastDate = "2017-10-09";
		String curentDate = "2017-10-10";
		String query = "?start_date=" + lastDate + "&end_date=" + curentDate;

		JSONArray response = callcenter.getHttpRequest(baseUrl + query,
				authKey);

		assertTrue(response.length() > 0);

	}

	/**
	 * Case 3: This case is not execute due ServerService it don't allow to
	 * inser the user form and user result form . i am unable to firgureout the
	 * problem yet ...
	 */
	public void saveForm() {

		String query = "?id=253342";
		String result = "";
		JSONObject object = null;
		// without login we can't save our data..
		if (callcenter.login(ccUsername, ccPassword)) {

			JSONArray responseArray = callcenter.getHttpRequest(baseUrl
					+ query, authKey);
			System.out.println("Result:-" + responseArray.toString());
			for (int i = 0; i < responseArray.length(); i++) {

				try {
					object = responseArray.getJSONObject(i);
					result = callcenter.saveForm(RequestType.CC_CRM_FORM,
							object);

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (PatternSyntaxException e) {
					e.printStackTrace();
				} catch (HibernateException e) {
					e.printStackTrace();
				} catch (ValidationException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						assertTrue(object.getString("uuid").equalsIgnoreCase(
								result));

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}

		}

	}

	/**
	 * Case 4 : just check the login status with call center user.
	 */
	@Test
	public void login() {
		boolean isTrue = callcenter.login(ccUsername,
				ccPassword);
		assertTrue(isTrue);

	}

	/**
	 * Case 5:
	 */
	//@Test
	public void testEmptyBehavior() {

		String query = "?id=253342";
		JSONArray emptyJAList = callcenter.getHttpRequest(baseUrl + query,
				ccPassword);
		assertEquals("Empty list should have 0 elements", 0,
				emptyJAList.length());
	}

	/**
	 * Case 6: this is case not execute due some user authentication ...em
	 * unable to fixed this issue. Note: I have handle this case in class and
	 * work perfectly .
	 */
	@Test
	public void testUuidWithNull() {
		try {

			if (callcenter.login(ccUsername,ccPassword)) {
				JSONObject newJsonObj = new JSONObject(
						"{\"lead_id\":\"268262\",\"uuid\":\"268262-59e4c102d1a194.52279591\",\"date_entered\":\"1508162889\",\"user_id\":\"21406\",\"call_id\":null,\"priority_status\":null,\"caller_category\":\"\",\"patient_id\":\" RFH8X-6\",\"name\":\"Saira\",\"gender\":\"female\",\"province\":\"\",\"city\":\"\",\"town_area\":\"\",\"preferred_language\":\"\",\"source_of_referral\":\"\",\"complaint_type\":\"Quality of service\",\"complaint_facility\":\"22\",\"caller_comments\":\"patient reported that she suffered from hepatitis after taking medications. she had to stop treatment. she also reported that she told at facility but no one addressed to the issue.  she has been counselled that she has to take complete treatment of TB and she can go to facility again and explain case to doctor. \",\"cse_comments\":\"contact number: 03133067295 \",\"consent_to_call_back\":\"\",\"consent_to_sms\":\"\",\"call_disposition\":\"Counselled\",\"other_tb_patient_facility\":\"IHK\",\"age_range\":\"35 years to less than 45 years\",\"purpose\":\"Counsellor check-in\",\"actions\":\"Other\",\"form_data\":null,\"facility_name\":\"Indus Hospital Korangi\",\"facility_id\":\"7f65d926-57d6-4402-ae10-a5b3bcbf7986\"}");

				JSONObject jsonObj = new JSONObject(
						"{\"lead_id\":\"254492\",\"uuid\":null,\"date_entered\":\"1507692573\",\"user_id\":null,\"call_id\":\"03002796454\",\"priority_status\":null,\"caller_category\":null,\"patient_id\":null,\"name\":null,\"gender\":null,\"province\":null,\"city\":null,\"town_area\":null,\"preferred_language\":null,\"source_of_referral\":null,\"complaint_type\":null,\"complaint_facility\":null,\"caller_comments\":null,\"cse_comments\":null,\"consent_to_call_back\":null,\"consent_to_sms\":null,\"call_disposition\":null,\"other_tb_patient_facility\":null,\"age_range\":null,\"purpose\":null,\"actions\":null,\"form_data\":null,\"facility_name\":null,\"facility_id\":null}");

				String value = callcenter.saveForm(RequestType.CC_CRM_FORM,
						jsonObj);
				System.out.println(value);

			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	//@Test
	public void httpsCallTest() {

		String query = "?id=252742"; // ID used just for testing ...
		JSONObject jsonObj = null;
		String errorMessage = "";
		JSONArray returnJsonArray = new JSONArray();
		String url = baseUrl + query;
		try {
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			System.out.println(con.getInputStream());
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
			con.setRequestProperty("Authorization", "Basic "
					+ baseUrl);
			con.setDoOutput(true);
			int responseCode = con.getResponseCode();
			assertEquals(200, responseCode);
			BufferedReader bufferedData = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String responseData;
			StringBuffer response = new StringBuffer();
			while ((responseData = bufferedData.readLine()) != null) {
				response.append(responseData);
			}
			bufferedData.close();

			try {
				jsonObj = new JSONObject(response.toString());
				returnJsonArray = jsonObj.getJSONArray("data");

			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			errorMessage = CustomMessage
					.getErrorMessage(ErrorType.INTERNET_CONNECTION_ERROR);
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {

			// / need to return

		}
		try {
			// /check
			assertNotEquals(
					new JSONObject(
							"{\"error\":0,\"message\":\"\",\"data\":[{\"lead_id\":\"252742\",\"uuid\":\"252742-59daeaa587cb42.07702573\",\"date_entered\":\"1507518832\",\"user_id\":\"21415\",\"call_id\":\"03223110529\",\"priority_status\":null,\"caller_category\":\"General Population\",\"patient_id\":\"\",\"name\":\"n/a\",\"gender\":\"female\",\"province\":\"Sindh\",\"city\":\"Karachi\",\"town_area\":\"n/a\",\"preferred_language\":\"Urdu\",\"source_of_referral\":\"Other\",\"complaint_type\":\"Not applicaple\",\"complaint_facility\":\"10292\",\"caller_comments\":\"query asked\",\"cse_comments\":\"query adressed\",\"consent_to_call_back\":\"Yes\",\"consent_to_sms\":\"Yes\",\"call_disposition\":\"Query addressed\",\"other_tb_patient_facility\":\"\",\"age_range\":\"25 years to less than 35 years\",\"purpose\":\"FAQs\",\"actions\":\"No additional actions required\",\"form_data\":null,\"facility_name\":\"Kharadar General Hospital\",\"facility_id\":\"d994a1d3-efd3-4c85-ba06-11342aa174d9\"}]}"),
					jsonObj);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
   /**
    * Case 7:  In this case, I need to verify every object keys with the object we expect.
    * 
    */
	@Test
	public void matchesKeyTest(){
		
		JSONObject  shouldExist = new JSONObject("{\"lead_id\":\"261592\",\"uuid\":\"261592-59e1a31f3381d6.59130862\",\"date_entered\":\"1507959417\",\"user_id\":\"21414\",\"call_id\":null,\"priority_status\":null,\"caller_category\":\"\",\"patient_id\":\"TTV2I-5\",\"name\":\"MOHAMMAD SALEEM\",\"gender\":\"\",\"province\":\"\",\"city\":\"\",\"town_area\":\"\",\"preferred_language\":\"\",\"source_of_referral\":\"\",\"complaint_type\":\"\",\"complaint_facility\":\"\",\"caller_comments\":\"no answer\",\"cse_comments\":\"no answer\",\"consent_to_call_back\":\"\",\"consent_to_sms\":\"\",\"call_disposition\":\"Follow UP\",\"other_tb_patient_facility\":\"\",\"age_range\":\"\",\"purpose\":\"FAQs\",\"actions\":\"No additional actions required\",\"form_data\":null,\"facility_name\":null,\"facility_id\":null}");
		
		JSONArray ousideData = callcenter.getHttpRequest(baseUrl+"?patient_id=TTV2I-5", authKey);
		
		JSONObject objec = ousideData.getJSONObject(0);
		/// get all the keys ..
		Set<?> testData =objec.keySet();
		Set<?> existData =shouldExist.keySet();
	    Iterator<?> i = testData.iterator();
	    do{
	       
			String k = i.next().toString();
			boolean keyIsContain = existData.contains(k);//this line of code check whether key is contain or not ..
			assertTrue(keyIsContain);
			System.out.println(k);

	    }while(i.hasNext());
		
		
		
	}
	/**
	 * Case 8 : In this case,I need to verify the objec keys values.
	 * 
	 * 
	 */
	@Test
	public void matchesKeyValues(){
	   
		String queryUrlString = baseUrl+"?start_date=2017-10-20&end_date=2017-10-21";
		
	    ///JSONObject shouldBeExist= new JSONObject("{\"lead_id\":\"275572\",\"uuid\":\"275572-59e9840b4b07e6.45113708\",\"date_entered\":\"1508475524\",\"user_id\":\"21402\",\"call_id\":null,\"priority_status\":null,\"caller_category\":\"\",\"patient_id\":\"Q0RXR-0\",\"name\":\"\",\"gender\":\"\",\"province\":\"\",\"city\":\"\",\"town_area\":\"\",\"preferred_language\":\"\",\"source_of_referral\":\"\",\"complaint_type\":\"\",\"complaint_facility\":\"\",\"caller_comments\":\"Contact requested to call after 1 hour.\",\"cse_comments\":\"\",\"consent_to_call_back\":\"\",\"consent_to_sms\":\"\",\"call_disposition\":\"Follow UP\",\"other_tb_patient_facility\":\"\",\"age_range\":\"\",\"purpose\":\"Counsellor check-in\",\"actions\":\"Other\",\"form_data\":null,\"facility_name\":null,\"facility_id\":null}");
		JSONObject shouldBeExist= new JSONObject("{\"lead_id\":\"275582\",\"uuid\":\"275582-59e98406844d87.49293867\",\"date_entered\":\"1508475772\",\"user_id\":\"21415\",\"call_id\":null,\"priority_status\":null,\"caller_category\":\"\",\"patient_id\":\"\",\"name\":\"\",\"gender\":\"\",\"province\":\"\",\"city\":\"\",\"town_area\":\"\",\"preferred_language\":\"\",\"source_of_referral\":\"\",\"complaint_type\":\"\",\"complaint_facility\":\"\",\"caller_comments\":\"not responding\",\"cse_comments\":\"\",\"consent_to_call_back\":\"\",\"consent_to_sms\":\"\",\"call_disposition\":\"Not Responding \",\"other_tb_patient_facility\":\"\",\"age_range\":\"\",\"purpose\":\"Counsellor check-in\",\"actions\":\"No additional actions required\",\"form_data\":null,\"facility_name\":null,\"facility_id\":null},{\"lead_id\":\"275592\",\"uuid\":\"275592-59e9871bd70799.60478099\",\"date_entered\":\"1508475914\",\"user_id\":\"21415\",\"call_id\":null,\"priority_status\":null,\"caller_category\":\"\",\"patient_id\":\"6IC72-0\",\"name\":\"\",\"gender\":\"\",\"province\":\"\",\"city\":\"\",\"town_area\":\"\",\"preferred_language\":\"\",\"source_of_referral\":\"\",\"complaint_type\":\"\",\"complaint_facility\":\"\",\"caller_comments\":\"counseled\",\"cse_comments\":\"\",\"consent_to_call_back\":\"\",\"consent_to_sms\":\"\",\"call_disposition\":\"Counselled\",\"other_tb_patient_facility\":\"\",\"age_range\":\"\",\"purpose\":\"Counsellor check-in\",\"actions\":\"No additional actions required\",\"form_data\":null,\"facility_name\":null,\"facility_id\":null}");
		
		JSONArray ousideData = callcenter.getHttpRequest(queryUrlString, authKey);
		JSONObject outsideDataObj=  ousideData.getJSONObject(2);
			JSONArray names = outsideDataObj.names();
			int length = names.length();
			
			for (int i = 0; i < length; i++) {
				
			  String naam = names.getString(i);
			  
			  String alreadyName = shouldBeExist.isNull(naam) ? "Null" : shouldBeExist.getString(naam); 
			  String outsideName = outsideDataObj.isNull(naam) ? "Null" : outsideDataObj.getString(naam);  
			  
			  assertEquals(alreadyName, outsideName);
			 
			}
		
	}
	
	/**
	 * Case 9:  In this case, i need to verify the saveForm method pass different  JsonObject
	 * expected or unexcepted object ...
	 * 
	 * 
	 */
	@Test
	public void keyValuesSave(){
		
		JSONObject result = new JSONObject("{\"lead_id\":\"261512\",\"uuid\":\"261512-59e188aa793139.73009411\",\"date_entered\":\"1507952477\",\"user_id\":\"21415\",\"call_id\":\"03007026426\",\"priority_status\":null,\"caller_category\":\"General Population\",\"patient_id\":\"\",\"name\":\"asia\",\"gender\":\"female\",\"province\":\"Sindh\",\"city\":\"Karachi\",\"town_area\":\"shah faisal\",\"preferred_language\":\"Urdu\",\"source_of_referral\":\"Television\",\"complaint_type\":\"Not applicaple\",\"complaint_facility\":\"9282\",\"caller_comments\":\"nearest facility inquiry sdfsadfkasdlkf  sdkfjsadlfkjads asdfladskfjlads sa dsfsadklfjldfasd sadlfkjadslf asdfkdsjflkdj sadfsdlfkjs sad lkjfaslkdfjasdkl asdflsadkfjl sdfadslkfjlkxnmc,vns lkjlkjiojlskfj ntjlkjlkfas llfksdfjslkjle lskfjsldkfsldfk sdfsdtelk saldfkjslkdfslatk\",\"cse_comments\":\"reffered to shaukat omer memorial hosp shah faisal\",\"consent_to_call_back\":\"Yes\",\"consent_to_sms\":\"Yes\",\"call_disposition\":\"Referred to a AaoTBMitao site\",\"other_tb_patient_facility\":\"\",\"age_range\":\"35 years to less than 45 years\",\"purpose\":\"FAQs\",\"actions\":\"Refer to nearest hospital / facility\",\"form_data\":null,\"facility_name\":\"Dr Ziauddin Hospital - Clifton\",\"facility_id\":\"ad31fdbc-70dc-4434-a35d-37000317f301\"}");
		String returnVal= "";
		if(login(ccUsername, ccPassword))
		{
			String responseDetail = "";
			String errorMessage = "";
			UserForm userForm = null;
			UserFormType userFormType = apiService.getUserFormService()
					.getUserFormTypeByName(ccUserForm.toString());

			// this line of code used to convert the timestamp to date.
			long dates = Long.valueOf(result.getString("date_entered"));
			Date d = new Date(dates * 1000);
			try {
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

					String naam = names.getString(i);
					value = result.isNull(naam) ? "Null" : result.getString(naam);
					// this check is because we don't need to insert some data who don't  have element id 				// result form just like uuid
					if (element != null) {
						
						UserFormResult userFormResult = new UserFormResult(element,
								value, userForm);
						userFormResults.add(userFormResult);
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
			}

			finally {

				if (userForm.getUserFormId() != null) {
				

				} else {
					responseDetail = "ERROR:" + errorMessage;
				}

			}
		}
		
		
	}
     /**
      * case 10: In this case, i need to verify/check the duplicate values insertion. 
      *  i am trying to insert the duplicate values,it shoud be fail to insert
      */
	
	public void checkTheDuplicatedValue(){
		
		
		
	}

	@Override
	protected void handleRequest(HttpServletRequest request,
			HttpServletResponse resp) throws IOException, JSONException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean login(String username, String password)
	{
	   return apiService.getAuthenticationService().authenticate(username,
		   	password);
	} 
	
}
