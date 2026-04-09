package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.MainActivity.touchCharInput;
import static org.lwjgl.glfw.CallbackBridge.sendMouseButton;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.movtery.zalithlauncher.event.single.RefreshHotbarEvent;
import com.movtery.zalithlauncher.feature.customprofilepath.ProfilePathHome;
import com.movtery.zalithlauncher.feature.MCOptions;
import com.movtery.zalithlauncher.feature.log.Logging;
import com.movtery.zalithlauncher.feature.version.VersionsManager;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;

import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.gamepad.DefaultDataProvider;
import net.kdt.pojavlaunch.customcontrols.gamepad.Gamepad;
import net.kdt.pojavlaunch.customcontrols.mouse.AbstractTouchpad;
import net.kdt.pojavlaunch.customcontrols.mouse.AndroidPointerCapture;
import net.kdt.pojavlaunch.customcontrols.mouse.InGUIEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.InGameEventProcessor;
import net.kdt.pojavlaunch.customcontrols.mouse.TouchEventProcessor;
import net.kdt.pojavlaunch.utils.JREUtils;

import org.greenrobot.eventbus.EventBus;
import org.lwjgl.glfw.CallbackBridge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.Locale;

import fr.spse.gamepad_remapper.RemapperManager;
import fr.spse.gamepad_remapper.RemapperView;

/**
 * Class dealing with showing minecraft surface and taking inputs to dispatch them to minecraft
 */
public class MinecraftGLSurface extends View implements GrabListener {
    /* Gamepad object for gamepad inputs, instantiated on need */
    private Gamepad mGamepad = null;
    
    private static String sCurrentVersionName = null;

    public static void setCurrentVersionName(String versionName) {
        sCurrentVersionName = versionName;
    }
    /* The RemapperView.Builder object allows you to set which buttons to remap */
    private final RemapperManager mInputManager = new RemapperManager(getContext(), new RemapperView.Builder(null)
            .remapA(true)
            .remapB(true)
            .remapX(true)
            .remapY(true)

            .remapLeftJoystick(true)
            .remapRightJoystick(true)
            .remapStart(true)
            .remapSelect(true)
            .remapLeftShoulder(true)
            .remapRightShoulder(true)
            .remapLeftTrigger(true)
            .remapRightTrigger(true)
            .remapDpad(true));

    /* Sensitivity, adjusted according to screen size */
    private final double mSensitivityFactor = (1.4 * (1080f/ Tools.getDisplayMetrics((BaseActivity) getContext()).heightPixels));

    /* Surface ready listener, used by the activity to launch minecraft */
    SurfaceReadyListener mSurfaceReadyListener = null;
    final Object mSurfaceReadyListenerLock = new Object();
    /* View holding the surface, either a SurfaceView or a TextureView */
    View mSurface;

    private final InGameEventProcessor mIngameProcessor = new InGameEventProcessor(mSensitivityFactor);
    private final InGUIEventProcessor mInGUIProcessor = new InGUIEventProcessor();
    private TouchEventProcessor mCurrentTouchProcessor = mInGUIProcessor;
    private AndroidPointerCapture mPointerCapture;
    private boolean mLastGrabState = false;

    private OnRenderingStartedListener mOnRenderingStartedListener = null;
    private boolean mIsRenderingStarted = false;

    public MinecraftGLSurface(Context context) {
        this(context, null);
    }

    public MinecraftGLSurface(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
    }

    private void setUpPointerCapture(AbstractTouchpad touchpad) {
        if(mPointerCapture != null) mPointerCapture.detach();
        mPointerCapture = new AndroidPointerCapture(touchpad, this);
    }

    /** Initialize the view and all its settings
     * @param isAlreadyRunning set to true to tell the view that the game is already running
     *                         (only updates the window without calling the start listener)
     * @param touchpad the optional cursor-emulating touchpad, used for touch event processing
     *                 when the cursor is not grabbed
     */
    public void start(boolean isAlreadyRunning, AbstractTouchpad touchpad){
        setUpPointerCapture(touchpad);
        mInGUIProcessor.setAbstractTouchpad(touchpad);
        if(AllSettings.getAlternateSurface().getValue()){
            SurfaceView surfaceView = new SurfaceView(getContext());
            mSurface = surfaceView;

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                private boolean isCalled = isAlreadyRunning;
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    if(isCalled) {
                        JREUtils.setupBridgeWindow(surfaceView.getHolder().getSurface());
                        return;
                    }
                    isCalled = true;

                    realStart(surfaceView.getHolder().getSurface());
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    refreshSize();
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {}
            });

            ((ViewGroup)getParent()).addView(surfaceView);
        } else {
            TextureView textureView = new TextureView(getContext());
            textureView.setOpaque(true);
            textureView.setAlpha(1.0f);
            mSurface = textureView;

            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                private boolean isCalled = isAlreadyRunning;
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                    Surface tSurface = new Surface(surface);
                    if(isCalled) {
                        JREUtils.setupBridgeWindow(tSurface);
                        return;
                    }
                    isCalled = true;

                    realStart(tSurface);
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                    refreshSize();
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                    if (!mIsRenderingStarted) {
                        mIsRenderingStarted = true;
                        //在正式渲染画面的时候，调用这个监听器，关闭启动器背景图像，防止一些设备的半透明问题
                        if (mOnRenderingStartedListener != null) mOnRenderingStartedListener.isStarted();
                    }
                }
            });

            ((ViewGroup)getParent()).addView(textureView);
        }


    }

    /**
     * The touch event for both grabbed an non-grabbed mouse state on the touch screen
     * Does not cover the virtual mouse touchpad
     */
    @Override
    @SuppressWarnings("accessibility")
    public boolean onTouchEvent(MotionEvent e) {
        // Kinda need to send this back to the layout
        if(((ControlLayout)getParent()).getModifiable()) return false;

        // Looking for a mouse to handle, won't have an effect if no mouse exists.
        for (int i = 0; i < e.getPointerCount(); i++) {
            int toolType = e.getToolType(i);
            if(toolType == MotionEvent.TOOL_TYPE_MOUSE) {
                if(mPointerCapture != null) {
                    mPointerCapture.handleAutomaticCapture();
                    return true;
                }
            }else if(toolType != MotionEvent.TOOL_TYPE_STYLUS) continue;

            // Mouse found
            if(CallbackBridge.isGrabbing()) return false;
            CallbackBridge.sendCursorPos(   e.getX(i) * AllStaticSettings.scaleFactor, e.getY(i) * AllStaticSettings.scaleFactor);
            return true; //mouse event handled successfully
        }
        if (mIngameProcessor == null || mInGUIProcessor == null) return true;
        return mCurrentTouchProcessor.processTouchEvent(e);
    }

    private void createGamepad(View contextView, InputDevice inputDevice) {
        mGamepad = new Gamepad(contextView, inputDevice, DefaultDataProvider.INSTANCE, true);
    }

    /**
     * The event for mouse/joystick movements
     */
    @SuppressLint("NewApi")
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        int mouseCursorIndex = -1;

        if(Gamepad.isGamepadEvent(event)){
            if(mGamepad == null) createGamepad(this, event.getDevice());

            mInputManager.handleMotionEventInput(getContext(), event, mGamepad);
            return true;
        }

        for(int i = 0; i < event.getPointerCount(); i++) {
            if(event.getToolType(i) != MotionEvent.TOOL_TYPE_MOUSE && event.getToolType(i) != MotionEvent.TOOL_TYPE_STYLUS ) continue;
            // Mouse found
            mouseCursorIndex = i;
            break;
        }
        if(mouseCursorIndex == -1) return false; // we cant consoom that, theres no mice!

        // Make sure we grabbed the mouse if necessary
        updateGrabState(CallbackBridge.isGrabbing());

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_HOVER_MOVE:
                CallbackBridge.mouseX = (event.getX(mouseCursorIndex) * AllStaticSettings.scaleFactor);
                CallbackBridge.mouseY = (event.getY(mouseCursorIndex) * AllStaticSettings.scaleFactor);
                CallbackBridge.sendCursorPos(CallbackBridge.mouseX, CallbackBridge.mouseY);
                return true;
            case MotionEvent.ACTION_SCROLL:
                CallbackBridge.sendScroll(event.getAxisValue(MotionEvent.AXIS_HSCROLL), event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                return true;
            case MotionEvent.ACTION_BUTTON_PRESS:
                return sendMouseButtonUnconverted(event.getActionButton(),true);
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return sendMouseButtonUnconverted(event.getActionButton(),false);
            default:
                return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mCurrentTouchProcessor != null) {
            mCurrentTouchProcessor.dispatchTouchEvent(event, this);
        }
        return super.dispatchTouchEvent(event);
    }

    /** The event for keyboard/ gamepad button inputs */
    public boolean processKeyEvent(KeyEvent event) {
        //Log.i("KeyEvent", event.toString());

        //Filtering useless events by order of probability
        int eventKeycode = event.getKeyCode();
        if(eventKeycode == KeyEvent.KEYCODE_UNKNOWN) return true;
        if(eventKeycode == KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        if(eventKeycode == KeyEvent.KEYCODE_VOLUME_UP) return false;
        if(event.getRepeatCount() != 0) return true;
        int action = event.getAction();
        if(action == KeyEvent.ACTION_MULTIPLE) return true;
        // Ignore the cancelled up events. They occur when the user switches layouts.
        // In accordance with https://developer.android.com/reference/android/view/KeyEvent#FLAG_CANCELED
        if(action == KeyEvent.ACTION_UP &&
                (event.getFlags() & KeyEvent.FLAG_CANCELED) != 0) return true;

        //Sometimes, key events comes from SOME keys of the software keyboard
        //Even weirder, is is unknown why a key or another is selected to trigger a keyEvent
        if((event.getFlags() & KeyEvent.FLAG_SOFT_KEYBOARD) == KeyEvent.FLAG_SOFT_KEYBOARD){
            if(eventKeycode == KeyEvent.KEYCODE_ENTER) return true; //We already listen to it.
            touchCharInput.dispatchKeyEvent(event);
            return true;
        }

        //Sometimes, key events may come from the mouse
        if(event.getDevice() != null
                && ( (event.getSource() & InputDevice.SOURCE_MOUSE_RELATIVE) == InputDevice.SOURCE_MOUSE_RELATIVE
                ||   (event.getSource() & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE)  ){

            if(eventKeycode == KeyEvent.KEYCODE_BACK){
                sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT, event.getAction() == KeyEvent.ACTION_DOWN);
                return true;
            }
        }

        if(Gamepad.isGamepadEvent(event)){
            if(mGamepad == null) createGamepad(this, event.getDevice());

            mInputManager.handleKeyEventInput(getContext(), event, mGamepad);
            return true;
        }

        int index = EfficientAndroidLWJGLKeycode.getIndexByKey(eventKeycode);
        if(EfficientAndroidLWJGLKeycode.containsIndex(index)) {
            EfficientAndroidLWJGLKeycode.execKey(event, index);
            return true;
        }

        // Some events will be generated an infinite number of times when no consumed
        return (event.getFlags() & KeyEvent.FLAG_FALLBACK) == KeyEvent.FLAG_FALLBACK;
    }

    /** Convert the mouse button, then send it
     * @return Whether the event was processed
     */
    public static boolean sendMouseButtonUnconverted(int button, boolean status) {
        int glfwButton = -256;
        switch (button) {
            case MotionEvent.BUTTON_PRIMARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT;
                break;
            case MotionEvent.BUTTON_TERTIARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_MIDDLE;
                break;
            case MotionEvent.BUTTON_SECONDARY:
                glfwButton = LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT;
                break;
        }
        if(glfwButton == -256) return false;
        sendMouseButton(glfwButton, status);
        return true;
    }





    /** Called when the size need to be set at any point during the surface lifecycle **/
    public void refreshSize() {
        int newWidth = Tools.getDisplayFriendlyRes(Tools.currentDisplayMetrics.widthPixels, AllStaticSettings.scaleFactor);
        int newHeight = Tools.getDisplayFriendlyRes(Tools.currentDisplayMetrics.heightPixels, AllStaticSettings.scaleFactor);
        if (newHeight < 1 || newWidth < 1) {
            Logging.e("MGLSurface", String.format(Locale.getDefault(), "Impossible resolution : %dx%d", newWidth, newHeight));
            return;
        }
        windowWidth = newWidth;
        windowHeight = newHeight;
        if(mSurface == null){
            Logging.w("MGLSurface", "Attempt to refresh size on null surface");
            return;
        }
        if(AllSettings.getAlternateSurface().getValue()){
            SurfaceView view = (SurfaceView) mSurface;
            if(view.getHolder() != null){
                view.getHolder().setFixedSize(windowWidth, windowHeight);
            }
        }else{
            TextureView view = (TextureView)mSurface;
            if(view.getSurfaceTexture() != null){
                view.getSurfaceTexture().setDefaultBufferSize(windowWidth, windowHeight);
            }
        }

        CallbackBridge.sendUpdateWindowSize(windowWidth, windowHeight);
        EventBus.getDefault().post(new RefreshHotbarEvent());
    }

    private void realStart(Surface surface){
        // Initial size set
        refreshSize();

        //Load Minecraft options:
        MCOptions.INSTANCE.set("fullscreen", "false");
        MCOptions.INSTANCE.set("overrideWidth", String.valueOf(windowWidth));
        MCOptions.INSTANCE.set("overrideHeight", String.valueOf(windowHeight));
        forceGraphicsApiForRenderer();
        MCOptions.INSTANCE.save();
        MCOptions.INSTANCE.getMcScale();

        JREUtils.setupBridgeWindow(surface);

        new Thread(() -> {
            try {
                // Wait until the listener is attached
                synchronized(mSurfaceReadyListenerLock) {
                    if(mSurfaceReadyListener == null) mSurfaceReadyListenerLock.wait();
                }

                mSurfaceReadyListener.isReady();
            } catch (Throwable e) {
                Tools.showError(getContext(), e, true);
            }
        }, "JVM Main thread").start();
    }

    @Override
    public void onGrabState(boolean isGrabbing) {
        post(()->updateGrabState(isGrabbing));
    }

    private TouchEventProcessor pickEventProcessor(boolean isGrabbing) {
        return isGrabbing ? mIngameProcessor : mInGUIProcessor;
    }

    private void updateGrabState(boolean isGrabbing) {
        if(mLastGrabState != isGrabbing) {
            mCurrentTouchProcessor.cancelPendingActions();
            mCurrentTouchProcessor = pickEventProcessor(isGrabbing);
            mLastGrabState = isGrabbing;
        }
    }

    /**
     * Returns true if the device reports Vulkan 1.2 or higher support.
     * Uses the PackageManager hardware feature flag introduced in API 26.
     * The version value is encoded as (major << 22) | (minor << 12) | patch,
     * so Vulkan 1.2.0 = 0x00402000.
     */
    private boolean isVulkan12Supported() {
        try {
            PackageManager pm = getContext().getPackageManager();
            // 0x00402000 = (1 << 22) | (2 << 12) = Vulkan 1.2.0
            return pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION, 0x00402000);
        } catch (Throwable t) {
            Logging.w("MGLSurface", "Vulkan version check failed, assuming no Vulkan 1.2", t);
            return false;
        }
    }
    
    /**
     * @return true if the current Minecraft version is 26.2 or higher,
     *         or if the version cannot be determined (fail-safe returns false).
     */
    private void forceGraphicsApiForRenderer() {
        try {
            String versionName = sCurrentVersionName;
            if (versionName == null || versionName.isEmpty()) {
                try {
                    versionName = VersionsManager.INSTANCE.getCurrentGameInfo().getVersion();
                } catch (Throwable t) {
                    return;
                }
            }
            if (versionName == null || versionName.isEmpty()) return;
    
            File versionFolder = new File(ProfilePathHome.getVersionsHome(), versionName);
            File versionInfoFile = new File(VersionsManager.INSTANCE.getZalithVersionPath(versionFolder), "VersionInfo.json");
            if (!versionInfoFile.exists()) return;
    
            String jsonContent = Tools.read(versionInfoFile);
            JsonObject obj = JsonParser.parseString(jsonContent).getAsJsonObject();
            if (!obj.has("minecraftVersion")) return;
    
            String minecraftVersion = obj.get("minecraftVersion").getAsString();
            int hyphen = minecraftVersion.indexOf('-');
            String numericPart = (hyphen != -1) ? minecraftVersion.substring(0, hyphen) : minecraftVersion;
            String[] parts = numericPart.split("\\.");
            if (parts.length < 2) return;
    
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
    
            if (major > 26 || (major == 26 && minor >= 2)) {
                if (isVulkan12Supported()) {
                    MCOptions.INSTANCE.set("preferredGraphicsBackend", "vulkan");
                } else {
                    MCOptions.INSTANCE.set("preferredGraphicsBackend", "opengl");
                }
            }
        } catch (Throwable ignored) {}
    }
    
    /** A small interface called when the listener is ready for the first time */
    public interface SurfaceReadyListener {
        void isReady();
    }

    public void setSurfaceReadyListener(SurfaceReadyListener listener){
        synchronized (mSurfaceReadyListenerLock) {
            mSurfaceReadyListener = listener;
            mSurfaceReadyListenerLock.notifyAll();
        }
    }

    public interface OnRenderingStartedListener {
        void isStarted();
    }

    public void setOnRenderingStartedListener(OnRenderingStartedListener listener) {
        mOnRenderingStartedListener = listener;
    }
}
