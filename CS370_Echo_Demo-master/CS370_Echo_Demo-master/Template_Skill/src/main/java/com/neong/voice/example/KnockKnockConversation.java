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
	private ProfContact cachedProf;
	private Boolean duplicates;

	//Intent names
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
	private final static String INTENT_HELP = "HelpIntent";

	//State keys 
	private final static Integer STATE_GET_PROFESSOR = 2;
	private final static Integer STATE_GET_EMAIL = 3;
	private final static Integer STATE_GET_PHONE = 4;
	private final static Integer STATE_GET_EMAIL_PHONE = 5;
	private final static Integer STATE_AMBIGUOUS_PROF = 6;


	//Session state storage key
	private final static String SESSION_PROF_STATE = "profState";
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
		supportedIntentNames.add(INTENT_MORE_INFO);
		supportedIntentNames.add(INTENT_REPEAT);
		supportedIntentNames.add(INTENT_HELP);

	}

	//TODO: (done)Set cachedList to null wherever the conversation ends.
	//TODO: Handler to user response for professor clarification.
	//TODO: (done) put if(cachedList.size() > 1) block into function
			// -> set global duplicates to true if set.size() < cachedList.size()
	//TODO:(done) Remove unnecessary Intents, session, states, and function
	
	
	@Override
	public SpeechletResponse respondToIntentRequest(IntentRequest intentReq, Session session) {

		Intent intent = intentReq.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;
		SpeechletResponse response = null;
		//CASE I:
		//User asks for professors office hours
		if(INTENT_OFFICE_HOURS.equals(intentName)){
			response = handleOfficeHoursIntent(intentReq, session);
		}
		//CASE II:
		//User does not clarify whether they want email or phone
		else if (INTENT_COMBO.equals(intentName)){
			response = handleContactInformationIntent(intentReq, session);
		}
		//CASE III:
		//User specifically asks for phone
		else if (INTENT_PHONE_NUMBER.equals(intentName)){
			response = handlePhoneNumberIntent(intentReq, session);
		}
		//CASE IV:
		//User specifically asks for email
		else if (INTENT_EMAIL_ADDRESS.equals(intentName)){
			response = handleEmailAddressIntent(intentReq, session);
		}
		//CASE VI:
		//User has not given professor name
		else if (INTENT_CLARIFY_PROF.equals(intentName)) {
			response = handleProfessorNameIntent(intentReq, session);
		}
		//CASE VII:
		//User asks for information to be repeated.
		else if (INTENT_REPEAT.equals(intentName)){
			response = handleRepeatIntent(intentReq, session);
		}
		//CASE VIII:
		//User asks for more information
		else if (INTENT_MORE_INFO.equals(intentName)){
			response = handleMoreInfoIntent(intentReq, session);
		}
		//CASE IX:
		//User asks for help
		else if (INTENT_HELP.equals(intentName)){
			response = handleHelpIntent(intentReq, session);
		}
		//CASE X:
		//User says yeas somewhere.
		else if (INTENT_YES.equals(intentName)){
			response = handleYesIntent(intentReq, session);
		}
		//CASE XI:
		//User says no somewhere
		else if(INTENT_NO.equals(intentName)){
			response = handleNoIntent(intentReq, session);
		}
		//CASE XII:
		//Unmapped intent
		else {
			response = newTellResponse("<speak> Whatchu talkin' bout! </speak>", true);
			cachedList = null;

		}


		return response;
	}
	//TODO:(done) put if(cachedList.size() > 1) block into function
	// -> set global duplicates to true if set.size() < cachedList.size()
	private SpeechletResponse makeListOfDistinctProfessors(Session session)
	{

			session.setAttribute(SESSION_PROF_STATE, STATE_AMBIGUOUS_PROF);
			
			
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
				if(i == distinct.size()-1)
					list = list + " or " + s;
				else
					list = list + " " + s;
				i++;
			}
			return newAskResponse("Did you mean, " + list + ", say first and last name please", false, "Did you mean, " + list, false);
	}
	private SpeechletResponse handleMoreInfoIntent(IntentRequest intentReq, Session session){
		//If they have already gotten email/phone, give them the other.
		SpeechletResponse response = null;
		ProfContact pc = new ProfContact();
		pc = cachedProf;
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
			String email = pc.getEmail();
			String name = pc.getName();
			response = newAskResponse("<speak> " + name + "s email address is " + " <say-as interpret-as=\"spell-out\">" + email + "</say-as>, would you like me to repeat that? You can say repeat or ask for more information.</speak>", true, " <speak> would you like me to repeat their email address? </speak>", true);
			session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
			}
		}
		else
			response = newTellResponse("<speak> Watchu talkin about free willie? </speak>", true);
			cachedList = null;

		return response;
	}

	private SpeechletResponse handleHelpIntent(IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;


		String office_hours_intent = "Here are some things you can say to get your professor's office hours. " +
				"where can I find ProfessorName office, or " +
				"where is ProfessorName, or " +
				"Locate ProfessorName, or " +
				"pinpoint ProfessorName. ";

		String contact_information_combo_intent = "Here are some things you can say to get your professor's contact information. " +
				"what is ProfessorName contact info, or " +
				"get me contact info for ProfessorName.";

		response = newTellResponse("<speak>" + office_hours_intent + contact_information_combo_intent + "</speak>", true);
		cachedList = null;

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
		pc = cachedProf;

		if(STATE_GET_EMAIL_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String email = pc.getEmail();
			String phone = pc.getPhone();
			response = newTellResponse("<speak>" + name + " s email is " + " <say-as interpret-as=\"spell-out\">" + email + "</say-as> their phone number is <say-as interpret-as=\"telephone\">" + phone + "</say-as>. </speak>", true);
			cachedList = null;

		}
		else if (STATE_GET_EMAIL.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String email = pc.getEmail();
			response = newTellResponse("<speak>" + name + "s email address is " + " <say-as interpret-as=\"spell-out\">" + email + " </say-as> . </speak>", true);
			cachedList = null;

		}
		else if (STATE_GET_PHONE.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0){
			String name = pc.getName();
			String phone = pc.getPhone();
			response = newTellResponse("<speak>" + name + "s phone number is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as> . </speak>", true);
			cachedList = null;

		}
		else
			response = newTellResponse("<speak> Watchu talkin about willis? </speak>", true);
			cachedList = null;

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

			return newTellResponse("<speak> No thank you? sheesh, last time i help you.</speak>", true);
		}
		cachedList = null;

		return newTellResponse("<speak> Please ask for professor information first.</speak>", true);
	}

	private SpeechletResponse handleOfficeHoursIntent(IntentRequest intentReq, Session session)
	{

		Intent intent = intentReq.getIntent();
		Map<String, Slot> slots = intent.getSlots();
		Slot professorNameSlot = slots.get("ProfessorName");
		SpeechletResponse response = null;
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
				return makeListOfDistinctProfessors(session);

			}
			else
			{
				pc = cachedList.get(0);
				if(pc.getEmail() == null || pc.getEmail().isEmpty())
				{
					if(pc.getPhone() == null || pc.getPhone().isEmpty())
					{
						//No Phone or Email
						String name = pc.getName();
						response = newTellResponse("<speak>" + name + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable. </speak>", true);
						cachedProf = pc;
						cachedList = null;

					}
					else
					{
						//Phone, but no Email
						String name = pc.getName();
						String phone = pc.getPhone();
						response = newAskResponse("<speak>" + name + " has no email listed, but their phone is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as> would you like me to repeat that? You can say repeat or ask for more information.</speak>", true, "<speak> I did not catch that, did you want me to repeat the phone number </speak>", true);
						session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
						cachedProf = pc;
					}
				}
				else
				{
					if(pc.getPhone() == null || pc.getPhone().isEmpty())
					{
						//Email, but no Phone
						String name = pc.getName();
						String email = pc.getEmail();
						response = newAskResponse("<speak>" + name + " has no phone listed, but their email is " + " <say-as interpret-as=\"spell-out\">" + email + "</say-as> would you like me to repeat that? You can say repeat or ask for more information.</speak>", true, "<speak> I did not catch that, did you want me to repeat the email address. </speak>", true);
						session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
						cachedProf = pc;
					}
					else
					{
						//Email and Phone
						String name = pc.getName();
						String email = pc.getEmail();
						String phone = pc.getPhone();
						response = newAskResponse("<speak>" + name + "s email is " + " <say-as interpret-as=\"spell-out\">" + email +  " their phone is " + " <say-as interpret-as=\"telephone\">" + phone + "</say-as>, would you like me to repeat that? You can say repeat or ask for more information.</speak>", true, "<speak> I did not catch that, did you want me to repeat " + name + "'s contact info? </speak>", true);
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

	private SpeechletResponse handleProfessorNameIntent (IntentRequest intentReq, Session session)
	{
		SpeechletResponse response = null;

		if(STATE_GET_PROFESSOR.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
		{
		response = handleContactInformationIntent(intentReq, session);
		return response;
		}
		else if(STATE_AMBIGUOUS_PROF.compareTo((Integer)session.getAttribute(SESSION_PROF_STATE)) == 0)
		{
			Intent intent = intentReq.getIntent();
			Map<String, Slot> slots = intent.getSlots();
			// may give error if slot is empty
			String professor_name_string = slots.get("ProfessorName").getValue();

			int lowestLD = levenshteinDistance(professor_name_string, cachedList.get(0).getName(); //ultimately might want to cap in levenshtein function, to improve efficiency
			int lowesti = 0;
			for(int i = 1; i < cachedList.size(); i++)
			{
				int ld = levenshteinDistance(professor_name_string, cachedList.get(i).getName());
				if (ld < lowestLD))
				{
					lowestLD = ld;
					lowesti = i;
				}
			}
			ProfContact profcont = cachedList.get(lowesti);
			cachedList.clear(); //might be a bit of a memory leak, but we got garbage collectors, eh?
			cachedList.add(profcont);
			response = null;
			response = handleContactInformationIntent(intentReq, session);//wrongish if came from email and phone intent
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
				return makeListOfDistinctProfessors(session);
				
				
			}
			else
			{
				pc = cachedList.get(0);
				String phone_number = pc.getPhone();

				if(phone_number != null && !phone_number.isEmpty())
				{		
					// phone number exists
					response = newAskResponse("<speak> Here is " + professor_name + "'s phone number: " + " <say-as interpret-as=\"telephone\">" + phone_number + "</say-as>, would you like me to repeat that or give you more info on " + professor_name + "?</speak>", true, "<speak> I didn't catch that, would you like me to repeat their phone number or give you more info? </speak>", true);
				}
				else if(pc.getEmail() != null && !pc.getEmail().isEmpty())
				{
					// phone number doesn't exist, but email does
					response = newAskResponse("This professor has no phone number, would you like their email ", false, "Would you like their email?", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
					cachedProf = pc;
				}
				else
				{
					//neither phone nor email exist
					response = newTellResponse(pc.getName() + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable.", false);
					cachedList = null;

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
				return makeListOfDistinctProfessors(session);

			}
			else
			{
				pc = cachedList.get(0);
				if(pc.getEmail() != null && !pc.getEmail().isEmpty())
				{
					//We have email
					response = newAskResponse("<speak> Here is " + professor_name + "'s email address: " + " <say-as interpret-as=\"spell-out\">" + pc.getEmail() + "</say-as>, would you like me to repeat that or give you more info on " + professor_name + "? </speak>", true, "<speak>I didn't catch that, would you like me to repeat their email or give you more info?</speak>", true);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_EMAIL);
					cachedProf = pc;
				}
				else if(pc.getPhone() != null && !pc.getPhone().isEmpty())
				{
					//No email, but we have phone
					response = newAskResponse("This professor has no email address listed. Would you like their phone?  ", false, "Would you like their phone?", false);
					session.setAttribute(SESSION_PROF_STATE, STATE_GET_PHONE);
					cachedProf = pc;
				}
				else
				{
					//No email nor phone
					response = newTellResponse(pc.getName() + " has no email or phone listed. I am sorry to have failed you. I accept whatever horrific punishment you deem suitable.", false);
					cachedList = null;

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

	public static void GetEmailPhone(String name2) throws ClassNotFoundException, SQLException
	{
		//Template url added with professor name asked for
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
			for(int i = 0; i < arr.length(); i=i+1){
				JSONObject json = arr.getJSONObject(i);
				ProfContact pc = new ProfContact();
				if(!json.isNull("email")){//if the value for email is not null, set the email
					pc.setEmail(json.getString("email"));
				}
				if(!json.isNull("phone")){//if the value for phone is not null, set the phone
					pc.setPhone(json.getString("phone"));
				}
				if(!json.isNull("name")){//if the value for name is not null, set the name
					pc.setName(json.getString("name").toLowerCase());
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
}
