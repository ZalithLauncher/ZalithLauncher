package com.movtery.zalithlauncher.feature.unpack

import com.movtery.zalithlauncher.R

enum class Components(val component: String, val displayName: String, val summary: Int?, val privateDirectory: Boolean) {
    OTHER_LOGIN("other_login", "authlib-injector", R.string.splash_screen_authlib_injector, false),
    CACIOCAVALLO("caciocavallo", "caciocavallo", R.string.splash_screen_cacio, false),
    CACIOCAVALLO17("caciocavallo17", "caciocavallo 17", R.string.splash_screen_cacio, false),
    LWJGL3("lwjgl3", "LWJGL 3.3.6", R.string.splash_screen_lwjgl, false),
    COMPONENTS("components", "Launcher Components", R.string.splash_screen_components, true)
}