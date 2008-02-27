package eeml;

//v.0.5 (January 2008)
//EEML (Extended Environment Markup Language) see http://www.eeml.org/ for more info

//The EEML library is a development of an earlier project library, environmentXML 
//located at http://www.haque.co.uk/environmentxml/ for archive purposes
//it is used in connecting to the main repository for feeds located at http://www.pachube.com/
//both the earlier library and this one make use of the existing processing net library
//XML parsing is based on proXML by Christian Riekoff (see http://www.texone.org/proxml/ for more info)
//thank you! 


import processing.core.PApplet;

/**
 * This is one of the basic classes but should not normally be used, since it's not threaded.
 * Instead see the DataOut class. 
 * @see DataOut
 */


public class Out {

	PApplet parent;
	private XMLElement localEnvironment;	
	private Server myServer;
	private String incomingMsg;

	private String xmlHeader = "HTTP/1.1 200 OK\nContent-type: application/xml\n\n<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private String eemlHeader = "<eeml xmlns=\"http://www.eeml.org/xsd/005\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.eeml.org/xsd/005 http://www.eeml.org/xsd/005/005.xsd\" version=\"5\">\n";
	private String eemlFooter = "\n</eeml>";

	private String locationData = "";
	
	private XMLElement valueChild;
	private XMLElement unitChild;

	//private XMLElement dataElement;
	private XMLElement valueElement;
	//private XMLElement tagElement;
	
	public Out (PApplet parent, int myport) { 

		this.parent = parent;
		myServer = new Server(parent, myport);
		localEnvironment = new XMLElement("environment");
		incomingMsg = null;		
		
		//dataElement = new XMLElement("data");
		valueElement = new XMLElement("value");
		//tagElement = new XMLElement("tag");
	} 


	public void addData(int id, String tags){		
		
		XMLElement thisDatastream = new XMLElement("data");   
		thisDatastream.addAttribute("id",Integer.toString(id));
		String thisDatastreamTags[] =  tags.split(","); 
		int thisDatastreamTagCount = thisDatastreamTags.length;

		for (int i = 0; i < thisDatastreamTagCount ; i++)

		{
			XMLElement tag = new XMLElement("tag");
			XMLElement tagChild = new XMLElement(thisDatastreamTags[i], true);
			tag.addChild(tagChild);
			thisDatastream.addChild(tag);
		}

		valueChild = new XMLElement("value");		
		XMLElement valueChildChild = new XMLElement("",true);
		valueChild.addChild(valueChildChild);
		thisDatastream.addChild(valueChild);				

		unitChild = new XMLElement("unit");		
		XMLElement unitChildChild = new XMLElement(" ",true);
		unitChild.addChild(unitChildChild);
		thisDatastream.addChild(unitChild);				

		localEnvironment.addChild(thisDatastream);
		
	}

	public void addData(int id, String tags, float minimum, float maximum){		
		
	this.addData(id,tags);
	this.setMinimum(id,minimum);
	this.setMaximum(id,maximum);
		
	}

	public void setLocation(String exposure, String domain, String disposition, float lat, float lon, float ele){
		locationData = "  <location exposure=\""+ exposure + "\" domain=\""+domain+"\" disposition=\""+disposition+"\">\n    <lat>"+lat+"</lat>\n    <lon>"+lon+"</lon>\n    <ele>"+ele+"</ele>\n  </location>\n";
	}

	public void setLocation(float lat, float lon, float ele){
		locationData = "  <location>\n    <lat>"+lat+"</lat>\n    <lon>"+lon+"</lon>\n    <ele>"+ele+"</ele>\n  </location>\n";

	}

	public void setMinimum(int id, float minimum){
		localEnvironment.getChild(id).getChild(localEnvironment.getChild(id).countChildren()-2).addAttribute("minValue",minimum);
	}

	public void setMaximum(int id, float maximum){
		localEnvironment.getChild(id).getChild(localEnvironment.getChild(id).countChildren()-2).addAttribute("maxValue",maximum);
	}


	public void setUnits(int id, String unit_, String symbol_, String type_){
		XMLElement units = new XMLElement("unit");
		units.addAttribute("symbol", symbol_);
		units.addAttribute("type",type_);
		units.addChild(new XMLElement(unit_, true));
		localEnvironment.getChild(id).removeChild(localEnvironment.getChild(id).countChildren()-1);
		localEnvironment.getChild(id).addChild(units);
	}


	public void update(int id, float value){
		XMLElement val = localEnvironment.getChild(id).getChild(localEnvironment.getChild(id).countChildren()-2);
		if (isValueElement(val)){
		val.removeChild(0);
		val.addChild(new XMLElement(Float.toString(value), true),0);
		}
	}

	public void update(int id, String value){
		XMLElement val = localEnvironment.getChild(id).getChild(localEnvironment.getChild(id).countChildren()-2);
		if (isValueElement(val)){
		val.removeChild(0);
		val.addChild(new XMLElement(value, true),0);
		}
	}

	public void printXML() {
		localEnvironment.printElementTree(); 
	}

	public String serve() {

		Client thisClient = myServer.available();

		if (thisClient != null ) {

			incomingMsg = null;

			int totalDatastreams = localEnvironment.countChildren();
			String servedXML = xmlHeader + eemlHeader + localEnvironment + "\n" + locationData;
			
			for (int i = 0; i < totalDatastreams; i++) {

				XMLElement thisStream = localEnvironment.getChild(i);

				servedXML += "  " + thisStream + "\n";


				int totalTags = thisStream.countChildren() - 2;

				for (int j = 0; j < totalTags; j++) {

					servedXML += "    <tag>" + thisStream.getChild(j).getChild(0).toString().trim() + "</tag>\n";
				}


				servedXML += "    " + thisStream.getChild(totalTags);
				servedXML += thisStream.getChild(totalTags).getChild(0);
				servedXML += "</value>\n";


				servedXML += "    " + thisStream.getChild(totalTags+1);
				servedXML += thisStream.getChild(totalTags+1).getChild(0);
				servedXML += "</unit>\n";
				servedXML += "  " + "</data>" + "\n";           	            
			}

			servedXML += "</environment>"+eemlFooter;

			myServer.write(servedXML);

			//next few lines should contain the IP address of the device sniffing your XML

			incomingMsg = thisClient.readString(); 

			//System.out.println("actualoutput: " + servedXML);    

			myServer.disconnect(thisClient);

		}

		return incomingMsg;

	}

	public String serverMessage(){
		return incomingMsg;
	}

	boolean hasClient(){

		boolean hasClient_ = false;	
		Client thisClient = myServer.available();
		if (thisClient != null ) { hasClient_ = true; }	
		return hasClient_;
	}

	/*
	private boolean isDataElement(XMLElement thisElement){
		boolean is = false;
		if (thisElement.getElement().equals(dataElement.getElement())) is = true;
		return is;
	}
*/
	private boolean isValueElement(XMLElement thisElement){

		boolean is = false;
		if (thisElement.getElement().equals(valueElement.getElement())) is = true;
		return is;
	}
/*
	private boolean isTagElement(XMLElement thisElement){

		boolean is = false;
		if (thisElement.getElement().equals(tagElement.getElement())) is = true;
		return is;
	}
*/
}