package co.emblock.sdk;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

public class EmblockUtils {

    public static boolean isHexString(String value) {
        return value.startsWith("0x");
    }

    public static boolean isValidEthAddress(String address) {
        return isHexString(address) && Pattern.compile("^0x[0-9a-fA-F]{40}$").matcher(address).matches();
    }

    public static String utf8ToBytes32Hex(String value) {
        if (value.length() >= 32) throw new IllegalArgumentException("value must be 32 characters max");
        String hex = "";

        for (int i = 0; i < value.length(); i++) {
            Integer code = value.codePointAt(i);
            System.out.println("code=" + code);
            if (code == 0) {
                // code is 0
            } else {
                hex += Integer.toHexString(code);
                System.out.println("hex=" + hex);
            }
        }

        while (hex.length() < 64) {
            hex += "0";
        }

        return "0x" + hex;
    }

    public static String bytes32HexToUtf8(String hex) {
        if (!isHexString(hex)) throw new IllegalArgumentException("it's not an hex");
        if (hex.length() > 66) throw new IllegalArgumentException("this is not an bytes 32 hex, must be 66 characters");
        String hexx = hex.subSequence(2, hex.length()).toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < hexx.length(); i += 2) {
            String str = hexx.substring(i, i + 2);
            int byteVal = Integer.parseInt(str, 16);
            baos.write(byteVal);
        }
        return new String(baos.toByteArray(), Charset.forName("UTF-8"));
    }

    public static void checkNotEmptyOrNull(String val, String msg) {
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
    }

}


