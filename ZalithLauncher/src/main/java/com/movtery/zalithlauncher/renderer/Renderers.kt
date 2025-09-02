package com.movtery.zalithlauncher.renderer

import android.content.Context
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.renderer.renderers.FreedrenoRenderer
import com.movtery.zalithlauncher.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.renderer.renderers.PanfrostRenderer
import com.movtery.zalithlauncher.renderer.renderers.VirGLRenderer
import com.movtery.zalithlauncher.renderer.renderers.VulkanZinkRenderer
import com.movtery.zalithlauncher.renderer.renderers.GLES3Renderer
import net.kdt.pojavlaunch.Architecture
import net.kdt.pojavlaunch.Tools

/**
 * 启动器所有渲染器总管理者，启动器内置的渲染器与渲染器插件加载的渲染器，都会加载到这里
 */
object Renderers {
    private val renderers: MutableList<RendererInterface> = mutableListOf()
    private var compatibleRenderers: Pair<RenderersList, MutableList<RendererInterface>>? = null
    private var currentRenderer: RendererInterface? = null
    private var isInitialized: Boolean = false

    fun init(reset: Boolean = false) {
        if (isInitialized && !reset) return
        isInitialized = true

        if (reset) {
            renderers.clear()
            compatibleRenderers = null
            currentRenderer = null
        }

        addRenderers(
            GL4ESRenderer(),
            GLES3Renderer(),
            VulkanZinkRenderer(),
            VirGLRenderer(),
            FreedrenoRenderer(),
            PanfrostRenderer()
        )
    }

    /**
     * 获取兼容当前设备的所有渲染器
     */
    fun getCompatibleRenderers(context: Context): Pair<RenderersList, List<RendererInterface>> = compatibleRenderers ?: run {
        val deviceHasVulkan = Tools.checkVulkanSupport(context.packageManager)
        // Currently, only 32-bit x86 does not have the Zink binary
        val deviceHasZinkBinary = !(Architecture.is32BitsDevice() && Architecture.isx86Device())

        val compatibleRenderers1: MutableList<RendererInterface> = mutableListOf()
        renderers.forEach { renderer ->
            if (renderer.getRendererId().contains("vulkan") && !deviceHasVulkan) return@forEach
            if (renderer.getRendererId().contains("zink") && !deviceHasZinkBinary) return@forEach
            compatibleRenderers1.add(renderer)
        }

        val rendererIdentifiers: MutableList<String> = mutableListOf()
        val rendererNames: MutableList<String> = mutableListOf()
        compatibleRenderers1.forEach { renderer ->
            rendererIdentifiers.add(renderer.getUniqueIdentifier())
            rendererNames.add(renderer.getRendererName())
        }

        val rendererPair = Pair(RenderersList(rendererIdentifiers, rendererNames), compatibleRenderers1)
        compatibleRenderers = rendererPair
        rendererPair
    }

    /**
     * 加入一些渲染器
     */
    @JvmStatic
    fun addRenderers(vararg renderers: RendererInterface) {
        renderers.forEach { renderer ->
            addRenderer(renderer)
        }
    }

    /**
     * 加入单个渲染器
     */
    @JvmStatic
    fun addRenderer(renderer: RendererInterface): Boolean {
        return if (this.renderers.any { it.getUniqueIdentifier() == renderer.getUniqueIdentifier() }) {
            Logging.w("Renderers", "The unique identifier of this renderer (${renderer.getRendererName()} - ${renderer.getUniqueIdentifier()}) conflicts with an already loaded renderer. " +
                    "Normally, this shouldn't happen. You deliberately caused this conflict, didn't you, user?")
            false
        } else {
            this.renderers.add(renderer)
            Logging.i("Renderers", "Renderer loaded: ${renderer.getRendererName()} (${renderer.getRendererId()} - ${renderer.getUniqueIdentifier()})")
            true
        }
    }

    /**
     * 设置当前的渲染器
     * @param context 用于初始化适配当前设备的渲染器
     * @param uniqueIdentifier 渲染器的唯一标识符，用于找到当前想要设置的渲染器
     * @param retryToFirstOnFailure 如果未找到匹配的渲染器，是否跳回渲染器列表的首个渲染器
     */
    fun setCurrentRenderer(context: Context, uniqueIdentifier: String, retryToFirstOnFailure: Boolean = true) {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        val compatibleRenderers = getCompatibleRenderers(context).second
        currentRenderer = compatibleRenderers.find { it.getUniqueIdentifier() == uniqueIdentifier } ?: run {
            if (retryToFirstOnFailure) {
                val renderer = compatibleRenderers[0]
                Logging.w("Renderers", "Incompatible renderer $uniqueIdentifier will be replaced with ${renderer.getUniqueIdentifier()} (${renderer.getRendererName()})")
                renderer
            } else null
        }
    }

    /**
     * 获取当前的渲染器
     */
    fun getCurrentRenderer(): RendererInterface {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        return currentRenderer ?: throw IllegalStateException("Current renderer not set")
    }

    /**
     * 当前是否设置了渲染器
     */
    fun isCurrentRendererValid(): Boolean = isInitialized && this.currentRenderer != null
}
