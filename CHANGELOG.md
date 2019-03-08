# Building Gadgets Changelog
The format of this document is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and should continue to adhere to the conventions outlined in the Keep a Changelog guidelines.

## [Unreleased]
### Added
- The copy/paste gadget can now mirror pastes, as well as rotate them. Sneak while pressing the keybind that previously just rotated them to mirror them left-to-right instead.
- Allowed gadgets to bind Simple Storage Network networks as a remote inventories (you can only bind the Storage Network Master block).
- Dense construction blocks now drop 4 items (configurable), with a 50:50 chance (configurable) of a given item being construction paste or a dense construction chunk. Without other mods, the chunks are just useless byproduct.
- The process of generating construction paste is now as follows:
    1. Craft a construction powder block.
    2. Convert the powder into a dense construction block by either placing it in the word next to water, or using one of the following machines:
        - Fluid Transposer *[Thermal Expansion]*
        - Hydrator *[Cyclic]*    
        - Moistener *[Forestry]*
    3. Convert the block into dense construction chunks either by breaking it (also getting paste directly), or using one of the following machines (1 block -> 4 chunks):
        - Pulverizer *[Thermal Expansion]*
        - Macerator *[Industrial Craft 2]*
        - Grinder *[Applied Energistics 2]*
        - Crusher *[Mekanism]*
        - Crusher *[Immersive Engineering]*
        - Crusher *[Actually Additions]*
    4. Convert the chunks into paste using any of the machines listed in step 3 (1 chunk -> 1 paste).

### Changed
- Remote networks are now only wrapped once build/exchange/undo operation, rather than twice per placed block.

## [2.6.6] - 2019-02-27
### Added
- Allowed gadgets to bind Applied Energistics 2 networks as a remote inventories.
- Added gadget modes for:
    - [all gadgets] raytracing fluids
    - [building gadget] placing in or on blocks
- More German localisation

### Fixed
- Refined Storage network IO now respects security permissions.
- Fixed destruction overlay rendering bug when in F1 mode.
- When in connected area mode, the destruction gadget now does nothing when set to 0 depth, just as it does otherwise.
- Allowed destruction overlay render cache to update when the side of the observed block changes.

## [2.6.5] - 2019-02-22
### Changed
- In addition to extracting items from bound Refined Storage networks when using the building/exchanging/copy-paste gadgets, items can now also be inserted into them when using, and undoing the use of, the building/copy-paste gadgets.
- The Template Manager now displays a special Error-Message if someone tries to paste a link instead of JSON-Data

## [2.6.4] - 2019-02-17
### Fixed
- Drastically improved rendering performance of Destruction Gadget in Connected Area mode when set to a large area

## [2.6.3] - 2019-02-11
### Fixed
- Fixed server crashing on startup due to beep sound being unavailable server side
- Fixed the yellow box rendering around chests to always render if the tool is in your hand, not only when other renders are occurring (Like if you're looking at nothing).

## [2.6.2] - 2019-02-12
### Note
- The Performance of the DestructionGadget was very poor when being in the (by default active) connected-Blocks mode. You can always disable that by assigning a corresponding keybinding and toggling it to inactive.
### Added
- Adds surface mode to the building gadget, and renames the exchanging gadget's 'wall' mode to 'surface' for consistency.
- Adds means of setting connected area mode for building/exchanging/destruction gadgets (referred to as 'connected surface' for the building/exchanging gadgets), and makes it default to true.
    - Given the above, the newly added 'fuzzy' and 'connected area' keybinds are set to NONE, so as to reduce excessive conflicts that modpack users will have to resolve.
- Replaced standard MC 'click' sounds with custom 'beep' sounds for GUI operations.
- Adds togglable buttons to the GUI to change values currently only toggled with keybinds.
- Yellow box around bound inventories
- Shift Right Click an existing bound inventory to un-bind it.
### Changed
- Applies fuzzy mode to the building/destruction gadgets, and separates out the fuzzy mode toggling from the undo keybind into its own keybind.
- Allows clicking a mode in the radial menu GUI to change it without having to close the GUI.
- Allows the slice in the GUI for the gadget's currently selected mode to render green with green text.
- Changes the GUI circle to a torus, and requires the cursor to be on its surface to select a mode.
### Fixed
- Fixes a longstanding bug where relative up/down directions are inverted when interacting with the bottom sides of blocks.

## [2.6.1] - 2019-02-06
### Added
- Added the ability to bind inventories to Refined Storage Interfaces. 
### Fixed
- Misc bugfixes. 
- Fixes a possible crash, when changing the powered By FE energy setting whilst being in a world
- Fixes energy not updating when the Max Energy setting is changed in Game

## [2.6.0] - 2019-02-01
### Note
Please let me know if you run into anything weird!
### Added
- Bind any tool to a chest - Sneak-Right click any tool on any chest (Including modded chests) to bind it. The tool will pull items out of that chest now. Thanks @Phylogeny for helping with the rendering portions of this!!
- Updated configs to the new forge way, which means you can change your config options from in-game. Go to the mod options button and search for building gadgets. Thanks @MajorTuvok for this PR! :)
    - Note that as Configs are now synced from Server to Client changes applied on a MP Server probaply 
### Fixed
- Misc bug fixes. 

## [2.5.0] - 2018-12-29
### Changed
- It also features a rather significant refactor effort made by the community. Shoutout to MichaelHillcox and Phylogeny and MajorTuvok for all the help with suggestions and making the mod cleaner and easier to read! :) I flagged this as beta due to the fact there are MAJOR refactors in the code. I did a good amount of testing and didn't find too many bugs, but I wanted to flag it as beta until its been tested a bit more :).
### Fixed
- This release sees a few minor bug fixes and issues with the copy tool failing to copy an area. 

## [2.4.6] - 2018-11-12
### Note
- This version contains a crash possibilty, when the newly added UI is faced by empty values!
### Added
- Added a UI that lets you change the Paste offset position. Shift-right click the Copy/Paste tool while in Paste mode. 
### Fixed
- Misc bug fixes.

## [2.4.5] - 2018-11-06
### Fixed
- More better handling of destruction rendering, thanks Phylogeny! (Far less of an FPS hit)

## [2.4.4] - 2018-11-06
### Changed
- Increased RF Capacity of destruction tool to 1 million RF. This is required to void a max size area. 
### Fixed
- Improved the FPS hit caused by a very large (16x16x16) destruction tool render. Reminder: If this bothers you, you can press the 'range' hotkey to hide the render completely, and the tool will still work. 
- Minor crash fixes (XNet cable rendering). 

## [2.4.3] - 2018-10-29
### Changed
- Copy/Paste from the template manager buttons give a message to the player on success as well as fail. 
### Fixed
- Fixed a server side crash with the destruction gadget. 

## [2.4.2] - 2018-10-26
### Added
- Added a config option to prevent overwriting blocks like a player can (Water/lava/tall grass, etc) - At the request of Darkosto for evil modpack reasons.
### Fixed
- DoubleSlab blocks not copy/pasting variants properly.
- Trying to build below Y=0 wouldn't build, but took items from your inventory. This no longer happens.

## [2.4.1] - 2018-10-24
### Added
- Added a config option to completely disable the destruction tool, in case you don't want it available on your server for example :).

## [2.4.0] - 2018-10-24
### Added
- This version adds the Destruction Gadget:
    - Shift-Right click to bring up a GUI to set the area that will be destroyed. All blocks will be VOIDED - meaning they don't drop items. Use at your own risk to clear areas. This tool is not meant to be used for mining, so no item drops. Sorry!
    - Use the 'range change' hotkey to hide the overlay (red blocks) showing what will be voided. This tool has a much larger area available than other tools and may cause some lag on older machines if voiding a very large area. If this happens, just hide the overlay. 
    - Max void area size is 16x16x16. 
    - Use the 'Undo' hotkey to undo the last void you did. This tool only remembers ONE iteration of voiding. 

## [2.3.8] - 2018-10-23
### Changed
- Code Cleanup and Nicer Template Manager UI by Phylogeny

## [2.3.6] - 2018-10-07
### Added
- Added tiered construction paste containers
    - Right click any construction paste container to absorb all paste from your inventory (Handy for putting lots of paste into a container at once).
### Fixed
- Fixed lighting bugs with half-slabs and construction paste. 
- Misc minor fixes

## [2.3.5] - 2018-10-01
### Added
- Added a GUI to the Copy/paste tool -- Shift Right Click on empty air to access it.
- Added redstone repeat delay state to the allowed blockstates for copying. 

## [2.3.4] - 2018-09-30
### Note
- Making this beta so more people test it :). Let me know if you run into any bugs please, by reporting them on the Github!
### Changed
- Template Manager only allows 1 piece of paper at a time. 

## [2.3.3] - 2018-09-25
### Changed
- Construction Paste chisel blocks can now properly connect with Real chisel blocks (Connected textures) and vice versa. - Thanks TTerrag!
### Fixed
- Fixed glass rendering

## [2.3.1] - 2018-09-23
### Added
- Added recipe for the Template Manager

## [2.3.0] - 2018-09-23
### Note 
- __EARLY ALPHA -- USE AT YOUR OWN RISK -- Make world backups__
### Added
- Implemented the Template Manager

## [2.2.1] - 2018-08-24
### Note
- __WARNING: USE AT YOUR OWN RISK!__ If you opt to test this __ALPHA__ version, please report any bugs you find on Github, thanks!
- Note that none of the textures are final. 
### Added
- Copy Paste Tool
    - Use mode change to switch between copy/paste mode
    - In copy mode:
        - Right click on the 'first' block in your area. Shift right click on the 2nd block. A box is drawn around the area you've copied. 
        - Note its best to go from bottom to top of an area when doing this due to how paste works. Planned to be changed. 
        - Max size is 125x125x125 or 32,768 non-air blocks
    - In Paste Mode:
        - Right click to paste the blocks where they are rendered (One block up from the block you're looking at). 
        - Press <range change hotkey>  to rotate the blocks
        - Press <Anchor hotkey>  to anchor the blocks to the block you're looking at (Like the building gadget)
        - Press <Undo hotkey> to undo the last build you did (Only remembers 1, unlike the building gadget which remembers 10). 

## [2.1.0] - 2018-08-15
### Added
- Added a config option to disable paste all together.
### Fixed
- Fixed a crash bug or two. 

## [2.0.0] - 2018-08-10
### Note
- This is an __ALPHA release__ of Building Gadgets v2.0 -- Please report any bugs you find :). 
- Note that textures are temporary.
### Added
- Added Construction Paste:
    - Make construction powder, and place it in world next to water, over 4 seconds it will harden into a construction block.
    - Break the construction block to receive Construction Paste
    - Construction paste can be used to build any block (Non-Tile Entity) instead of the real block.
    - Some attributes like light opacity and bounding box are copied to the construction paste block
    - Some are not - Like light / redstone output

## [1.6.2] - 2018-07-27
### Changed
- Exchanger and Builder(During Undo) now properly fire BlockBreakEvents for modders to cancel if need be. 

## [1.6.1] - 2018-07-26
### Added
- Added a config option to allow the tools to take item durability damage (Like vanilla tools) instead of using Forge Energy. 
  - Tools will never break.
  - Can be fully repaired with a single diamond in an anvil. 
  - Can accept the normal suite of tool enchants (Unbreaking/Mending/etc).

## [1.5.1] - 2018-07-25
### Added
- Added radial menu for Mode Select. 
### Removed
- Can no longer exchange air/liquids using fuzzy mode. 

## [1.4.0] - 2018-07-24
### Added
- Added an Energy Cost to the tools (Configs to change the cost/max storage). 
### Changed
- Significant refactor of a lot of code, please let me know if I introduced any bugs :). Flagged as Alpha due to refactor and new energy stuff. 

## [1.3.0] - 2018-07-20
### Added
- Add support for the Dank/Null and other inventory containers:
  - The undo function and exchanger tool will insert into the dank/null (if appropriate) but only up to a max stacksize inside the dank/null of 64. The Dank/Null mod needs to implement a change to fix this. For now, it'll insert up to 64, and the rest will go into your inventory. Extracting from the dank null works fine. 

## [1.2.0] - 2018-07-19
### Added
- Added 'Checkerboard' mode to both the builder and exchanger.
- Added 'Fuzzy mode' to the exchanger (will swap blocks that don't match the block you're looking at if true).

## [1.1.5] - 2018-07-18
### Changed
- More better BlockState copying
- Plants are allowed again. 
### Fixed
- Effect block won't un-till earth. 

## [1.1.4] - 2018-07-18
### Added
- Lang File updates (Thanks Everyone who submitted one!)
### Changed
- Plants are now blacklisted - Sorry Soaryn!
- Better State handling -- Places blocks with many default properties, and only copies certain properties like facing/etc. 
- Better Undo block breaking
### Fixed
- Minor Z-Fighting fix

## [1.1.3] - 2018-07-17
### Added
- Added a few more languages (Thanks Pull request people!)
- Added localization to a lot of the messages / tooltips
- Added support for tools in the offhand
### Changed
- Changed Default hotkey (Mode Change) to not conflict with vanilla
- Better Item Picking for the silk touch method
### Removed
- Removed effectBlock's item registration (No longer an item form of the block)
### Fixed
- Proper doubleslab support (Fixes dupe bug)
- BlockBuildEntity now stores it's state on chunk unload / world exit (Should prevent effect blocks being left behind on world exit)
- Minor Undo improvement (If blocks no longer exist, they are still properly removed from the undo list)

## [1.1.2] - 2018-07-15
### Added
- Initial release

---
# Example
Types of changes include: 
- `Added` for new features.
- `Changed` for changes in existing functionality.
- `Deprecated` for soon-to-be removed features.
- `Removed` for now removed features.
- `Fixed` for any bug fixes.
- `Security` in case of vulnerabilities.

Please do not include your own or edit the heading of these. If something somehow does not fit into these types then add a `Note:` to the top of your version heading. 

## [Full Version] - Date of release in Eastern Standard Time [yyyy-mm-dd]
### Added
- Example Line
### Changed
- Example Line
### Deprecated
- Example Line
### Removed
- Example Line
### Fixed
- Example Line
### Security
- Example Line
