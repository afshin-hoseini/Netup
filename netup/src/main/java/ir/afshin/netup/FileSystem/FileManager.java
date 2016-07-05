package ir.afshin.netup.FileSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileManager {


// ____________________________________________________________________
	public static String readFileAsString(String fileName) throws Exception
	{
		FileInputStream fis = new FileInputStream(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String content = "";
		String line;

		while ((line = br.readLine()) != null )
			content += line;

		if(br != null)
			br.close();
		if(fis != null)
			fis.close();

		return content;
	}
	
// ____________________________________________________________________	
	/**
	 * Gets project base folder.
	 * @param ctx An instance of {@link Context}
	 * @return A path, ends with <b>/</b>
	 * @author afshin
	 */
	public static String getProjectFolder(Context ctx)
	{
		return Environment.getDataDirectory().getAbsolutePath() + "/data/" + ctx.getPackageName() + "/appData/";
	}
// ____________________________________________________________________
	public static void copyFromAssets(Context ctx, String destFolder, String assetsFolder, String assetsFilename) throws Exception
	{
		byte[] buffer = new byte[1024];
		int len = 0;
		InputStream input =null;
		
		if(isMountedMedia(ctx))
		{
		
			makeDir(destFolder);
			OutputStream output = new FileOutputStream(new File(destFolder + assetsFilename));
	

			try{
				input = ctx.getAssets().open(assetsFolder + assetsFilename);
			}catch(Exception e)	{}

			while((len = input.read(buffer)) > 0)
			{
				output.write(buffer);
				output.flush();
			}
				

			
			
			output.close();
			input.close();
		}
	}
	
// ____________________________________________________________________
	public static void unzip(String zipFile, String destFolder, boolean deleteAfterUnzip)
	{
		Decompress decompress = new Decompress(zipFile, destFolder);
		decompress.unzip();
		
		if(deleteAfterUnzip)
		{
			new File(zipFile).delete();
		}
	}
// ____________________________________________________________________
	/**
	 * Checks if external storage like SD card is mounted or not.
	 * @param ctx An instance of {@link Context}
	 * @return <i>true</i> if external storage is mounted, <i>false</i> otherwise.
	 */
	public static synchronized boolean isMountedMedia(Context ctx) {
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) 
			{
				return true;
			} 
		} catch (Exception e) {}

		return false;
	}
// ____________________________________________________________________
	/**
	 * Makes given directory and also ensures that a directory is exists or not.
	 * @param dir The directory path you wanna make.
	 * @return <i>true</i> if made successfully, <i>false</i> otherwise.
	 */
	public static boolean makeDir(String dir)
	{
		boolean success = false;

		File path = new File(dir);

		return path.mkdirs() || path.isDirectory();
	}
// ____________________________________________________________________
	/**
	 * Checks if a file is exists or not
	 * @param filename The file path you wanna check is exists or not.
	 * @return <i>true</i> if exists, <i>false</i> otherwise.
	 */
	public static boolean isExist(String filename)
	{
		return new File(filename).exists();
	}

// ____________________________________________________________________

	/**
	 *
	 * @param strPath Must enclosed with "/"
	 * @return <i>true</i> if the given path is deleted successfully, <i>false</i> otherwise.
	 */
	public static boolean delete(String strPath)
	{
		File path = new File(strPath);

		if(path.isDirectory())
		{
			if(!strPath.endsWith("/"))
				strPath += "/";

			String[] subFiles = path.list();
			for(String file : subFiles)
			{
				delete(strPath + file);
			}
		}

		path.delete();

		return true;

	}
// ____________________________________________________________________
	public static boolean rename(String filename, String newFilename)
	{
		boolean success = true;
		
		File file = new File(filename);
		if(file.exists())
		{
			File newFile = new File(newFilename);
			success = file.renameTo(newFile);
		}
		else
		{
			success = false;
		}
		
		return success;
	}
// ____________________________________________________________________
	
	public static boolean copy(String srcFilename, String destFilename, boolean makeDirs)
	{
		boolean success = true;
		
		if( ! isExist(srcFilename))
			return false;

		if(makeDirs) {
			String destPath = destFilename.substring(0, destFilename.lastIndexOf("/"));
			makeDir(destPath);
		}
		
		try{
			FileInputStream src_inStream = new FileInputStream(srcFilename);
			FileOutputStream dest_outStream = new FileOutputStream(destFilename);
			
			byte[] buffer = new byte[1024];
			int len = -1;
			
			while((len = src_inStream.read(buffer)) != -1)
				dest_outStream.write(buffer, 0, len);
			
			dest_outStream.flush();
			
			src_inStream.close();
			dest_outStream.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
			success = false;
		}
		
		
		return success;
	}

// ____________________________________________________________________

	public static boolean copyAll(String srcPath, String destPath)
	{
		boolean success = false;

		if( ! makeDir(destPath))
			return false;

		File srcDir = new File(srcPath);
		File[] srcFiles = srcDir.listFiles();

		for (File file : srcFiles)
			copy(file.getPath()+file.getName(), destPath + file.getName(), true);

		success = true;

		return success;
	}

// ____________________________________________________________________

	public static boolean copyRecursively(String srcPath, String dstPath) {


		boolean success = true;


		File file_srcFile = new File(srcPath);

		if(file_srcFile.isFile()){

			success = copy(srcPath, dstPath, false);
		}
		else if(file_srcFile.isDirectory()) {

			File[] fileList = file_srcFile.listFiles();
			for(File file : fileList) {

				if(file.isDirectory()) {

					File file_dstPath = new File(appendPathComponent(dstPath, file.getName()));
					file_dstPath.mkdirs();
					success = copyRecursively(file.getPath(), appendPathComponent(dstPath, file.getName()));
				}
				else {

					String src = file.getPath();
					String dst = appendPathComponent(dstPath, file.getName());
					success = copyRecursively(src, dst);
				}

				if(!success)
					break;
			}
		}

		return success;
	}
// ____________________________________________________________________

	public static String appendPathComponent(String firstPath, String secondPart) {

		if(firstPath.endsWith("/"))
			firstPath = firstPath.substring(0, firstPath.length()-1);
		if(secondPart.startsWith("/"))
			secondPart = secondPart.substring(1);

		return firstPath + "/" + secondPart;
	}

// ____________________________________________________________________
	public static boolean renameAll(String srcPath, String destPath)
	{
		boolean success = true;
		File destFile = new File(destPath);


		makeDir(destPath);

		if(destFile.exists() && destFile.isDirectory())
		{
			File srcDir = new File(srcPath);
			File[] srcFiles = srcDir.listFiles();

			if(!destPath.endsWith("/"))
				destPath = destPath + "/";

			if(srcFiles != null)
			{
				for (File file : srcFiles)
					rename(file.getPath(), destPath + file.getName());
			}
		}
		else
		{
			rename(srcPath, destPath);
		}



		return success;
	}
// ____________________________________________________________________

	public static long getAvailableSpace()
	{


		StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
		statFs.restat(Environment.getDataDirectory().getPath());
		long bytesAvailable = (long)statFs.getBlockSize() *(long)statFs.getAvailableBlocks();

//		Log.e("DISK", Environment.getDataDirectory().getAbsolutePath()+" : " + bytesAvailable);

		return bytesAvailable;
	}

	
// ____________________________________________________________________

}
