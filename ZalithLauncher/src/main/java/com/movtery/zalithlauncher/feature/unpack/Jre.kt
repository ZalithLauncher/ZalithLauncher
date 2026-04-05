package com.movtery.zalithlauncher.feature.unpack

import com.movtery.zalithlauncher.R

enum class Jre(val jreName: String, val jrePath: String, val summary: Int) {
    JRE_8("Internal-8", "components/jre-8", R.string.splash_screen_jre8),
    JRE_17("Internal-17", "components/jre-17", R.string.splash_screen_jre17),
    JRE_21("Internal-21", "components/jre-21", R.string.splash_screen_jre21),
    JRE_25("Internal-25", "components/jre-25", R.string.splash_screen_jre25)
}