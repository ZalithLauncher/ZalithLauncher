package net.kdt.pojavlaunch.multirt;

import java.util.Objects;

public class Runtime {
    public static final String ENGINE_OPENJDK = "openjdk";
    public static final String ENGINE_OPENJ9 = "openj9";
    public static final String ENGINE_GRAALVM = "graalvm";

    public final String name;
    public final String versionString;
    public final String arch;
    public final int javaVersion;
    public final String jvmEngine;
    public boolean isProvidedByLauncher = false;

    public Runtime(String name) {
        this.name = name;
        this.versionString = null;
        this.arch = null;
        this.javaVersion = 0;
        this.jvmEngine = detectJvmEngine(name, null);
    }
    Runtime(String name, String versionString, String arch, int javaVersion) {
        this.name = name;
        this.versionString = versionString;
        this.arch = arch;
        this.javaVersion = javaVersion;
        this.jvmEngine = detectJvmEngine(name, versionString);
    }

    public String getJvmEngineDisplayName() {
        switch (jvmEngine) {
            case ENGINE_OPENJ9:
                return "OpenJ9";
            case ENGINE_GRAALVM:
                return "GraalVM";
            case ENGINE_OPENJDK:
            default:
                return "OpenJDK";
        }
    }

    private static String detectJvmEngine(String name, String versionString) {
        StringBuilder builder = new StringBuilder();
        if (name != null) builder.append(name.toLowerCase());
        if (versionString != null) builder.append(' ').append(versionString.toLowerCase());
        String value = builder.toString();

        if (value.contains("openj9") || value.contains("ibm semeru")) {
            return ENGINE_OPENJ9;
        }
        if (value.contains("graalvm") || value.contains("jvmci")) {
            return ENGINE_GRAALVM;
        }
        return ENGINE_OPENJDK;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Runtime runtime = (Runtime) o;
        return name.equals(runtime.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
