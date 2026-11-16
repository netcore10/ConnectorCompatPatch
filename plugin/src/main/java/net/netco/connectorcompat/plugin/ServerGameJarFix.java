package net.netco.connectorcompat.plugin;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.sinytra.connector.util.ConnectorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Connector adapts Fabric mixins by comparing NeoForge's game against Mojang's clean jar, cached as
 * {@code .cache/connector/minecraft/minecraft-<version>-<side>.jar}. On a dedicated server it caches Mojang's server
 * <em>bundler</em>, whose game classes are nested in {@code META-INF/versions/<v>/server-<v>.jar}, so it sees no game
 * classes, adapts nothing, and Fabric mixins that need adapting (e.g. Create Fly's BlockItemMixin) crash the server.
 * <p>
 * Runs when Connector loads its plugins, before it first reads that jar: put the real server jar there, extracted from
 * the bundler NeoForge's installer leaves in {@code libraries/}. If the cache already holds a bundler (made by an earlier
 * start), unpack it and drop what Connector derived from it. Fails safe: if Connector rejects what we wrote, it downloads
 * the bundler again and the next start fixes it.
 */
final class ServerGameJarFix {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerGameJarFix.class);
    private static final Pattern INNER_SERVER_JAR = Pattern.compile("META-INF/versions/[^/]+/server-[^/]+\\.jar");

    private ServerGameJarFix() {
    }

    static void run() {
        try {
            if (!FMLEnvironment.getDist().isDedicatedServer()) return;
            Path gameDir = FMLPaths.GAMEDIR.get();
            Path cache = gameDir.resolve(".cache/connector");
            Path serverLibs = gameDir.resolve("libraries/net/minecraft/server");
            if (!Files.isDirectory(serverLibs)) return;
            try (DirectoryStream<Path> versions = Files.newDirectoryStream(serverLibs, Files::isDirectory)) {
                for (Path versionDir : versions) {
                    String version = versionDir.getFileName().toString();
                    fix(cache, versionDir.resolve("server-" + version + ".jar"),
                        cache.resolve("minecraft/minecraft-" + version + "-server.jar"));
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("Couldn't prepare Connector's clean server jar; Fabric mixins may not be adapted", t);
        }
    }

    private static void fix(Path cache, Path installerJar, Path cachedJar) throws IOException {
        if (Files.isRegularFile(cachedJar)) {
            if (extractInner(cachedJar, cachedJar)) {
                // Everything Connector derived from the bundler is wrong; let it rebuild from the real jar
                try (Stream<Path> files = Files.list(cache)) {
                    for (Path f : files.filter(Files::isRegularFile).toList()) Files.deleteIfExists(f);
                }
                deleteTree(cache.resolve("adapter"));
                LOGGER.info("Replaced Connector's cached server bundler with the real server jar ({})", cachedJar.getFileName());
            }
        } else if (Files.isRegularFile(installerJar)) {
            Files.createDirectories(cachedJar.getParent());
            if (extractInner(installerJar, cachedJar)) {
                // Same marker Connector writes for this file (TransformerUtil.cache): its cache version, which already
                // ends with the side (e.g. "3.1.0-beta.4+26.2,dedicated_server")
                Files.writeString(cachedJar.resolveSibling(cachedJar.getFileName() + ".input"),
                    ConnectorUtil.getJarCacheVersion(), StandardCharsets.UTF_8);
                LOGGER.info("Prepared Connector's clean server jar from {}", installerJar.getFileName());
            }
        }
    }

    /** Writes the jar nested in a server bundler to {@code target}; false if {@code bundler} isn't a bundler. */
    private static boolean extractInner(Path bundler, Path target) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try (ZipFile zip = new ZipFile(bundler.toFile())) {
            ZipEntry inner = zip.stream().filter(e -> INNER_SERVER_JAR.matcher(e.getName()).matches()).findFirst().orElse(null);
            if (inner == null) return false;
            try (InputStream in = zip.getInputStream(inner)) {
                Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    private static void deleteTree(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
        }
    }
}
