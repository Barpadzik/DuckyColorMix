# DuckyColorMix Plugin for Minecraft 🦆

A competitive mini-game plugin for Paper 1.18.2+ featuring dynamic color-based survival gameplay with full Paper Adventure API support. 🎮

## Features ✨

- **🎨 Advanced Text Formatting** - Full Paper Adventure API support with hex colors, gradients, and rainbow effects
- **📝 MiniMessage Format** - Modern text formatting (`<red>`, `<gradient>`, `<rainbow>`)
- **⚡ Dynamic Gameplay** - Games can end instantly when only one player remains
- **⏱️ Configurable Countdown** - Decreasing countdown time with customizable intervals
- **👥 Real-time Player Tracking** - Counts players in the game area (5 blocks above platform)
- **🌈 Random Wool Colors** - Generates colorful platforms with random wool blocks
- **🛡️ Safe Color System** - One color remains safe while others disappear
- **🔄 Platform Regeneration** - Automatic platform reset between rounds and after games
- **🏆 Instant Win Detection** - Game ends immediately when one player remains
- **🎆 Winner Celebration** - 5-second fireworks display for the victor
- **⏸️ Pause/Resume System** - Full game state management
- **🔔 Update Checker** - Automatic GitHub release checking

## Commands 💻

| Command | Description | Permission |
|---------|-------------|------------|
| `/colormix start [seconds] [change_every_rounds]` | 🚀 Start a new game | `duckycolormix.colormix` |
| `/colormix stop` | 🛑 Stop the current game | `duckycolormix.colormix` |
| `/colormix pause` / `/colormix pause` | ⏸️ Pause the game | `duckycolormix.colormix` |
| `/colormix resume` / `/colormix resume` | ▶️ Resume the paused game | `duckycolormix.colormix` |
| `/colormix setplatform` | 🏗️ Set up the platform area | `duckycolormix.colormix` |
| `/colormix reload` | 🔄 Reload plugin configuration | `duckycolormix.colormix` |

**Aliases:** `/cm`, `/dcm` 🔗

## Installation 📦

**REQUIREMENTS:** Paper 1.16+ (Does NOT work on Spigot/Bukkit) ⚠️

1. Build the plugin using Gradle: 🔨
   ```bash
   ./gradlew shadowJar
   ```

2. Copy the JAR file from `build/libs/` to your Paper server's `plugins/` folder. 📁

3. Start/restart your server. 🔄

4. The plugin will generate a default configuration file. ⚙️

## Quick Start Guide 🚀

### 1. Set Up Platform 🏗️
```
/colormix setplatform
```
Click two opposite corners to define the platform area. 🖱️

### 2. Start a Game 🎮
```
/colormix start
/colormix start 10 2
```
- Default: 5 seconds countdown, decreases every 3 rounds ⏰
- Custom: 10 seconds countdown, decreases every 2 rounds 🎯

### 3. Game Controls 🎛️
```
/colormix pause    # ⏸️ Pause the game
/colormix resume   # ▶️ Resume the game
/colormix stop     # 🛑 Stop the game
```

## How to Play 🎯

1. **🏗️ Platform Setup** - Admin sets the platform area using `/colormix setplatform`
2. **🚀 Game Start** - Admin starts the game with customizable countdown settings
3. **🎨 Round Begins** - Platform fills with random colored wool blocks
4. **🛡️ Safe Color** - A random color is chosen as "safe" (displayed in action bar)
5. **⏱️ Countdown** - Players have limited time to stand on the safe color
6. **💥 Block Removal** - All unsafe colors disappear after countdown
7. **🏆 Win Conditions**:
   - Game ends instantly when only 1 player remains on platform ⚡
   - Winner gets 5-second fireworks celebration 🎆
   - If no players remain, game ends with no winner 😢
8. **🔄 Next Round** - If 2+ players remain, new round starts with shorter countdown

## Configuration ⚙️

The plugin features extensive configuration options in `config.yml`: 📝

### Game Settings 🎮
```yaml
game:
  default-starting-seconds: 5      # ⏰ Initial countdown time
  default-change-every-rounds: 3   # 📊 Rounds before countdown decreases
  minimum-countdown: 1             # ⏱️ Minimum countdown time
  round-delay: 60                  # ⏳ Delay between rounds (ticks)
  fireworks-duration: 5            # 🎆 Winner celebration duration
  fireworks-per-second: 5          # 🎇 Fireworks intensity
```

### Message Customization 💬
All messages support:
- **🔤 Legacy colors** (`&a`, `&c`, etc.)
- **🎨 Hex colors** (`#FF0000`, `#00FF00`, etc.)
- **✨ MiniMessage** (`<red>`, `<gradient:#FF0000:#00FF00>`, `<rainbow>`)

Example:
```yaml
messages:
  winner-announcement: "<rainbow><bold>🎉 {player} won DuckyColorMix! 🎉</bold></rainbow>"
  safe-color: "<gradient:#FFFF00:#FFD700>Safe color: <bold>{color}</bold></gradient>"
```

### Color Names 🌈
Customize color names for different languages:
```yaml
colors:
  WHITE: "WHITE"
  RED: "RED"
  BLUE: "BLUE"
  # ... etc
```

## Permissions 🔐

| Permission | Description | Default |
|------------|-------------|---------|
| `duckycolormix.colormix` | 🎮 Manage DuckyColorMix games | `op` |
| `duckycolormix.update` | 🔔 Receive update notifications | `op` |

## Technical Details 🔧

### Game Area Detection 📍
- Players are considered "in game" when within platform boundaries 🏗️
- Detection area extends 5 blocks above the platform 📏
- Real-time player counting every game tick ⚡

### Platform System 🏗️
- Supports any rectangular area 📐
- Automatic wool generation with 16 different colors 🌈
- Smart block removal system 💥
- Platform regeneration between rounds 🔄

### Performance ⚡
- Optimized for Paper API 🚀
- Minimal server impact 💪
- Efficient player detection 🎯
- Asynchronous update checking 🔄

## API Information 🛠️

### Paper Adventure API 📚
This plugin fully utilizes Paper's Adventure API for:
- Component-based text handling 📝
- Native hex color support 🎨
- MiniMessage parsing ✨
- Action bar animations 📊
- Modern text formatting 💫

### Compatibility 🔗
- **Minimum:** Paper 1.18.2 📋
- **Recommended:** Paper 1.19+ ⭐
- **Java:** 21+ ☕
- **NOT compatible** with PlugMan(and forks) ❌

## Support & Updates 🆘

### Automatic Updates 🔄
The plugin automatically checks for updates on startup and notifies administrators about new releases. 📢

### Manual Update Check 📥
Updates are available on the [GitHub Releases](https://github.com/Barpadzik/DuckyColorMix/releases) page. 🔗

### Issues & Suggestions 🐛
Report issues or suggest features on the [GitHub Issues](https://github.com/Barpadzik/DuckyColorMix/issues) page. 💡

## License 📄

This project is licensed under the GNU General Public License - see the [LICENSE WIKI](https://en.wikipedia.org/wiki/GNU_General_Public_License) site for details. ⚖️

## Credits 👨‍💻

**Author:** Barpad 🧑‍💻  
**Version:** 1.0.0 🏷️  
**API:** Paper Adventure API 📚  
**Minecraft Version:** 1.18.2+ 🎮

---

*DuckyColorMix - Where colors decide your fate!* 🎨🏆🦆
