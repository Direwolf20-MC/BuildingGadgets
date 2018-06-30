package com.direwolf20.buildinggadgets.Tools;

import net.minecraftforge.event.world.NoteBlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

public class UndoBuild {
    private static final Map<UUID,Stack<UndoState>> playerUndoList = new HashMap<UUID,Stack<UndoState>>();

    public UndoBuild(UUID PlayerUUID, Stack<UndoState> undoStack) {
        playerUndoList.put(PlayerUUID,undoStack);
    }

    public static void updatePlayerMap(UUID PlayerUUID, Stack<UndoState> undoStack) {
        if (playerUndoList.containsKey(PlayerUUID)) {
            playerUndoList.replace(PlayerUUID, undoStack);
        }
        else {
            playerUndoList.put(PlayerUUID,undoStack);
        }

    }

    public static Stack<UndoState> getPlayerMap(UUID PlayerUUID) {
        if (playerUndoList.containsKey(PlayerUUID)) {
            return playerUndoList.get(PlayerUUID);
        } else {
            Stack<UndoState> undoStack = new Stack<UndoState>();
            playerUndoList.put(PlayerUUID, undoStack);
            return undoStack;
        }
    }
}
