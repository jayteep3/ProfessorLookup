
package com.neong.voice.example;
import com.neong.voice.Classes.*;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;	
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.neong.voice.model.base.Conversation;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.net.*;


/**
 * This is an example implementation of a Conversation subclass. It is
 * important to register your intents by adding them to the supportedIntentNames
 * array in the constructor. Your conversation must internally track the current
 * state of the conversation and all state transitions so that it feels natural.
 * The state machine below is the simplest of examples so feel free to create a
 * more robust state-machine object for your more complex needs.
 * 
 * @author Death Records
 * @version 1.0
 * 
 */
// directory for build command :
// Template_Skill
// build:
// mvn assembly:assembly -DdescriptorId=jar-with-dependencies package

public class KnockKnockConversation extends Conversation {

	//Cached variables in case of clarification
	private static List<ProfContact> cachedList = new ArrayList <ProfContact>();
	private Boolean duplicates;
	private String joke_opener;
	private String joke_punchline;

	//Intent names

	private final static String INTENT_OFFICE_HOURS = "officehoursIntent"; // 1
	private final static String INTENT_CONTACTINFO = "ContactInformationIntent"; // 2
	private final static String INTENT_PHONE_NUMBER = "ContactInformationPhoneIntent"; // 3
	private final static String INTENT_EMAIL_ADDRESS = "ContactInformationEmailIntent"; // 4
	private final static String INTENT_CLASSES = "ClassesTaughtIntent"; // 5
	private final static String INTENT_COMBO = "ContactInformationComboIntent"; // 6
	private final static String INTENT_CLARIFY_PROF = "ProfessorNameIntent"; // 7
	private final static String INTENT_YES = "AMAZON.YesIntent"; // 8
	private final static String INTENT_NO = "AMAZON.NoIntent"; // 9
	private final static String INTENT_STOP = "AMAZON.StopIntent"; // 10
	private final static String INTENT_REPEAT = "RepeatIntent"; // 11
	private final static String INTENT_MORE_INFO = "MoreInfoIntent"; //12
	private final static String INTENT_HELP = "HelpIntent"; // 13
	private final static String INTENT_TELLJOKE = "IntentTellJoke"; // 14
	private final static String INTENT_LOCATION = "locationIntent"; // 15

	//State keys 
	private final static Integer STATE_GET_PROFESSOR = 2;
	private final static Integer STATE_GET_EMAIL = 3;
	private final static Integer STATE_GET_PHONE = 4;
	private final static Integer STATE_GET_EMAIL_PHONE = 5;
	private final static Integer STATE_AMBIGUOUS_PROF = 6;
	private final static Integer STATE_GET_JOKE = 7;
	private final static Integer STATE_GET_LOCATION = 8;

	//Session state storage key
	private final static String SESSION_PROF_STATE = "profState";
	private final static String SESSION_PROF_STATE_2 = "profState2"; //need it because in ambiguous state, still need to store email, phone, email_phone state
	public KnockKnockConversation() {
		super();

		//Add custom intent names for dispatcher use
		supportedIntentNames.add(INTENT_OFFICE_HOURS);
		supportedIntentNames.add(INTENT_CONTACTINFO);
		supportedIntentNames.add(INTENT_PHONE_NUMBER);
		supportedIntentNames.add(INTENT_EMAIL_ADDRESS);
		supportedIntentNames.add(INTENT_CLASSES);
		supportedIntentNames.add(INTENT_COMBO);
		supportedIntentNames.add(INTENT_CLARIFY_PROF);
		supportedIntentNames.add(INTENT_YES);
		supportedIntentNames.add(INTENT_NO);
		supportedIntentNames.add(INTENT_STOP);
		supportedIntentNames.add(INTENT_MORE_INFO);
		supportedIntentNames.add(INTENT_REPEAT);
		supportedIntentNames.add(INTENT_HELP);
		supportedIntentNames.add(INTENT_TELLJOKE);
		supportedIntentNames.add(INTENT_LOCATION);

	}

	//TOD: (done) Set cachedList to null wherever the conversation ends.
	//TOD: (done) Handler to user response for professor clarification.
	//TOD: (done) put if(cachedList.size() > 1) block into function
	// -> set global duplicates to true if set.size() < cachedList.size()
	//TOD:(only took out Jeff's functions and global variables) Remove unnecessary Intents, session, states, and function


	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {

		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		//CASE 1:
		//User asks for professors office hours
		if(INTENT_OFFICE_HOURS.equals(intentName)){
			response = handleOfficeHoursIntent(intentReq, session);
		}
		//CASE 2:
		//User does not clarify whether they want email or phone
		else if (INTENT_COMBO.equals(intentName)){
			response = handleContactInformationIntent(intentReq, session);
		}
		//CASE 3:
		//User specifically asks for phone
		else if (INTENT_PHONE_NUMBER.equals(intentName)){
			response = handlePhoneNumberIntent(intentReq, session);
		}
		//CASE 4:
		//User specifically asks for email
		else if (INTENT_EMAIL_ADDRESS.equals(intentName)){
			response = handleEmailAddressIntent(intentReq, session);
		}
		//CASE 5:
		//User has not given professor name
		else if (INTENT_CLARIFY_PROF.equals(intentName)) {
			response = handleProfessorNameIntent(intentReq, session);
		}
		//CASE 6:
		//User asks for information to be repeated.
		else if (INTENT_REPEAT.equals(intentName)){
			response = handleRepeatIntent(intentReq, session);
		}
		//CASE 7:
		//User asks for more information
		else if (INTENT_MORE_INFO.equals(intentName)){
			response = handleMoreInfoIntent(intentReq, session);
		}
		//CASE 8:
		//User asks for help
		else if (INTENT_HELP.equals(intentName)){
			response = handleHelpIntent(intentReq, session);
		}
		//CASE 9:
		// User asks for location
		else if (INTENT_LOCATION.equals(intentName)){
			response = handleLocationIntent(intentReq, session);
		}
		//CASE 10:
		//User says yeas somewhere.
		else if (INTENT_YES.equals(intentName)){
			response = handleYesIntent(intentReq, session);
		}
		//CASE 11:
		//User says no somewhere
		else if(INTENT_NO.equals(intentName)){
			response = handleNoIntent(intentReq, session);
		}
		//CASE 12:
		//User says tell me a joke
		else if(INTENT_TELLJOKE.equals(intentName)){
			response = handleJokeIntent(intentReq, session);
		}
		//CASE 13:
		else if(INTENT_STOP.equals(intentName)){
			response = handleStopIntent(intentReq, session);
		}
		else {
			response = newTellResponse("<speak> Whatchu talkin' bout! </speak>", true);
			cachedList = null;

		}


		return response;
	}
	//TOD: (done) put if(cachedList.size() > 1) block into function
	// -> set global duplicates to true if set.size() < cachedList.size()
	private String makeListOfDistinctProfessors(Session session)
	{

		session.setAttribute(SESSION_PROF_STATE_2, STATE_AMBIGUOUS_PROF);


		Set<String> distinct = new HashSet<String>();
		for(int i = 0; i < cachedList.size(); i++)
			distinct.add(cachedList.get(i).getName());


		if(distinct.size() < cachedList.size())
			duplicates = true;


		String list ="";
		int i = 0;
		Iterator iter = distinct.iterator();
		while(iter.hasNext())
		{

			String s = iter.next().toString();
			// if 
			if(i == distinct.size()-1)

				list = list + " or " + s;
			else
				list = list + " " + s;
			i++;
		}
		return list;
	}

	// what does this do 
	private SpeechletResponse respondToSessionEndRequest(SessionEndedRequest sessionEndedReq, Session session)
	{
		SpeechletResponse response = null;

		response.setShouldEndSession(true);
		return response;

	}
	private SpeechletResponse handleStopIntent(IntentRequest intentReq, Session session)
	{	
		return newTellResponse("<speak> ok, see you later alligator </speak>", true);
	}
	private SpeechletResponse handleMoreInfoIntent(IntentRequest intentReq, Session session){
		//If they have already gotten email/phone, give them the other.
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		pc = cachedList.get(0);
		if (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			//We last gave email, so its time to give phone
			if(pc.getPhone() == null || pc.getPhone().isEmpty()){ // checking if we have phone
				String name = pc.getName();
				response = newTellResponse("<speak>I'm sorry, I don't have any more contact info for " + name + ". </speak>", true);
				cachedList = null;
			}
			else{
				String name = pc.getName();
				String phoneNum = pc.getPhone();
				response = newAskResponse("<speak>" + name + "s phone number is " +  " <say-as interpret-as=\"telephone\">" + phoneNum + " </say-as>, would you like me to repeat that? </speak>", true, " <speak> would you like me to repeat their phone number? </speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
			}
		}
		else if (STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			//We last gave phone, so its time to give email
			if(pc.getEmail() == null || pc.getEmail().isEmpty()){ // checking if we have email
				String name = pc.getName();
				response = newTellResponse("<speak>I'm sorry, I don't have any more contact info for " + name + ". </speak>", true);
				cachedList = null;

			}
			else{
				String sp = "@ sonoma . e, d, u";
				String name = pc.getName();
				String email = pc.getEmail();
				String [] parts = email.split("@");
				String fp = parts[0].replace("",", ");
				if(fp.contains(".")){
					fp.replaceAll(".", "dot");
				}
				if(parts[1].toLowerCase() == "sonoma.edu" )
				{
					sp = "@ sonoma . e d u ";
				}
				else if(parts[1].toLowerCase() == "gmail.com" )
				{
					sp = "@ g mail . com ";
				}
				else if(parts[1].toLowerCase() == "yahoo.com" )
				{
					sp = "@ yahoo . com";
				}
				else if(parts[1].toLowerCase() == "hotmail.com" )
				{
					sp = "@ hot mail . com";
				}
				response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + "</say-as>, would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true); 

				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
			}
		}

		else if (STATE_GET_LOCATION.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			//TODO
			// check which parameters are null and give what we do have 
			//If they have already gotten email/phone, give them the other.
			if(pc.getEmail() == null || pc.getEmail().isEmpty()) {
				String phonenum = pc.getPhone(); // give phone
				if(pc.getPhone() == null || pc.getPhone().isEmpty()) {
					// both are null
					response = newTellResponse (" <speak> This professor has no more info available </speak> ", false);
				}
				// else{
				//	TODO response = newTellResponse (" <speak> <>")
			}
			// TODO check other cases 
		}


		else
		{
			response = newTellResponse("<speak> Peace out cub scout! </speak>", true);
		}

		return response;
	}

	private SpeechletResponse handleHelpIntent(IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;


		String location_intent = "Here are some things you can ask to get a professor's location . " +
				"where can I find Professor Smith's office, or " +
				"Locate Professor Smith, ";

		String contact_information_combo_intent = ",, To get a professor's contact info. say something like. " +
				"what is Professor Smith's contact info, or " +
				"get me contact info for Professor Smith.";

		response = newTellResponse("<speak>" + location_intent + contact_information_combo_intent + "</speak>", true);

		return response;
	}

	/*
	 * This function handles the intent when the user asks for information to be repeated.
	 * Checks the states to see what information to repeat.
	 */
	private SpeechletResponse handleRepeatIntent(IntentRequest intentReq, Session session){
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		//Get professor stored in global variable cachedProf
		pc = cachedList.get(0);

		if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String sp = "@ sonoma . e, d, u";
			String email = pc.getEmail();
			String [] parts = email.split("@");
			String fp = parts[0].replace("",", ");
			if(fp.contains(".")){
				fp.replaceAll(".", "dot");
			}
			if(parts[1].toLowerCase() == "sonoma.edu" )
			{
				sp = "@ sonoma . e d u ";
			}
			else if(parts[1].toLowerCase() == "gmail.com" )
			{
				sp = "@ g mail . com ";
			}
			else if(parts[1].toLowerCase() == "yahoo.com" )
			{
				sp = "@ yahoo . com";
			}
			else if(parts[1].toLowerCase() == "hotmail.com" )
			{
				sp = "@ hot mail . com";
			}
			response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + ", would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true); 

		}
		else if (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String sp = "@ sonoma . e, d, u";
			String email = pc.getEmail();
			String [] parts = email.split("@");
			String fp = parts[0].replace("",", ");
			if(fp.contains(".")){
				fp.replaceAll(".", "dot");
			}
			if(parts[1].toLowerCase() == "sonoma.edu" )
			{
				sp = "@ sonoma . e d u ";
				response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + ", would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true); 
			}
			else if(parts[1].toLowerCase() == "gmail.com" )
			{
				sp = "@ g mail . com ";
			}
			else if(parts[1].toLowerCase() == "yahoo.com" )
			{
				sp = "@ yahoo . com";
			}
			else if(parts[1].toLowerCase() == "hotmail.com" )
			{
				sp = "@ hot mail . com";

			}
			response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + ", would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true);


		}
		else if (STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String phone = pc.getPhone();
			response = newTellResponse("<speak>" + name + "s phone number is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as> . </speak>", true);
			cachedList = null;
		}
		else if (STATE_GET_LOCATION.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			if(pc.getBuildingName() != null && !pc.getBuildingName().isEmpty())
			{
				//We have building name
				response = newTellResponse("<speak> " + pc.getName() + "can be found at" + pc.getBuildingName() + "</speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
			}
			// they don't have a location
			else
			{
				response = newTellResponse("<speak> " + pc.getName() + "is in the eternal ether" + "</speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
			}
		}
		else
		{
			response = newTellResponse("<speak> Watchu talkin about willis? </speak>", true);
			cachedList = null;
		}

		return response;
	}
	/*
	 * User says something in yes intent.
	 */
	private SpeechletResponse handleYesIntent(IntentRequest intentReq, Session session)
	{

		if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
		{//Case where user was given both email and phone and wants it repeated.
			return handleRepeatIntent(intentReq, session);
		}
		else if(STATE_GET_JOKE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			getJoke();
			return newTellResponse("<speak>" + joke_opener + ",,," + joke_punchline + "</speak>", true);
		}

		else
		{//Case where user was asked if they wanted info repeated or more information, and user responded with yes intent.

			cachedList = null;
			return newTellResponse("<speak> That wasn't a yes or no question, dumb dumb.</speak>", true);


		}
	}
	/*
	 * User said no.
	 */
	private SpeechletResponse handleNoIntent(IntentRequest intentReq, Session session)
	{
		if((STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0) || (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0) || 
				(STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0))
		{
			cachedList = null;

			return newTellResponse("<speak> Okie dokie.</speak>", true);
		}
		cachedList = null;

		return newTellResponse("<speak> Please ask for professor information first.</speak>", true);
	}

	private SpeechletResponse handleOfficeHoursIntent(IntentRequest intentReq, Session session)
	{

		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		Slot professorNameSlot = slots.get("ProfessorName");
		String professor = professorNameSlot.getValue();
		cachedList = null;

		return newTellResponse("<speak>" + professor + "has office hours Wednesdays at 8 AM. </speak>", true);
		// return a tell response object within SpeechletResponse
		// what is the difference between ask and tell (does alexa use both)
	}

	private SpeechletResponse classesTaughtIntent(IntentRequest intentReq, Session session)
	{
		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		Slot professorNameSlot = slots.get("ProfessorName");
		SpeechletResponse response = null;
		String professor = professorNameSlot.getValue();
		String cs = "CS";
		//check state
		response = newTellResponse("<speak>" + professor + "is teaching " + "<say-as interpret-as=\"characters\">" + cs + "</say-as> 315 and " + "<say-as interpret-as=\"characters\">" + cs + "</say-as> 115 this semester" + ". </speak>", true);
		cachedList = null;


		return response;
	}

	private SpeechletResponse ContactInformationIntentResponse(IntentRequest intentReq, Session session){
		ProfContact pc = null;
		pc = cachedList.get(0);

		SpeechletResponse response = null;
		if(pc.getName().toLowerCase() == "Kathy Morris"){
			response = newAskResponse("Sorry there is no contact information for " + pc.getName() + ". Would you like to hear a joke instead? ", false, "Would you like to hear a joke instead? ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		if(pc.getEmail() == null || pc.getEmail().isEmpty())
		{
			if(pc.getPhone() == null || pc.getPhone().isEmpty() || pc.getName().toLowerCase() == "kathy morris")
			{
				//No Phone or Email
				response = newAskResponse("Sorry there is no contact information for " + pc.getName() + ". Would you like to hear a joke instead? ", false, "Would you like to hear a joke instead? ", false);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
				cachedList = null;

			}
			else
			{
				//Phone, but no Email
				String name = pc.getName();
				String phone = pc.getPhone();
				response = newAskResponse("<speak>" + name + " has no email listed, but their phone is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as> . Would you like me to repeat that? </speak>", true, "<speak> I did not catch that, You can say repeat, more information, or tell me a joke </speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
			}
		}
		else
		{
			if(pc.getPhone() == null || pc.getPhone().isEmpty())
			{
				//Email, but no Phone
				String name = pc.getName();
				String sp = "@ sonoma . e, d, u";
				String email = pc.getEmail();
				String [] parts = email.split("@");
				String fp = parts[0].replace("",", ");
				if(fp.contains(".")){
					fp.replaceAll(".", "dot");
				}
				if(parts[1].toLowerCase() == "sonoma.edu" )
				{
					sp = "@ sonoma . e d u ";
					response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + ", would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true); 
				}
				else if(parts[1].toLowerCase() == "gmail.com" )
				{
					sp = "@ g mail . com ";
				}
				else if(parts[1].toLowerCase() == "yahoo.com" )
				{
					sp = "@ yahoo . com";
				}
				else if(parts[1].toLowerCase() == "hotmail.com" )
				{
					sp = "@ hot mail . com";

				}

				response = newAskResponse("<speak>" + name + " has no phone listed, but their email is " + fp + sp + "Would you like me to repeat that? You can say repeat or ask for more information.</speak>", true, "<speak> I did not catch that, did you want me to repeat the email address. </speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
			}
			else
			{
				//Email and Phone
				String name = pc.getName();
				String sp = "@ sonoma . e, d, u";
				String email = pc.getEmail();
				String [] parts = email.split("@");
				String fp = parts[0].replace("",", ");
				if(fp.contains(".")){
					fp.replaceAll(".", "dot");
				}
				if(parts[1].toLowerCase() == "sonoma.edu" )
				{
					sp = "@ sonoma . e d u ";
					response = newAskResponse("<speak> " + name + "s email address is " + fp + sp + ", would you like me to repeat that?</speak>", true, " <speak> I didn't catch that, You can say something like repeat, more information, or tell me a joke</speak>", true); 
				}
				else if(parts[1].toLowerCase() == "gmail.com" )
				{
					sp = "@ g mail . com ";
				}
				else if(parts[1].toLowerCase() == "yahoo.com" )
				{
					sp = "@ yahoo . com";
				}
				else if(parts[1].toLowerCase() == "hotmail.com" )
				{
					sp = "@ hot mail . com";

				}

				String phone = pc.getPhone();
				response = newAskResponse("<speak>" + name + "s email is " + fp + sp + ", their phone is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as> . Would you like me to repeat that?</speak>", true, "<speak> I did not catch that, You can say repeat, more information, or tell me a joke.</speak>", true);
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL_PHONE);
			}
		}	
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
			response = newAskResponse("<speak> What is this professor's name? </speak>", true, "<speak> I didn't catch that,  Can I have a professor name </speak>", true);
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
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL_PHONE);
				String list = makeListOfDistinctProfessors(session);
				return newAskResponse("Did you mean, " + list + ", say first and last name please", false, "Did you mean, " + list, false);
			}
			else
			{
				return ContactInformationIntentResponse(intentReq, session);
			}
		}

		else
		{
			response = newAskResponse("I did not hear a professor name, can you try again", false, "I didn't catch that,  Can I have a professor name ", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		return response;
	}

	private SpeechletResponse handleProfessorNameIntent (IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;

		if(STATE_GET_PROFESSOR.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
		{
			response = handleContactInformationIntent(intentReq, session);
			return response;
		}
		else if(STATE_AMBIGUOUS_PROF.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE_2)) == 0)
		{
			session.setAttribute(SESSION_PROF_STATE_2, null); //Assume we have disambiguated, so no need to disambiguate again.
			Intent intent = intentReq.getIntent();
			Map<String, Slot> slots = intent.getSlots();
			// may give error if slot is empty
			String professor_name_string = slots.get("ProfessorName").getValue();

			int lowestLD = levenshteinDistance(professor_name_string, cachedList.get(0).getName()); //ultimately might want to cap in levenshtein function, to improve efficiency
			int lowesti = 0;
			for(int i = 1; i < cachedList.size(); i++)
			{
				int ld = levenshteinDistance(professor_name_string, cachedList.get(i).getName());
				if (ld < lowestLD)
				{
					lowestLD = ld;
					lowesti = i;
				}
			}
			ProfContact profcont = cachedList.get(lowesti);
			cachedList.clear(); //might be a bit of a memory leak, but we got garbage collectors, eh?
			cachedList.add(profcont);
			response = null;
			if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
			{
				response = ContactInformationIntentResponse(intentReq, session);
			}
			else if(STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
			{
				response = EmailAddressIntentResponse(intentReq, session);
			}
			else if(STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
			{
				response = PhoneNumberIntentResponse(intentReq, session);
			}
			else if(STATE_GET_LOCATION.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
			{
				response = LocationIntentResponse(intentReq, session);
			}
			else
			{
				response = newTellResponse("error. I am unsure what peice of info was originally requested", false); //Should never happen, but if it does, heres an error message
			}
			return response;

			//something for if there is too far a match match
			//return newAskResponse("I did not hear one of the professors I spoke of, can you repeat, and say their full name?", false, "I didn't catch that, can you repeat your professor's full name?", false);
		}
		else
		{
			//something for if they just say a professor's name out of the blue. maybe ask if they want email or phone?
			// has to return something 
			response = handleContactInformationIntent(intentReq, session); //this works for now... I think.
			return response;
		}

	}
	private SpeechletResponse PhoneNumberIntentResponse(IntentRequest intentReq, Session session)
	{
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;

		pc = cachedList.get(0);
		String phone_number = pc.getPhone();
		if(phone_number != null && !phone_number.isEmpty())
		{
			// phone number exists
			response = newAskResponse("<speak> Here is " + pc.getName() + "'s phone number: " + " <say-as interpret-as=\"telephone\">" + phone_number + "</say-as> . Would you like me to repeat that or give you more info on " + pc.getName() + "?</speak>", true, "<speak> I didn't catch that, would you like me to repeat their phone number or give you more info? </speak>", true);
		}

		else if(pc.getEmail() != null && !pc.getEmail().isEmpty())
		{
			// phone number doesn't exist, but email does
			response = newAskResponse("This professor has no phone number, would you like their email ", false, "Would you like their email?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
		}
		return response;
	}
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
			try
			{
				GetEmailPhone(professor_name);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
			}
			if(cachedList.size() > 1)
			{
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
				String list = makeListOfDistinctProfessors(session);
				return newAskResponse("<speak> Did you man, " + list + ", say first and last name please</speak>", true, "Did you mean, " + list, false);
			}
			else
			{
				response = PhoneNumberIntentResponse(intentReq, session);
			}	
		}
		else
			//prof name is null or empty
		{
			response = newAskResponse("<speak>I did not hear a professor name, can you try again</speak>", true, "<speak>I didn't catch that,  Can I have a professor name</speak>", true);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PROFESSOR);
		}
		return response;
		// phone number = getFakeinfo(professor_name, "phone")
		// return response with professor_name and phone number
	}
	private SpeechletResponse EmailAddressIntentResponse(IntentRequest intentReq, Session session)
	{
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;
		pc = cachedList.get(0);

		if(pc.getEmail() != null && !pc.getEmail().isEmpty())
		{
			//We have email
			response = newAskResponse("<speak> Here is " + pc.getName() + "'s email address: " + " <say-as interpret-as=\"spell-out\">" + pc.getEmail() + "</say-as> . Would you like me to repeat that or give you more info on " + pc.getName() + "? </speak>", true, "<speak>I didn't catch that, would you like me to repeat their email or give you more info?</speak>", true);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
		}

		else if(pc.getPhone() != null && !pc.getPhone().isEmpty())
		{
			//No email, but we have phone
			response = newAskResponse("This professor has no email address listed. Would you like their phone?  ", false, "Would you like their phone?", false);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
		}
		return response;
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
			try
			{
				session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
				GetEmailPhone(professor_name);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
			}
			if(cachedList.size() > 1)
			{
				String list = makeListOfDistinctProfessors(session);
				return newAskResponse("Did you mean, " + list + ", say first and last name please", false, "Did you mean, " + list, false);
				//return 
			}
			else
			{
				response = EmailAddressIntentResponse(intentReq, session);
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

	public SpeechletResponse LocationIntentResponse(IntentRequest intentReq, Session session)
	{
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;
		pc = cachedList.get(0);
		// they have a location
		if(pc.getBuildingName() != null && !pc.getBuildingName().isEmpty())
		{
			//We have building name
			response = newAskResponse("<speak> " + pc.getName() + " can be found at " + pc.getBuildingName() + " . Would you like me to repeat that or give you more info on " + pc.getName() + "? </speak>", true, "<speak>I didn't catch that, would you like me to repeat their location or give you more info?</speak>", true);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
		}
		// they don't have a location
		else
		{
			response = newAskResponse("<speak> " + pc.getName() + "is in the eternal ether" + " . Would you like me to repeat that or give you more info on " + pc.getName() + "? </speak>", true, "<speak>I didn't catch that, would you like me to repeat their location or give you more info?</speak>", true);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
		}
		return response;
	}

	public SpeechletResponse handleLocationIntent(IntentRequest intentReq, Session session)
	{
		Intent location_intent = intentReq.getIntent();
		String professor_name = location_intent.getSlots().get("ProfessorName").getValue();
		ProfContact pc = new ProfContact();
		SpeechletResponse response = null;

		// get list of people
		if(professor_name != null && !professor_name.isEmpty())
			//we have prof name
		{
			try
			{
				GetEmailPhone(professor_name);
			}
			catch (ClassNotFoundException | SQLException e)
			{	// TODO Auto-generated catch block
				pc.setPhone(e.toString());
			}
		}
		// will get a list of professors with building names
		// narrow list down
		if(cachedList.size() > 1)
		{
			String list = makeListOfDistinctProfessors(session);

			session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
			session.setAttribute(SESSION_PROF_STATE_2, STATE_AMBIGUOUS_PROF);
			response = newAskResponse("Did you mean" + list + "say first name and last name  please", false, "Did you mean, " + list, false);
		}
		else
		{

			response = LocationIntentResponse(intentReq, session);
		}
		session.setAttribute(SESSION_PROF_STATE, STATE_GET_LOCATION);
		return response;

	}

	private SpeechletResponse handleJokeIntent(IntentRequest intentReq, Session session)
	{
		getJoke();
		return newTellResponse("<speak>" + joke_opener + ",,," + joke_punchline + "</speak>", true);
	}

	public static void GetEmailPhone(String name2) throws ClassNotFoundException, SQLException
	{
		//Template url added with professor name asked for
		name2 = name2.replace(" ", "%20");
		if(name2.contains("'s"))
		{
			name2 = name2.replace("'s", "");
		}
		String full_url = "https://moonlight.cs.sonoma.edu/api/v1/directory/person/?format=json&search=" + name2;

		ArrayList<ProfContact> array = new ArrayList<ProfContact>();
		try
		{

			StringBuilder result = new StringBuilder();
			//Create url to correctly encode url (i.e. spaces become %20)

			URL url2 = new URL(full_url);
			//Make Http Connection
			HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
			//Request to get for information
			conn.setRequestMethod("GET");
			//Read the http response into a BufferReader
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null)
			{
				result.append(line);
			}
			//End connection
			rd.close();
			conn.disconnect();

			String json_text = result.toString();
			//Interpret json_text string as a json array
			JSONArray arr = new JSONArray(json_text);

			//iterate through the json array, which consists of professor information
			for(int i = 0; i < arr.length(); i=i+1)
			{
				JSONObject json = arr.getJSONObject(i);
				ProfContact pc = new ProfContact();
				if(!json.isNull("email"))//if the value for email is not null, set the email
				{
					pc.setEmail(json.getString("email"));
				}
				if(!json.isNull("phone"))//if the value for phone is not null, set the phone
				{
					pc.setPhone(json.getString("phone"));
				}
				if(!json.isNull("name"))//if the value for name is not null, set the name
				{
					pc.setName(json.getString("name").toLowerCase());
				}
				if(!json.isNull("building_name"))//if the value for building_name is not null, set building_name
				{
					pc.setBuildingName(json.getString("building_name"));
				}
				array.add(pc.copy());
				pc= null;
				json = null;
			}
			//store the array of ProfContacts globally for repeated use
			cachedList = array;
			return;

		}
		//Catches in case of errors
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
	}
	public int levenshteinDistance (CharSequence lhs, CharSequence rhs) {                          
		int len0 = lhs.length() + 1;                                                     
		int len1 = rhs.length() + 1;                                                     

		// the array of distances                                                       
		int[] cost = new int[len0];                                                     
		int[] newcost = new int[len0];                                                  

		// initial cost of skipping prefix in String s0                                 
		for (int i = 0; i < len0; i++) cost[i] = i;                                     

		// dynamically computing the array of distances                                  

		// transformation cost for each letter in s1                                    
		for (int j = 1; j < len1; j++) {                                                
			// initial cost of skipping prefix in String s1                             
			newcost[0] = j;                                                             

			// transformation cost for each letter in s0                                
			for(int i = 1; i < len0; i++) {                                             
				// matching current letters in both strings                             
				int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             

				// computing cost for each transformation                               
				int cost_replace = cost[i - 1] + match;                                 
				int cost_insert  = cost[i] + 1;                                         
				int cost_delete  = newcost[i - 1] + 1;                                  

				// keep minimum cost                                                    
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}                                                                           

			// swap cost/newcost arrays                                                 
			int[] swap = cost; cost = newcost; newcost = swap;                          
		}                                                                               

		// the distance is the cost for transforming all letters in both strings        
		return cost[len0 - 1];                                                          
	}

	private void getJoke()
	{
		Connection con = null;
		try
		{
			byte[] bits = new byte[]{88, 116, 108, 97, 116, 105, 108, 112, 97, 53};
			int x =  1 + (int) (Math.random() * 23);
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://cwolf.cs.sonoma.edu:3306/restrella?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=PST", "restrella", new String(bits,"UTF-8"));
			Statement stmnt = con.createStatement();
			String sql = "SELECT jokes.opener, jokes.punchline FROM jokes WHERE jokes.joke_id = " + x;
			stmnt = con.createStatement();
			ResultSet rs = stmnt.executeQuery(sql);
			while(rs.next()){
				joke_opener = rs.getString(1);
				if(rs.wasNull()){
					joke_opener = x + "";
				}

				joke_punchline = rs.getString(2);
				if(rs.wasNull()){
					joke_punchline = x + "";
				}

			}
			return;
		}
		catch (SQLException | ClassNotFoundException e)
		{
			joke_opener = "Errors: " + e.toString();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			joke_opener = e.toString();
		}
	}
}
