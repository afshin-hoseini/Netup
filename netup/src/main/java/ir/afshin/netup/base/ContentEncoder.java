package ir.afshin.netup.base;

/**
 * Created by afshinhoseini on 4/7/16.
 */
public interface ContentEncoder {

    /**
     * @param forUrl The url this conversion is belong to.
     * @param forKey The key of parameter.
     * @param valueToEncode The value that this method must encode in the the way it wants.
     * @return The encoded content of <i>valueToEncode</i> parameter, or <b>null</b> if you wish to
     * default encoder take the control.
     */
    String encodeContent(String forUrl, String forKey, String valueToEncode);
}
