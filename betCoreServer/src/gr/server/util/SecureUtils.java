package gr.server.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import gr.server.common.CommonConstants;
import gr.server.common.IgnoreConstants;
import gr.server.common.ServerConstants;
import gr.server.common.bean.AuthorizationBean;

public class SecureUtils {

	private static final String DECRYPT_ALGO = "AES/ECB/NoPadding";// "AES/CBC/NoPadding";

	private static final Charset UTF_8 = StandardCharsets.UTF_8;

	public static String decode(String cipherText) throws Exception {

		String substring = createKey(IgnoreConstants.URL_FORMAT);
		byte[] encoded = substring.getBytes(UTF_8);
		SecretKey originalKey = new SecretKeySpec(encoded, "AES");

		String decryptedText = do_AESDecryption(Base64.getDecoder().decode(cipherText), originalKey);

		StringBuilder sb = new StringBuilder();
		boolean foundAt = false;
		for (int i = 0; i < decryptedText.length(); i++) {
			char charAt = decryptedText.charAt(i);

			if (!foundAt) {
				sb.append(charAt);
				if (charAt == '@') {
					foundAt = true;
				}
			} else {
				if (charAt == '@') {
					break;
				}
				sb.append(charAt);
			}
		}

		System.out.println(sb.toString());
		return sb.toString();

	}

	public static String encode(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String createKey(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = s.length() - 1; i >= 0; i--) {
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}

	static String do_AESDecryption(byte[] cipherText, SecretKey secretKey) throws Exception {

		Cipher cipher = Cipher.getInstance(DECRYPT_ALGO);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		byte[] plainText = cipher.doFinal(cipherText);

//        return new String(Base64.getDecoder().decode(plainText));

		return new String(plainText, UTF_8);
	}

	public static String validateDeviceId(AuthorizationBean authBean) throws Exception {
		String decoded = decode(authBean.getUniqueDeviceId());
		if (!decoded.startsWith(CommonConstants.URL_FORMAT)) {
			throw new RuntimeException("AUTH ERROR");
		}
		
		return decoded;

	}

}
