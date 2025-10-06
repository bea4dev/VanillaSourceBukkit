package com.github.bea4dev.vanilla_source.util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class FileUtil {
    public static void deleteDirectoriesStartingWith(Path root, String prefix) throws IOException {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(prefix, "prefix");
        if (!Files.isDirectory(root)) {
            throw new NotDirectoryException(root.toString());
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, Files::isDirectory)) {
            for (Path dir : stream) {
                String name = dir.getFileName().toString();
                if (name.startsWith(prefix)) {
                    deleteRecursively(dir);
                }
            }
        }
    }

    public static void deleteRecursively(Path target) throws IOException {
        if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) return;

        Files.walkFileTree(target, new SimpleFileVisitor<Path>() {
            @Override
            public @NotNull FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public @NotNull FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
