package cn.mwee.base_common.utils.codec;

import cn.mwee.base_common.support.misc.Encodings;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by liaomengge on 16/9/6.
 */
public final class MwBase64Util {

    private MwBase64Util() {
    }

    public static String encode(byte[] binaryData) {
        return encode(binaryData, Encodings.UTF_8);
    }

    public static String decode(String base64String) {
        return decode(base64String, Encodings.UTF_8);
    }

    public static String encode(byte[] binaryData, String charsetName) {
        try {
            return new String(Base64.encodeBase64(binaryData), charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String decode(String base64String, String charsetName) {
        try {
            return new String(Base64.decodeBase64(base64String.getBytes()), charsetName);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}
