# Building Gadgets Spec
In an attempt to make 1.14/1.15 and future versions inline with our original 1.12 version.
To do this, I've outlined every feature specification for the mod below, when extending, improving,
or rewriting any features from below, the implementation MUST meet at the very least the spec below.

This is not a complete over view of every feature, instead it is a complete description of the core features of every part of the mod. Over time this will grow as more features become core aspects of the mod.

## The Gadgets
All gadgets follow the following specification unless stated otherwise below: 
- The are powered gadgets meaning they extend or implement the forge built `CapabilitityEnergy`
    - They can not be used as a power sourcing meaning the implementation needs to be modified to support this
    - They can be charged at any rate from 1rf/t to ♾rf/t we do not restrict it's input
    - The gadgets should show a power bar
    - There should be a tooltip with the power taking top priority meaning it's as close to the top as possible.
        - It should be the color of gray
- Each gadget has modes (excluding the destruction gadget)
    - These modes are selected either through a toggle cycle or from a gui menu (specified below)
    - Their implementation should be a simplistic and pure as possible.
    - Each mode should be easy to understand
    - The selected mode should be the second priority item in the tooltip
    - If no mode is selected on the gadget should default to the first in it's logical list and not default to none.
    - The modes use a selected block which is captured using `shift+right click` on a block.
        - The selected block should be shown under it's mode in the tooltip
        - Selected blocks should have a blacklist which is accepted for both Building and Exchanging
        - When selecting a block a `player status message` should show in either green or red depending on the success of the selection. It should say the block you selected or that a block has been selected depending on which is possible in each MC version. A user readable error should be shown if the selection fails
    - When changing the mode a `player status message` should show in either green or red depending on the success of the action. (it shouldn't fail) with either the name of the mode or a readable error.
- On a break or place, we must fire the correct forge event for mod comparability

### Building Gadget
The Building Gadget is the OG gadget and has a very clear outline of requirements:
- Power of 500,000rf
- Power per operation (placing of a single block) of 50rf
- There is no undo cost
- Supports undo's
    - Max of 10 undo's using a stack based list. The newest should be at the top of the stack and the gadget should pull from the stack using the first item in the stack.
- Has the following modes: `Surface`, `Build to me`, `Checkerboard / Grid`, `Stairs`, `Vertical and Horizontal Wall`, `Vertical and Horizontal Column`
- It should be able to `Mirror` and `Rotate` it's `Selected block` to adjust it's rotation.
 
### Exchanging Gadget
- Power of 500,000rf
- Power per operation of 100rf
- Does not have undo support (by design, not by limitation)
- Supports the silk touch enchant
    - When applied it will do a 1:1 exchange meaning if Diamond ore is exchanged for dirt, you will get the diamond ore
    - When not applied it will provide the drops from the Item in the case the item does not drop itself, like an ore. 
- Has the following modes: `Surface`, `Checkerboard / Grid`, `Wall`, `Vertical and Horizontal Column`
- It should be able to `Mirror` and `Rotate` it's `Selected block` to adjust it's rotation.
- Uses the currently targeted (looking at) block as it's collector parameter meaning if you're looking at grass, the exchanger will only find grass to replace
- Supports a `Fuzzy` option to allow the gadget to exchange a collection of different blocks and not just the type of block the user is targeting

### Copy Paste Gadget
- Power of 500,000rf
- Power per operation of 50rf
- Supports a single undo (by design, not by limitation)
- Uses a special type of modes which follow all the above rules but with a few modifications
    - The modes do not use a selected block
    - The modes do not do a functional task other that signifying which task to operate
        - Copy should set the gadget to copy
        - Paste should set the gadget to paste
    - The way the modes are setup and used is in the modes section
- Supports `rotating` of the copied area when in `paste mode`. This should rotate the entire scheme / template around a single point and not rotate each block in the template / schema.
- Supports `mirroring` of the copied area. This flips the entire schema meaning the last block is now the first block. Basic mirroring on a large scale.
- Max copy size of 125x125x125 or 32,768 blocks. This of course is the same for templates and pastes.
    - When the max size or block count has been reached, this should be shown in a `player status message` in red with a readable error message
    
### Destruction Gadget
- Power of 1,000,000rf
- Power per operation of 200rf per block
- Supports a single undo (by design, not by limitation)
- Does not have modes
- Max destruction area of 16x16x16
    - The selection area can be configured in a x,y,z - -x,-y,-z area meaning you can have 16 blocks to the (left, top, bottom, right) of the selection but only on that axis. Possible configurations are:
        - left: 8, top: 8, bottom: 8, right: 8, depth (z): 16 this would be it's max in every direction
        - left: 16, top: 8, bottom: 8, right: 0, depth (z): 16 this would be it's max area but on the left of the selection (looking at point) 

## Modes

## Templates / Schematics

## Construction Paste

## Inventory Management / linking

## Rendering

## Undo

## Effect Block

## Key Bindings

## Configurable
