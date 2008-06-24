package eeml;

//v.0.9 (June 2008)
//EEML (Extended Environments Markup Language) see http://www.eeml.org/ for more info

//The EEML library facilitates the sharing of EEML documents between remote environments via
//a network. It is used particularly in connecting to the main repository for feeds 
//located at http://www.pachube.com/
//The EEML library makes use of the existing processing net library
//XML parsing is based on proXML by Christian Riekoff (see http://www.texone.org/proxml/ for more info)
//thank you! 

import processing.core.PApplet;

/**
 * This is one of the basic classes but should not normally be used, since it's not threaded.
 * Instead see the DataIn class. 
 * @see DataIn
 */

public class In {

	PApplet parent;
	private String remoteURL;
	private XMLInOut remoteXML;	
	private XMLElement remoteEnvironment;

	private XMLElement dataElement;
	private XMLElement statusElement;
	private XMLElement valueElement;
	private XMLElement tagElement;
	private XMLElement unitElement;

	private String pachubeAPIKey;

	public In (String remoteURL_) {  
		remoteURL = remoteURL_;   
		remoteXML = new XMLInOut(parent);
		//remoteEnvironment = new XMLElement("");

		dataElement = new XMLElement("data");
		statusElement = new XMLElement("status");
		valueElement = new XMLElement("value");
		tagElement = new XMLElement("tag");
		unitElement = new XMLElement("unit");
	} 

	public In (String remoteURL_, String apiKey) {  
		remoteURL = remoteURL_;   
		remoteXML = new XMLInOut(parent);
		//remoteEnvironment = new XMLElement("");

		dataElement = new XMLElement("data");
		statusElement = new XMLElement("status");
		valueElement = new XMLElement("value");
		tagElement = new XMLElement("tag");
		unitElement = new XMLElement("unit");

		pachubeAPIKey = apiKey;
	} 

	public void update() { 
		remoteEnvironment = remoteXML.loadElementFrom(remoteURL,pachubeAPIKey);
	} 

	public float getValue(String tag) {
		int datastreamid = -1;
		XMLElement thisEnvironment = remoteEnvironment.getChild(0);
		int totalElements = thisEnvironment.countChildren();
		float datastreamValue = 0;

		boolean foundTag = false;
		int i = 0;
		int j = 0;

		while(!foundTag & (i < totalElements)){

			XMLElement thisChild = thisEnvironment.getChild(i);
			if (is(thisChild, dataElement)){
				int totalTags = thisChild.countChildren();
				j = 0;
				while (!foundTag & (j < totalTags)) {
					if (is(thisChild.getChild(j),tagElement)){
						if (thisChild.getChild(j).getChild(0).toString().trim().equals(tag.trim())){
							foundTag = true;
							datastreamid = Integer.parseInt(thisChild.getAttribute("id").trim());
							datastreamValue = getValue( datastreamid);

						}
					}
					j++;
				}
			} 
			i++;
		}
		return datastreamValue; 

	}

	public String getStringValue(String tag) {
		int datastreamid = -1;
		XMLElement thisEnvironment = remoteEnvironment.getChild(0);
		int totalElements = thisEnvironment.countChildren();
		String datastreamValue = "";

		boolean foundTag = false;
		int i = 0;
		int j = 0;

		while(!foundTag & (i < totalElements)){

			XMLElement thisChild = thisEnvironment.getChild(i);
			if (is(thisChild,dataElement)){
				int totalTags = thisChild.countChildren();
				j = 0;
				while (!foundTag & (j < totalTags)) {
					if (is(thisChild.getChild(j),tagElement)){
						if (thisChild.getChild(j).getChild(0).toString().trim().equals(tag.trim())){
							foundTag = true;
							datastreamid = Integer.parseInt(thisChild.getAttribute("id").trim());
							datastreamValue = getStringValue( datastreamid);

						}
					}
					j++;
				}
			} 
			i++;
		}
		return datastreamValue; 

	}


	public float getValue(int datastreamid) {

		XMLElement thisChild = dataElementById(datastreamid);
		float datastreamValue = 0;

		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i),valueElement)){
				datastreamValue = Float.valueOf(thisChild.getChild(i).getChild(0).toString().trim()).floatValue();

			}

		}


		return datastreamValue; 
	}

	public String getStringValue(int datastreamid) {

		XMLElement thisChild = dataElementById(datastreamid);
		String datastreamValue = "";

		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i),valueElement)){
				datastreamValue = thisChild.getChild(i).getChild(0).toString().trim();
			}

		}
		return datastreamValue; 
	}

	/*
	public int getAge() {

		int age=999999999;
		try{
			int totalDatastreams = remoteEnvironment.countChildren();
			if (totalDatastreams > 0) {
				age = Integer.parseInt(remoteEnvironment.getAttribute("age").trim());
			}
		} catch (Exception e){

			System.out.println("There was a problem getting the feed age; perhaps because this attribute only applies to feeds taken from pachube.com: " + e); 

		}

		return age; 

	}
	 */
	public void printXML() {
		remoteEnvironment.printElementTree(); 
	}

	public int countDatastreams() {

		int total = 0;
		int totalEnvironmentChildren = remoteEnvironment.getChild(0).countChildren();

		for (int i = 0; i < totalEnvironmentChildren; i++){

			if (is(remoteEnvironment.getChild(0).getChild(i),dataElement)) total++;

		}

		return total;

	}

	public String getTag(int id) {

		XMLElement thisChild = dataElementById(id);

		String returnString = "";

		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i), tagElement)){							
				returnString += thisChild.getChild(i).getChild(0).toString().trim()+",";
			}
		}

		return returnString;
	}


	public int[] getId(String tag){


		XMLElement thisEnvironment = remoteEnvironment.getChild(0);
		int totalElements = thisEnvironment.countChildren();

		boolean foundTag = false;

		int j = 0;

		int totalFound = 0;
		int[] tempArray = new int[100];

		for (int i=0; i < totalElements; i++){
			foundTag = false;
			XMLElement thisChild = thisEnvironment.getChild(i);
			if (is(thisChild,dataElement)){
				int totalTags = thisChild.countChildren();
				j = 0;
				while (!foundTag & (j < totalTags)) {
					if (is(thisChild.getChild(j),tagElement)){
						if (thisChild.getChild(j).getChild(0).toString().trim().equals(tag.trim())){
							foundTag = true;
							tempArray[totalFound]=Integer.parseInt(thisChild.getAttribute("id").trim());
							totalFound++;
						}
					}
					j++;
				}
			} 

		}

		int[] returnArray = new int[totalFound];

		java.lang.System.arraycopy(tempArray,0,returnArray,0,totalFound);

		return returnArray; 

	}

	public String getStatus(){
		String status = "no status";

		XMLElement thisEnvironment = remoteEnvironment.getChild(0);
		int totalEnvironmentChildren = thisEnvironment.countChildren();
		boolean foundStatusElement = false;
		int k = 0;

		while (!foundStatusElement & (k < totalEnvironmentChildren)) {		
			XMLElement thisChild = thisEnvironment.getChild(k);

			if (is(thisChild,statusElement)){				
				status = thisChild.getChild(0).toString();
			}
			k++;
		}



		return status;

	}


	private boolean is(XMLElement thisElement, XMLElement isElement ){

		boolean is = false;
		if (thisElement.getElement().equals(isElement.getElement())) is = true;
		return is;


	}


	public float getMaximum(int id){
		float returnValue = 0;

		XMLElement thisChild = dataElementById(id);
		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i), valueElement)){							
				returnValue = Float.valueOf(thisChild.getChild(i).getAttribute("maxValue").trim()).floatValue();
			}
		}

		return returnValue;
	}

	public float getMinimum(int id){
		float returnValue = 0;

		XMLElement thisChild = dataElementById(id);
		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i), valueElement)){							
				returnValue = Float.valueOf(thisChild.getChild(i).getAttribute("minValue").trim()).floatValue();
			}
		}

		return returnValue;
	}

	
	public String[] getUnits(int id){
		
		String[] returnString = { "none","none","none" };
		
		XMLElement thisChild = dataElementById(id);
		int totalDataChildren = thisChild.countChildren();

		for (int i = 0; i < totalDataChildren; i++){
			if (is(thisChild.getChild(i), unitElement)){							
				
				XMLElement unit = thisChild.getChild(i);
				
				if (!unit.getChild(0).equals("")){
					returnString[0]=unit.getChild(0).toString().trim();
				}
				if (!unit.getAttribute("symbol").equals("")){
					returnString[1]=unit.getAttribute("symbol").toString().trim();
				}
				if (!unit.getAttribute("type").equals("")){
					returnString[2]=unit.getAttribute("type").toString().trim();
				}
				//String unit_, String symbol_, String type_){
			}
		}	
		
		return returnString;
	}


	
	
	private XMLElement dataElementById(int id){

		XMLElement thisEnvironment = remoteEnvironment.getChild(0);

		int totalEnvironmentChildren = thisEnvironment.countChildren();

		int k = 0;

		XMLElement returnElement = dataElement;
		boolean foundDataElement = false;

		while (!foundDataElement & (k < totalEnvironmentChildren)) {

			XMLElement thisChild = thisEnvironment.getChild(k);


			if (is(thisChild,dataElement)){

				if (thisChild.getAttribute("id").equals(Integer.toString(id))){
					returnElement = thisChild;
					foundDataElement = true;
				}	
			}
			k++;
		}
		return returnElement;

	}






	/*	
	private boolean isDataElement(XMLElement thisElement){
		boolean is = false;
		if (thisElement.getElement().equals(dataElement.getElement())) is = true;
		return is;
	}

	private boolean isStatusElement(XMLElement thisElement){
		boolean is = false;
		if (thisElement.getElement().equals(statusElement.getElement())) is = true;
		return is;
	}

	private boolean isValueElement(XMLElement thisElement){

		boolean is = false;
		if (thisElement.getElement().equals(valueElement.getElement())) is = true;
		return is;
	}

	private boolean isTagElement(XMLElement thisElement){

		boolean is = false;
		if (thisElement.getElement().equals(tagElement.getElement())) is = true;
		return is;
	}

	 */
	/* needs debugging
	public String xmlString(){
		return(remoteEnvironment.xmlString());
	}
	 */
}
