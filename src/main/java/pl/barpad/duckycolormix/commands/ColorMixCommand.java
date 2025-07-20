package pl.barpad.duckycolormix.commands;

import org.jetbrains.annotations.NotNull;
import pl.barpad.duckycolormix.Main;
import pl.barpad.duckycolormix.config.ConfigManager;
import pl.barpad.duckycolormix.command.AbstractCommand;
import pl.barpad.duckycolormix.game.ColorMixGame;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * ColorMixCommand handles all command-line interactions for the DuckyColorMix plugin.
 * This class extends AbstractCommand to provide custom command registration and handling.
 * It supports starting/stopping games, setting up platforms, and provides help information.
 * <p>
 * Commands supported:
 * - /colormix start [seconds] [change_every_rounds] - Starts a new game
 * - /colormix stop - Stops the current game
 * - /colormix setplatform - Enables platform setup mode
 * - /colormix reload - Reloads the plugin configuration
 *
 * @author Barpad
 * @version 1.0.0
 */
public class ColorMixCommand extends AbstractCommand {

    private final Main plugin;
    private final ConfigManager configManager;
    private final ColorMixGame game;

    /**
     * Constructor for ColorMixCommand.
     * Sets up the command with proper usage, description, permission message, and aliases.
     *
     * @param plugin The main plugin instance
     * @param configManager The configuration manager for messages
     * @param game The game instance to control
     */
    public ColorMixCommand(Main plugin, ConfigManager configManager, ColorMixGame game) {
        super("colormix",
                "/colormix <start [seconds] [change_every_rounds]|stop|setplatform|reload>",
                "Game management > DuckyColorMix",
                configManager.getMessage("no-permission", "&cYou do not have permission to use this command!"),
                Arrays.asList("cm", "dcm", "duckycolormix"));
        this.plugin = plugin;
        this.configManager = configManager;
        this.game = game;
    }

    /**
     * Handles command execution for all ColorMix commands.
     * Validates permissions, parses arguments, and delegates to appropriate game methods.
     *
     * @param sender The command sender (must be a player)
     * @param command The command object
     * @param label The command label used
     * @param args The command arguments
     * @return true if command was handled successfully
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("only-players", "&cThis command can only be used by players!"));
            return true;
        }

        if (!player.hasPermission("duckycolormix.colormix")) {
            player.sendMessage(configManager.getMessage("no-permission", "&cYou do not have permission to use this command!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                handleStartCommand(player, args);
                break;

            case "stop":
                handleStopCommand(player);
                break;

            case "pause":
                handlePauseCommand(player);
                break;

            case "resume":
                handleResumeCommand(player);
                break;

            case "setplatform":
                handleSetPlatformCommand(player);
                break;

            case "reload":
                handleReloadCommand(player);
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    /**
     * Handles the start command with optional parameters.
     * Validates input parameters and starts the game with specified or default settings.
     *
     * @param player The player executing the command
     * @param args The command arguments
     */
    private void handleStartCommand(Player player, String[] args) {
        int startSeconds = configManager.getDefaultStartingSeconds();
        int changeEveryRounds = configManager.getDefaultChangeEveryRounds();

        // Parse starting seconds if provided
        if (args.length >= 2) {
            try {
                startSeconds = Integer.parseInt(args[1]);
                if (startSeconds < 1) {
                    player.sendMessage(configManager.getMessage("seconds-too-low", "&cThe number of seconds must be greater than 0!"));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(configManager.getMessage("invalid-seconds", "&cInvalid number of seconds!"));
                return;
            }
        }

        // Parse change frequency if provided
        if (args.length >= 3) {
            try {
                changeEveryRounds = Integer.parseInt(args[2]);
                if (changeEveryRounds < 1) {
                    player.sendMessage(configManager.getMessage("rounds-too-low", "&cThe change after how many rounds must be greater than 0!"));
                    return;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(configManager.getMessage("invalid-rounds", "&cIncorrect value of change after how many rounds!"));
                return;
            }
        }

        // Attempt to start the game
        if (game.isGameActive()) {
            player.sendMessage(configManager.getMessage("game-already-active", "&cDuckyColorMix already in progress!"));
        } else if (!game.hasPlatform()) {
            player.sendMessage(configManager.getMessage("platform-not-set", "&cFirst, set up the platform using /colormix setplatform!"));
        } else {
            game.startGame(startSeconds, changeEveryRounds);
            player.sendMessage(configManager.getMessage("game-started", "&aDuckyColorMix has been started!"));
            player.sendMessage(configManager.getMessage("game-settings", "&eStarting seconds: {seconds}, change every {rounds} rounds",
                    "{seconds}", String.valueOf(startSeconds),
                    "{rounds}", String.valueOf(changeEveryRounds)));
        }
    }

    /**
     * Handles the stop command.
     * Stops the current game if one is active.
     *
     * @param player The player executing the command
     */
    private void handleStopCommand(Player player) {
        if (!game.isGameActive()) {
            player.sendMessage(configManager.getMessage("game-not-active", "&cDuckyColorMix is not active!"));
        } else {
            game.stopGame();
            player.sendMessage(configManager.getMessage("game-stopped", "&cDuckyColorMix was detained!"));
        }
    }

    /**
     * Handles the pause command.
     * Pauses the current game if one is active and not already paused.
     *
     * @param player The player executing the command
     */
    private void handlePauseCommand(Player player) {
        if (!game.isGameActive()) {
            player.sendMessage(configManager.getMessage("game-not-active", "&cDuckyColorMix is not active!"));
        } else if (game.isGamePaused()) {
            player.sendMessage(configManager.getMessage("game-already-paused", "&cDuckyColorMix is already on hold!"));
        } else {
            game.pauseGame();
            player.sendMessage(configManager.getMessage("game-paused-by-player", "&eYou paused the game DuckyColorMix!"));
        }
    }

    /**
     * Handles the resume command.
     * Resumes the paused game if one exists.
     *
     * @param player The player executing the command
     */
    private void handleResumeCommand(Player player) {
        if (!game.isGameActive()) {
            player.sendMessage(configManager.getMessage("game-not-active", "&cDuckyColorMix is not active!"));
        } else if (!game.isGamePaused()) {
            player.sendMessage(configManager.getMessage("game-not-paused", "&cDuckyColorMix is not suspended!"));
        } else {
            game.resumeGame();
            player.sendMessage(configManager.getMessage("game-resumed-by-player", "&aYou have resumed the game DuckyColorMix!"));
        }
    }

    /**
     * Handles the setplatform command.
     * Enables platform setup mode for the executing player.
     *
     * @param player The player executing the command
     */
    private void handleSetPlatformCommand(Player player) {
        game.setPlatformMode(player);
        player.sendMessage(configManager.getMessage("platform-mode-enabled", "&ePlatform setup mode has been enabled!"));
        player.sendMessage(configManager.getMessage("platform-click-instruction", "&eClick on two opposite corners of the platform area!"));
    }

    /**
     * Handles the reload command.
     * Reloads the plugin configuration and notifies the player.
     * If a game is active, it will be stopped before reloading.
     *
     * @param player The player executing the command
     */
    private void handleReloadCommand(Player player) {
        // Stop active game before reloading
        if (game.isGameActive()) {
            game.stopGame();
            player.sendMessage(Component.text("The game was paused before reloading.")
                    .color(net.kyori.adventure.text.format.TextColor.color(255, 255, 0)));
        }

        // Reload configuration
        configManager.reloadConfig();

        // Send a success message using the newly loaded config
        player.sendMessage(configManager.getMessageComponent("config-reloaded", "<green>The configuration has been reloaded!</green>"));

        // Log reload action
        plugin.getLogger().info("The configuration was reloaded by the player: " + player.getName());
    }

    /**
     * Provides tab completion for command arguments.
     * Returns appropriate suggestions based on the current argument position.
     *
     * @param sender The command sender
     * @param cmd The command object
     * @param label The command label
     * @param args The current arguments
     * @return A list of possible completions
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "stop", "pause", "resume", "setplatform", "reload");
        }
        return null;
    }

    /**
     * Sends help information to the player.
     * Displays all available commands and usage examples using Paper API.
     *
     * @param player The player to send help to
     */
    private void sendHelp(Player player) {
        player.sendMessage(configManager.getMessageComponent("help-header", "<rainbow><bold>=== DuckyColorMix Commands ===</bold></rainbow>"));
        player.sendMessage(configManager.getMessageComponent("help-start", "<yellow>/colormix start [seconds] [change_every_round]</yellow><white> - Rozpoczyna grę</white>"));
        player.sendMessage(configManager.getMessageComponent("help-stop", "<yellow>/colormix stop</yellow><white> - Pauses the game</white>"));
        player.sendMessage(configManager.getMessageComponent("help-pause", "<yellow>/colormix pause</yellow><white> - Pauses the game</white>"));
        player.sendMessage(configManager.getMessageComponent("help-resume", "<yellow>/colormix resume</yellow><white> - Resume the game</white>"));
        player.sendMessage(configManager.getMessageComponent("help-setplatform", "<yellow>/colormix setplatform</yellow><white> - Sets the platform areay</white>"));
        player.sendMessage(configManager.getMessageComponent("help-reload", "<yellow>/colormix reload</yellow><white> - Reloads the configuration</white>"));
        player.sendMessage(configManager.getMessageComponent("help-example", "<gray>Przykład: /colormix start 10 2 (10 seconds, decreases every)</gray>"));
    }
}