package com.movtery.zalithlauncher.feature.version.install

import android.content.Intent
import com.google.gson.JsonParser
import com.kdt.mcgui.ProgressLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.feature.customprofilepath.ProfilePathHome
import com.movtery.zalithlauncher.utils.path.LibPath
import net.kdt.pojavlaunch.JavaGUILauncherActivity
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class InstallArgsUtils(private val mcVersion: String, private val loaderVersion: String) {
    fun setFabric(intent: Intent, jarFile: File, customName: String) {
        val args = "-DprofileName=\"$customName\" -javaagent:${LibPath.MIO_FABRIC_AGENT.absolutePath}" +
                " -jar ${jarFile.absolutePath} client -mcversion \"$mcVersion\" -loader \"$loaderVersion\" -dir \"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
    }

    @Deprecated("不支持JRE 8进行安装，更高的JRE环境安装时，不会自动退出，因此暂时不使用这个函数进行配置安装")
    fun setQuilt(intent: Intent, jarFile: File) {
        val args = "-jar ${jarFile.absolutePath} install client \"$mcVersion\" \"$loaderVersion\" --install-dir=\"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
    }

    @Throws(Throwable::class)
    fun setForge(intent: Intent, jarFile: File, customName: String) {
        forgeLikeCustomVersionName(jarFile, customName)

        val args = "-javaagent:${LibPath.FORGE_INSTALLER.absolutePath}=\"$loaderVersion\" -jar ${jarFile.absolutePath}"
        intent.putExtra("javaArgs", args)
    }

    @Throws(Throwable::class)
    fun setNeoForge(intent: Intent, jarFile: File, customName: String) {
        forgeLikeCustomVersionName(jarFile, customName)

        val args = "-jar ${jarFile.absolutePath} --installClient \"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
        intent.putExtra("disableSecurityManager", true)
    }

    fun setOptiFine(intent: Intent, jarFile: File, customName: String) {
        val args = "-javaagent:${LibPath.FORGE_INSTALLER.absolutePath}=OFNPS " +
                "-javaagent:${LibPath.OPTIFINE_RENAMER.absolutePath}=\"$customName\" " +
                "-jar ${jarFile.absolutePath}"
        intent.putExtra("javaArgs", args)
    }

    /**
     * 将Forge或NeoForge安装器中的install_profile.json 文件中的 version 的键，修改为 customName
     * Forge安装器会根据 version 这个值，来生成对应的版本文件夹
     * 这样做是为了自定义版本 json 的安装位置
     */
    @Throws(Throwable::class)
    private fun forgeLikeCustomVersionName(jarFile: File, customName: String) {
        val tempJarFile = File(jarFile.parentFile, "${jarFile.nameWithoutExtension}_temp.jar")
        val profileJson = File(jarFile.parentFile, "install_profile.json")
        try {
            updateProgress(0)

            if (tempJarFile.exists()) tempJarFile.delete()
            extractInstallProfile(jarFile, profileJson)
            updateProgress(50)

            modifyJsonFile(profileJson, customName)
            writeTempJarFile(jarFile, tempJarFile, profileJson)
            updateProgress(100)

            if (!jarFile.delete()) throw IOException("Failed to delete original Installer file!")
            if (!tempJarFile.renameTo(jarFile)) throw IOException("Failed to rename temp Installer file to original!")
            profileJson.delete()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
        }
    }

    private fun updateProgress(progress: Int) {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, progress, R.string.mod_forge_custom_version)
    }

    /**
     * 解压出install_profile.json
     */
    @Throws(Throwable::class)
    private fun extractInstallProfile(jarFile: File, profileJson: File) {
        val zipFile = ZipFile(jarFile)
        val entry = zipFile.getEntry("install_profile.json")
            ?: throw IOException("File \"install_profile.json\" not found in the Installer")
        profileJson.outputStream().use { outputStream ->
            zipFile.getInputStream(entry).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * 通过修改install_profile.json文件中的值，来实现自定义版本名称的效果
     */
    @Throws(Throwable::class)
    private fun modifyJsonFile(profileJson: File, customName: String) {
        val jsonObject = JsonParser.parseString(profileJson.readText()).asJsonObject
        //通过检查是否有spec这个键，来判断是否为新版本的Installer
        if (jsonObject.has("spec")) { //新版安装器
            if (!jsonObject.has("version")) throw IOException("Unable to find version key!")
            //install_profile.json中，把version这个值改为customName，也就完成自定义版本名的效果
            jsonObject.addProperty("version", customName)
        } else { //旧版安装器
            if (!jsonObject.has("install")) throw IOException("Unable to find install key!")
            val install = jsonObject.get("install").asJsonObject
            if (!install.has("target")) throw IOException("Unable to find install-target key!")
            //把target这个值改为customName，也就完成旧版自定义版本名的效果
            install.addProperty("target", customName)
            jsonObject.add("install", install)
        }
        profileJson.writeText(jsonObject.toString())
    }

    @Throws(Throwable::class)
    private fun writeTempJarFile(jarFile: File, tempJarFile: File, profileJson: File) {
        //仅跳过META-INF中后缀为.SF或.RSA的文件，避免验证的时候发现install_profile.json被修改
        fun needSkip(entryName: String) = entryName.startsWith("META-INF/") && (entryName.endsWith(".SF") || entryName.endsWith(".RSA"))

        ZipFile(jarFile).use { zipFile ->
            ZipOutputStream(tempJarFile.outputStream()).use { zos ->
                zipFile.entries().asSequence().forEach { originalEntry ->
                    zos.putNextEntry(ZipEntry(originalEntry.name))
                    if (originalEntry.name == "install_profile.json") {
                        profileJson.inputStream().use { fis -> fis.copyTo(zos) }
                    } else {
                        if (!originalEntry.isDirectory && !needSkip(originalEntry.name)) {
                            //写入原始文件
                            zipFile.getInputStream(originalEntry).use { it.copyTo(zos) }
                        }
                    }
                    zos.closeEntry()
                }
            }
        }
    }
}