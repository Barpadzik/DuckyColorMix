package pl.barpad.duckycolormix.utils;

import org.bukkit.Bukkit;
import pl.barpad.duckycolormix.Main;
import pl.barpad.duckycolormix.config.ConfigManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * UpdateChecker handles automatic update checking for the DuckyColorMix plugin.
 * This class connects to GitHub API to check for new releases and notifies
 * administrators about available updates.
 * <p>
 * Features:
 * - Asynchronous update checking to prevent server lag
 * - GitHub API integration for reliable version checking
 * - Permission-based notifications to administrators
 * - Configurable messages with full ColorUtils support
 * - Error handling for network issues
 *
 * @author Barpad
 * @version 1.0.0
 */
public class UpdateChecker {

    private final Main plugin;
    private final ConfigManager configManager;
    private static final String GITHUB_API_URL = "https://api.github.com/repos/Barpadzik/DuckyColorMix/releases/latest";

    /**
     * Constructor for UpdateChecker.
     * Initializes the update checker with required dependencies.
     *
     * @param plugin The main plugin instance
     * @param configManager The configuration manager for messages
     */
    public UpdateChecker(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    /**
     * Checks for updates asynchronously.
     * Connects to GitHub API to retrieve the latest release information
     * and compares it with the current plugin version.
     */
    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Create HTTP connection to GitHub API
                HttpURLConnection connection = (HttpURLConnection) new URL(GITHUB_API_URL).openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("User-Agent", "DuckyColorMix-Plugin/1.0");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                reader.close();
                String json = responseBuilder.toString();

                // Extract version and download URL from JSON response
                String latestVersion = extractValue(json, "tag_name");
                String downloadUrl = extractValue(json, "html_url");

                if (latestVersion == null || downloadUrl == null) {
                    throw new Exception("Could not parse GitHub API response");
                }

                // Compare versions
                String currentVersion = plugin.getDescription().getVersion();
                if (!currentVersion.equalsIgnoreCase(latestVersion)) {
                    // New version available - notify console and players with permission
                    notifyUpdate(latestVersion, downloadUrl);
                } else {
                    // Plugin is up to date
                    String upToDateMessage = configManager.getMessage("update-up-to-date",
                            "&aDuckyColorMix is up to date! Versja: {version}",
                            "{version}", currentVersion);
                    Bukkit.getConsoleSender().sendMessage(upToDateMessage);
                }

            } catch (Exception e) {
                // Handle errors gracefully
                String errorMessage = configManager.getMessage("update-check-failed",
                        "&cFailed to check for DuckyColorMix updates: {error}",
                        "{error}", e.getMessage());
                Bukkit.getConsoleSender().sendMessage(errorMessage);

                // Log detailed error for debugging
                // plugin.getLogger().warning("Update check failed: " + e.getMessage());
            }
        });
    }

    /**
     * Notifies console and players with permission about available updates.
     * Sends formatted messages with version information and download links.
     *
     * @param latestVersion The latest available version
     * @param downloadUrl The URL to download the update
     */
    private void notifyUpdate(String latestVersion, String downloadUrl) {
        String currentVersion = plugin.getDescription().getVersion();

        // Prepare update messages
        String updateMessage = configManager.getMessage("update-available",
                "<gradient:#FFD700,#FFA500>&l🔔 DuckyColorMix</gradient> &8» &eNew version available: &c{version} &7(aktualna: {current})",
                "{version}", latestVersion,
                "{current}", currentVersion);

        String downloadMessage = configManager.getMessage("update-download",
                "<gradient:#FFD700,#FFA500>&l📥 DuckyColorMix</gradient> &8» &eDownload: &a{url}",
                "{url}", downloadUrl);

        // Send it to console
        Bukkit.getConsoleSender().sendMessage(updateMessage);
        Bukkit.getConsoleSender().sendMessage(downloadMessage);

        // Send it to players with update permission
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("duckycolormix.update"))
                    .forEach(player -> {
                        player.sendMessage(updateMessage);
                        player.sendMessage(downloadMessage);
                    });
        });
    }

    /**
     * Extracts a value from JSON response using simple string parsing.
     * This method provides basic JSON parsing without external dependencies.
     *
     * @param json The JSON string to parse
     * @param key The key to extract value for
     * @return The extracted value or null if not found
     */
    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        // Find the start of the value (after the colon and optional whitespace)
        int valueStart = keyIndex + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        // Check if the value is a string (starts with quote)
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            int quoteEnd = json.indexOf('"', valueStart + 1);

            if (quoteEnd == -1) return null;

            return json.substring(valueStart + 1, quoteEnd);
        }

        return null;
    }
}