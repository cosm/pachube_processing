package eeml;

//v.1.02a (January 2009) Stefan Ferber
// Important: Not ready yet
//  * Date-Time-Stamp is not part of the XML Archive File
// Changes
// - Added printXML() methods
// - Added updateDateTimeStamp() methods
// - Added archive methods: setArchiveDirectory(), updateArchive(), setArchiveFilename//

//v.1.01 (November 2008)
//EEML (Extended Environments Markup Language) see http://www.eeml.org/ for more info

//The EEML library facilitates the sharing of EEML documents between remote environments via
//a network. It is used particularly in connecting to the main repository for feeds 
//located at http://www.pachube.com/
//The EEML library makes use of the existing processing net library
//XML parsing is based on proXML by Christian Riekoff (see http://www.texone.org/proxml/ for more info)
//thank you! 

import processing.core.PApplet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.lang.reflect.*;



/**
 * This is the class to use for making data available to remote environments/applications.
 * It creates a 'server' object (based on Processing's network library) that sends 
 * data to remote clients that request your local EEML data. (See below for instructions
 * on manual updates, in case a 'server' is not possible or desirable -- this is tailored 
 * specifically for use with Pachube).
 * <br><br>
 * <b>Automatic updates on request</b>
 * <br><br>
 * The normal way to use the DataOut object is asynchronous which means that once a DataOut 
 * object is constructed it invokes an onReceiveRequest() method every time it receives a request for 
 * data. You therefore must have a method void onReceiveRequest(DataOut d) in your application, and you 
 * would at this point normally update the values of your individual streamed data with the update() 
 * method. The onReceiveRequest method then automatically creates an EEML document with all relevant
 * data and serves it to the requesting client. 
 * <pre>
 * DataOut myDataOut = new DataOut(this, 5210);
 * myDataOut.addData(0,"tag1,tag2,tag3");
 * 
 * void onReceiveRequest(DataOut d){ 
 *    d.update(0, myVariable); // updates stream 0 with the value of myVariable.
 * }
 * </pre>
 * <b>Manual updates only when required</b>
 * <br><br>
 * If you are behind a firewall, don't have an open IP address, or want to update data only when necessary 
 * (e.g. only if a datastream state changes) then you can use the alternative form of the DataOut object, 
 * to update manually. In this case you would normally update the values of your individual streamed
 * data with the update() method then update the EEML data using the updatePachube() method, which 
 * returns 200 unless there was a problem (in which case it returns other status codes 
 * (http://www.w3.org/Protocols/HTTP/HTRESP.html) and/or throws an exception).
 * <pre>
 * DataOut myDataOut = new DataOut(this, "FEED_URL", "API_KEY");  
 * myDataOut.addData(0,"tag1,tag2,tag3");
 * 
 * myDataOut.update(0, myVariable); // updates stream 0 with the value of myVariable.
 * 
 * if (myDataOut.updatePachube() == 200){
 * 		// data was updated successfully.
 * }
 * </pre>
 * <b>Using Pachube</b>
 * <br><br>
 * To register on the Pachube website you would need to know the IP address of your serving machine
 * and you would need to ensure that requests on your selected port are forwarded to the 
 * serving machine (unless you are using the manual updates version of the DataOut object, see above).
 * <br><br>
 * I.e. if your IP address is 100.100.100.100, and you have selected port 5250, then your 
 * serving IP address as registered on Pachube would be http://100.100.100.100:5250/
 * <br><br>
 * If your machine is on a local network, with an IP address of, say 192.168.0.5, then you would 
 * first have to find out the IP address of your router (as seen by the external world; for
 * example 100.100.100.100), enable port forwarding on the router so that all traffic
 * that comes to port 5250 is forwarded to the machine at 192.168.0.5. Then register the 
 * URL as http://100.100.100.100:5250/ 
 * 
 * <pre>
 * DataOut myDataOut = new DataOut(this, 5210);
 * myDataOut.addData(0,"tag1,tag2,tag3");
 * 
 * void onReceiveRequest(DataOut d){ 
 * 
 *    d.update(0, myVariable); // updates stream 0 with the value of myVariable.
 * 
 * }
 * </pre>
 * @see void addData()
 * @see void update(int id, float value)
 */


public class DataOut extends Thread {

	private Method eventMethod;
	private Thread exmlThread;
	private int myport;
	private Out dataOut;
	private boolean running;
	private String updateURL;
	private String pachubeAPIKey;
	PApplet parent;

	// added by Fb 09-01-11
	private File archiveDirectory;
	private File archiveFile;
	private Calendar dateTimeStamp;
	
	/**
	 * When the object receives a request from a remote client
	 * it invokes the onReceiveRequest() method in your application
	 * where data streams can be updated prior to serving.
	 * 
	 * <pre>DataOut myDataOut = new DataOut(this, 5210);</pre>
	 * @param parent usually 'this'
	 * @param port port to serve on 
	 */
	public DataOut(PApplet parent, int port){
		this(parent);
		this.myport = port;		
		
		System.out.println("New DataOut created, serving on port " + myport);

		try {
			eventMethod = parent.getClass().getMethod("onReceiveRequest", new Class[] { 
					DataOut.class             }
			);
			dataOut = new Out(parent, myport);
		} 
		catch (Exception e) {
			System.out.println("The DataOut class requires an onReceiveRequest() method to serve requested XML...");
			System.out.println(e);
			quit();
		}

		running = true;
		try {
		exmlThread = new Thread(this);
		exmlThread.start();   
		}
		catch (Exception e) {
			System.out.println("There was a problem starting a thread.");
		}
	}

	/**
	 * DataOut object that enables manual updating of EEML data
	 * 
	 * <pre>DataOut myDataOut = new DataOut(this, "URL_TO_UPDATE_TO", "YOUR_API_KEY");</pre>
	 * @param parent usually 'this'
	 * @param updateURL URL that was provided when the feed was registered
	 * @param key the Pachube API key used to access Pachube feeds (requires registration at pachube.com)
	 * @see DataOut(PApplet parent, int port)
	 */
	public DataOut(PApplet parent, String updateURL, String key) {
		this(parent);
		this.updateURL = updateURL;
		this.pachubeAPIKey = key;

		
		System.out.println("New DataOut created, manual update enabled.");

		try {
			dataOut = new Out(parent);
		} 
		catch (Exception e) {
			System.out.println("There was a problem creating the manual-update DataOut object.");
			System.out.println(e);
			quit();
		}
		running = false;
	}

	protected DataOut(PApplet parent) {
		this.parent = parent;
		archiveDirectory = new File(".");
		archiveFile = new File(archiveDirectory, "DataOut.xml");
		dateTimeStamp = Calendar.getInstance();
	}

	/**
	 * Set up a data stream with a particular id with comma-delimited tags.
	 * <pre>myDataOut.addData(0,"tag1,tag2,tag3");</pre>
	 * @param id
	 * @param tags
	 */
	public void addData(int id, String tags){       
		dataOut.addData(id, tags);
	}

	/**
	 * Set up a data stream with a particular id with comma-delimited tags and minimum and maximum values.
	 * <pre>myDataOut.addData(0,"tag1,tag2,tag3",23.0,100.0);</pre>
	 * @param id
	 * @param tags
	 */
	public void addData(int id, String tags, float min, float max){       
		dataOut.addData(id, tags, min, max);
	}

	/**
	 * Updates a particular data stream with a float value.
	 * <pre>d.update(3, 4.2);</pre>
	 * @param id
	 * @param value
	 */
	public void update(int id, float value){
		dataOut.update(id,value);
	}

	/**
	 * Updates a particular data stream with a String value.
	 * <pre>d.update(0, "myName");</pre>
	 * @param id
	 * @param value
	 */
	public void update(int id, String value){
		dataOut.update(id,value);
	}

	/**
	 * Used to PUT data to Pachube in order to update a feed (for example
	 * when your IP address is not externally accessible and/or you want to 
	 * update your datastreams manually). To see typical serve response codes
	 * see here: http://www.w3.org/Protocols/HTTP/HTRESP.html (200 is when
	 * everything goes right).
	 * <pre>if (myDataOut.updatePachube() == 200){
	 *     - server response indicates that the feed was successfully updated
	 * }</pre>
	 * @see DataOut(PApplet parent, String updateURL, String key)
	 * @return
	 */
	public int updatePachube(){
		return dataOut.updatePachube(updateURL, pachubeAPIKey);
	}

	/**
	 * Sets location data for this environment, including exposure ("outdoor"/"indoor"), domain ("physical"/"virtual"),
	 * disposition ("fixed"/"mobile") and latitude, longitude and elevation. (See EEML specs for more info).
	 * <pre>d.setLocation("indoor", "physical", "fixed", 51.566742, -0.099373, 22.4);</pre>
	 * @param exposure
	 * @param domain
	 * @param disposition
	 * @param lat
	 * @param lon
	 * @param ele
	 */
	public void setLocation(String exposure, String domain, String disposition, float lat, float lon, float ele){
		dataOut.setLocation(exposure,domain,disposition,lat,lon,ele);
	}

	/**
	 * Sets location data for this environment:  ONLY exposure ("outdoor"/"indoor"), domain ("physical"/"virtual"),
	 * disposition ("fixed"/"mobile") (See EEML specs for more info).
	 * <pre>d.setLocation("indoor", "physical", "fixed");</pre>
	 * @param exposure
	 * @param domain
	 * @param disposition
	 */
	public void setLocation(String exposure, String domain, String disposition){
		dataOut.setLocation(exposure,domain,disposition);
	}

	/**
	 * Set location data for this environment: ONLY latitude, longitude and elevation. (See EEML specs for more info).
	 * @param lat
	 * @param lon
	 * @param ele
	 */
	public void setLocation(float lat, float lon, float ele){
		dataOut.setLocation(lat,lon,ele);
	}

	/**
	 * Sets the "minimum" attribute for a particular data stream's value
	 * @param id
	 * @param minimum
	 */
	public void setMinimum(int id, float minimum){
		dataOut.setMinimum(id,minimum);
	}

	/**
	 * Sets the "maximum" attribute for a particular data stream's value
	 * @param id
	 * @param maximum
	 */
	public void setMaximum(int id, float maximum){
		dataOut.setMaximum(id,maximum);
	}

	/**
	 * Sets the unit element for a particular data stream's value.
	 * <pre>d.setUnits(0, "Celsius","C","basicSI");</pre>
	 * @param id
	 * @param unit_
	 * @param symbol_
	 * @param type_
	 */
	public void setUnits(int id, String unit_, String symbol_, String type_){

		dataOut.setUnits(id,unit_, symbol_,type_);
	}


	/**
	 * Returns the last message received from the requesting client.
	 * Useful for parsing for the IP address of the remote client.
	 * @return
	 */
	public String serverMessage(){       
		return(dataOut.serverMessage());
	}

	/**
	 * Ignore.
	 */
	public void run() {

		try {

			while (running) {              

				if ((eventMethod != null) & (dataOut.hasClient())) {
					try {
						eventMethod.invoke(parent, new Object[] { this } );
						dataOut.serve();	                        
					} 
					catch (Exception e) {
						System.err.println("Problem running DataOut...");
						e.printStackTrace();
						eventMethod = null;
					}
				}

				try {
					sleep(8);
				}
				catch (Exception e) {
					System.err.println("DataOut: There was a problem sleeping.");
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			System.err.println("DataOut: There was a problem running.");
			e.printStackTrace();
		}
	}

	/**
	 * Ignore.
	 */
	public void quit()
	{
		running = false;  
		exmlThread = null;
		interrupt(); 
	}

	/**
	 * Prints the XML information in this object to the console
	 * 
	 * <pre>myDataOut.printXML();</pre>
	 */
	public void printXML() {
		dataOut.printXML(); 
	}
	/**
	 * Prints the XML information in this object to the console. 
	 * Very nice for test purpose.
	 * <pre>myDataOut.printXML();</pre>
	 * @param output Output stream
	 */
	protected void printXML(PrintWriter output) {
		dataOut.printXML(output);
	}
	public void updateDateTimeStamp(int year, int month, int date, int hourOfDay, int minute, int second){
		dateTimeStamp.set(year, month, date, hourOfDay, minute, second);
		
	}
	
	protected boolean setArchiveFilename(String filename){
		archiveFile = new File(archiveDirectory, filename);
		return !archiveFile.exists();
	}

	public boolean setArchiveDirectory(String pathName){
		archiveDirectory = new File(pathName);
		return (archiveDirectory.canWrite() && archiveDirectory.isDirectory());
	}
	
	public boolean updateArchive() throws IOException{
		if (setArchiveFilename("DataOut" + Long.toString(dateTimeStamp.getTimeInMillis())+".xml")){
			if (archiveFile.createNewFile()){
				PrintWriter pw = new PrintWriter(new FileOutputStream(archiveFile));
				printXML(pw);
				pw.close();
				return pw.checkError();
			}
			else return false;
		}
		else return false;
	}
}