package pl.barpad.duckycolormix.game;

import pl.barpad.duckycolormix.Main;
import pl.barpad.duckycolormix.config.ConfigManager;
import pl.barpad.duckycolormix.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Random;

/**
 * GameEffects handles all visual and audio effects for the DuckyColorMix game.
 * This includes action bar animations, firework celebrations, and sound effects.
 * The class manages timed effects and provides methods for creating engaging player experiences.
 *
 * @author Barpad
 * @version 1.0.0
 */
public class GameEffects {

    private final Main plugin;
    private final ConfigManager configManager;
    private final PlatformManager platformManager;

    private BukkitTask actionBarTask = null;
    private BukkitTask fireworkTask = null;
    private final Random random = new Random();

    // Available firework colors for celebrations
    private static final Color[] FIREWORK_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.PURPLE, Color.ORANGE, Color.ORANGE, Color.LIME
    };

    /**
     * Constructor for GameEffects.
     * Initializes the effects manager with required dependencies.
     *
     * @param plugin The main plugin instance
     * @param configManager The configuration manager for settings and messages
     * @param platformManager The platform manager for player detection
     */
    public GameEffects(Main plugin, ConfigManager configManager, PlatformManager platformManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.platformManager = platformManager;
    }

    /**
     * Starts an animated action bar display for players in the game.
     * The action bar shows the safe color with pulsating star animations using Paper API.
     * Only players within the game area will see the action bar.
     *
     * @param safeColor The DyeColor that is safe for this round
     */
    public void startActionBarAnimation(DyeColor safeColor) {
        // Stop any existing action bar animation
        stopActionBarAnimation();

        Component formattedColorName = configManager.getFormattedColorNameComponent(safeColor);

        actionBarTask = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                List<Player> playersInGame = platformManager.getPlayersInGameList();
                if (playersInGame.isEmpty()) {
                    return;
                }

                // Create an animated action bar message using Components
                Component stars = getAnimatedStars(tick);
                Component actionBarMessage = Component.empty()
                        .append(stars)
                        .append(Component.text(" "))
                        .append(configManager.getMessageComponent("action-bar-format", "<gold><bold>SAFE COLOR: {color}</bold></gold>")
                                .replaceText(builder -> builder.matchLiteral("{color}").replacement(formattedColorName)))
                        .append(Component.text(" "))
                        .append(stars);

                // Send action bar to all players in the game area
                for (Player player : playersInGame) {
                    // Send action bar using Paper API - much simpler and better hex support!
                    player.sendActionBar(actionBarMessage);
                }

                tick++;
                if (tick >= 40) tick = 0; // Reset animation cycle every 2 seconds
            }
        }.runTaskTimer(plugin, 0L, 2L); // Update 10 times per second for smooth animation
    }

    /**
     * Stops the current action bar animation if one is running.
     * This method should be called when round end or the game stops.
     */
    public void stopActionBarAnimation() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
    }

    /**
     * Generates animated star patterns for the action bar using Components.
     * Creates a pulsating effect with different star symbols and colors using Paper API.
     *
     * @param tick The current animation tick (used to determine animation phase)
     * @return A Component with colored star symbols
     */
    private Component getAnimatedStars(int tick) {
        // Create a pulsating animation cycle over 20 ticks (1 second)
        int phase = tick % 20;

        if (phase < 5) {
            return ColorUtils.hexColor("#FFFF00", "★★★");
        } else if (phase < 10) {
            return ColorUtils.hexColor("#FFAA00", "✦✦✦");
        } else if (phase < 15) {
            return ColorUtils.hexColor("#FFFF00", "✧✧✧");
        } else {
            return ColorUtils.hexColor("#FFAA00", "❋❋❋");
        }
    }

    /**
     * Starts a firework celebration for the winning player.
     * Creates multiple colorful fireworks around the winner for a specified duration.
     * The fireworks have random colors and spawn at random locations near the player.
     *
     * @param winner The player who won the game
     */
    public void startFireworksForWinner(Player winner) {
        // Stop any existing fireworks
        stopFireworks();

        int duration = configManager.getFireworksDuration();
        int perSecond = configManager.getFireworksPerSecond();
        int totalFireworks = duration * perSecond;

        fireworkTask = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= totalFireworks) {
                    cancel();
                    return;
                }

                // Create firework at random position around the winner
                Location loc = winner.getLocation().clone();
                loc.add(
                        (random.nextDouble() - 0.5) * 6, // Random X offset (-3 to +3)
                        random.nextDouble() * 3 + 1,     // Random Y offset (1 to 4)
                        (random.nextDouble() - 0.5) * 6  // Random Z offset (-3 to +3)
                );

                Firework firework = (Firework) winner.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                FireworkMeta meta = firework.getFireworkMeta();

                // Create firework effect with random color
                Color randomColor = FIREWORK_COLORS[random.nextInt(FIREWORK_COLORS.length)];
                FireworkEffect effect = FireworkEffect.builder()
                        .withColor(randomColor)
                        .with(FireworkEffect.Type.BALL_LARGE)
                        .withFlicker()
                        .withTrail()
                        .build();

                meta.addEffect(effect);
                meta.setPower(1);
                firework.setFireworkMeta(meta);

                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L / perSecond); // Schedule based on fireworks per second
    }

    /**
     * Stops the current fireworks celebration if one is running.
     * This method should be called when cleaning up effects.
     */
    public void stopFireworks() {
        if (fireworkTask != null) {
            fireworkTask.cancel();
            fireworkTask = null;
        }
    }

    /**
     * Plays countdown sound effects for all online players.
     * Creates an audible countdown that helps players prepare for the round end.
     *
     * @param timeLeft The number of seconds remaining in the countdown
     */
    public void playCountdownSound(int timeLeft) {
        // Play sound for the final 5 seconds of countdown
        if (timeLeft <= 5) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
        }
    }

    /**
     * Plays the explosion sound effect when unsafe blocks are removed.
     * This provides audio feedback when the round ends and blocks disappear.
     */
    public void playBlockRemovalSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
    }

    /**
     * Stops all currently running effects.
     * This method should be called when the game ends or is stopped.
     */
    public void stopAllEffects() {
        stopActionBarAnimation();
        stopFireworks();
    }
}