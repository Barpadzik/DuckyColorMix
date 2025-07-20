package pl.barpad.duckycolormix.config;

import pl.barpad.duckycolormix.Main;
import pl.barpad.duckycolormix.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * ConfigManager handles all configuration-related operations for the DuckyColorMix plugin.
 * This class provides methods to load, save, and retrieve configuration values and messages.
 * It also handles color code translation and provides default values if config entries are missing.
 *
 * @author Barpad
 * @version 1.0.0
 */
public class ConfigManager {

    private final Main plugin;
    private FileConfiguration config;

    /**
     * Constructor for ConfigManager.
     * Initializes the configuration manager with the main plugin instance.
     *
     * @param plugin The main plugin instance
     */
    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads the configuration file from the plugin's data folder.
     * If the config file doesn't exist, it creates a default one.
     * This method should be called during plugin initialization.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Reloads the configuration file from the disk.
     * This method can be used to refresh configuration without restarting the plugin.
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Retrieves a message from the configuration as a Component.
     * Supports all Paper Adventure formatting: legacy codes (&), hex colors (#RRGGBB), MiniMessage, and gradients.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if a path is not found
     * @return Component with full Adventure formatting applied
     */
    public Component getMessageComponent(String path, String defaultMessage) {
        String message = config.getString("messages." + path, defaultMessage);

        return ColorUtils.translate(message);
    }

    /**
     * Retrieves a message from the configuration as a legacy string.
     * For backward compatibility with methods that need String.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if a path is not found
     * @return Legacy formatted string
     */
    public String getMessage(String path, String defaultMessage) {
        Component component = getMessageComponent(path, defaultMessage);
        return ColorUtils.componentToLegacy(component);
    }

    /**
     * Retrieves a message component with placeholder replacement.
     * This method allows for dynamic content insertion into messages.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if the path is not found
     * @param placeholder The placeholder string to replace (e.g., "{player}")
     * @param replacement The value to replace the placeholder with
     * @return Component with placeholders replaced and formatting applied
     */
    public Component getMessageComponent(String path, String defaultMessage, String placeholder, String replacement) {
        String message = config.getString("messages." + path, defaultMessage);
        message = message.replace(placeholder, replacement);
        return ColorUtils.translate(message);
    }

    /**
     * Retrieves a message as legacy string with placeholder replacement.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if a path is not found
     * @param placeholder The placeholder string to replace
     * @param replacement The value to replace the placeholder with
     * @return Legacy formatted string with placeholders replaced
     */
    public String getMessage(String path, String defaultMessage, String placeholder, String replacement) {
        Component component = getMessageComponent(path, defaultMessage, placeholder, replacement);
        return ColorUtils.componentToLegacy(component);
    }

    /**
     * Retrieves a message component with multiple placeholder replacements.
     * This method supports replacing multiple placeholders in a single message.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if a path is not found
     * @param placeholders Array of placeholder strings (must be even length: placeholder, replacement, placeholder, replacement...)
     * @return Component with all placeholders replaced and formatting applied
     */
    public Component getMessageComponent(String path, String defaultMessage, String... placeholders) {
        String message = config.getString("messages." + path, defaultMessage);

        // Replace placeholders in pairs (placeholder, replacement)
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace(placeholders[i], placeholders[i + 1]);
        }

        return ColorUtils.translate(message);
    }

    /**
     * Retrieves a message as legacy string with multiple placeholder replacements.
     *
     * @param path The configuration path to the message
     * @param defaultMessage The default message to return if a path is not found
     * @param placeholders Array of placeholder strings
     * @return Legacy formatted string with all placeholders replaced
     */
    public String getMessage(String path, String defaultMessage, String... placeholders) {
        Component component = getMessageComponent(path, defaultMessage, placeholders);
        return ColorUtils.componentToLegacy(component);
    }

    /**
     * Gets the default starting seconds for the countdown from configuration.
     * This value determines how long the first round countdown will be.
     *
     * @return The default starting seconds (default: 5)
     */
    public int getDefaultStartingSeconds() {
        return config.getInt("game.default-starting-seconds", 5);
    }

    /**
     * Gets the default value for how many rounds before countdown decreases.
     * This determines the frequency of countdown time reduction.
     *
     * @return The default change every rounds value (default: 3)
     */
    public int getDefaultChangeEveryRounds() {
        return config.getInt("game.default-change-every-rounds", 3);
    }

    /**
     * Gets the minimum countdown time that cannot be reduced further.
     * This prevents the countdown from becoming too short.
     *
     * @return The minimum countdown in seconds (default: 1)
     */
    public int getMinimumCountdown() {
        return config.getInt("game.minimum-countdown", 1);
    }

    /**
     * Gets the delay between rounds in server ticks.
     * This determines how long to wait between game rounds.
     *
     * @return The round delay in ticks (default: 60 ticks = 3 seconds)
     */
    public int getRoundDelay() {
        return config.getInt("game.round-delay", 60);
    }

    /**
     * Gets the duration of the firework celebration in seconds.
     * This determines how long fireworks will be displayed for the winner.
     *
     * @return The firework duration in seconds (default: 5)
     */
    public int getFireworksDuration() {
        return config.getInt("game.fireworks-duration", 5);
    }

    /**
     * Gets the number of fireworks to spawn per second during celebration.
     * This controls the intensity of the firework display.
     *
     * @return The number of fireworks per second (default: 5)
     */
    public int getFireworksPerSecond() {
        return config.getInt("game.fireworks-per-second", 5);
    }

    /**
     * Gets the localized name of a wool color from the configuration.
     * This method retrieves the Polish name for the specified dye color.
     *
     * @param color The DyeColor to get the name for
     * @return The localized color name, or the English name if not found in config
     */
    public String getColorName(DyeColor color) {
        return config.getString("colors." + color.name(), color.name());
    }

    /**
     * Gets the hex color code that corresponds to a DyeColor.
     * This method provides accurate color representation using hex codes.
     *
     * @param color The DyeColor to convert
     * @return The corresponding hex color code
     */
    public String getColorCode(DyeColor color) {
        switch (color) {
            case WHITE:
                return "#F9FFFE";      // White Hex
            case ORANGE:
                return "#F9801D";      // Orange Hex
            case MAGENTA:
                return "#D731C9";      // Magenta Hex
            case LIGHT_BLUE:
                return "#00C1FF";      // Light Blue Hex
            case YELLOW:
                return "#FED83D";      // Yellow Hex
            case LIME:
                return "#43FF00";      // Lime Hex
            case PINK:
                return "#F38BAA";      // Pink Hex
            case GRAY:
                return "#474F52";      // Gray Hex
            case LIGHT_GRAY:
                return "#9D9D97";      // Light Gray Hex
            case CYAN:
                return "#169C9C";      // Cyan Hex
            case PURPLE:
                return "#8932B8";      // Purple Hex
            case BLUE:
                return "#0013FF";      // Blue Hex
            case BROWN:
                return "#835432";      // Brown Hex
            case GREEN:
                return "#5E7C16";      // Green Hex
            case RED:
                return "#B02E26";      // Red Hex
            case BLACK:
                return "#1D1D21";      // Black
            default:
                return "#F9FFFE";      // Default White Hex
        }
    }

    /**
     * Gets a formatted color name Component with the appropriate hex color applied.
     * This combines the localized color name with the corresponding hex color using Paper API.
     *
     * @param color The DyeColor to format
     * @return Component with hex color and bold formatting applied
     */
    public Component getFormattedColorNameComponent(DyeColor color) {
        String hexColor = getColorCode(color);
        String colorName = getColorName(color);

        return ColorUtils.hexColorBold(hexColor, colorName);
    }

    /**
     * Gets a formatted color name as legacy string for backward compatibility.
     *
     * @param color The DyeColor to format
     * @return Legacy formatted string with hex color and bold formatting
     */
    public String getFormattedColorName(DyeColor color) {
        Component component = getFormattedColorNameComponent(color);
        return ColorUtils.componentToLegacy(component);
    }
}