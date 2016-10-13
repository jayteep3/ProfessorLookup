package com.neong.voice.example;
import com.neong.voice.Classes.*;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;	
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.neong.voice.model.base.Conversation;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.net.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import javax.net.ssl.HttpsURLConnection;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.*;
//import org.apache.commons.io.IOUtils;

//import java.sql.*;
//import com.mysql.jdbc.Driver;
/**
 * This is an example implementation of a Conversation subclass. It is
 * important to register your intents by adding them to the supportedIntentNames
 * array in the constructor. Your conversation must internally track the current
 * state of the conversation and all state transitions so that it feels natural.
 * The state machine below is the simplest of examples so feel free to create a
 * more robust state-machine object for your more complex needs.
 * 
 * @author Jeffrey Neong
 * @version 1.0
 * 
 */
// directory for build command :
// Template_Skill
// build:
// mvn assembly:assembly -DdescriptorId=jar-with-dependencies package

public class KnockKnockConversation extends Conversation {
	//Intent names
	private String cachedName = null;
	private final static String INTENT_START = "StartKnockIntent";
	private final static String INTENT_WHO_DER = "WhoDerIntent";
	private final static String INTENT_DR_WHO = "DrWhoIntent";
	private final static String INTENT_OFFICE_HOURS = "officehoursIntent";
    private final static String INTENT_CONTACTINFO = "ContactInformationIntent";
    private final static String INTENT_PHONE_NUMBER = "PhoneNumberIntent";
    private final static String INTENT_EMAIL_ADDRESS = "ContactInformationEmailIntent";
    private final static String INTENT_CLASSES = "ClassesTaughtIntent";
    private final static String INTENT_COMBO = "ContactInformationComboIntent";
    private final static String INTENT_CLARIFY_PROF = "ProfessorNameIntent";
    private final static String INTENT_YES = "AMAZON.YesIntent";
    private final static String INTENT_NO = "AMAZON.NoIntent";
	//Slots
	//private final static String SLOT_RELATIVE_TIME = "timeOfDay";

	//State
	private final static Integer STATE_WAITING_WHO_DER = 100000;
	private final static Integer STATE_WAITING_DR_WHO = 100001;
	private final static Integer STATE_GET_PROFESSOR = 2;
	private final static Integer STATE_GET_EMAIL = 3;
	private final static Integer STATE_GET_PHONE = 4;
	private final static Integer STATE_GET_EMAIL_PHONE = 5;
	

	//Session state storage key
	private final static String SESSION_KNOCK_STATE = "knockState";	
	private final static String SESSION_PROF_STATE = "profState";
	public KnockKnockConversation() {
		super();
		
		//Add custom intent names for dispatcher use
		supportedIntentNames.add(INTENT_START);
		supportedIntentNames.add(INTENT_WHO_DER);
		supportedIntentNames.add(INTENT_DR_WHO);
		supportedIntentNames.add(INTENT_OFFICE_HOURS);
		supportedIntentNames.add(INTENT_CONTACTINFO);
		supportedIntentNames.add(INTENT_PHONE_NUMBER);
		supportedIntentNames.add(INTENT_EMAIL_ADDRESS);
		supportedIntentNames.add(INTENT_CLASSES);
		supportedIntentNames.add(INTENT_COMBO);
		supportedIntentNames.add(INTENT_CLARIFY_PROF);
		supportedIntentNames.add(INTENT_YES);
		supportedIntentNames.add(INTENT_NO);

	}


	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		if(INTENT_OFFICE_HOURS.equals(intentName)){
		    response = handleOfficeHoursIntent(intentReq, session);
		}
		else if (INTENT_COMBO.equals(intentName)){
			response = handleContactInformationIntent(intentReq, session);
		}
		else if (INTENT_PHONE_NUMBER.equals(intentName)){
			response = handlePhoneNumberIntent(intentReq, session);
		}
		else if (INTENT_EMAIL_ADDRESS.equals(intentName)){
			response = handleEmailAddressIntent(intentReq, session);
		}
		else if (INTENT_START.equals(intentName)) {
			response = handleStartJokeIntent(intentReq, session);
        }
		else if (INTENT_WHO_DER.equals(intentName)) {
			response = handleWhoThereIntent(intentReq, session);
        }
		else if (INTENT_DR_WHO.equals(intentName)) {
			response = handleDrWhoIntent(intentReq, session);
        }
        else if (INTENT_CLARIFY_PROF.equals(intentName)) {
        	response = handleProfessorNameIntent(intentReq, session);
        }
        else if (INTENT_YES.equals(intentName)){
        	response = handleYesIntent(intentReq, session);
        }
        else if(INTENT_NO.equals(intentName)){
        	response = handleNoIntent(intentReq, session);
        }
		else {
			response = newTellResponse("Whatchu talkin' bout!", false);
		}
		
		
		return response;
	}
//STATE_WAITING_DR_WHO.compareTo((Integer)session.getAttribute(SESSION_KNOCK_STATE)) == 0
	private SpeechletResponse handleYesIntent(IntentRequest intentReq, Session session){
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		pc.setName(cachedName);
		try{
		pc.GetEmailPhone(pc.getName());
		} catch(ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
			//pc.setEmail("yes");
		}
		if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			response = newTellResponse(pc.getName()+ "s email is " + pc.getEmail() + " their phone number is " + pc.getPhone(), false);
		}
		else if (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			response = newTellResponse(pc.getName() + "s email address is " + pc.getEmail(), false);
		}
		else if (STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			response = newTellResponse(pc.getName() + "s phone number is " + pc.getPhone(), false);
		}
		else
			response = newTellResponse("Watchu talkin about willis?", false);
		return response;
	}
	private SpeechletResponse handleNoIntent(IntentRequest intentReq, Session session){
		if((STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0) || (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0) || 
				(STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0))
				return newTellResponse("No thank you? sheesh, last time i help you", false);
		return newTellResponse("Please ask for professor information first", false);
	}
    private SpeechletResponse handleOfficeHoursIntent(IntentRequest intentReq, Session session) {
	
    Intent intent = intentReq.getIntent();
    Map<String, Slot> slots = intent.getSlots();
	Slot professorNameSlot = slots.get("ProfessorName");
	SpeechletResponse response = null;
	String professor = professorNameSlot.getValue();
    
	return newTellResponse(professor + " has office hours Wednesdays at 8 AM", false);
	// return a tell response object within SpeechletResponse
	// what is the difference between ask and tell (does alexa use both)
    }

	private SpeechletResponse classesTaughtIntent(IntentRequest intentReq, Session session) {
		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		Slot professorNameSlot = slots.get("ProfessorName");
		SpeechletResponse response = null;
 		String professor = professorNameSlot.getValue();
 		//check state
 		response = newTellResponse(professor + " is teaching CS 315 and CS 115 this semester",false);
 		
		
 		return response;
 	}

	// don't modify this function
	private SpeechletResponse handleContactInformationIntent(IntentRequest intentReq, Session session){
		
		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		// may give error if slot is empty
		String professor_name_string = slots.get("ProfessorName").getValue();
		SpeechletResponse response = null;
		// alexa can respond with null or "?" so both must be covered
		if(professor_name_string != null && !professor_name_string.isEmpty())
		{
			//String professor = professorNameSlot.getValue();
			// change state
			ProfContact pc = new ProfContact();
			pc.setName(professor_name_string);
			/*
			try
			{
				pc.GetEmailPhone(professor_name_string);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
				//pc.setEmail("yes");
			}*/
			
			// setup
			String url = "https://moonlight.cs.sonoma.edu/api/v1/directory/person/";
			String char_set = java.nio.charset.StandardCharsets.UTF_8.name();
			String param = "Ali Kooshesh";
					//"?search=Ali%20Kooshesh";
			// failse when "Ali Kooshesh" is added
			// succeeds when "Kooshesh" is added
			String full_url = "https://moonlight.cs.sonoma.edu/api/v1/directory/person/?format=json&search=George%20Ledin";
			// gives an unknown exception
			// from front desk: george ledin has no phone number
			try
			{
				//URL obj = new URL(full_url);
				//HttpURLConnection con =(HttpURLConnection) obj.openConnection();
				//con.setRequestMethod("Get");
				//con.setDoOutput(true);
				//con.connect();
				//con.setRequestProperty("User-Agent", "Mozilla/5.0");
				//int response_code = con.getResponseCode();
				//System.out.println("\nSending 'GET' request to URL: " + full_url);
				//System
				/*
				BufferedReader in = new BufferedReader(
		                new InputStreamReader(con.getInputStream()));
		        String inputLine;
		        StringBuffer response4 = new StringBuffer();

		        while ((inputLine = in.readLine()) != null) {
		            response4.append(inputLine);
		        }
		        in.close();*/
		        //pc.setPhone(response4.toString());
				// setup url for call
				//String query = String.format("search=%s", URLEncoder.encode(param, char_set));
				StringBuilder result = new StringBuilder();
				URL url2 = new URL(full_url);
				HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			      conn.setRequestMethod("GET");
			      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			      String line;
			      while ((line = rd.readLine()) != null) {
			         result.append(line);
			      }
			      rd.close();
			      conn.disconnect();
			      String json_text = result.toString();
				//URLConnection connection = new URL(full_url/*url + "?" + query*/).openConnection();
								
				
				// gets the html page with the json
				
				//connection.setRequestProperty("Accept-Charset", value);
				//InputStream response2 = connection.getInputStream();
				
				//try(Scanner scanner = new Scanner(response2))
				//{
					//String response_body = scanner.useDelimiter("\\A").next();
					// have the json text but cannot convert it to a json array without triggering an uncatchable exception
					//JSONArray arr = new JSONArray(response_body);
					JSONArray arr = new JSONArray(json_text);
					JSONObject json = arr.getJSONObject(0);

					// assumes response_body is already valid json
					//JSONObject json = new JSONObject(response2);
					//JSONObject obj = null;
					// com.amazonaws.util.json.JSONException: JSONObject[\"display_name\"] not found."
					//for(int i = 0; i < arr.length(); i++)
					//{
						
						//if(arr.getJSONObject(i).getString("first_name").equals("Janet"))
						
							//obj = arr.getJSONObject(0);
//}
					//}
					pc.setEmail(json.getString("email"));
					pc.setPhone(json.getString("phone")/* + "   " + arr.toString()*//*json.getString("first_name") + " " + json.getString("last_name")*//*result.toString()*//*response_body*//*obj.toString()*//*.getString("first_name")*//*works/*arr.getJSONObject(0).getString("created")*//*obj.getString("id")*//*response_body*//*json.getString("display_name")*/);//json.getString("name")); //System.out.println(response_body);
				} catch (JSONException e) {
				
					pc.setPhone(e.toString());

			}
			catch(IOException e)
			{
				pc.setPhone(e.toString());
			}
		
			/*
			 try {
		         URL url2 = new URL("https://www.google.com");
		         URLConnection urlConnection = url2.openConnection();
		         HttpURLConnection connection = null;
		         if(urlConnection instanceof HttpURLConnection) {
		            connection = (HttpURLConnection) urlConnection;
		         }else {
		            System.out.println("Please enter an HTTP URL.");
		            return newTellResponse("sucess", false);
		         }
		         
		         BufferedReader in = new BufferedReader(
		            new InputStreamReader(connection.getInputStream()));
		         String urlString = "";
		         String current;
		         
		         while((current = in.readLine()) != null) {
		            urlString += current;
		         }
		         System.out.println(urlString);
		      }catch(IOException e) {
		         pc.setPhone(e.toString());//System.out.print(e);//e.printStackTrace();
		      }*/
			if(pc.getEmail() == null || pc.getEmail().isEmpty())
			{
				response = newAskResponse(pc.getName() + " has no email listed, but their phone is " + pc.getPhone() + " would you like me to repeat that", false, "I did not catch that, did you want me to repeat the phone number", false);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
				cachedName = pc.getName();
			}
			else if(pc.getPhone() == null || pc.getPhone().isEmpty()){
				response = newAskResponse(pc.getName() + " has no phone listed, but their email is " + pc.getEmail() + " would you like me to repeat that", false, "I did not catch that, did you want me to repeat the email address", false);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
				cachedName = pc.getName();
			}
			else{
				response = newTellResponse(pc.getName() + "s email is " + pc.getEmail() +  " their phone is " + pc.getPhone(), false);
			}

			
			//response = newAskResponse("Would you like " + professor_name_string +"'s email or phone?", false, "Do you want phone or email?" ,false);
		}
		else
		{
			response = newAskResponse("I did not hear a professor name, can you try again", false, "I didn't catch that,  Can I have a professor name ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		return response;
	}

	private SpeechletResponse handleProfessorNameIntent (IntentRequest intentReq, Session session){
		SpeechletResponse response = null;
		response = handleContactInformationIntent(intentReq, session);
		return response;
	}
	// don't do any state checking
	// if someone says phone
	// get the phone (dummy phone number)
	// assignment: code up dummy intents for phoneIntent and emailIntent
	// phoneIntent
	private SpeechletResponse handlePhoneNumberIntent(IntentRequest intentReq, Session session)
	{
		// get professor_name from intentReq as was done in handleContactInformationIntent
		// 
		Intent phone_number_intent = intentReq.getIntent();
		// assume slot is not empty
		// assume we get the professor name from the voice recognition
		String professor_name = phone_number_intent.getSlots().get("ProfessorName").getValue();
		// assume contact info for the professor is stored in our data structure
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;
		if(professor_name != null && !professor_name.isEmpty())
		{
		pc.setName(professor_name);
		
		try
		{
			pc.GetEmailPhone(professor_name);
		}
		catch (ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
			//pc.setEmail("yes");
		}
		String phone_number = pc.getPhone();
		
		if(phone_number != null && !phone_number.isEmpty())
		{
			response = newTellResponse("Here is " + professor_name + "'s phone number: " + phone_number, false);
		}
		else if((phone_number == null || phone_number.isEmpty()) && (pc.getEmail() != null && !pc.getEmail().isEmpty()))
		{
			// phone number doesn't exist
			response = newAskResponse("This professor has no phone number, would you like their email ", false, "Would you like their email?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
			cachedName = pc.getName();
		}
		}
		else
		{
			response = newAskResponse("I did not hear a professor name, can you try again", false, "I didn't catch that,  Can I have a professor name ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		return response;
		// phone number = getFakeinfo(professor_name, "phone")
		// return response with professor_name and phone number
	}
	private SpeechletResponse handleEmailAddressIntent(IntentRequest intentReq, Session session)
	{
		Intent email_intent = intentReq.getIntent();
		String professor_name = email_intent.getSlots().get("ProfessorName").getValue();
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;
		if(professor_name != null && !professor_name.isEmpty())
		{
		pc.setName(professor_name);
		try
		{
			pc.GetEmailPhone(professor_name);
		}
		catch (ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
			//pc.setEmail("yes");
		}
		
		
		if(pc.getEmail() != null && !pc.getEmail().isEmpty())
		{
			response = newAskResponse("Here is " + professor_name + "'s email address: " + pc.getEmail() + " , would you like me to repeat it?", false, "Would you like me to repeat the email?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
			cachedName = pc.getName();
		}
		else if( (pc.getEmail() == null || pc.getEmail().isEmpty()) && (pc.getPhone() != null && !pc.getPhone().isEmpty()))
		{
			response = newAskResponse("This professor has no email address. Would you like their phone?  ", false, "Would you like their phone?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
			cachedName = pc.getName();
		}
	}
		else
		{
			response = newAskResponse("I did not hear a professor name, can you try again", false, "I didn't catch that,  Can I have a professor name ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		return response;
	}
	
	private SpeechletResponse handleStartJokeIntent(IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = newAskResponse("Knock knock.", false, "I said, Knock knock!", false);
		session.setAttribute(SESSION_KNOCK_STATE, STATE_WAITING_WHO_DER);
		return response;	
	}
	
	private SpeechletResponse handleWhoThereIntent(IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;
		
		//check state
		if(session.getAttribute(SESSION_KNOCK_STATE) != null 
				&& STATE_WAITING_WHO_DER.compareTo((Integer)session.getAttribute(SESSION_KNOCK_STATE)) == 0) {
			response = newAskResponse("Doctor.", false," Doctor is here.",false);
			//Update state
			session.setAttribute(SESSION_KNOCK_STATE, STATE_WAITING_DR_WHO);
		}
		else {
			response = newTellResponse("You have to say knock knock first.", false);
		}
		
		return response;	
	}
	
	private SpeechletResponse handleDrWhoIntent(IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;
		//check state
		
		if(session.getAttribute(SESSION_KNOCK_STATE) != null 
				&& STATE_WAITING_DR_WHO.compareTo((Integer)session.getAttribute(SESSION_KNOCK_STATE)) == 0) {			
			response = newTellResponse(" Exactly. How did you know?", false);
			//Clear final state
			session.removeAttribute(SESSION_KNOCK_STATE);
		}
		else {
			response = newTellResponse("You have to say knock knock first.", false);
		}
		return response;	
	}
	
}
