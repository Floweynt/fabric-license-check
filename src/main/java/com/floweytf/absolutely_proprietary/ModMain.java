package com.floweytf.absolutely_proprietary;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.api.metadata.Person;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Collectors;

public class ModMain implements ModInitializer {
    private static String formatModName(ModContainer mod) {
        if (mod.getOrigin().getKind() == ModOrigin.Kind.PATH) {
            return String.format("mod '%s' (id %s) by '%s' (in files %s)",
                mod.getMetadata().getName(),
                mod.getMetadata().getId(),
                String.join(", ", mod.getMetadata().getAuthors().stream().map(Person::getName).toList()),
                String.join(", ", mod.getOrigin().getPaths().stream().map(Path::toString).toList())
            );
        }

        return String.format("mod '%s' (id %s) by '%s'",
            mod.getMetadata().getName(),
            mod.getMetadata().getId(),
            String.join(", ", mod.getMetadata().getAuthors().stream().map(Person::getName).toList())
        );
    }

    private static void printLicenseTrace(ModContainer mod) {
        Log.logLine(String.format(
            "Bad! %s has licenses [%s]!",
            formatModName(mod),
            String.join(", ", mod.getMetadata().getLicense())
        ));

        while (mod.getContainingMod().isPresent()) {
            mod = mod.getContainingMod().get();
            Log.logLine(String.format("    from %s", formatModName(mod)));
        }
    }

    public static String cleanLicense(String str) {
        return str.toLowerCase().replaceAll("[-_ ]", "");
    }

    @Override
    public void onInitialize() {
        Log.logLine("Running mod license check...");
        final var config = Config.read();
        final var licenseSet = config.getLicenseList();
        final var modSet = config.getModList();

        Log.logLine("Allowing mods " + modSet);

        final var nonCompliantMods = FabricLoader.getInstance().getAllMods()
            .stream()
            // For nested mods, fallback to the parent mod if the nested mod has no license
            .map(mod -> {
                if (!config.shouldFallbackEmptyContainedJarToParent)
                    return mod;

                while (mod.getMetadata().getLicense().isEmpty() && mod.getContainingMod().isPresent()) {
                    mod = mod.getContainingMod().get();
                }

                return mod;
            })
            // Ignore builtin mods, so "java" and "minecraft"
            .filter(mod -> !(config.whitelistBuiltin && mod.getMetadata().getType().equals("builtin")))
            // Remove mods with allowed licenses
            .filter(mod -> {
                final var s = mod.getMetadata().getLicense()
                    .stream()
                    .map(ModMain::cleanLicense)
                    .collect(Collectors.toSet());

                s.retainAll(licenseSet);
                return s.isEmpty();
            })
            // Remove whitelisted mods
            .filter(mod -> !modSet.contains(mod.getMetadata().getId()))
            .toList();

        if (!nonCompliantMods.isEmpty()) {
            Log.logLine("!!! Offending Mods Found !!!");
            for (final var mod : nonCompliantMods) {
                printLicenseTrace(mod);
            }

            throw new RuntimeException("proprietary mods found");
        }

        Log.logLine("No proprietary & non-excluded mods found");
    }
}