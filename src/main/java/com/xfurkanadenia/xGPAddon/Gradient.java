package com.xfurkanadenia.xGPAddon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Gradient {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern GRADIENT_TAG_PATTERN = Pattern.compile("<gradient:#([a-fA-F0-9]{6}):#([a-fA-F0-9]{6})>(.*?)</gradient>");
    private static final Pattern COLOR_TAG_PATTERN = Pattern.compile("<color:#([a-fA-F0-9]{6})>(.*?)</color>");
    private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile("&([klmnor])");

    public static String processText(String text) {

        if (text == null || text.isEmpty()) {
            return "";
        }
        text = applyGradient(text);
        text = applyColor(text);
        text = applyHexColorsAndFormats(text);
        return text;
    }

    private static String applyGradient(String text) {
        Matcher matcher = GRADIENT_TAG_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String startColorHex = matcher.group(1);
            String endColorHex = matcher.group(2);
            String content = matcher.group(3);
            String gradientText = applyFormattedText(content, startColorHex, true, endColorHex);
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText + "§r"));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String applyColor(String text) {
        Matcher matcher = COLOR_TAG_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String colorHex = matcher.group(1);
            String content = matcher.group(2);
            String colorText = applyFormattedText(content, colorHex, false, null);
            matcher.appendReplacement(result, Matcher.quoteReplacement(colorText + "§r"));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String applyFormattedText(String text, String colorHex, boolean isGradient, String endColorHex) {
        StringBuilder result = new StringBuilder();
        String activeFormats = "";
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char currentChar = text.charAt(i);
            if (currentChar == '&' && i + 1 < length) {
                char formatCode = text.charAt(i + 1);
                if ("klmnor".indexOf(formatCode) >= 0) {
                    activeFormats += "§" + formatCode;
                    i++;
                    continue;
                }
            }

            if (isGradient) {
                double ratio = (double) i / (length - 1);
                int[] startColor = hexToRgb(colorHex);
                int[] endColor = hexToRgb(endColorHex);
                int red = (int) (startColor[0] * (1 - ratio) + endColor[0] * ratio);
                int green = (int) (startColor[1] * (1 - ratio) + endColor[1] * ratio);
                int blue = (int) (startColor[2] * (1 - ratio) + endColor[2] * ratio);
                String hexColor = String.format("§x§%s§%s§%s§%s§%s§%s",
                        Integer.toHexString(red >> 4),
                        Integer.toHexString(red & 0xF),
                        Integer.toHexString(green >> 4),
                        Integer.toHexString(green & 0xF),
                        Integer.toHexString(blue >> 4),
                        Integer.toHexString(blue & 0xF)
                );
                result.append(hexColor).append(activeFormats).append(currentChar);
            } else {
                String hexColor = convertHexToMinecraftColor(colorHex);
                result.append(hexColor).append(activeFormats).append(currentChar);
            }
        }
        return result.toString();
    }

    public static boolean hasColorFormat(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return HEX_COLOR_PATTERN.matcher(text).find()
                || GRADIENT_TAG_PATTERN.matcher(text).find()
                || COLOR_TAG_PATTERN.matcher(text).find()
                || FORMAT_CODE_PATTERN.matcher(text).find();
    }

    private static String applyHexColorsAndFormats(String text) {
        text = applyHexColors(text);
        return applyFormatCodes(text);
    }

    private static String applyHexColors(String text) {
        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            String minecraftColor = convertHexToMinecraftColor(hexColor);
            hexMatcher.appendReplacement(result, Matcher.quoteReplacement(minecraftColor));
        }
        hexMatcher.appendTail(result);
        return result.toString();
    }

    private static String applyFormatCodes(String text) {
        Matcher formatMatcher = FORMAT_CODE_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (formatMatcher.find()) {
            String formatCode = formatMatcher.group(1);
            formatMatcher.appendReplacement(result, "§" + formatCode);
        }
        formatMatcher.appendTail(result);
        return result.toString();
    }

    private static String convertHexToMinecraftColor(String hex) {
        return "§x" + hex.chars()
                .mapToObj(c -> "§" + (char) c)
                .collect(Collectors.joining());
    }

    private static int[] hexToRgb(String hex) {
        if (hex == null || hex.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }
        return IntStream.range(0, 3)
                .map(i -> Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16))
                .toArray();
    }

}