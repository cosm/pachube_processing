package eeml;

/**
 * This source code is based on proXML by Christian Riekoff (see http://www.texone.org/proxml/ for more info)
 * 
 ** An InvalidAttributeException occurs when a XMLElement does not have the requested 
 * attribute, or when you use getIntAttribute() or getFloatAttribute() for Attributes 
 * that are not numeric. Another reason could be that you try to add an attribute to a PCDATA 
 * section.
 * @nosuperclasses
 */

final class InvalidAttributeException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4192753777462605694L;

	public InvalidAttributeException(String attributeName){
		super("You can't add the attribute " + attributeName + " to a PCDATA section.");
	}

	public InvalidAttributeException(String elementName, String attributeName){
		super("The XMLElement " + elementName + " has no attribute " + attributeName + "!");
	}

	public InvalidAttributeException(String elementName, String attributeName, String type){
		super("The XMLElement " + elementName + " has no attribute " + attributeName + " of the type " + type + "!");
	}

}
