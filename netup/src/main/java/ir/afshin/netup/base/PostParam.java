package ir.afshin.netup.base;

import java.io.File;
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
	public enum ParamType {String, File}

	/**
	 * Parameter's type.
	 */
	public ParamType type = ParamType.String;
	/**
	 * Parameter's name
	 */
	String name = "";
	/**
	 * Parameters mime type which is usable if the parameter's type is ParamType.File
	 */
	String mimeType = "";
	/**
	 * If parameter's type is ParamType.File then this input stream will contains the stream of the
	 * file which should be sent.
	 */
	File fileToUpload = null;
	/**
	 * If parameter's type is paramType.String, this variable stores the string value which should be sent.
	 */
	private String value = "";
	/**
	 * If parameter's type is ParamType.File then this field stores the file name to inform server about filename.
	 */
	String fileName = "";


	/**
	 * An instance of {@link ContentEncoder} encodes the value the way programmer wants.
	 */
	private ContentEncoder customValueEncoder = null;
	
// ____________________________________________________________________
	public PostParam(ParamType type, String name, String fileName, String value, String mimeType, File fileToUpload)
	{
		setParams(type, name, fileName, value, mimeType, fileToUpload);
	}
// ____________________________________________________________________

	/**
	 * Creates a string type post parameter.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 */
	public PostParam(String name, String value)
	{
		setParams(ParamType.String, name, "", value, "plain-text", null);
	}
// ____________________________________________________________________

	public PostParam(String name, String value, ContentEncoder customValueEncoder)
	{
		this.customValueEncoder = customValueEncoder;
		setParams(ParamType.String, name, "", value, "plain-text", null);
	}
// ____________________________________________________________________

	/**
	 * Creates a file type post parameters.
	 * @param name The name of the parameter.
	 * @param fileName The filename. It must not include the path.
	 * @param mimeType The mime type of the file which is going to be sent.
	 * @param fileToUpload The file object which must be uploaded.
	 */
	public PostParam(String name, String fileName, String mimeType, File fileToUpload)
	{
		setParams(ParamType.File, name, fileName, "", mimeType, fileToUpload);
	}
// ____________________________________________________________________

	/**
	 * A simple setter.
	 */
	private void setParams(ParamType type, String name, String fileName, String value, String mimeType, File fileToUpload)
	{
		this.type = type;
		this.name = name;
		this.value = value;
		this.mimeType = mimeType;
		this.fileToUpload = fileToUpload;
		this.fileName = fileName;
	}

// ____________________________________________________________________

	String getValue(String url){

		Pair pair = new Pair(name, value, customValueEncoder);
		return pair.getSecond(url);
	}

// ____________________________________________________________________

	String getValueWithoutEncoding() {

		return value;
	}
// ____________________________________________________________________	

}
