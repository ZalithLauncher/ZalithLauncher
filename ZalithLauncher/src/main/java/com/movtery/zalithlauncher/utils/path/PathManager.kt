package com.movtery.zalithlauncher.utils.path

import android.content.Context
import android.os.Build.VERSION
import android.os.Environment
import com.movtery.zalithlauncher.InfoDistributor
import org.apache.commons.io.FileUtils
import java.io.File

class PathManager {
    companion object {
        lateinit var DIR_NATIVE_LIB: String
        lateinit var DIR_FILE: File
        lateinit var DIR_DATA: String //Initialized later to get context
        lateinit var DIR_CACHE: File
        lateinit var DIR_MULTIRT_HOME: String
        @JvmField var DIR_GAME_HOME: String = Environment.getExternalStorageDirectory().absolutePath + "/games/${InfoDistributor.LAUNCHER_NAME}"
        lateinit var DIR_LAUNCHER_LOG: String
        lateinit var DIR_CTRLMAP_PATH: String
        lateinit var DIR_ACCOUNT_NEW: String
        lateinit var DIR_CACHE_STRING: String
        lateinit var DIR_ADDONS_INFO_CACHE: String
        @JvmField var DIR_RUNTIME_MOD: File? = null

        lateinit var DIR_CUSTOM_MOUSE: String
        lateinit var DIR_BACKGROUND: File
        lateinit var DIR_APP_CACHE: File
        lateinit var DIR_USER_SKIN: File
        lateinit var DIR_INSTALLED_RENDERER_PLUGIN: File

        lateinit var FILE_SETTINGS: File
        lateinit var FILE_PROFILE_PATH: File
        lateinit var FILE_CTRLDEF_FILE: String
        lateinit var FILE_VERSION_LIST: String
        lateinit var FILE_NEWBIE_GUIDE: File

        @JvmStatic
        fun initContextConstants(context: Context) {
            DIR_NATIVE_LIB = context.applicationInfo.nativeLibraryDir
            DIR_FILE = context.filesDir
            DIR_DATA = DIR_FILE.getParent()!!
            DIR_CACHE = context.cacheDir
            DIR_MULTIRT_HOME = "$DIR_DATA/runtimes"
            DIR_GAME_HOME = getExternalStorageRoot(context).absolutePath
            DIR_LAUNCHER_LOG = "$DIR_GAME_HOME/launcher_log"
            DIR_CTRLMAP_PATH = "$DIR_GAME_HOME/controlmap"
            DIR_ACCOUNT_NEW = "$DIR_FILE/accounts"
            DIR_CACHE_STRING = "$DIR_CACHE/string_cache"
            DIR_ADDONS_INFO_CACHE = "$DIR_CACHE/addons_info_cache"
            DIR_CUSTOM_MOUSE = "$DIR_GAME_HOME/mouse"
            DIR_BACKGROUND = File("$DIR_GAME_HOME/background")
            DIR_APP_CACHE = context.externalCacheDir!!
            DIR_USER_SKIN = File(DIR_FILE, "/user_skin")
            DIR_INSTALLED_RENDERER_PLUGIN = File(DIR_FILE, "/renderer_plugins")
            DIR_RUNTIME_MOD = context.getDir("runtime_mod", 0)?.also {
                it.mkdirs()
            }

            FILE_PROFILE_PATH = File(DIR_DATA, "/profile_path.json")
            FILE_CTRLDEF_FILE = "$DIR_GAME_HOME/controlmap/default.json"
            FILE_VERSION_LIST = "$DIR_DATA/version_list.json"
            FILE_NEWBIE_GUIDE = File(DIR_DATA, "/newbie_guide.json")
            FILE_SETTINGS = File(DIR_FILE, "/launcher_settings.json")

            runCatching {
                //此处的账号文件已不再使用，需要检查并清除
                FileUtils.deleteQuietly(File("$DIR_DATA/accounts"))
                FileUtils.deleteQuietly(File(DIR_DATA, "/user_skin"))
            }
        }

        @JvmStatic
        fun getExternalStorageRoot(ctx: Context): File {
            return if (VERSION.SDK_INT >= 29) {
                ctx.getExternalFilesDir(null)!!
            } else {
                File(Environment.getExternalStorageDirectory(), "games/${InfoDistributor.LAUNCHER_NAME}")
            }
        }
    }
}
