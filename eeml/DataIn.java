package eeml;
import processing.core.PApplet;
import java.lang.reflect.*;

// v.0.9 (June 2008)
//EEML (Extended Environments Markup Language) see http://www.eeml.org/ for more info

//The EEML library facilitates the sharing of EEML documents between remote environments via
//a network. It is used particularly in connecting to the main repository for feeds 
//located at http://www.pachube.com/
//The EEML library makes use of the existing processing net library
//XML parsing is based on proXML by Christian Riekoff (see http://www.texone.org/proxml/ for more info)
//thank you! 

/**
 * This is the class to use for receiving data from remote environments/applications.
 * It is asynchronous which means that once a DataIn object is constructed it invokes
 * an onReceiveEEML() method every time it receives data. You therefore must have a method
 * void onReceiveEEML(DataIn d) in your application, and you would normally use
 * the getValue() method to extract relevant data.  
 * <pre>
 * DataIn myDataIn = new DataIn(this,"http://remote_url/001.xml",5000);
 * 
 * void onReceiveEEML(DataIn d){ 
 * 
 *    float myVariable = d.getValue(0); 
 *    float myVariable2 = d.getValue("tagName");
 * 
 * }
 * </pre>
 */

public class DataIn extends Thread {

	PApplet parent;
	private Method eventMethod;
	private Thread exmlThread;
	private String remoteURL;
	private int p;
	private In dataIn;
	private boolean running;

	private String pachubeAPIKey;

	/**
	 * This object is asynchronous which means that it is set up to make requests at a particular interval 
	 * (say once every five seconds, meaning period would be '5000'). When the object receives 
	 * a response from the remote server, it invokes the onReceiveEEML() method in your application
	 * where you can read the data for particular variables by using the getValue() method.
	 * 
	 * <pre>DataIn d = new DataIn(this,"http://remoteurl/001.xml",5000);</pre>
	 * @param parent usually 'this'
	 * @param url the URL to connect to
	 * @param period the number of milliseconds between requests to the remote feed.
	 * @see getValue(String tag)
	 * @see getValue(int id)
	 * @see DataIn(PApplet parent_, String url, String key, int period)
	 */
	public DataIn(PApplet parent_, String url, int period) {

		parent = parent_;
		remoteURL = url;
		p = period;
		pachubeAPIKey = "";
		
		System.out.println("New DataIn created attempting to access " + remoteURL + " every " + p + " milliseconds.");

		try {
			eventMethod = parent.getClass().getMethod("onReceiveEEML", new Class[] { 
					DataIn.class             }
			);
		} 
		catch (Exception e) {
			System.out.println("The DataIn class requires an onReceiveEEML() method to implement returned XML.");
			System.out.println(e);
			quit();
		}

		running = true;
		exmlThread = new Thread(this);
		exmlThread.start();   

	}

	/**
	 * Just like DataIn(PApplet parent_, String url, int period) but enables the setting of a header 
	 * to authenticate access to Pachube
	 *  
	 * <pre>DataIn d = new DataIn(this,"http://pachube.com/api/001.xml", "YourKeyGoesHere", 5000);</pre>
	 * @param parent usually 'this'
	 * @param url the URL to connect to
	 * @param key the Pachube API key used to access Pachube feeds (requires registration at pachube.com)
	 * @param period the number of milliseconds between requests to the remote feed.
	 * @see getValue(String tag)
	 * @see getValue(int id)
	 */
	public DataIn(PApplet parent_, String url, String key, int period) {

		parent = parent_;
		remoteURL = url;
		p = period;
		pachubeAPIKey = key;

		System.out.println("New DataIn created attempting to access " + remoteURL + " every " + p + " milliseconds.");

		try {
			eventMethod = parent.getClass().getMethod("onReceiveEEML", new Class[] { 
					DataIn.class             }
			);
		} 
		catch (Exception e) {
			System.out.println("The DataIn class requires an onReceiveEEML() method to implement returned XML.");
			System.out.println(e);
			quit();
		}

		running = true;
		exmlThread = new Thread(this);
		exmlThread.start();   

	}

	
	/**
	 * Returns the value of the remote data feed identified through its id number.
	 * <pre>d.getValue(2);</pre>
	 * @param id 
	 * @return value  
	 */
	public float getValue(int id) {       
		float value = dataIn.getValue(id);
		return value;
	}

	/**
	 * Returns the value of a remote data feed identified through its tag -- if there are several streams
	 * with the same tag, then only the first identified is returned. (Use "getId" to retrieve an array
	 * containing the IDs of all the streams that contain a particular tag).
	 * <pre>d.getValue("usefulTag");</pre>
	 * @param tag
	 * @return
	 * @see getId(String tag)
	 */
	public float getValue(String tag) {       
		float value = dataIn.getValue(tag);
		return value;
	}

	/**
	 * Returns the string value of the remote data feed identified through its id number (useful 
	 * when a stream's value is a string).
	 * <pre>String avatarName = d.getStringValue(4);</pre>
	 * @param id
	 * @return
	 * @see getValue(int id)
	 * @see getStringValue(String tag)
	 */
	public String getStringValue(int id) {       
		String value = dataIn.getStringValue(id);
		return value;
	}

	/**
	 * Returns the string value of the remote data feed identified through its tag (useful 
	 * when a stream's value is a string).
	 * <pre>String avatarName = d.getStringValue("avatarName");</pre>
	 * @param tag
	 * @return
	 * @see getValue(int id)
	 * @see getStringValue(String tag)
	 */
	public String getStringValue(String tag) {       
		String value = dataIn.getStringValue(tag);
		return value;
	}


	/**
	 * Returns the tags for a specific data stream id.
	 * @return
	 */
	public String getTag(int id){
		return dataIn.getTag(id);
	}

	/**
	 * Returns an array of all the data stream ID's that are tagged with String tag.
	 * @param tag
	 * @return
	 */
	public int[] getId(String tag){
		
		return dataIn.getId(tag);
		
	}
	
	/**
	 * Returns the number of data elements in the remote EEML.
	 * @return
	 */
	public int countDatastreams(){
		return dataIn.countDatastreams();   	
	}

/*
	/**
	 * Returns the "age" of the remote data. For example, Pachube may return data
	 * almost immediately, but the data it returns may have been cached some time before.
	 * So this method makes it possible to check how "old" the data is based on the "age"
	 * attribute of the EEML returned.
	 * <pre>if (d.getAge() > 10){
	 *    doSomething(); // feed is older than 10 seconds therfore we won't consider it live
	 * }
	 * </pre>
	 * @return
	 */
	/*
	public int getAge() {       
		int value = dataIn.getAge();
		return value;
	}
*/
	/**
	 * Useful for debugging; returns the entire EEML document received.
	 */
	public void printXML() {       
		dataIn.printXML();
	}

	/**
	 * Returns the URL requested by this object (useful in the onReceiveEEML method to determine
	 * which particular DataIn object's request has been received).
	 * <pre>if (thisD.getURL().equals(thatD.getURL()){
	 *    doSomething(); // we know which feed's EEML we have just received
	 * }
	 * </pre>@return
	 */
	public String getURL() {       
		return remoteURL;
	}

	/**
	 * Ignore -- this is where the threads are created.
	 */
	public void run() {
		try {

			dataIn = new In(remoteURL, pachubeAPIKey);
			//System.out.println("Starting to run....");
			while (running){              

				//System.out.println("Starting update");
				dataIn.update();
				//System.out.println("Finished update");


				if (eventMethod != null) {
					try {
						//System.out.println("Starting invoke method");
						eventMethod.invoke(parent, new Object[] { this } );
						//System.out.println("Finished invoke method");
					} 
					catch (Exception e) {
						System.err.println("Problem running DataIn...");
						e.printStackTrace();
						eventMethod = null;
					}
				}

				//System.out.println("about to sleep.......");

				try {
					sleep((p));
				}
				catch (Exception e) {
				}

				//System.out.println("finished sleeping......");

			}

		} catch (Exception e) {

		}
	}

	/**
	 * Used to determine the status of an EEML feed ("live", "frozen" or "no status")/
	 * @return
	 */
	public String getStatus(){
		return dataIn.getStatus();
	}

	/**
	 * Used to determine the maximum value of a numeric data stream.
	 * @param
	 * @return
	 */
	public float getMaximum(int id){
		return dataIn.getMaximum(id);
	}

	/**
	 * Used to determine the minimum value of a numeric data stream.
	 * @param
	 * @return
	 */
	public float getMinimum(int id){
		return dataIn.getMinimum(id);
	}

	/**
	 * Used to determine the units of a data stream. Returns an array, e.g.
	 * <pre>
	 * { "Celsius","C","basicSI" }
	 * </pre>
	 * @param
	 * @return
	 */
	public String[] getUnits(int id){
		return dataIn.getUnits(id);
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

	
	/*  needs proper debugging
    public String xmlString(){

    	return dataIn.xmlString();

    }
	 */


}
