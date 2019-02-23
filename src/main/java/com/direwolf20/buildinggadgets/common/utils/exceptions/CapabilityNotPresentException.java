package com.direwolf20.buildinggadgets.common.utils.exceptions;

public class CapabilityNotPresentException extends IllegalStateException {

    public CapabilityNotPresentException() {
        super("Capability was not present and therefore could not be queried. No further Information available!");
    }

    public CapabilityNotPresentException(Throwable cause) {
        super("Capability was not present and therefore could not be queried. No further Information available!", cause);
    }
}
