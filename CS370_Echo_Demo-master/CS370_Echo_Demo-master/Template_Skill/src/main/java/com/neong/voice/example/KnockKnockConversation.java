package com.neong.voice.example;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.neong.voice.model.base.Conversation;
import java.io.*;
import java.util.*;


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
    ///Intitialize string for each intent
    private final static String INTENT_START = "StartKnockIntent";
    private final static String INTENT_WHO_DER = "WhoDerIntent";
    private final static String INTENT_DR_WHO = "DrWhoIntent";
    private final static String INTENT_OFFICE_HOURS = "officehoursIntent";
    private final static String INTENT_CONTACTINFO = "ContactInformationIntent";
    private final static String INTENT_CLASSES = "ClassesTaughtIntent";
    
	//Slots
	//private final static String SLOT_RELATIVE_TIME = "timeOfDay";

	//State
	private final static Integer STATE_WAITING_WHO_DER = 100000;
	private final static Integer STATE_WAITING_DR_WHO = 100001;
	private final static Integer STATE_GET_PROFESSOR = 2;

	//Session state storage key
	
	///
	private final static String SESSION_KNOCK_STATE = "knockState";

	public KnockKnockConversation() {
		super();
		
		//Add custom intent names for dispatcher use
		supportedIntentNames.add(INTENT_START);
		supportedIntentNames.add(INTENT_WHO_DER);
		supportedIntentNames.add(INTENT_DR_WHO);
		supportedIntentNames.add(INTENT_OFFICE_HOURS);
		supportedIntentNames.add(INTENT_CONTACTINFO);
		supportedIntentNames.add(INTENT_CLASSES);

	}


	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {
		
		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		if(INTENT_OFFICE_HOURS.equals(intentName)){
		    response = handleOfficeHoursIntent(intentReq, session);
		}
		else if (INTENT_CONTACTINFO.equals(intentName)){
			response = handleContactinformationIntent(intentReq, session);
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

	private SpeechletResponse handleContactinformationIntent(IntentRequest intentReq, Session session){
		// change state
		// asking what they want

		// don't check for state because it can work requardless of place conversation

		// are we in state two?
		// two : 2
		// if we are in state two then do below
		// else we are screwed
		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		// may give error if slot is empty
		String professorNameString = slots.get("ProfessorName").getValue();
		SpeechletResponse response = null;
		if(professorNameString != null){
			//String professor = professorNameSlot.getValue();
			// change state
			response = newAskResponse("Would you like " + professorNameString +"'s email or phone?", false, "Do you want phone or email?" ,false);
		} else {
			response = newAskResponse("Can I have a professor name?", false, "I didn't catch that.  Can I have a professor name?", false);
		}
		return response;
	}
	// phoneIntent
	
	// emailIntent

	private SpeechletResponse handleStartJokeIntent(IntentRequest intentReq, Session session) {
		SpeechletResponse response = newAskResponse("Knock knock.", false, "I said, Knock knock!", false);
		session.setAttribute(SESSION_KNOCK_STATE, STATE_WAITING_WHO_DER);
		return response;	
	}
	
	private SpeechletResponse handleWhoThereIntent(IntentRequest intentReq, Session session) {
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
	
	private SpeechletResponse handleDrWhoIntent(IntentRequest intentReq, Session session) {
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
