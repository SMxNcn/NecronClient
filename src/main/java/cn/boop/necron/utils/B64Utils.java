package cn.boop.necron.utils;

import java.util.Base64;
import java.util.Random;

public class B64Utils {

    /**
     * 对字符串进行带偏移的Base64编码
     * @param input 原始字符串
     * @return 编码后的字符串，格式为 :n + Base64编码 + 偏移位数(前一位1/0表示正负，后一位表示位数 0-9):3
     */
    public static String encodeWithOffset(String input) {
        String base64Encoded = Base64.getEncoder().encodeToString(input.getBytes());

        Random random = new Random();
        int offset;
        do {
            offset = random.nextInt(19) - 9; // -9 到 9
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

    /**
     * 尝试解码带偏移的Base64字符串
     * @param encodedInput 编码的字符串
     * @return 解码后的原始字符串，如果格式不正确则返回null
     */
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

    /**
     * 对Base64字符串应用字母偏移
     * @param base64Str Base64字符串
     * @param offset 偏移量
     * @return 应用偏移后的字符串
     */
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
