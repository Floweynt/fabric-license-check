package com.floweytf.absolutely_proprietary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {
    private static final Path CONFIG = FabricLoader.getInstance()
        .getConfigDir()
        .resolve("absolutely_proprietary.json");

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<String>> LICENSE_INCLUDES = new MapBuilder<String, List<String>>()
        .put("@osi", List.of(
            // OSI approved from https://github.com/spdx/license-list-data
            "0bsd", "aal", "afl1.1", "afl1.2", "afl2.0", "afl2.1", "afl3.0", "agpl3.0", "agpl3.0only",
            "agpl3.0orlater", "apache1.1", "apache2.0", "apl1.0", "apsl1.0", "apsl1.1", "apsl1.2", "apsl2.0", "artistic1.0",
            "artistic1.0cl8", "artistic1.0perl", "artistic2.0", "blueoak1.0.0", "bsd1clause", "bsd2clause",
            "bsd2clausepatent", "bsd3clause", "bsd3clauselbnl", "bsl1.0", "cal1.0", "cal1.0combinedworkexception",
            "catosl1.1", "cddl1.0", "cecill2.1", "cernohlp2.0", "cernohls2.0", "cernohlw2.0", "cnripython", "cpal1.0",
            "cpl1.0", "cuaopl1.0", "ecl1.0", "ecl2.0", "efl1.0", "efl2.0", "entessa", "epl1.0", "epl2.0", "eudatagrid",
            "eupl1.1", "eupl1.2", "fair", "frameworx1.0", "gpl2.0", "gpl2.0+", "gpl2.0only", "gpl2.0orlater", "gpl3.0",
            "gpl3.0+", "gpl3.0only", "gpl3.0orlater", "gpl3.0withgccexception", "hpnd", "icu", "intel", "ipa", "ipl1.0",
            "isc", "jam", "lgpl2.0", "lgpl2.0+", "lgpl2.0only", "lgpl2.0orlater", "lgpl2.1", "lgpl2.1+", "lgpl2.1only",
            "lgpl2.1orlater", "lgpl3.0", "lgpl3.0+", "lgpl3.0only", "lgpl3.0orlater", "liliqp1.1", "liliqr1.1",
            "liliqrplus1.1", "lpl1.0", "lpl1.02", "lppl1.3c", "miros", "mit", "mit0", "mitmodernvariant", "motosoto",
            "mpl1.0", "mpl1.1", "mpl2.0", "mpl2.0nocopyleftexception", "mspl", "msrl", "mulanpsl2.0", "multics", "nasa1.3",
            "naumen", "ncsa", "ngpl", "nokia", "nposl3.0", "ntp", "oclc2.0", "ofl1.1", "ofl1.1norfn", "ofl1.1rfn", "ogtsl",
            "oldap2.8", "olfl1.3", "osetpl2.1", "osl1.0", "osl2.0", "osl2.1", "osl3.0", "php3.0", "php3.01", "postgresql",
            "python2.0", "qpl1.0", "rpl1.1", "rpl1.5", "rpsl1.0", "rscpl", "simpl2.0", "sissl", "sleepycat", "spl1.0",
            "ucl1.0", "unicode3.0", "unicodedfs2016", "unlicense", "upl1.0", "vsl1.0", "w3c", "watcom1.0", "wxwindows",
            "xnet", "zlib", "zpl2.0", "zpl2.1"
        ))
        .put("@alt", List.of(
            "gpl3",
            "gnulgplv3",
            "lgplv3",
            "gnulgplv2.1",
            "gnulgplv3.0"
        ))
        .getImmutable();

    private static final Map<String, List<String>> MOD_INCLUDES = new MapBuilder<String, List<String>>()
        .getImmutable();

    @SerializedName("allowed_license")
    public List<String> allowedLicense = List.of("@osi", "@alt");

    @SerializedName("allowed_mods")
    public List<String> allowedMods = List.of();

    @SerializedName("fallback_to_parent")
    public boolean shouldFallbackEmptyContainedJarToParent = true;

    @SerializedName("whitelist_builtin")
    public boolean whitelistBuiltin = true;

    public static Config read() {
        var config = new Config();

        try {
            config = GSON.fromJson(Files.readString(CONFIG), Config.class);
        } catch (IOException e) {
            config.write();
        }

        return config;
    }

    public Set<String> getLicenseList() {
        return allowedLicense.stream()
            .flatMap(license -> {
                if (LICENSE_INCLUDES.containsKey(license)) {
                    return LICENSE_INCLUDES.get(license).stream();
                }
                return Stream.of(license);
            })
            .map(ModMain::cleanLicense)
            .collect(Collectors.toSet());
    }

    public Set<String> getModList() {
        return allowedMods.stream()
            .flatMap(license -> {
                if (LICENSE_INCLUDES.containsKey(license)) {
                    return MOD_INCLUDES.get(license).stream();
                }
                return Stream.of(license);
            })
            .map(ModMain::cleanLicense)
            .collect(Collectors.toSet());
    }

    public void write() {
        try {
            Files.writeString(CONFIG, GSON.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}