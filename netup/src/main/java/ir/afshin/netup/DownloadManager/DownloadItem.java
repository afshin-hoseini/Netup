package ir.afshin.netup.DownloadManager;

/**
 * Created by Afshin on 8/2/2015.
 */
public class DownloadItem {

    public String url = "";
    public String packageName = "";
    public String packagePath = "";
    public int fileSize = 0;
    private String destFilename = null;

    public void setDestFilename(String destFilename)
    {
        if(destFilename != null)
        {
            packagePath = destFilename.substring(0, destFilename.lastIndexOf("/") + 1);
            this.destFilename = destFilename;
        }
    }

    public String getDestFilename()
    {
        if(destFilename != null)
        {
            return destFilename;
        }

        if( ! packagePath.endsWith("/"))
            packagePath += "/";

        return packagePath + packageName;
    }

}
