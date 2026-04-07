package net.kdt.pojavlaunch;

import static net.kdt.pojavlaunch.Tools.currentDisplayMetrics;
import static org.lwjgl.glfw.CallbackBridge.sendKeyPress;
import static org.lwjgl.glfw.CallbackBridge.windowHeight;
import static org.lwjgl.glfw.CallbackBridge.windowWidth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.context.ContextExecutor;
import com.movtery.zalithlauncher.databinding.ActivityGameBinding;
import com.movtery.zalithlauncher.databinding.ViewControlMenuBinding;
import com.movtery.zalithlauncher.databinding.ViewGameMenuBinding;
import com.movtery.zalithlauncher.event.single.RefreshHotbarEvent;
import com.movtery.zalithlauncher.event.value.HotbarChangeEvent;
import com.movtery.zalithlauncher.feature.MCOptions;
import com.movtery.zalithlauncher.feature.ProfileLanguageSelector;
import com.movtery.zalithlauncher.feature.background.BackgroundManager;
import com.movtery.zalithlauncher.feature.background.BackgroundType;
import com.movtery.zalithlauncher.feature.log.Logging;
import com.movtery.zalithlauncher.feature.version.Version;
import com.movtery.zalithlauncher.feature.version.VersionInfo;
import com.movtery.zalithlauncher.launch.LaunchGame;
import com.movtery.zalithlauncher.listener.SimpleTextWatcher;
import com.movtery.zalithlauncher.plugins.driver.DriverPluginManager;
import com.movtery.zalithlauncher.renderer.Renderers;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.setting.AllStaticSettings;
import com.movtery.zalithlauncher.task.Task;
import com.movtery.zalithlauncher.task.TaskExecutors;
import com.movtery.zalithlauncher.ui.activity.BaseActivity;
import com.movtery.zalithlauncher.ui.dialog.KeyboardDialog;
import com.movtery.zalithlauncher.ui.dialog.SelectControlsDialog;
import com.movtery.zalithlauncher.ui.dialog.SelectMouseDialog;
import com.movtery.zalithlauncher.ui.fragment.settings.VideoSettingsFragment;
import com.movtery.zalithlauncher.ui.subassembly.adapter.ObjectSpinnerAdapter;
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarType;
import com.movtery.zalithlauncher.ui.subassembly.hotbar.HotbarUtils;
import com.movtery.zalithlauncher.ui.subassembly.menu.ControlMenu;
import com.movtery.zalithlauncher.ui.subassembly.menu.MenuUtils;
import com.movtery.zalithlauncher.ui.subassembly.view.FloatingLoggerWindow; // NEW import
import com.movtery.zalithlauncher.ui.subassembly.view.GameMenuViewWrapper;
import com.movtery.zalithlauncher.utils.path.PathManager;
import com.movtery.zalithlauncher.utils.ZHTools;
import com.movtery.zalithlauncher.utils.anim.AnimUtils;
import com.movtery.zalithlauncher.utils.file.FileTools;
import com.movtery.zalithlauncher.utils.stringutils.StringUtils;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;

import net.kdt.pojavlaunch.customcontrols.ControlButtonMenuListener;
import net.kdt.pojavlaunch.customcontrols.ControlLayout;
import net.kdt.pojavlaunch.customcontrols.CustomControls;
import net.kdt.pojavlaunch.customcontrols.EditorExitable;
import net.kdt.pojavlaunch.customcontrols.keyboard.LwjglCharSender;
import net.kdt.pojavlaunch.customcontrols.keyboard.TouchCharInput;
import net.kdt.pojavlaunch.customcontrols.mouse.GyroControl;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.services.GameService;

import org.greenrobot.eventbus.EventBus;
import org.lwjgl.glfw.CallbackBridge;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseActivity implements ControlButtonMenuListener, EditorExitable, ServiceConnection, ViewTreeObserver.OnGlobalLayoutListener {
    public static volatile ClipboardManager GLOBAL_CLIPBOARD;
    public static final String INTENT_VERSION = "intent_version";

    volatile public static boolean isInputStackCall;

    @SuppressLint("StaticFieldLeak")
    private static ActivityGameBinding binding = null;
    public static TouchCharInput touchCharInput;
    private GameMenuViewWrapper mGameMenuWrapper;
    private GyroControl mGyroControl;
    private KeyboardDialog keyboardDialog;

    private Version minecraftVersion;

    private ViewGameMenuBinding mGameMenuBinding;
    private ViewControlMenuBinding mControlSettingsBinding;
    private MenuSettingsInitListener mMenuSettingsInitListener;
    boolean isInEditor;

    private SimpleTextWatcher mInputWatcher;
    private final AnimPlayer mInputPreviewAnim = new AnimPlayer();
    boolean isKeyboardVisible = false;

    private FloatingLoggerWindow floatingLogger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        minecraftVersion = getIntent().getParcelableExtra(INTENT_VERSION);
        if (minecraftVersion == null) throw new RuntimeException("The game version is not selected!");

        MCOptions.INSTANCE.setup(this, () -> minecraftVersion);
        if (AllSettings.getAutoSetGameLanguage().getValue()) {
            ProfileLanguageSelector.setGameLanguage(minecraftVersion, AllSettings.getGameLanguageOverridden().getValue());
        }

        Intent gameServiceIntent = new Intent(this, GameService.class);
        // Start the service a bit early
        ContextCompat.startForegroundService(this, gameServiceIntent);
        initLayout();
        CallbackBridge.addGrabListener(binding.mainTouchpad);
        CallbackBridge.addGrabListener(binding.mainGameRenderView);
        mGyroControl = new GyroControl(this);

        Window window = getWindow();
        // Enabling this on TextureView results in a broken white result
        if(AllSettings.getAlternateSurface().getValue()) window.setBackgroundDrawable(null);
        else window.setBackgroundDrawable(new ColorDrawable(Color.BLACK));

        // Set the sustained performance mode for available APIs
        window.setSustainedPerformanceMode(AllSettings.getSustainedPerformance().getValue());

        // 防止系统息屏
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ControlLayout controlLayout = binding.mainControlLayout;
        mControlSettingsBinding = ViewControlMenuBinding.inflate(getLayoutInflater());
        new ControlMenu(this, this, mControlSettingsBinding, controlLayout, false);
        mControlSettingsBinding.saveAndExport.setVisibility(View.GONE);

        binding.mainControlLayout.setModifiable(false);

        //Now, attach to the service. The game will only start when this happens, to make sure that we know the right state.
        bindService(gameServiceIntent, this, 0);

        //初始化输入监听器，当输入法遮挡了游戏画面时，将设置这个监听器
        mInputWatcher = s -> binding.inputPreview.setText(s.toString().trim());
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(this);

        Logger.setLogListener(text -> {
            runOnUiThread(() -> {
                if (floatingLogger != null) {
                    floatingLogger.appendLog(text + "\n");
                }
            });
        });
    }

    protected void initLayout() {
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        floatingLogger = new FloatingLoggerWindow(this);
        floatingLogger.hide();   // start hidden

        mGameMenuWrapper = new GameMenuViewWrapper(this, v -> onClickedMenu(), true);
        touchCharInput = binding.mainTouchCharInput;

        BackgroundManager.setBackgroundImage(this, BackgroundType.IN_GAME, binding.backgroundView, null);

        keyboardDialog = new KeyboardDialog(this).setShowSpecialButtons(false);

        binding.mainControlLayout.setMenuListener(this);

        binding.mainDrawerOptions.setScrimColor(Color.TRANSPARENT);
        binding.mainDrawerOptions.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        try {
            File latestLogFile = new File(PathManager.DIR_GAME_HOME, "latestlog.txt");
            if(!latestLogFile.exists() && !latestLogFile.createNewFile())
                throw new IOException("Failed to create a new log file");
            Logger.begin(latestLogFile.getAbsolutePath());
            // FIXME: is it safe for multi thread?
            GLOBAL_CLIPBOARD = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            binding.mainTouchCharInput.setCharacterSender(new LwjglCharSender());

            Logging.i("RdrDebug","__P_renderer=" + minecraftVersion.getRenderer());
            Renderers.INSTANCE.setCurrentRenderer(this, minecraftVersion.getRenderer(), false);
            DriverPluginManager.setDriverByName(minecraftVersion.getDriver());

            setTitle("Minecraft " + minecraftVersion.getVersionName());

            // Minecraft 1.13+
            JMinecraftVersionList.Version mVersionInfo = Tools.getVersionInfo(minecraftVersion);
            isInputStackCall = mVersionInfo.arguments != null;
            CallbackBridge.nativeSetUseInputStackQueue(isInputStackCall);

            Tools.getDisplayMetrics(this);
            windowWidth = Tools.getDisplayFriendlyRes(currentDisplayMetrics.widthPixels, 1f);
            windowHeight = Tools.getDisplayFriendlyRes(currentDisplayMetrics.heightPixels, 1f);

            // Menu
            mGameMenuBinding = ViewGameMenuBinding.inflate(getLayoutInflater());
            mMenuSettingsInitListener = new MenuSettingsInitListener(mGameMenuBinding);

            binding.mainNavigationView.removeAllViews();
            binding.mainNavigationView.addView(mGameMenuBinding.getRoot());

            binding.mainDrawerOptions.addDrawerListener(mMenuSettingsInitListener);
            binding.mainDrawerOptions.closeDrawers();

            binding.mainGameRenderView.setSurfaceReadyListener(() -> {
                try {
                    // Setup virtual mouse right before launching
                    if (AllSettings.getVirtualMouseStart().getValue()) {
                        binding.mainTouchpad.post(() -> binding.mainTouchpad.switchState());
                    }
                    LaunchGame.runGame(this, minecraftVersion, mVersionInfo);
                } catch (Throwable e) {
                    Tools.showErrorRemote(e);
                }
            });

            binding.mainGameRenderView.setOnRenderingStartedListener(() -> {
                //彻底清除背景图片，确保一些设备不再出现“半透明渲染”的问题
                BackgroundManager.clearBackgroundImage(binding.backgroundView);
                Logging.i("Rendering Game", "The game rendering has started, " +
                        "and the background image has been cleared to prevent certain issues from occurring.");
            });

            if (AllSettings.getEnableLogOutput().getValue()) {
                floatingLogger.show();
            }

            String mcInfo = "";
            VersionInfo versionInfo = minecraftVersion.getVersionInfo();
            if (versionInfo != null) {
                mcInfo = versionInfo.getInfoString();
            }
            String tipString = StringUtils.insertNewline(
                    binding.gameTip.getText(),
                    StringUtils.insertSpace(getString(R.string.game_tip_version), minecraftVersion.getVersionName())
            );
            if (!mcInfo.isEmpty()) {
                tipString = StringUtils.insertNewline(tipString, StringUtils.insertSpace(getString(R.string.game_tip_mc_info), mcInfo));
            }
            binding.gameTip.setText(tipString);
            AnimUtils.setVisibilityAnim(binding.gameTip, 1000, true, 300, new AnimUtils.AnimationListener() {
                @Override public void onStart() {}
                @Override public void onEnd() {
                    AnimUtils.setVisibilityAnim(binding.gameTip, 15000, false, 300, null);
                }
            });
        } catch (Throwable e) {
            Tools.showError(this, e, true);
        }
    }

    private void loadControls() {
        try {
            // Load keys
            binding.mainControlLayout.loadLayout(minecraftVersion.getControl());
        } catch(IOException e) {
            try {
                Logging.w("MainActivity", "Unable to load the control file, loading the default now", e);
                binding.mainControlLayout.loadLayout((String) null);
            } catch (IOException ioException) {
                Tools.showError(this, ioException);
            }
        } catch (Throwable th) {
            Tools.showError(this, th);
        }
        mGameMenuWrapper.setVisibility(!binding.mainControlLayout.hasMenuButton());
        binding.mainControlLayout.toggleControlVisible();
    }

    @Override
    public void onAttachedToWindow() {
        LauncherPreferences.computeNotchSize(this);
        loadControls();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (AllStaticSettings.enableGyro) mGyroControl.enable();
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1);
    }

    @Override
    protected void onPause() {
        mGyroControl.disable();
        if (CallbackBridge.isGrabbing()){
            sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE);
        }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_VISIBLE, 1);
    }

    @Override
    protected void onStop() {
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_VISIBLE, 0);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMenuSettingsInitListener.closeSpinner();
        CallbackBridge.removeGrabListener(binding.mainTouchpad);
        CallbackBridge.removeGrabListener(binding.mainGameRenderView);
        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
        ContextExecutor.clearActivity();

        Logger.setLogListener(null);
        if (floatingLogger != null) {
            floatingLogger.hide();
            floatingLogger = null;
        }
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mGyroControl.updateOrientation();
        Tools.updateWindowSize(this);
        binding.mainGameRenderView.refreshSize();
        runOnUiThread(() -> binding.mainControlLayout.refreshControlButtonPositions());
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        TaskExecutors.getUIHandler().postDelayed(() -> binding.mainGameRenderView.refreshSize(), 500);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            try {
                binding.mainControlLayout.loadLayout((String) null);
            } catch (IOException e) {
                Logging.e("LoadLayout", Tools.printToString(e));
            }
        }
    }

    @Override
    public boolean shouldIgnoreNotch() {
        return AllSettings.getIgnoreNotch().getValue();
    }

    @Override
    public void onGlobalLayout() {
        Rect rect = new Rect();
        View decorView = getWindow().getDecorView();
        decorView.getWindowVisibleDisplayFrame(rect);

        int screenHeight = decorView.getHeight();
        if (screenHeight * 2 / 3 > rect.bottom) {
            if (!isKeyboardVisible) {
                binding.mainTouchCharInput.addTextChangedListener(mInputWatcher);
                setInputPreview(true);
            }
            isKeyboardVisible = true;
        } else if (isKeyboardVisible) {
            binding.mainTouchCharInput.removeTextChangedListener(mInputWatcher);
            setInputPreview(false);
            isKeyboardVisible = false;
        }
    }

    //使用一个输入预览框来展示用户输入的内容
    private void setInputPreview(boolean show) {
        mInputPreviewAnim.clearEntries();
        mInputPreviewAnim.apply(new AnimPlayer.Entry(binding.inputPreviewLayout, show ? Animations.FadeIn : Animations.FadeOut))
                .setOnStart(() -> binding.inputPreviewLayout.setVisibility(View.VISIBLE))
                .setOnEnd(() -> binding.inputPreviewLayout.setVisibility(show ? View.VISIBLE : View.GONE))
                .start();
    }

    public static void toggleMouse(Context ctx) {
        if (CallbackBridge.isGrabbing()) return;

        if (binding != null) {
            Toast.makeText(ctx, binding.mainTouchpad.switchState()
                            ? R.string.control_mouseon : R.string.control_mouseoff,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(isInEditor) {
            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if(event.getAction() == KeyEvent.ACTION_DOWN) binding.mainControlLayout.askToExit(this);
                return true;
            }
            return super.dispatchKeyEvent(event);
        }
        boolean handleEvent;
        if(!(handleEvent = binding.mainGameRenderView.processKeyEvent(event))) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && !binding.mainTouchCharInput.isEnabled()) {
                if(event.getAction() != KeyEvent.ACTION_UP) return true; // We eat it anyway
                sendKeyPress(LwjglGlfwKeycode.GLFW_KEY_ESCAPE);
                return true;
            }
        }
        return handleEvent;
    }

    @Override
    public void onBackPressed() {
        // If floating logger is visible, hide it first instead of going back
        if (floatingLogger != null && floatingLogger.isVisible()) {
            floatingLogger.hide();
            return;
        }
        super.onBackPressed();
    }

    public static void switchKeyboardState() {
        if (binding != null) binding.mainTouchCharInput.switchKeyboardState();
    }

    private static void setUri(Context context, String input) {
        if(input.startsWith("file:")) {
            int truncLength = 5;
            if(input.startsWith("file://")) truncLength = 7;
            input = input.substring(truncLength);
            Logging.i("MainActivity", input);

            File inputFile = new File(input);
            FileTools.shareFile(context, inputFile);
            Logging.i("In-game Share File/Folder", "Start!");
        } else {
            ZHTools.openLink(context, input, "*/*");
        }
    }

    public static void openLink(String link) {
        Context ctx = binding.mainTouchpad.getContext(); // no more better way to obtain a context statically
        ((Activity)ctx).runOnUiThread(() -> {
            try {
                setUri(ctx, link);
            } catch (Throwable th) {
                Tools.showError(ctx, th);
            }
        });
    }

    public static void querySystemClipboard() {
        TaskExecutors.runInUIThread(()->{
            ClipData clipData = GLOBAL_CLIPBOARD.getPrimaryClip();
            if(clipData == null) {
                AWTInputBridge.nativeClipboardReceived(null, null);
                return;
            }
            ClipData.Item firstClipItem = clipData.getItemAt(0);
            //TODO: coerce to HTML if the clip item is styled
            CharSequence clipItemText = firstClipItem.getText();
            if(clipItemText == null) {
                AWTInputBridge.nativeClipboardReceived(null, null);
                return;
            }
            AWTInputBridge.nativeClipboardReceived(clipItemText.toString(), "plain");
        });
    }

    public static void putClipboardData(String data, String mimeType) {
        TaskExecutors.runInUIThread(()-> {
            ClipData clipData = null;
            switch(mimeType) {
                case "text/plain":
                    clipData = ClipData.newPlainText("AWT Paste", data);
                    break;
                case "text/html":
                    clipData = ClipData.newHtmlText("AWT Paste", data, data);
            }
            if(clipData != null) GLOBAL_CLIPBOARD.setPrimaryClip(clipData);
        });
    }

    @Override
    public void onClickedMenu() {
        DrawerLayout drawerLayout = binding.mainDrawerOptions;
        View navigationView = binding.mainNavigationView;

        boolean open = drawerLayout.isDrawerOpen(navigationView);
        if (open) drawerLayout.closeDrawer(navigationView);
        else drawerLayout.openDrawer(navigationView);

        navigationView.requestLayout();
    }

    @Override
    public void exitEditor() {
        try {
            MainActivity.binding.mainControlLayout.loadLayout((CustomControls)null);
            MainActivity.binding.mainControlLayout.setModifiable(false);
            System.gc();
            MainActivity.binding.mainControlLayout.loadLayout(minecraftVersion.getControl());
            mGameMenuWrapper.setVisibility(!binding.mainControlLayout.hasMenuButton());
        } catch (IOException e) {
            Tools.showError(this,e);
        }
        binding.mainNavigationView.removeAllViews();
        binding.mainNavigationView.addView(mGameMenuBinding.getRoot());
        isInEditor = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binding.mainGameRenderView.start(GameService.isActive(), binding.mainTouchpad);
        GameService.setActive(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /*
     * Android 14 (or some devices, at least) seems to dispatch the the captured mouse events as trackball events
     * due to a bug(?) somewhere(????)
     */
    private boolean checkCaptureDispatchConditions(MotionEvent event) {
        int eventSource = event.getSource();
        // On my device, the mouse sends events as a relative mouse device.
        // Not comparing with == here because apparently `eventSource` is a mask that can
        // sometimes indicate multiple sources, like in the case of InputDevice.SOURCE_TOUCHPAD
        // (which is *also* an InputDevice.SOURCE_MOUSE when controlling a cursor)
        return (eventSource & InputDevice.SOURCE_MOUSE_RELATIVE) != 0 ||
                (eventSource & InputDevice.SOURCE_MOUSE) != 0;
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        if(checkCaptureDispatchConditions(ev))
            return binding.mainGameRenderView.dispatchCapturedPointerEvent(ev);
        else return super.dispatchTrackballEvent(ev);
    }

    private class MenuSettingsInitListener implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, OnSpinnerItemSelectedListener<HotbarType>, DrawerLayout.DrawerListener {
        private final ViewGameMenuBinding binding;

        public MenuSettingsInitListener(ViewGameMenuBinding binding) {
            this.binding = binding;
            //初始化状态
            this.binding.hotbarWidth.setMax(currentDisplayMetrics.widthPixels / 2);
            this.binding.hotbarHeight.setMax(currentDisplayMetrics.heightPixels / 2);

            //初始化Seekbar的值
            MenuUtils.initSeekBarValue(this.binding.resolutionScaler, AllSettings.getResolutionRatio().getValue(), this.binding.resolutionScalerValue, "%");
            binding.resolutionScalerPreview.setText(VideoSettingsFragment.getResolutionRatioPreview(getResources(), AllSettings.getResolutionRatio().getValue()));
            MenuUtils.initSeekBarValue(this.binding.timeLongPressTrigger, AllSettings.getTimeLongPressTrigger().getValue(), this.binding.timeLongPressTriggerValue, "ms");
            MenuUtils.initSeekBarValue(this.binding.mouseSpeed, AllSettings.getMouseSpeed().getValue(), this.binding.mouseSpeedValue, "%");
            MenuUtils.initSeekBarValue(this.binding.gyroSensitivity, AllSettings.getGyroSensitivity().getValue(), this.binding.gyroSensitivityValue, "%");
            MenuUtils.initSeekBarValue(this.binding.hotbarHeight, AllSettings.getHotbarHeight().getValue().getValue(), this.binding.hotbarHeightValue, "px");
            MenuUtils.initSeekBarValue(this.binding.hotbarWidth, AllSettings.getHotbarWidth().getValue().getValue(), this.binding.hotbarWidthValue, "px");

            //初始化Switch的状态
            this.binding.openMemoryInfo.setChecked(AllSettings.getGameMenuShowMemory().getValue());
            this.binding.openFpsInfo.setChecked(AllSettings.getGameMenuShowFPS().getValue());
            this.binding.disableGestures.setChecked(AllSettings.getDisableGestures().getValue());
            this.binding.disableDoubleTap.setChecked(AllSettings.getDisableDoubleTap().getValue());
            this.binding.enableGyro.setChecked(AllSettings.getEnableGyro().getValue());
            this.binding.gyroInvertX.setChecked(AllSettings.getGyroInvertX().getValue());
            this.binding.gyroInvertY.setChecked(AllSettings.getGyroInvertY().getValue());

            refreshLayoutVisible(this.binding.timeLongPressTriggerLayout, !AllSettings.getDisableGestures().getValue());
            refreshLayoutVisible(this.binding.gyroLayout, AllSettings.getEnableGyro().getValue());

            //初始化点击事件
            this.binding.forceClose.setOnClickListener(this);
            this.binding.logOutput.setOnClickListener(this);
            this.binding.sendCustomKey.setOnClickListener(this);
            this.binding.openMemoryInfo.setOnCheckedChangeListener(this);
            this.binding.openMemoryInfoLayout.setOnClickListener(this);
            this.binding.openFpsInfo.setOnCheckedChangeListener(this);
            this.binding.openFpsInfoLayout.setOnClickListener(this);

            this.binding.resolutionScaler.setOnSeekBarChangeListener(this);
            this.binding.resolutionScalerRemove.setOnClickListener(this);
            this.binding.resolutionScalerAdd.setOnClickListener(this);

            this.binding.disableGestures.setOnCheckedChangeListener(this);
            this.binding.disableGesturesLayout.setOnClickListener(this);

            this.binding.disableDoubleTap.setOnCheckedChangeListener(this);
            this.binding.disableDoubleTapLayout.setOnClickListener(this);

            this.binding.timeLongPressTrigger.setOnSeekBarChangeListener(this);
            this.binding.timeLongPressTriggerRemove.setOnClickListener(this);
            this.binding.timeLongPressTriggerAdd.setOnClickListener(this);

            this.binding.mouseSpeed.setOnSeekBarChangeListener(this);
            this.binding.mouseSpeedRemove.setOnClickListener(this);
            this.binding.mouseSpeedAdd.setOnClickListener(this);

            this.binding.customMouse.setOnClickListener(this);
            this.binding.replacementCustomcontrol.setOnClickListener(this);
            this.binding.editControl.setOnClickListener(this);

            this.binding.enableGyro.setOnCheckedChangeListener(this);
            this.binding.enableGyroLayout.setOnClickListener(this);

            this.binding.gyroSensitivity.setOnSeekBarChangeListener(this);
            this.binding.gyroSensitivityRemove.setOnClickListener(this);
            this.binding.gyroSensitivityAdd.setOnClickListener(this);

            this.binding.gyroInvertX.setOnCheckedChangeListener(this);
            this.binding.gyroInvertXLayout.setOnClickListener(this);

            this.binding.gyroInvertY.setOnCheckedChangeListener(this);
            this.binding.gyroInvertYLayout.setOnClickListener(this);

            ObjectSpinnerAdapter<HotbarType> hotbarTypeAdapter = new ObjectSpinnerAdapter<>(
                    this.binding.hotbarType,
                    hotbarType -> getString(hotbarType.getNameId())
            );
            hotbarTypeAdapter.setItems(HotbarType.getEntries());
            this.binding.hotbarType.setSpinnerAdapter(hotbarTypeAdapter);
            this.binding.hotbarType.setIsFocusable(true);
            this.binding.hotbarType.setOnSpinnerItemSelectedListener(this);
            this.binding.hotbarType.selectItemByIndex(HotbarUtils.getCurrentTypeIndex());

            this.binding.hotbarHeight.setOnSeekBarChangeListener(this);
            this.binding.hotbarHeightRemove.setOnClickListener(this);
            this.binding.hotbarHeightAdd.setOnClickListener(this);

            this.binding.hotbarWidth.setOnSeekBarChangeListener(this);
            this.binding.hotbarWidthRemove.setOnClickListener(this);
            this.binding.hotbarWidthAdd.setOnClickListener(this);
        }

        private void dialogSendCustomKey() {
            keyboardDialog.setOnMultiKeycodeSelectListener(selectedKeycodes -> {
                //模拟同时按下，同时松开按键
                Task.runTask(() -> {
                    selectedKeycodes.forEach(keycode -> sendKeyPress(keycode, true));
                    return null;
                }).ended(a -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignore) {
                    }
                    selectedKeycodes.forEach(keycode -> sendKeyPress(keycode, false));
                }).execute();
            }).show();
        }

        private void sendKeyPress(int keycode, boolean isDown) {
            System.out.println("Test keycode: " + keycode);
            int lwjglKeycode = EfficientAndroidLWJGLKeycode.getValueByIndex(keycode);
            System.out.println("Test lwjglKeycode: " + lwjglKeycode);
            if (keycode >= LwjglGlfwKeycode.GLFW_KEY_UNKNOWN) {
                CallbackBridge.sendKeyPress(lwjglKeycode, CallbackBridge.getCurrentMods(), isDown);
                CallbackBridge.setModifiers(lwjglKeycode, isDown);
            }
        }

        private void replacementCustomControls() {
            SelectControlsDialog dialog = new SelectControlsDialog(MainActivity.this, file -> {
                try {
                    MainActivity.binding.mainControlLayout.loadLayout(file.getAbsolutePath());
                    //刷新：是否隐藏菜单按钮
                    mGameMenuWrapper.setVisibility(!MainActivity.binding.mainControlLayout.hasMenuButton());
                } catch (IOException ignored) {}
            });
            dialog.setTitleText(R.string.replacement_customcontrol);
            dialog.show();
        }

        private void openCustomControls() {
            MainActivity.binding.mainControlLayout.setModifiable(true);
            MainActivity.binding.mainNavigationView.removeAllViews();
            MainActivity.binding.mainNavigationView.addView(mControlSettingsBinding.getRoot());
            mGameMenuWrapper.setVisibility(true);
            isInEditor = true;
        }

        @Override public void onClick(View v) {
            if (v == binding.forceClose) ZHTools.dialogForceClose(MainActivity.this);
            else if (v == binding.logOutput) floatingLogger.toggle();
            else if (v == binding.sendCustomKey) dialogSendCustomKey();
            else if (v == binding.openMemoryInfoLayout) MenuUtils.toggleSwitchState(binding.openMemoryInfo);
            else if (v == binding.openFpsInfoLayout) MenuUtils.toggleSwitchState(binding.openFpsInfo);
            else if (v == binding.resolutionScalerRemove) MenuUtils.adjustSeekbar(binding.resolutionScaler, -1);
            else if (v == binding.resolutionScalerAdd) MenuUtils.adjustSeekbar(binding.resolutionScaler, 1);
            else if (v == binding.disableGesturesLayout) MenuUtils.toggleSwitchState(binding.disableGestures);
            else if (v == binding.disableDoubleTapLayout) MenuUtils.toggleSwitchState(binding.disableDoubleTap);
            else if (v == binding.timeLongPressTriggerRemove) MenuUtils.adjustSeekbar(binding.timeLongPressTrigger, -1);
            else if (v == binding.timeLongPressTriggerAdd) MenuUtils.adjustSeekbar(binding.timeLongPressTrigger, 1);
            else if (v == binding.mouseSpeedRemove) MenuUtils.adjustSeekbar(binding.mouseSpeed, -1);
            else if (v == binding.mouseSpeedAdd) MenuUtils.adjustSeekbar(binding.mouseSpeed, 1);
            else if (v == binding.customMouse) new SelectMouseDialog(MainActivity.this, () -> MainActivity.binding.mainTouchpad.updateMouseDrawable()).show();
            else if (v == binding.replacementCustomcontrol) replacementCustomControls();
            else if (v == binding.editControl) openCustomControls();
            else if (v == binding.enableGyroLayout) MenuUtils.toggleSwitchState(binding.enableGyro);
            else if (v == binding.gyroSensitivityRemove) MenuUtils.adjustSeekbar(binding.gyroSensitivity, -1);
            else if (v == binding.gyroSensitivityAdd) MenuUtils.adjustSeekbar(binding.gyroSensitivity, 1);
            else if (v == binding.gyroInvertXLayout) MenuUtils.toggleSwitchState(binding.gyroInvertX);
            else if (v == binding.gyroInvertYLayout) MenuUtils.toggleSwitchState(binding.gyroInvertY);
            else if (v == binding.hotbarWidthRemove) MenuUtils.adjustSeekbar(binding.hotbarWidth, -1);
            else if (v == binding.hotbarWidthAdd) MenuUtils.adjustSeekbar(binding.hotbarWidth, 1);
            else if (v == binding.hotbarHeightRemove) MenuUtils.adjustSeekbar(binding.hotbarHeight, -1);
            else if (v == binding.hotbarHeightAdd) MenuUtils.adjustSeekbar(binding.hotbarHeight, 1);
        }

        @Override
        @SuppressLint("SetTextI18n")
        public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
            updateSeekbarValue(s, !fromUser);
        }
        @Override public void onStartTrackingTouch(SeekBar s) {}
        @Override public void onStopTrackingTouch(SeekBar s) {
            updateSeekbarValue(s, true);
        }

        private void updateSeekbarValue(SeekBar seekbar, boolean saveValue) {
            int progress = seekbar == null ? 0 : seekbar.getProgress();

            if (seekbar == binding.resolutionScaler) {
                if (saveValue) AllSettings.getResolutionRatio().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.resolutionScalerValue, "%");
                binding.resolutionScalerPreview.setText(VideoSettingsFragment.getResolutionRatioPreview(getResources(), progress));

                AllStaticSettings.scaleFactor = progress / 100f;
                MainActivity.binding.mainGameRenderView.refreshSize();
            } else if (seekbar == binding.timeLongPressTrigger) {
                if (saveValue) AllSettings.getTimeLongPressTrigger().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.timeLongPressTriggerValue, "ms");
                AllStaticSettings.timeLongPressTrigger = progress;
            } else if (seekbar == binding.mouseSpeed) {
                if (saveValue) AllSettings.getMouseSpeed().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.mouseSpeedValue, "%");
            } else if (seekbar == binding.gyroSensitivity) {
                if (saveValue) AllSettings.getGyroSensitivity().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.gyroSensitivityValue, "%");
                AllStaticSettings.gyroSensitivity = progress;
            } else if (seekbar == binding.hotbarWidth) {
                if (saveValue) AllSettings.getHotbarWidth().getValue().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.hotbarWidthValue, "px");
                EventBus.getDefault().post(new HotbarChangeEvent(progress, binding.hotbarHeight.getProgress()));
            } else if (seekbar == binding.hotbarHeight) {
                if (saveValue) AllSettings.getHotbarHeight().getValue().put(progress).save();

                MenuUtils.updateSeekbarValue(progress, binding.hotbarHeightValue, "px");
                EventBus.getDefault().post(new HotbarChangeEvent(binding.hotbarWidth.getProgress(), progress));
            }
        }

        @Override public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            if (v == binding.openMemoryInfo) {
                AllSettings.getGameMenuShowMemory().put(isChecked).save();
                mGameMenuWrapper.refreshSettingsState();
            } else if (v == binding.openFpsInfo) {
                AllSettings.getGameMenuShowFPS().put(isChecked).save();
                mGameMenuWrapper.refreshSettingsState();
            } else if (v == binding.disableGestures) {
                refreshLayoutVisible(binding.timeLongPressTriggerLayout, !isChecked);
                AllSettings.getDisableGestures().put(isChecked).save();
            } else if (v == binding.disableDoubleTap) {
                AllSettings.getDisableDoubleTap().put(isChecked).save();
                AllStaticSettings.disableDoubleTap = isChecked;
            } else if (v == binding.enableGyro) {
                refreshLayoutVisible(binding.gyroLayout, isChecked);
                AllSettings.getEnableGyro().put(isChecked).save();
                //刷新陀螺仪的启用状态
                AllStaticSettings.enableGyro = isChecked;
                mGyroControl.updateOrientation();
                if (isChecked) mGyroControl.enable();
                else mGyroControl.disable();
            } else if (v == binding.gyroInvertX) {
                AllSettings.getGyroInvertX().put(isChecked).save();
                AllStaticSettings.gyroInvertX = isChecked;
            } else if (v == binding.gyroInvertY) {
                AllSettings.getGyroInvertY().put(isChecked).save();
                AllStaticSettings.gyroInvertY = isChecked;
            }
        }

        /**
         * 刷新View的可见状态
         */
        private void refreshLayoutVisible(View view, boolean visible) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        @Override public void onItemSelected(int i, @Nullable HotbarType t, int i1, HotbarType t1) {
            if (t1 == HotbarType.AUTO) {
                binding.hotbarWidthLayout.setVisibility(View.GONE);
                binding.hotbarHeightLayout.setVisibility(View.GONE);
            } else if (t1 == HotbarType.MANUALLY) {
                binding.hotbarWidthLayout.setVisibility(View.VISIBLE);
                binding.hotbarHeightLayout.setVisibility(View.VISIBLE);
                binding.hotbarWidth.setProgress(AllSettings.getHotbarWidth().getValue().getValue());
                binding.hotbarHeight.setProgress(AllSettings.getHotbarHeight().getValue().getValue());
            }

            AllSettings.getHotbarType().put(t1.getValueName()).save();
            EventBus.getDefault().post(new RefreshHotbarEvent());
        }
        @Override public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}
        @Override public void onDrawerOpened(@NonNull View drawerView) {}
        @Override public void onDrawerClosed(@NonNull View drawerView) {}
        @Override public void onDrawerStateChanged(int newState) {
            //需要在菜单状态改变的时候，关闭Hotbar类型的Spinner，这个库并没有自动关闭的功能，所以需要这么做
            //关掉！关掉！一定要关掉！
            closeSpinner();
        }

        public void closeSpinner() {
            binding.hotbarType.dismiss();
        }
    }
}