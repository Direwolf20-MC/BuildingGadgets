<div style="padding: 3em; text-align:center;">
	<img width="60" src="https://i.imgur.com/hFRs7eN.png" />
	<h1 style="margin-top: 20px; border-bottom: 0;">Building Gadgets</h1>
    <p style="color: rgba(0, 0, 0, .5); margin-bottom: 2em;">Sometimes, building large structures can be a little tedious, and take a lot of effort. Not all of us are great builders you know!
    </p>
    <p>
        <img src="http://cf.way2muchnoise.eu/full_298187_downloads.svg" />
    </p>    
</div>

<h2 style=" border-bottom: 0; text-align: center;">Welcome to Dire's Building Gadgets!</h2>

`Dire's Building Gadgets` aims to make building a little bit easier. At this time there are `four` tools, and they are described below. Alternatively, watch the following mod spotlight for instructions!

---

- [Usage](#usage)
- [Gadgets](#gadgets)
  * [Building Gadget](#building-gadget)
    + [Builder Modes](#builder-modes)
  * [Exchanger Tool](#exchanger-tool)
    + [Exchanger Modes](#exchanger-modes)
  * [Construction Paste](#construction-paste)
  * [Copy Paste Tool](#copy-paste-tool)
    + [Copy Mode](#copy-mode)
    + [Paste Mode](#paste-mode)
  * [Template Manager](#template-manager)

---

### Usage
[Spotlight](https://youtu.be/D4Ib4h7aTSk), [Spotlight Pt2](https://youtu.be/JS1Xx_kwQQ0)

Press the `Mode Switch` hotkey to switch modes.

Press the `Range Change` hotkey to increase the range (Sneak+Hotkey decrements the range). 

Press the `Anchor` hotkey to stick the visual indicator to its current position, so you can see what your build will look like before commiting. With an anchor active, right click to build the anchor, or press the hotkey again to remove the anchor without building.

Press the `Undo` hotkey to undo the last build you executed (The tool remembers up to 10 previous builds) -- Not Available on the Exchanger tool. 

### Gadgets

#### Building Gadget

The building gadget allows you to build structures at range. Simple right click to place the blocks, and sneak-right click to set the block type to place. 

##### Builder Modes

 - `Build to Me:` Build from the block  and blockface you're looking at to the players current position. This is the only mode that does not use the `Range` modifier, as it always builds to the player's current position. 

 - `Vertical Column:` Build a vertical column of blocks of height equal to the `range` modifier.  If you look at the Top of a blockface it builds up, bottom of the blockface it builds down, and side of a blockface builds half of the blocks above, and half below. 

- `Horizontal Column:` Otherwise known as a row. Builds Horizontally away from the blockface you're looking at.  If you look at the top/bottom of a block, it builds in the direction your player is looking. 

- `Vertical Wall:` Builds a wall of size Range x Range (Example: 3x3, 5x5, etc). Like the vertical column, looking at the top of a block builds up, bottom of a block builds down, and middle of a block builds around it. 

- `Horizontal Wall:` Builds a wall of blocks towards the player off of the blockface the player is looking at. If looking at the top/bottom of a block, it builds around that block. 

- `Stairs:` Will build a set of stairs. If the block you are looking at is above you, the stairs will be built downwards and towards the player. If the block you look at is below you, they are built upwards away from the player. 
- `Checkerboard: ` Otherwise known as the Torch mode, this mode will build in a checkered pattern allowing you to place in the pattern of `[]-[]-[]-[]` `[]: being blocks` and `-: being air`.

#### Exchanger Tool

Will swap the blocks you are looking at with the block the tool is set to. Simple right click to swap the blocks, and sneak-right click to set the block type to place. 

##### Exchanger Modes

- `Wall:` -  Exchanges in a Range x Range (Example: 3x3 or 5x5), around the block you're looking at. It will only exchange blocks that match the block you are looking at. 

- `Horizontal Column:` - Horizontal with respect to the players look vector. This will be a straight line typically left and right of the block you're looking at. 

- `Vertical Column:` - Vertical with respect to the players look vector. This will be a straight line typically `up` and `down` of the block you're looking at.
- `Checkerboard: ` this mode will build in a checkered pattern allowing you to place in the pattern of `[]-[]-[]-[]` `[]: being blocks` and `-: being air`.

#### Construction Paste

This material can mimic the appearance of any other block, but cannot perform the same functions (Does not give off light, no redstone signals, etc).

First, craft Construction Paste Powder, and place it next to water, over 4 seconds it will transform into Construction Paste. Break this to get the item you need.

A construction paste container exists that can hold a large amount of construction paste.

All tools will attempt to build using the 'real' material first, and use construction paste if the real blocks are unavailable in your inventory.

#### Copy Paste Tool
Will allow you to copy an area of max size 125x125x125, or max 32,768 blocks, and paste it somewhere else.

##### Copy Mode
Right click on the 'starting' block you'd like to copy. This will be where the paste of this structure is anchored at.

Shift-Right click the opposing corner to form a cube. All blocks within the cube'd area will be copied to the tool.

Shift-Right Click on empty space to open a small UI that allows you to manually adjust the copied area

##### Paste Mode

Hold shift over the item in your inventory to expand the tooltip and see the blocks required to paste the current area

Anchor mode works as usual.

Use the 'range' hotkey to rotate while in paste mode.

Right click to paste the structure into the world -- it will use blocks / paste from your inventory.

#### Template Manager

Place a copy/paste tool in the left hand slot, and a piece of paper (vanilla) in the right hand slot. Click 'save' to write the structure stored on the tool to the paper, which turns into a 'template' item.
Optionally give it a name using the text box at the top of the UI

Click 'Load' to transfer the structure from the template item to the Copy/Paste tool.

Click 'Copy' to copy the structure on the tool (left slot) onto your computer's clipboard. This is a text string that can be shared with friends on forums/messageboards/pastebin/etc.

Place a new piece of paper in the right slot of this UI, and click 'paste' to transfer the contents of your computers clipboard, and turn the paper into a template. You may now load this template onto a tool as before.

Use this to share buildings with your friends!

---

[License MIT](License.md)
