package com.movtery.zalithlauncher.setting

/**
 * 静态设置项的值，用于一些临时生效的设置项使用
 * 这里的值不会被保存到设置配置中，软件重启就会消失！
 */
class AllStaticSettings {
    companion object {
        /**
         * 刘海屏缺口宽度 Int
         */
        @JvmField var notchSize = 0

        /**
         * 缩放因子 Float
         */
        @JvmField var scaleFactor = AllSettings.resolutionRatio.getValue() / 100f

        /**
         * 禁用双击交换手中物品 Boolean
         */
        @JvmField var disableDoubleTap = AllSettings.disableDoubleTap.getValue()
        @JvmField var forceGuiInput = AllSettings.forceGuiInput.getValue()

        /**
         * 触发长按延迟 Int
         */
        @JvmField var timeLongPressTrigger = AllSettings.timeLongPressTrigger.getValue()

        /**
         * 启用陀螺仪控制 Boolean
         */
        @JvmField var enableGyro = AllSettings.enableGyro.getValue()

        /**
         * 陀螺仪控制灵敏度 Int
         */
        @JvmField var gyroSensitivity = AllSettings.gyroSensitivity.getValue()

        /**
         * 陀螺仪反转X轴 Boolean
         */
        @JvmField var gyroInvertX = AllSettings.gyroInvertX.getValue()

        /**
         * 陀螺仪反转Y轴 Boolean
         */
        @JvmField var gyroInvertY = AllSettings.gyroInvertY.getValue()

        /**
         * 使用控制代理 Boolean
         */
        @JvmField var useControllerProxy = false
    }
}
