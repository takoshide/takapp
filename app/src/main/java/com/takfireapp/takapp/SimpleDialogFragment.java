package com.takfireapp.takapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SimpleDialogFragment extends DialogFragment {

    // インスタンスを生成するメソッド
    public static SimpleDialogFragment newInstance() {
        return new SimpleDialogFragment();
    }

    // イベントのコールバックを受け取るためのインターフェースを実装
    public interface SimpleDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    private SimpleDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("全件削除")
                .setMessage("全てのデータが消去されます。\nよろしいですか。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(SimpleDialogFragment.this);
                    }
                })
                .setNegativeButton("キャンセル", null);
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the SimpleDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the SimpleDialogListener so we can send events to the host
            listener = (SimpleDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception;
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        // onPause でダイアログを閉じる場合
        dismiss();
    }
}