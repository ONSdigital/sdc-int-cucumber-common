package uk.gov.ons.ctp.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UacUtil {
  private static final int SEGMENT_SIZE = 4;
  private static final int CODE_LENGTH = 16;
  private static final char[] VALID_CHARS = {
    'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'X', 'Z',
    '2', '3', '4', '5', '6', '7', '8', '9'
  };

  /**
   * Generate a UAC
   *
   * @return random generated UAC
   */
  public static String generateUac() {
    SecureRandom random = new SecureRandom();
    int segmentCount = CODE_LENGTH / SEGMENT_SIZE;

    StringBuilder codeBuilder = new StringBuilder(CODE_LENGTH);

    for (int segNum = 0; segNum < segmentCount; segNum++) {
      StringBuilder segmentBuilder = generateSegmentBuilder(random);
      codeBuilder.append(segmentBuilder.toString());
    }
    return codeBuilder.toString();
  }

  private static StringBuilder generateSegmentBuilder(SecureRandom random) {
    StringBuilder segmentBuilder = new StringBuilder(SEGMENT_SIZE);
    for (int n = 0; n < SEGMENT_SIZE; n++) {
      segmentBuilder.append(VALID_CHARS[random.nextInt(VALID_CHARS.length)]);
    }
    return segmentBuilder;
  }

  /**
   * Create the SHA256 Hash of the UAC
   *
   * @param uac
   * @return SHA256 Hash
   */
  public static String getSha256Hash(String uac) {
    String uacHash = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashInBytes = md.digest(uac.getBytes(StandardCharsets.UTF_8));

      // bytes to hex
      StringBuilder sb = new StringBuilder();
      for (byte b : hashInBytes) {
        sb.append(String.format("%02x", b));
      }
      uacHash = sb.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }

    if (uacHash.length() != 64) {
      throw new RuntimeException("UAC has wrong length");
    }

    return uacHash;
  }

  public static void main(String[] args) {
    System.out.println(generateUac());
  }
}
