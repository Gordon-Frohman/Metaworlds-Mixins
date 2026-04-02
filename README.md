# MetaWorlds (Mixins Version)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Gordon-Frohman/Metaworlds-Mixins)

A Minecraft 1.7.10 Forge mod that allows creating fully functional SubWorlds - embedded world instances that can be moved, rotated, and scaled within the main world.

## Features

- **SubWorld Creation**: Create independent world instances that exist as objects in the parent world
- **Full Transformations**: Move, rotate, and scale SubWorlds in 3D space
- **Entity Proxy System**: Players can interact with multiple worlds simultaneously through proxy entities
- **Mod Integration**: Extensive compatibility with major mods via conditional Mixins
- **Network Synchronization**: Custom packet system for multiplayer support
- **Configuration Options**: Customizable control schemes and rotation modes

## Installation

1. **Requirements**:
   - Minecraft 1.7.10
   - Forge 10.13.4.1614 or compatible
   - UniMixins

2. **Download**: Get the latest release from the [Releases](https://github.com/Gordon-Frohman/Metaworlds-Mixins/releases) page

3. **Install**: Place the JAR file in your `mods` folder

## Quick Start

1. **Create a SubWorld**: Place a `Blank SubWorld Creator` block.
2. **Add Controls**: Place a `SubWorld Controller` block inside the SubWorld and right-click it.
3. **Move the World**: Use movement keys (WASD or arrows) to translate, NUMPAD keys to rotate
4. **Scale**: Use `Supersizing Block` or `Miniaturization Block` to change size.

## Configuration

The mod creates a configuration file with these options:.

| Option | Default | Description |
|--------|---------|-------------|
| `usePlayerControls` | `true` | Use standard player controls (WASD) or custom keybindings |
| `viewBasedRotation` | `false` | Experimental view-based rotation control |

## Controls

### Player Controls Mode (Default)
- **Movement**: WASD keys
- **Up**: Space key
- **Down**: Sprint key
- **Rotation**: NUMPAD 8/2/4/6 (Roll)

### Custom Controls Mode
- **Movement**: Arrow keys
- **Up**: NUM+ key
- **Down**: NUM- key
- **Rotation**: NUMPAD 8/2/4/6

## Mod Compatibility

MetaWorlds includes conditional Mixins for compatibility with:

- Angelica (rendering optimization)
- ForgeMultipart (microblocks)
- LittleTiles
- TerraFirmaCraft / TFC+
- GregTech 6
- WarpDrive
- WAILA / NEI
- And more...

## Commands

- `/mwc` - Opens administration GUI
- `/setblockinsubworld <ID> <x> <y> <z> <Tile>` - Place blocks in specific SubWorlds
- `/tpworlds` - Teleport all SubWorlds to your location

## Building from Source

1. **Prerequisites**: Java 8+ (Jabel enables modern syntax)

2. **Clone**:
   ```bash
   git clone https://github.com/Gordon-Frohman/Metaworlds-Mixins.git
   cd Metaworlds-Mixins
   ```

3. **Build**:
   ```bash
   ./gradlew build
   ```

4. **Run** (for development):
   ```bash
   ./gradlew runClient
   ```

## Technical Details

### Architecture
- Uses Mixins to inject functionality into Minecraft classes
- Shadowed dependencies: JOML (math library) and Commons Math
- Custom packet system for SubWorld synchronization

### Key Components
- `SubWorld` - Core world instance class
- `EntitySubWorldController` - Control entity for steering SubWorlds
- `IMixinWorld` - Interface injected into World class for MetaWorlds functionality

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

### Integration requests

Since MetaWorlds is implementing hell lot of mixins, it may be incompatible with a lot of other mods, especially ones using custom package systems.
Therefore I have to write an integration patch for every such mod. If you want me to fix a crash or any other kind of issue with another mod - please open an issue with crash report and/or issue description.

Subworlds are also using a custom system for block rotation on reintegration of a rotated world, which requires every block type to be registered separately.
If you want blocks from another mod to be rotated - please provide a full list of such blocks and (preferrably) all the rotations each block can take.
