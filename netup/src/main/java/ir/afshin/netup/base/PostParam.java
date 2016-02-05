package ir.afshin.netup.base;

import java.io.InputStream;
import java.io.Serializable;

/**
 * @author afshin
 * Represent the structure of a single parameter which will be pass to the server as post parameter.
 */
public class PostParam implements Serializable{

	/**
	 * Represents the types of post parameters.
	 */
	public static enum ParamType {String, File, Form}

	/**
	 * Parameter's type.
	 */
	public ParamType type = ParamType.String;
	/**
	 * Parameter's name
	 */
	public String name = "";
	/**
	 * Parameters mime type which is usable if the parameter's type is ParamType.File
	 */
	public String mimeType = "";
	/**
	 * If parameter's type is ParamType.File then this input stream will contains the stream of the
	 * file which should sent.
	 */
	public InputStream stream = null;
	/**
	 * If parameter's type is paramType.String, this variable stores the string value which should be sent.
	 */
	public String value = "";
	/**
	 * If parameter's type is ParamType.File then this field stores the file name to inform server about filename.
	 */
	public String fileName = "";
	
// ____________________________________________________________________
	public PostParam(ParamType type, String name, String fileName, String value, String mimeType, InputStream stream)
	{
		setParams(type, name, fileName, value, mimeType, stream);
	}
// ____________________________________________________________________

	/**
	 * Creates a string type post parameter.
	 * @param name
	 * @param value
	 * @param type
	 */
	public PostParam(String name, String value, ParamType type)
	{
		setParams(type, name, "", value, "plain-text", null);
	}
// ____________________________________________________________________

	/**
	 * Creates a file type post parameters.
	 * @param name
	 * @param fileName
	 * @param mimeType
	 * @param stream
	 */
	public PostParam(String name, String fileName, String mimeType, InputStream stream)
	{
		setParams(ParamType.File, name, fileName, "", mimeType, stream);
	}
// ____________________________________________________________________

	/**
	 * A simple setter.
	 */
	private void setParams(ParamType type, String name, String fileName, String value, String mimeType, InputStream stream)
	{
		this.type = type;
		this.name = name;
		this.value = value;
		this.mimeType = mimeType;
		this.stream = stream;
		this.fileName = fileName;
	}
// ____________________________________________________________________	

}
