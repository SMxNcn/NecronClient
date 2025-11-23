package cn.boop.necron.utils;

import java.util.Base64;
import java.util.Random;

public class B64Utils {
    public static String encodeWithOffset(String input) {
        String base64Encoded = Base64.getEncoder().encodeToString(input.getBytes());

        Random random = new Random();
        int offset;
        do {
            offset = random.nextInt(19) - 9;
        } while (offset == 0);

        String offsetEncoded = applyOffset(base64Encoded, offset);

        StringBuilder hexPadding = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            hexPadding.append(Integer.toHexString(random.nextInt(16)));
        }

        String sign = offset > 0 ? "1" : "0";
        String offsetDigit = String.valueOf(Math.abs(offset));

        return "::" + offsetEncoded  + hexPadding + sign + offsetDigit + "%]";
    }

    public static String decodeWithOffset(String encodedInput) {
        try {
            if (!encodedInput.startsWith("::") || !encodedInput.endsWith("%]")) {
                return null;
            }

            String content = encodedInput.substring(2, encodedInput.length() - 2);

            if (content.length() < 6) {
                return null;
            }

            String offsetPart = content.substring(content.length() - 2);
            String base64Part = content.substring(0, content.length() - 6);

            int sign = offsetPart.charAt(0) == '1' ? 1 : -1;
            int offsetValue = Character.getNumericValue(offsetPart.charAt(1));
            int offset = sign * offsetValue;

            String originalBase64 = applyOffset(base64Part, -offset);

            byte[] decodedBytes = Base64.getDecoder().decode(originalBase64);
            return new String(decodedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String applyOffset(String base64Str, int offset) {
        StringBuilder result = new StringBuilder();

        for (char c : base64Str.toCharArray()) {
            if (Character.isLetter(c)) {
                char shifted;
                if (Character.isLowerCase(c)) {
                    int newPos = ((c - 'a' + offset) % 26 + 26) % 26;
                    shifted = (char) ('A' + newPos);
                } else {
                    int newPos = ((c - 'A' + offset) % 26 + 26) % 26;
                    shifted = (char) ('a' + newPos);
                }
                result.append(shifted);
            } else if (Character.isDigit(c)) {
                int newDigit = ((c - '0' + offset) % 10 + 10) % 10;
                result.append(newDigit);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
