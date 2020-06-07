package com.direwolf20.buildinggadgets.common.util.exceptions;

import com.google.gson.JsonParseException;

public class TemplateParseException extends JsonParseException {

    public TemplateParseException(String msg) {
        super(msg);
    }

    public TemplateParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public static final class IllegalMinecraftVersionException extends TemplateParseException {
        private final String minecraftVersion;
        private final String expectedVersion;

        public IllegalMinecraftVersionException(String minecraftVersion, String expectedVersion) {
            super("Attempted to load Template for illegal minecraft version " + minecraftVersion + "!");
            this.minecraftVersion = minecraftVersion;
            this.expectedVersion = expectedVersion;
        }

        public IllegalMinecraftVersionException(Throwable cause, String minecraftVersion, String expectedVersion) {
            super("Attempted to load Template for illegal minecraft version " + minecraftVersion + "!", cause);
            this.minecraftVersion = minecraftVersion;
            this.expectedVersion = expectedVersion;
        }

        public String getMinecraftVersion() {
            return minecraftVersion;
        }

        public String getExpectedVersion() {
            return expectedVersion;
        }
    }

    public static final class UnknownTemplateVersionException extends TemplateParseException {
        private final String templateVersion;

        public UnknownTemplateVersionException(String templateVersion) {
            super("Attempted to load Template with too recent (unknown) format (version=" + templateVersion + ")!");
            this.templateVersion = templateVersion;
        }

        public UnknownTemplateVersionException(Throwable cause, String templateVersion) {
            super("Attempted to load Template with too recent (unknown) format (version=" + templateVersion + ")!", cause);
            this.templateVersion = templateVersion;
        }

        public String getTemplateVersion() {
            return templateVersion;
        }
    }
}
