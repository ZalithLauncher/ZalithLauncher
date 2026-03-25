package com.movtery.zalithlauncher.setting

import com.movtery.zalithlauncher.InfoDistributor
import com.movtery.zalithlauncher.context.ContextExecutor
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.LongSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.utils.path.PathManager
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences

class AllSettings {
    companion object {
        // Video
        @JvmStatic
        val renderer = StringSettingUnit("renderer", "opengles2")

        @JvmStatic
        val driver = StringSettingUnit("driver", "Turnip")

        @JvmStatic
        val ignoreNotch = BooleanSettingUnit("ignoreNotch", true)

        @JvmStatic
        val ignoreNotchLauncher = BooleanSettingUnit("ignoreNotchLauncher", true)

        @JvmStatic
        val resolutionRatio = IntSettingUnit("resolutionRatio", 100)

        @JvmStatic
        val sustainedPerformance = BooleanSettingUnit("sustainedPerformance", false)

        @JvmStatic
        val alternateSurface = BooleanSettingUnit("alternate_surface", false)

        @JvmStatic
        val forceVsync = BooleanSettingUnit("force_vsync", false)

        @JvmStatic
        val vsyncInZink = BooleanSettingUnit("vsync_in_zink", false)

        @JvmStatic
        val zinkPreferSystemDriver = BooleanSettingUnit("zinkPreferSystemDriver", false)

        // Control
        @JvmStatic
        val disableGestures = BooleanSettingUnit("disableGestures", false)

        @JvmStatic
        val disableDoubleTap = BooleanSettingUnit("disableDoubleTap", false)

        @JvmStatic
        val timeLongPressTrigger = IntSettingUnit("timeLongPressTrigger", 300)

        @JvmStatic
        val buttonScale = IntSettingUnit("buttonscale", 100)

        @JvmStatic
        val buttonAllCaps = BooleanSettingUnit("buttonAllCaps", false)

        @JvmStatic
        val mouseScale = IntSettingUnit("mousescale", 100)

        @JvmStatic
        val mouseSpeed = IntSettingUnit("mousespeed", 100)

        @JvmStatic
        val virtualMouseStart = BooleanSettingUnit("mouse_start", true)

        @JvmStatic
        val customMouse = StringSettingUnit("custom_mouse", "")

        @JvmStatic
        val enableGyro = BooleanSettingUnit("enableGyro", false)

        @JvmStatic
        val gyroSensitivity = IntSettingUnit("gyroSensitivity", 100)

        @JvmStatic
        val gyroSampleRate = IntSettingUnit("gyroSampleRate", 16)

        @JvmStatic
        val gyroSmoothing = BooleanSettingUnit("gyroSmoothing", true)

        @JvmStatic
        val gyroInvertX = BooleanSettingUnit("gyroInvertX", false)

        @JvmStatic
        val gyroInvertY = BooleanSettingUnit("gyroInvertY", false)

        @JvmStatic
        val deadZoneScale = IntSettingUnit("gamepad_deadzone_scale", 100)

        // Game
        @JvmStatic
        val versionIsolation = BooleanSettingUnit("versionIsolation", true)

        @JvmStatic
        val versionCustomInfo = StringSettingUnit("versionCustomInfo", "${InfoDistributor.LAUNCHER_NAME}[zl_version]")

        @JvmStatic
        val autoSetGameLanguage = BooleanSettingUnit("autoSetGameLanguage", true)

        @JvmStatic
        val gameLanguageOverridden = BooleanSettingUnit("gameLanguageOverridden", false)

        @JvmStatic
        val setGameLanguage = StringSettingUnit("setGameLanguage", "system")

        @JvmStatic
        val selectRuntimeMode = StringSettingUnit("selectRuntimeMode", "auto")

        @JvmStatic
        val jvmEngine = StringSettingUnit("jvmEngine", "openjdk")

        @JvmStatic
        val garbageCollector = StringSettingUnit("garbageCollector", "g1gc")

        @JvmStatic
        val javaArgs = StringSettingUnit("javaArgs", "")

        @JvmStatic
        val ramAllocation = lazy {
            //涉及到Context初始化，需要进行懒加载
            IntSettingUnit("allocation", LauncherPreferences.findBestRAMAllocation(ContextExecutor.getApplication()))
        }

        @JvmStatic
        val javaSandbox = BooleanSettingUnit("java_sandbox", true)

        @JvmStatic
        val gameMenuShowMemory = BooleanSettingUnit("gameMenuShowMemory", false)

        @JvmStatic
        val gameMenuShowFPS = BooleanSettingUnit("gameMenuShowFPS", false)

        @JvmStatic
        val gameMenuMemoryText = StringSettingUnit("gameMenuMemoryText", "M:")

        @JvmStatic
        val gameMenuLocation = StringSettingUnit("gameMenuLocation", "center")

        @JvmStatic
        val gameMenuInfoRefreshRate = IntSettingUnit("gameMenuInfoRefreshRate", 1000)

        @JvmStatic
        val gameMenuAlpha = IntSettingUnit("gameMenuAlpha", 100)

        // Launcher
        @JvmStatic
        val checkLibraries = BooleanSettingUnit("checkLibraries", true)

        @JvmStatic
        val verifyManifest = BooleanSettingUnit("verifyManifest", true)

        @JvmStatic
        val resourceImageCache = BooleanSettingUnit("resourceImageCache", false)

        @JvmStatic
        val addFullResourceName = BooleanSettingUnit("addFullResourceName", true)

        @JvmStatic
        val downloadSource = StringSettingUnit("downloadSource", "default")

        @JvmStatic
        val maxDownloadThreads = IntSettingUnit("maxDownloadThreads", 64)

        @JvmStatic
        val launcherTheme = StringSettingUnit("launcherTheme", "system")

        @JvmStatic
        val animation = BooleanSettingUnit("animation", true)

        @JvmStatic
        val animationSpeed = IntSettingUnit("animationSpeed", 600)

        @JvmStatic
        val pageOpacity = IntSettingUnit("pageOpacity", 100)

        @JvmStatic
        val enableLogOutput = BooleanSettingUnit("enableLogOutput", false)

        @JvmStatic
        val quitLauncher = BooleanSettingUnit("quitLauncher", true)

        @JvmStatic
        val acceptPreReleaseUpdates = BooleanSettingUnit("acceptPreReleaseUpdates", false)

        // Experimental
        @JvmStatic
        val dumpShaders = BooleanSettingUnit("dump_shaders", false)

        @JvmStatic
        val bigCoreAffinity = BooleanSettingUnit("bigCoreAffinity", false)

        @JvmStatic
        val tcVibrateDuration = IntSettingUnit("tcVibrateDuration", 100)

        // Other
        @JvmStatic
        val currentAccount = StringSettingUnit("currentAccount", "")

        @JvmStatic
        val launcherProfile = StringSettingUnit("launcherProfile", "default")

        @JvmStatic
        val defaultCtrl = StringSettingUnit("defaultCtrl", PathManager.FILE_CTRLDEF_FILE)

        @JvmStatic
        val defaultRuntime = StringSettingUnit("defaultRuntime", "")

        @JvmStatic
        val notificationPermissionRequest = BooleanSettingUnit("notification_permission_request", false)

        @JvmStatic
        val skipNotificationPermissionCheck = BooleanSettingUnit("skipNotificationPermissionCheck", false)

        @JvmStatic
        val localAccountReminders = BooleanSettingUnit("localAccountReminders", true)

        @JvmStatic
        val updateCheck = LongSettingUnit("updateCheck", 0L)

        @JvmStatic
        val ignoreUpdate = StringSettingUnit("ignoreUpdate", "")

        @JvmStatic
        val noticeCheck = LongSettingUnit("noticeCheck", 0L)

        @JvmStatic
        val noticeNumbering = IntSettingUnit("noticeNumbering", 0)

        @JvmStatic
        val noticeDefault = BooleanSettingUnit("noticeDefault", false)

        @JvmStatic
        val buttonSnapping = BooleanSettingUnit("buttonSnapping", true)

        @JvmStatic
        val buttonSnappingDistance = IntSettingUnit("buttonSnappingDistance", 8)

        @JvmStatic
        val hotbarType = StringSettingUnit("hotbarType", "auto")

        @JvmStatic
        val hotbarWidth = lazy {
            IntSettingUnit("hotbarWidth", Tools.currentDisplayMetrics.widthPixels / 3)
        }

        @JvmStatic
        val hotbarHeight = lazy {
            IntSettingUnit("hotbarHeight", Tools.currentDisplayMetrics.heightPixels / 4)
        }
    }
}
