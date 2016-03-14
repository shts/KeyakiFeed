package jp.shts.android.keyakifeed.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import jp.shts.android.keyakifeed.R;

public class DownloadConfirmDialog extends DialogFragment {

    private static final String TAG = DownloadConfirmDialog.class.getSimpleName();

    /**
     * Callbacks for dialog button click
     */
    public interface Callbacks {
        public void onClickPositiveButton();
        public void onClickNegativeButton();
    }

    private Callbacks callbacks;
    private String title;
    private String message;

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setDialogTitle(String title) { this.title = title; }
    public void setDialogMessage(String message) { this.message = message; }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(TextUtils.isEmpty(title) ?
                getString(R.string.dialog_confirm_download_title) : title);
        builder.setMessage(TextUtils.isEmpty(message) ?
                getString(R.string.dialog_confirm_download_message) : message);
        builder.setPositiveButton(R.string.dialog_confirm_download_positive_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callbacks != null) {
                            callbacks.onClickPositiveButton();
                        }
                    }
                });
        builder.setNegativeButton(R.string.dialog_confirm_download_negative_btn,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callbacks != null) {
                            callbacks.onClickNegativeButton();
                        }
                    }
                });
        return builder.create();
    }
}
