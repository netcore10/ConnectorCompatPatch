package net.netco.connectorcompat;

/**
 * True while NeoForge freezes the registries at the end of its own registry setup, which runs before Launchpad has run the
 * Fabric entrypoints. Launchpad unfreezes and freezes them again around the entrypoints; that later freeze checks as usual.
 */
public final class EarlyFreeze {
    public static volatile boolean active;
}
