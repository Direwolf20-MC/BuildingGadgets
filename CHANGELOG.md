# Building Gadgets Changelog
The format of this document is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and should continue to adhere to the conventions outlined in the Keep a Changelog guidelines.

## [Unreleased]
### Fixed
- Drastically improve Performance of Destruction Gadget when moving the mouse a lot within a small space of time.

## [1.6.2] - 2018-07-28
### Changed
- Exchanger and Builder(During Undo) now properly fire BlockBreakEvents for modders to cancel if need be. 

## [1.6.1] - 2018-07-26
### Added
- Added a config option to allow the tools to take item durability damage (Like vanilla tools) instead of using Forge Energy. 
  - Tools will never break.
  - Can be fully repaired with a single diamond in an anvil. 
  - Can accept the normal suite of tool enchants (Unbreaking/Mending/etc).

## [1.5.1] - Date of release in [yyyy-mm-dd]
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

## [1.1.5] - 2018-07-19
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
###Added
- Added a few more languages (Thanks Pull request people!)
- Added localization to a lot of the messages / tooltips
- Added support for tools in the offhand
### Changed
- Changed Default hotkey (Mode Change) to not conflict with vanilla
- Better Item Picking for the silk touch method
### Deprecated
- Example Line
### Removed
- Removed effectBlock's item registration (No longer an item form of the block)
### Fixed
- Proper doubleslab support (Fixes dupe bug)
- BlockBuildEntity now stores it's state on chunk unload / world exit (Should prevent effect blocks being left behind on world exit)
- Minor Undo improvement (If blocks no longer exist, they are still properly removed from the undo list)

## [1.1.2] - 2018-07-15
###Added
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

## [Full Version] - Date of release in [yyyy-mm-dd]
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
