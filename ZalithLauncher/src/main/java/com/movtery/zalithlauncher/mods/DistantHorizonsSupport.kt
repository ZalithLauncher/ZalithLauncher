package com.movtery.zalithlauncher.mods

import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.utils.path.PathManager
import net.kdt.pojavlaunch.Architecture
import java.io.File
import java.util.zip.ZipFile

object DistantHorizonsSupport {
    private const val ZSTD_NATIVE_PROPERTY = "ZstdNativePath"
    private const val BUNDLED_ZSTD_LIBRARY = "libzalith_dh_zstd.so"
    private const val MOD_MARKER_ENTRY = "dhcomgithubluben/zstd/util/Native.class"
    private val MOD_NAME_HINTS = listOf("distanthorizons", "distant-horizons")

    @JvmStatic
    fun buildJvmArgs(gameDir: File): List<String> {
        if (!Architecture.is64BitsDevice()) return emptyList()

        val bundledLibrary = File(PathManager.DIR_NATIVE_LIB, BUNDLED_ZSTD_LIBRARY)
        if (!bundledLibrary.isFile) {
            Logging.w("DistantHorizonsSupport", "Bundled Distant Horizons zstd library is missing: $bundledLibrary")
            return emptyList()
        }

        if (!containsDistantHorizons(gameDir)) return emptyList()

        Logging.i("DistantHorizonsSupport", "Detected Distant Horizons, forcing Android zstd native library: $bundledLibrary")
        return listOf("-D$ZSTD_NATIVE_PROPERTY=${bundledLibrary.absolutePath}")
    }

    private fun containsDistantHorizons(gameDir: File): Boolean {
        val modsDir = File(gameDir, "mods")
        val modFiles = modsDir.listFiles { file -> file.isFile && file.extension.equals("jar", true) } ?: return false
        return modFiles.any(::isDistantHorizonsJar)
    }

    private fun isDistantHorizonsJar(file: File): Boolean {
        val lowercaseName = file.name.lowercase()
        if (MOD_NAME_HINTS.any(lowercaseName::contains)) {
            return true
        }

        return runCatching {
            ZipFile(file).use { zipFile ->
                zipFile.getEntry(MOD_MARKER_ENTRY) != null
            }
        }.getOrElse { throwable ->
            Logging.w("DistantHorizonsSupport", "Failed to inspect mod archive ${file.absolutePath}", throwable)
            false
        }
    }
}
