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
import java.lang.reflect.*;



/**
 * This is the class to use for making data available to remote environments/applications.
 * It creates a 'server' object (based on Processing's network library) that sends 
 * data to remote clients that request your local EEML data.
 * <br><br>
 * It is asynchronous which means that once a DataOut object is constructed it invokes
 * an onReceiveRequest() method every time it receives a request for data. You therefore 
 * must have a method void onReceiveRequest(DataOut d) in your application, and you would at this
 * point normally update the values of your individual streamed data with the update() method.
 * The onReceiveRequest method then automatically creates an EEML document with all relevant
 * data and serves it to the requesting client. 
 * <br><br>
 * To register on the Pachube website you would need to know the IP address of your serving machine
 * and you would need to ensure that requests on your selected port are forwarded to the 
 * serving machine.
 * <br><br>
 * I.e. if you IP address is 100.100.100.100, and you have selected port 5250, then your 
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
	PApplet parent;

	/**
	 * When the object receives a request from a remote client
	 * it invokes the onReceiveRequest() method in your application
	 * where data streams can be updated prior to serving.
	 * 
	 * <pre>DataOut myDataOut = new DataOut(this, 2, 5210);</pre>
	 * @param parent usually 'this'
	 * @param numberOfStreams number of variables to be streamed 
	 * @param port port to serve on 
	 */
	public DataOut(PApplet parent, int port) {

		this.parent = parent;
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
		exmlThread = new Thread(this);
		exmlThread.start();   

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
	 * Set location data for this environment: latitude, longitude and elevation. (See EEML specs for more info).
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
				}

			}

		} catch (Exception e) {

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


}
