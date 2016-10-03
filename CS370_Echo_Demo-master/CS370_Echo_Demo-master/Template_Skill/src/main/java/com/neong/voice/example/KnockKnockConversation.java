package com.neong.voice.example;
import com.neong.voice.Classes.*;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;	
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

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
	private final static String INTENT_START = "StartKnockIntent";
	private final static String INTENT_WHO_DER = "WhoDerIntent";
	private final static String INTENT_DR_WHO = "DrWhoIntent";
	private final static String INTENT_OFFICE_HOURS = "officehoursIntent";
    private final static String INTENT_CONTACTINFO = "ContactInformationIntent";
    private final static String INTENT_PHONE_NUMBER = "PhoneNumberIntent";
    private final static String INTENT_EMAIL_ADDRESS = "ContactInformationEmailIntent";
    private final static String INTENT_CLASSES = "ClassesTaughtIntent";
    private final static String INTENT_COMBO = "ContactInformationComboIntent";
	//Slots
	//private final static String SLOT_RELATIVE_TIME = "timeOfDay";

	//State
	private final static Integer STATE_WAITING_WHO_DER = 100000;
	private final static Integer STATE_WAITING_DR_WHO = 100001;
	private final static Integer STATE_GET_PROFESSOR = 2;
	private final static Integer STATE_GET_EMAIL = 3;
	private final static Integer STATE_GET_PHONE = 4;

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
		else {
			response = newTellResponse("Whatchu talkin' bout!", false);
		}
		
		
		return response;
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
			try
			{
				pc.GetEmailPhone(professor_name_string);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
				//pc.setEmail("yes");
			}
			if(pc.getEmail() == null)
			{
				response = newAskResponse(pc.getName() + " has no email listed, but their phone is " + pc.getPhone() + " would you like me to repeat that", false, "I did not catch that, did you want me to repeat the phone number", false);
				
			}
			response = newTellResponse(pc.getName() + "s email is " + pc.getEmail() +  " their phone is " + pc.getPhone(), false);
			
			//response = newAskResponse("Would you like " + professor_name_string +"'s email or phone?", false, "Do you want phone or email?" ,false);
		}
		else
		{
			response = newAskResponse("I did not hear a professor name, can you try again", false, "I didn't catch that,  Can I have a professor name ", false);
		}
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
		SpeechletResponse response = null;
		if((phone_number != null || "?".equals(phone_number)) && (professor_name != null || "?".equals(professor_name)))
		{
			response = newTellResponse("Here is " + professor_name + "'s phone number: " + phone_number, false);
		}
		else
		{
			// phone number doesn't exist
			response = newAskResponse("This professor has no phone number.  ", false, "Would you like their email?", false);
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
		
		SpeechletResponse response = null;
		if(pc.getEmail() != null && !pc.getEmail().isEmpty())
		{
			response = newTellResponse("Here is " + professor_name + "'s email address: " + pc.getEmail(), false);
		}
		else
		{
			response = newAskResponse("This professor has no email address.  ", false, "Would you like their email?", false);
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
