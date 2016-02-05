package ir.afshin.netup.FileSystem;

import android.util.Log; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 

/** 
 * SOURCE : http://stackoverflow.com/questions/7697466/unzip-a-zipped-file-on-sd-card-in-android-application
 * @author jon 
 */ 
public class Decompress { 
  private String _zipFile; 
  private String _location;

  /**
   *
   * @param zipFile zip filename.
   * @param location The path name to extract zip file in.
   */
  public Decompress(String zipFile, String location) { 
    _zipFile = zipFile;


    if(!location.endsWith("/"))
      location += "/";
    _location = location;

    _dirChecker(""); 
  } 

  public void unzip() { 
	  FileInputStream fin = null;
	  ZipInputStream zin = null;
    try  { 
      fin = new FileInputStream(_zipFile); 
      zin = new ZipInputStream(fin); 
      ZipEntry ze = null; 
      byte[] buffer = new byte[3*1024];
      int len = 0;
      
      while ((ze = zin.getNextEntry()) != null) {
        Log.v("Decompress", "Unzipping " + ze.getName());

        String zeName = ze.getName().replace("\\", "/");

        if(ze.isDirectory()) 
        {
        	  _dirChecker(zeName);
        } 
        else 
        { 
	          FileOutputStream fout = new FileOutputStream(_location + zeName);
	          
	          while((len = zin.read(buffer)) > 0)
	          {
	        	  fout.write(buffer, 0, len);
	        	  fout.flush();
	          }
	
	          zin.closeEntry(); 
	          fout.close(); 
        } 

      } 
      zin.close(); 
      fin.close();
    } catch(Exception e) {
      
      try{
    	  if(fin != null)
    		  fin.close();
    	  if(zin != null)
    		  zin.close();
      }
      catch(Exception ex)
      {
        ex.printStackTrace();
      }
      
    } 

  } 

  private void _dirChecker(String dir) { 
    File f = new File(_location + dir); 

    if(!f.isDirectory()) { 
      f.mkdirs(); 
    } 
  } 
} 