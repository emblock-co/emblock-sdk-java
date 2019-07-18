package co.emblock.sdk;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class EmblockUtils {

    public static boolean isHexString(String value) {
        return value.startsWith("0x");
    }

    public static boolean isValidEthAddress(String address) {
        return isHexString(address) && Pattern.compile("^0x[0-9a-fA-F]{40}$").matcher(address).matches();
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String utf8ToBytes32Hex(String value) {
        if (value.length() >= 32) throw new IllegalArgumentException("value must be 32 characters max");
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        String hex = bytesToHex(bytes);
        while (hex.length() < 64) {
            hex += "0";
        }
        return "0x" + hex;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytes32HexToUtf8(String hex) {
        if (!isHexString(hex)) throw new IllegalArgumentException("it's not an hex");
        if (hex.length() > 66) throw new IllegalArgumentException("this is not an bytes 32 hex, must be 66 characters");
        String hexx = hex.subSequence(2, hex.length()).toString();
        byte[] bytes = hexStringToByteArray(hexx);
        return new String(bytes, Charset.forName("UTF-8")).trim();
    }

    public static void checkNotEmptyOrNull(String val, String msg) {
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

}


