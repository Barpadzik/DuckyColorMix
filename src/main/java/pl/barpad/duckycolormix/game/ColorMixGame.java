package pl.barpad.duckycolormix.game;

import pl.barpad.duckycolormix.Main;
import pl.barpad.duckycolormix.config.ConfigManager;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

/**
 * ColorMixGame is the main game controller for the DuckyColorMix plugin.
 * This class manages the complete game lifecycle including rounds, countdowns, and win conditions.
 * It coordinates between the platform manager and effects manager to create a complete game experience.
 * <p>
 * Game Flow:
 * 1. The Platform is filled with random-colored wool
 * 2. A safe color is randomly selected
 * 3. Countdown begins (decreases over time)
 * 4. Unsafe blocks are removed after countdown
 * 5. The Game continues until one player remains
 *
 * @author Barpad
 * @version 1.0.0
 */
public class ColorMixGame implements Listener {

    private final Main plugin;
    private final ConfigManager configManager;
    private final PlatformManager platformManager;
    private final GameEffects gameEffects;

    // Game state variables
    private boolean gameActive = false;
    private boolean gamePaused = false;
    private int startingSeconds = 5;
    private int changeEveryRounds = 3;
    private int currentCountdown = 5;
    private int round = 0;
    private BukkitTask gameTask = null;

    /**
     * Constructor for ColorMixGame.
     * Initializes the game with all required managers and dependencies.
     *
     * @param plugin The main plugin instance
     */
    public ColorMixGame(Main plugin) {
        this.plugin = plugin;
        this.configManager = new ConfigManager(plugin);
        this.platformManager = new PlatformManager(configManager);
        this.gameEffects = new GameEffects(plugin, configManager, platformManager);
    }

    /**
     * Checks if a game is currently active.
     *
     * @return true if a game is running, false otherwise
     */
    public boolean isGameActive() {
        return gameActive;
    }

    /**
     * Checks if the game is currently paused.
     *
     * @return true if game is paused, false otherwise
     */
    public boolean isGamePaused() {
        return gamePaused;
    }

    /**
     * Checks if a platform has been set up for the game.
     *
     * @return true if a platform is configured, false otherwise
     */
    public boolean hasPlatform() {
        return platformManager.hasPlatform();
    }

    /**
     * Gets the number of players currently in the game area.
     *
     * @return The count of players in the game
     */
    public int getPlayersInGame() {
        return platformManager.getPlayersInGame();
    }

    /**
     * Gets a list of all players currently in the game area.
     *
     * @return A list of players in the game
     */
    public List<Player> getPlayersInGameList() {
        return platformManager.getPlayersInGameList();
    }

    /**
     * Enables platform setup mode for a player.
     * Delegates to the platform manager for handling.
     *
     * @param player The player who will set up the platform
     */
    public void setPlatformMode(Player player) {
        platformManager.setPlatformMode(player);
    }

    /**
     * Handles player interaction events for platform setup.
     * Delegates to the platform manager and provide feedback messages.
     *
     * @param event The PlayerInteractEvent to handle
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Platform manager handles the interaction, we just need to register the event
        platformManager.onPlayerInteract(event);
    }

    /**
     * Starts a new game with specified parameters.
     * Initializes game state and begins the first round.
     *
     * @param startingSeconds The initial countdown time for the first round
     * @param changeEveryRounds How many rounds before countdown decreases
     */
    public void startGame(int startingSeconds, int changeEveryRounds) {
        if (!hasPlatform()) {
            return;
        }

        gameActive = true;
        round = 0;
        this.startingSeconds = startingSeconds;
        this.changeEveryRounds = changeEveryRounds;
        this.currentCountdown = startingSeconds;

        // Start the game cycle
        startGameCycle();
    }

    /**
     * Stops the current game and cleans up all resources.
     * Regenerates the platform and notifies players.
     */
    public void stopGame() {
        gameActive = false;
        gamePaused = false;
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // Stop all effects
        gameEffects.stopAllEffects();

        // Regenerate platform with random colors
        platformManager.fillPlatformWithWool();

        // Notify all players
        Bukkit.broadcast(configManager.getMessageComponent("game-stopped", "<red>DuckyColorMix was detained!</red>"));
    }

    /**
     * Pauses the current game.
     * Stops all effects and timers while preserving game state.
     */
    public void pauseGame() {
        if (!gameActive || gamePaused) {
            return;
        }

        gamePaused = true;

        // Stop the current game task
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        // Stop all effects
        gameEffects.stopAllEffects();

        // Notify all players
        Bukkit.broadcast(configManager.getMessageComponent("game-paused", "<yellow>DuckyColorMix has been suspended!</yellow>"));
    }

    /**
     * Resumes the paused game.
     * Continues from the current round with preserved state.
     */
    public void resumeGame() {
        if (!gameActive || !gamePaused) {
            return;
        }

        gamePaused = false;

        // Notify players about resume
        Bukkit.broadcast(configManager.getMessageComponent("game-resumed", "<green>DuckyColorMix has been resumed!</green>"));

        // Continue with the current round
        startGameCycle();
    }

    /**
     * Starts a new game cycle (round).
     * Sets up the platform, select safe color, and begin countdown.
     */
    private void startGameCycle() {
        round++;

        // Check player count
        int playersCount = getPlayersInGame();

        // Fill a platform with colored wool
        platformManager.fillPlatformWithWool();

        // Select random safe color
        DyeColor safeColor = platformManager.getRandomWoolColor();

        // Notify players with colored messages using Paper API
        Component formattedColorName = configManager.getFormattedColorNameComponent(safeColor);

        // Broadcast messages using Paper API for better formatting
        Bukkit.broadcast(configManager.getMessageComponent("round-header", "<gradient:#FFD700:#FFA500><bold>=== DuckyColorMix - Round {round} ===</bold></gradient>",
                "{round}", String.valueOf(round)));
        Bukkit.broadcast(configManager.getMessageComponent("players-count", "<aqua><bold>Players in the game: <white>{count}</bold></aqua>",
                "{count}", String.valueOf(playersCount)));

        // Create a safe color message with component replacement
        Component safeColorMessage = configManager.getMessageComponent("safe-color", "<gradient:#FFFF00:#FFD700>Safe Color: <bold>{color}</bold></gradient>")
                .replaceText(builder -> builder.matchLiteral("{color}").replacement(formattedColorName));
        Bukkit.broadcast(safeColorMessage);

        Bukkit.broadcast(configManager.getMessageComponent("countdown-info", "<yellow>Countdown: <bold>{seconds}</bold> seconds",
                "{seconds}", String.valueOf(currentCountdown)));

        // Start countdown and action bar animation
        startCountdown(safeColor);
        gameEffects.startActionBarAnimation(safeColor);
    }

    /**
     * Starts the countdown timer for the current round.
     * Handles countdown display, sound effects, and round completion.
     *
     * @param safeColor The color that will remain safe after countdown
     */
    private void startCountdown(DyeColor safeColor) {
        gameTask = new BukkitRunnable() {
            int timeLeft = currentCountdown;

            @Override
            public void run() {
                if (!gameActive) {
                    cancel();
                    return;
                }

                // Check if game is paused
                if (gamePaused) {
                    cancel();
                    return;
                }

                // Check player count every tick - game can end at any moment!
                List<Player> currentPlayers = platformManager.getPlayersInGameList();

                if (currentPlayers.size() == 1) {
                    // One player won during countdown!
                    Player winner = currentPlayers.getFirst();
                    Bukkit.broadcast(configManager.getMessageComponent("winner-announcement", "<rainbow><bold>🎉 {player} won the game DuckyColorMix! 🎉</bold></rainbow>",
                            "{player}", winner.getName()));

                    // Start firework celebration
                    gameEffects.startFireworksForWinner(winner);

                    // Stop game after firework duration
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            stopGame();
                        }
                    }.runTaskLater(plugin, configManager.getFireworksDuration() * 20L);

                    cancel();
                    return;
                } else if (currentPlayers.isEmpty()) {
                    // No players left during the countdown!
                    Bukkit.broadcast(configManager.getMessageComponent("no-players-left", "<red>All players have left the game!</red>"));
                    stopGame();
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    // Remove all colors except the safe one
                    platformManager.removeUnsafeWool(safeColor);
                    gameEffects.playBlockRemovalSound();

                    Bukkit.broadcastMessage(configManager.getMessage("unsafe-colors-removed", "&cThe dangerous colors are gone!"));

                    // Check remaining players after block removal
                    List<Player> remainingPlayers = platformManager.getPlayersInGameList();

                    if (remainingPlayers.size() == 1) {
                        // One player won!
                        Player winner = remainingPlayers.getFirst();
                        Bukkit.broadcast(configManager.getMessageComponent("winner-announcement", "<rainbow><bold>🎉 {player} won the game DuckyColorMix! 🎉</bold></rainbow>",
                                "{player}", winner.getName()));

                        // Start firework celebration
                        gameEffects.startFireworksForWinner(winner);

                        // Stop game after firework duration
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                stopGame();
                            }
                        }.runTaskLater(plugin, configManager.getFireworksDuration() * 20L);

                        cancel();
                        return;
                    } else if (remainingPlayers.isEmpty()) {
                        // No one won this round
                        Bukkit.broadcast(configManager.getMessageComponent("no-winner", "<red>Nobody won this round!</red>"));
                        stopGame();
                        cancel();
                        return;
                    }

                    // Wait a moment and start new round
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (gameActive) {
                                // Stop previous action bar animation
                                gameEffects.stopActionBarAnimation();

                                // Decrease countdown time every specified number of rounds (minimum 1 second)
                                if (round % changeEveryRounds == 0 && currentCountdown > configManager.getMinimumCountdown()) {
                                    currentCountdown--;
                                }
                                startGameCycle();
                            }
                        }
                    }.runTaskLater(plugin, configManager.getRoundDelay());

                    cancel();
                    return;
                }

                // Notify about remaining time and play sounds
                if (timeLeft <= 5) {
                    Bukkit.broadcast(Component.text(String.valueOf(timeLeft))
                            .color(net.kyori.adventure.text.format.TextColor.color(255, 0, 0))
                            .decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
                    gameEffects.playCountdownSound(timeLeft);
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
}