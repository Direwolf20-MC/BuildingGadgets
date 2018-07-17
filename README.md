# Building Gadgets
Welcome to Dire's Building Gadgets!

Sometimes, building large structures can be a little tedious, and take a lot of effort. Not all of us a great builders you know!

Dire's Building Gadgets aims to make building a little bit easier. At this time there are two tools, and they are described below. Alternatively, watch the following mod spotlight for instructions!

[Spotlight](https://youtu.be/D4Ib4h7aTSk)

Press the `Mode Switch` hotkey to switch modes.

Press the `Range Change` hotkey to increase the range (Sneak+Hotkey decrements the range). 

Press the `Anchor` hotkey to stick the visual indicator to its current position, so you can see what your build will look like before commiting. With an anchor active, right click to build the anchor, or press the hotkey again to remove the anchor without building.

Press the `Undo` hotkey to undo the last build you executed (The tool remembers up to 10 previous builds) -- Not Available on the Exchanger tool. 

### Building Gadget

The building gadget allows you to build structures at range. Simple right click to place the blocks, and sneak-right click to set the block type to place. 

#### Builder Modes

Build to Me - Build from the block  and blockface you're looking at to the players current position. This is the only mode that does not use the `Range` modifier, as it always builds to the player's current position. 

Vertical Column - Build a vertical column of blocks of height equal to the `range` modifier.  If you look at the Top of a blockface it builds up, bottom of the blockface it builds down, and side of a blockface builds half of the blocks above, and half below. 

Horizontal Column - Otherwise known as a row. Builds Horizontally away from the blockface you're looking at.  If you look at the top/bottom of a block, it builds in the direction your player is looking. 

Vertical Wall - Builds a wall of size Range x Range (Example: 3x3, 5x5, etc). Like the vertical column, looking at the top of a block builds up, bottom of a block builds down, and middle of a block builds around it. 

Horizontal Wall - Builds a wall of blocks towards the player off of the blockface the player is looking at. If looking at the top/bottom of a block, it builds around that block. 

Stairs - Will build a set of stairs. If the block you are looking at is above you, the stairs will be built downwards and towards the player. If the block you look at is below you, they are built upwards away from the player. 

### Exchanger Tool

Will swap the blocks you are looking at with the block the tool is set to. Simple right click to swap the blocks, and sneak-right click to set the block type to place. 

#### Exchanger Modes

Wall -  Exchanges in a Range x Range (Example: 3x3 or 5x5), around the block you're looking at. It will only exchange blocks that match the block you are looking at. 

Horizontal Column - Horizontal with respect to the players look vector. This will be a straight line typically left and right of the block you're looking at. 

Vertical Column - Vertical with respect to the players look vector. This will be a straight line typically `up` and `down` of the block you're looking at.
