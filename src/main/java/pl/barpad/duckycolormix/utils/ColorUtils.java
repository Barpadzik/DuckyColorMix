package pl.barpad.duckycolormix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

/**
 * ColorUtils provides advanced color formatting capabilities using Paper's Adventure API.
 * This utility class supports legacy color codes (&), hex colors (#RRGGBB), MiniMessage format,
 * and provides seamless conversion between different text formats.
 * <p>
 * Features:
 * - Legacy color code translation (&a, &c, etc.)
 * - Hex color support (#FF0000, #00FF00, etc.)
 * - MiniMessage format support (<red>, <#FF0000>, <gradient>, etc.)
 * - Component-based text handling
 * - Automatic format detection and conversion
 *
 * @author Barpad
 * @version 1.0.0 (Paper API)
 */
public class ColorUtils {

    // MiniMessage instance for parsing modern formats
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    // Legacy serializer for backward compatibility
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    // Regex patterns for different color formats
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fk-or])");

    /**
     * Translates any text format to a Component using Paper's Adventure API.
     * Supports legacy codes (&), hex colors (#), and MiniMessage format.
     *
     * @param text The text to translate
     * @return Component with proper formatting applied
     */
    public static Component translate(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        // Always try MiniMessage first for gradients and modern formatting
        try {
            // Check if a text contains MiniMessage tags
            if (text.contains("<") && text.contains(">")) {
                return MINI_MESSAGE.deserialize(text);
            }

            // If no MiniMessage tags, try legacy parsing
            return MINI_MESSAGE.deserialize(text);
        } catch (Exception e) {
            // If MiniMessage fails, fallback to legacy
            return LEGACY_SERIALIZER.deserialize(text);
        }
    }

    /**
     * Translates text to legacy format string for backward compatibility.
     * Useful when you need a String instead of Component.
     *
     * @param text The text to translate
     * @return Legacy formatted string
     */
    public static String translateToLegacy(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Component component = translate(text);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Creates a Component from hex color and text.
     * Convenience method for simple hex-colored text.
     *
     * @param hexColor Hex color (e.g., "#FF0000")
     * @param text The text to color
     * @return Component with hex color applied
     */
    public static Component hexColor(String hexColor, String text) {
        TextColor color = TextColor.fromHexString(hexColor);
        return Component.text(text).color(color);
    }

    /**
     * Creates a bold Component with hex color.
     * Convenience method for bold-colored text.
     *
     * @param hexColor Hex color (e.g., "#FF0000")
     * @param text The text to color and make bold
     * @return Component with hex color and bold formatting
     */
    public static Component hexColorBold(String hexColor, String text) {
        TextColor color = TextColor.fromHexString(hexColor);
        return Component.text(text)
                .color(color)
                .decoration(TextDecoration.BOLD, true);
    }

    /**
     * Creates a gradient Component using MiniMessage format.
     *
     * @param startColor Starting hex color
     * @param endColor Ending hex color
     * @param text Text to apply gradient to
     * @return Component with gradient applied
     */
    public static Component gradient(String startColor, String endColor, String text) {
        String miniMessage = "<gradient:" + startColor + ":" + endColor + ">" + text + "</gradient>";
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    /**
     * Creates a rainbow Component using MiniMessage format.
     *
     * @param text Text to apply rainbow effect to
     * @return Component with rainbow effect
     */
    public static Component rainbow(String text) {
        String miniMessage = "<rainbow>" + text + "</rainbow>";
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    /**
     * Strips all color codes from a text, leaving only plain text.
     *
     * @param text The text to strip colors from
     * @return Plain text without formatting
     */
    public static String stripColors(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Component component = translate(text);
        return LegacyComponentSerializer.legacySection().serialize(component)
                .replaceAll("§[0-9a-fk-or]", "");
    }

    /**
     * Converts Component to legacy string format.
     * Useful for compatibility with older systems.
     *
     * @param component The component to convert
     * @return Legacy formatted string
     */
    public static String componentToLegacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Converts legacy string to Component.
     *
     * @param legacyText Legacy formatted text
     * @return Component representation
     */
    public static Component legacyToComponent(String legacyText) {
        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }
}