package net.kdt.pojavlaunch.multirt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movtery.zalithlauncher.R;
import com.movtery.zalithlauncher.setting.AllSettings;
import com.movtery.zalithlauncher.task.Task;
import com.movtery.zalithlauncher.ui.dialog.SelectRuntimeDialog;
import com.movtery.zalithlauncher.utils.runtime.RuntimeSelectedListener;

import net.kdt.pojavlaunch.Architecture;
import net.kdt.pojavlaunch.Tools;

import java.util.List;
import java.util.Objects;

public class RTRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Runtime> mData;
    private RuntimeSelectedListener mSelectedListener;
    private SelectRuntimeDialog mDialog;
    private final int TYPE_MODE_SELECT = 0;
    private final int TYPE_MODE_EDIT = 1;
    private final int mType;
    private boolean mIsDeleting = false;

    public RTRecyclerViewAdapter(List<Runtime> mData) {
        this.mData = mData;
        this.mType = TYPE_MODE_EDIT;
    }

    public RTRecyclerViewAdapter(List<Runtime> mData, RuntimeSelectedListener listener, SelectRuntimeDialog dialog) {
        this.mData = mData;
        this.mType = TYPE_MODE_SELECT;
        this.mSelectedListener = listener;
        this.mDialog = dialog;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MODE_SELECT:
                View recyclableView1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_select_multirt_runtime, parent,false);
                return new RTSelectViewHolder(recyclableView1);
            case TYPE_MODE_EDIT:
            default:
                View recyclableView2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_multirt_runtime,parent,false);
                return new RTEditViewHolder(recyclableView2);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_MODE_EDIT) {
            ((RTEditViewHolder) holder).bindRuntime(mData.get(position), position);
        } else {
            ((RTSelectViewHolder) holder).bindRuntime(mData.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    public boolean isDefaultRuntime(Runtime rt) {
        return Objects.equals(AllSettings.getDefaultRuntime().getValue(), rt.name);
    }

    @Override
    public int getItemViewType(int position) {
        return mType;
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setDefault(Runtime rt) {
        AllSettings.getDefaultRuntime().put(rt.name).save();
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged") //not a problem, given the typical size of the list
    public void setIsEditing(boolean isEditing) {
        mIsDeleting = isEditing;
        notifyDataSetChanged();
    }

    public boolean getIsEditing(){
        return mIsDeleting;
    }

    private String getJavaVersionName(Runtime runtime) {
        String baseName = runtime.name.replace(".tar.xz", "")
                .replace("-", " ");
        return baseName + " (" + runtime.getJvmEngineDisplayName() + ")";
    }

    public class RTSelectViewHolder extends RecyclerView.ViewHolder {
        final View mainView;
        final Context mContext;
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;
        final TextView mProvidedByLauncherTextView;

        public RTSelectViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = itemView;
            mContext = itemView.getContext();
            mJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version);
            mFullJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version_full);
            mProvidedByLauncherTextView = itemView.findViewById(R.id.multirt_provided_by_launcher);
        }

        public void bindRuntime(Runtime runtime) {
            if (!Objects.equals(runtime.name, "auto")) {
                mProvidedByLauncherTextView.setVisibility(runtime.isProvidedByLauncher ? View.VISIBLE : View.GONE);

                if(runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                    mJavaVersionTextView.setText(getJavaVersionName(runtime));
                    mFullJavaVersionTextView.setText(runtime.versionString);

                    mainView.setOnClickListener(v -> selectRuntime(runtime.name));
                    return;
                }

                if (runtime.versionString == null) {
                    mFullJavaVersionTextView.setText(R.string.multirt_runtime_corrupt);
                } else {
                    mFullJavaVersionTextView.setText(mContext.getString(R.string.multirt_runtime_incompatiblearch, runtime.arch));
                }
                mJavaVersionTextView.setText(runtime.name);
                mFullJavaVersionTextView.setTextColor(Color.RED);
            } else {
                //自动选择
                mJavaVersionTextView.setText(R.string.install_auto_select);
                mFullJavaVersionTextView.setVisibility(View.GONE);
                mainView.setOnClickListener(v -> selectRuntime(null));
            }
        }

        private void selectRuntime(String jreName) {
            if (mSelectedListener != null) mSelectedListener.onSelected(jreName);
            if (mDialog != null) mDialog.dismiss();
        }
    }

    public class RTEditViewHolder extends RecyclerView.ViewHolder {
        final TextView mJavaVersionTextView;
        final TextView mFullJavaVersionTextView;
        final TextView mProvidedByLauncherTextView;
        final ColorStateList mDefaultColors;
        final Button mSetDefaultButton;
        final ImageButton mDeleteButton;
        final Context mContext;
        Runtime mCurrentRuntime;
        int mCurrentPosition;

        public RTEditViewHolder(View itemView) {
            super(itemView);
            mJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version);
            mFullJavaVersionTextView = itemView.findViewById(R.id.multirt_view_java_version_full);
            mProvidedByLauncherTextView = itemView.findViewById(R.id.multirt_provided_by_launcher);
            mSetDefaultButton = itemView.findViewById(R.id.multirt_view_setdefaultbtn);
            mDeleteButton = itemView.findViewById(R.id.multirt_view_removebtn);

            mDefaultColors =  mFullJavaVersionTextView.getTextColors();
            mContext = itemView.getContext();

            setupOnClickListeners();
        }

        @SuppressLint("NotifyDataSetChanged") // same as all the other ones
        private void setupOnClickListeners() {
            mSetDefaultButton.setOnClickListener(v -> {
                if(mCurrentRuntime != null) {
                    setDefault(mCurrentRuntime);
                    RTRecyclerViewAdapter.this.notifyDataSetChanged();
                }
            });

            mDeleteButton.setOnClickListener(v -> {
                if (mCurrentRuntime == null) return;

                Task.runTask(() -> {
                    MultiRTUtils.removeRuntimeNamed(mCurrentRuntime.name);
                    mDeleteButton.post(() -> {
                        if(getBindingAdapter() != null) {
                            mData.clear();
                            mData.addAll(MultiRTUtils.getRuntimes());
                            getBindingAdapter().notifyDataSetChanged();
                        }
                    });
                    return null;
                }).onThrowable(e -> Tools.showError(itemView.getContext(), e)).execute();
            });
        }

        public void bindRuntime(Runtime runtime, int pos) {
            mCurrentRuntime = runtime;
            mCurrentPosition = pos;

            updateButtonsVisibility(runtime);
            mProvidedByLauncherTextView.setVisibility(runtime.isProvidedByLauncher ? View.VISIBLE : View.GONE);

            if (runtime.versionString != null && Tools.DEVICE_ARCHITECTURE == Architecture.archAsInt(runtime.arch)) {
                mJavaVersionTextView.setText(getJavaVersionName(runtime));
                mFullJavaVersionTextView.setText(runtime.versionString);
                mFullJavaVersionTextView.setTextColor(mDefaultColors);

                boolean defaultRuntime = isDefaultRuntime(runtime);
                mSetDefaultButton.setEnabled(!defaultRuntime);
                mSetDefaultButton.setText(defaultRuntime ? R.string.generic_default : R.string.multirt_config_setdefault);
                return;
            }

            // Problematic runtime moment, force propose deletion
            mDeleteButton.setVisibility(View.VISIBLE);
            if (runtime.versionString == null) {
                mFullJavaVersionTextView.setText(R.string.multirt_runtime_corrupt);
            } else {
                mFullJavaVersionTextView.setText(mContext.getString(R.string.multirt_runtime_incompatiblearch, runtime.arch));
            }
            mJavaVersionTextView.setText(runtime.name);
            mFullJavaVersionTextView.setTextColor(Color.RED);
            mSetDefaultButton.setVisibility(View.GONE);
        }

        private void updateButtonsVisibility(Runtime runtime) {
            mSetDefaultButton.setVisibility(mIsDeleting ? View.INVISIBLE : View.VISIBLE);
            mDeleteButton.setVisibility(!mIsDeleting || runtime.isProvidedByLauncher ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
