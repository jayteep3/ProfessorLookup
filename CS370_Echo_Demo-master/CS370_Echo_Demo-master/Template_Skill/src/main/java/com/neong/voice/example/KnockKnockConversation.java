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
	private ArrayList<ProfContact> cachedList = new ArrayList <ProfContact>();
	private String cachedName = null;
	private final static String INTENT_START = "StartKnockIntent";
	private final static String INTENT_WHO_DER = "WhoDerIntent";
	private final static String INTENT_DR_WHO = "DrWhoIntent";
	private final static String INTENT_OFFICE_HOURS = "officehoursIntent";
	private final static String INTENT_CONTACTINFO = "ContactInformationIntent";
	private final static String INTENT_PHONE_NUMBER = "ContactInformationPhoneIntent";
	private final static String INTENT_EMAIL_ADDRESS = "ContactInformationEmailIntent";
	private final static String INTENT_CLASSES = "ClassesTaughtIntent";
	private final static String INTENT_COMBO = "ContactInformationComboIntent";
	private final static String INTENT_CLARIFY_PROF = "ProfessorNameIntent";
	private final static String INTENT_YES = "AMAZON.YesIntent";
	private final static String INTENT_NO = "AMAZON.NoIntent";
	private final static String INTENT_REPEAT = "RepeatIntent";
	private final static String INTENT_MORE_INFO = "MoreInfoIntent";
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
		else if (INTENT_REPEAT.equals(intentName)){
			response = handleRepeatIntent(intentReq, session);
		}
		else if (INTENT_MORE_INFO.equals(intentName)){
			response = handleMoreInfoIntent(intentReq, session);
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

	private SpeechletResponse handleMoreInfoIntent(IntentRequest intentReq, Session session){
		//If they have already gotten email/phone, give them the other.
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		pc.setName(cachedName);
		try
		{
			GetEmailPhone(pc.getName());
		}
		catch (ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
		}
		if (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			response = newAskResponse(pc.getName() + "s phone number is " + pc.getPhone() + ", would you like me to repeat that?", false, "would you like me to repeat their phone number?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
		}
		else if (STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			response = newAskResponse(pc.getName() + "s email address is " + pc.getEmail() + ", would you like me to repeat that?", false, "would you like me to repeat their email address?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
		}
		else
			response = newTellResponse("Watchu talkin about free willie?", false);
		return response;
	}

	private SpeechletResponse handleRepeatIntent(IntentRequest intentReq, Session session){
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		pc.setName(cachedName);
		try
		{
			GetEmailPhone(pc.getName());
		}
		catch (ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
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
	private SpeechletResponse handleYesIntent(IntentRequest intentReq, Session session){
		if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
		{
			return handleRepeatIntent(intentReq, session);
		}	
		else
		{
			return newTellResponse("That wasn't a yes or no question, dumb dumb.", false);
		}
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
		response = newTellResponse(professor + " is teaching CS 315 and CS 115 this semester", false);

		
		return response;
	}


	private SpeechletResponse handleContactInformationIntent(IntentRequest intentReq, Session session){
		
		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		// may give error if slot is empty
		String professor_name_string = slots.get("ProfessorName").getValue();
		SpeechletResponse response = null;
		/*
		 * Need to be able to handle just a professor name as an intent, or asking for a receiving a response within this handler. Not sure if possible.
		 */
		if(professor_name_string == "my professor" || professor_name_string == "my teacher" || professor_name_string == "a professor")
		{
			response = newAskResponse("What is this professor's name?", false, "I didn't catch that,  Can I have a professor name ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR); //not ideal, presumably wont allow user to respond with just name
		}
		// alexa can respond with null or "?" so both must be covered
		else if(professor_name_string != null && !professor_name_string.isEmpty())
		{
			//String professor = professorNameSlot.getValue();
			// change state
			ProfContact pc = new ProfContact();
			pc.setName(professor_name_string);
			try
			{
				GetEmailPhone(professor_name_string);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
			} 
			if(cachedList.size() > 1)
			{
				//TODO
			}
			else{
			if(pc.getEmail() == null || pc.getEmail().isEmpty())
			{
				if(pc.getPhone() == null || pc.getPhone().isEmpty())
				{
					//No Phone or Email
					response = newTellResponse(pc.getName() + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable.", false);
					cachedName = pc.getName();
				}
				else
				{
					//Phone, but no Email
					response = newAskResponse(pc.getName() + " has no email listed, but their phone is " + pc.getPhone() + " would you like me to repeat that", false, "I did not catch that, did you want me to repeat the phone number", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
					cachedName = pc.getName();
				}
			}
			else
			{
				if(pc.getPhone() == null || pc.getPhone().isEmpty())
				{
					//Email, but no Phone
					response = newAskResponse(pc.getName() + " has no phone listed, but their email is " + pc.getEmail() + " would you like me to repeat that", false, "I did not catch that, did you want me to repeat the email address", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
					cachedName = pc.getName();
				}
				else
				{
					//Email and Phone
					response = newAskResponse(pc.getName() + "s email is " + pc.getEmail() +  " their phone is " + pc.getPhone() + ", would you like me to repeat that?", false, "I did not catch that, did you want me to repeat " + pc.getName() + "'s contact info?", false);
				}
			}	
		}
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
		//We have prof name
	{
		pc.setName(professor_name);

		try
		{
			GetEmailPhone(professor_name);
		}
		catch (ClassNotFoundException | SQLException e)
		{	// TODO Auto-generated catch block
			pc.setPhone(e.toString());
		}
		if(cachedList.size() > 1){
		//TODO	
		}
		else
		{
			String phone_number = pc.getPhone();

			if(phone_number != null && !phone_number.isEmpty())
			{		
					// phone number exists
				response = newAskResponse("Here is " + professor_name + "'s phone number: " + phone_number + ", would you like me to repeat that or give you more info on " + professor_name + "?", false, "I didn't catch that, would you like me to repeat their phone number or give you more info?", false);
			}
			else if(pc.getEmail() != null && !pc.getEmail().isEmpty())
			{
					// phone number doesn't exist, but email does
				response = newAskResponse("This professor has no phone number, would you like their email ", false, "Would you like their email?", false);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
				cachedName = pc.getName();
			}
			else
			{
					//neither phone nor email exist
				response = newTellResponse(pc.getName() + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable.", false);
			}
		}	
	}
	else
		//prof name is null or empty
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
		//we have prof name
	{
		pc.setName(professor_name);
		try
		{
			GetEmailPhone(professor_name);
		}
		catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
			}
			if(cachedList.size() > 1){

			}
			else
			{
				if(pc.getEmail() != null && !pc.getEmail().isEmpty())
				{
				//We have email
					response = newAskResponse("Here is " + professor_name + "'s email address: " + pc.getEmail()+ ", would you like me to repeat that or give you more info on " + professor_name + "?", false, "I didn't catch that, would you like me to repeat their email or give you more info?", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
					cachedName = pc.getName();
				}
				else if(pc.getPhone() != null && !pc.getPhone().isEmpty())
				{
				//No email, but we have phone
					response = newAskResponse("This professor has no email address listed. Would you like their phone?  ", false, "Would you like their phone?", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
					cachedName = pc.getName();
				}
				else
				{
				//No email nor phone
					response = newTellResponse(pc.getName() + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable.", false);
				}
			}
		}
		else
		//professor name is null or empty
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
public void GetEmailPhone(String name2) throws ClassNotFoundException, SQLException
{

	String full_url = "https://moonlight.cs.sonoma.edu/api/v1/directory/person/?format=json&search=" + name2;
	ArrayList<ProfContact> array = new ArrayList<ProfContact>();
	try
	{

		StringBuilder result = new StringBuilder();
		URL url2 = new URL(full_url);

		HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}
		rd.close();
		conn.disconnect();
		String json_text = result.toString();

		JSONArray arr = new JSONArray(json_text);


		for(int i = 0; i < arr.length(); i++){
			JSONObject json = arr.getJSONObject(i);
			ProfContact pc = new ProfContact();
			pc.setEmail(json.getString("email"));
			pc.setPhone(json.getString("phone"));
			pc.setName(json.getString("name"));
			array.add(pc);
		}

	}
	catch (JSONException e)
	{
		ProfContact pc = new ProfContact();			
		pc.setPhone(e.toString());

	}
	catch(IOException e)
	{
		ProfContact pc = new ProfContact();			
		pc.setPhone(e.toString());
	}

	cachedList = array;
}
}
