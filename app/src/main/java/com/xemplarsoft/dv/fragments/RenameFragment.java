package com.xemplarsoft.dv.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.xemplarsoft.dv.MainActivity;
import com.xemplarsoft.dv.R;

public class RenameFragment extends DialogFragment implements View.OnClickListener {
    private String address, name;
    private EditText rename;

    public RenameFragment() { }

    public static RenameFragment newInstance(String address, String name) {
        RenameFragment frag = new RenameFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("address", address);
        frag.setArguments(args);
        return frag;
    }

//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ViewGroup rel = (ViewGroup) inflater.inflate(R.layout.dialog_rename, container);
//        rel.findViewById(R.id.dr_confirm).setOnClickListener(this);
//        rel.findViewById(R.id.dr_cancel).setOnClickListener(this);
//        return rel;
//    }
//
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        rename = view.findViewById(R.id.dr_name);
//        rename.setText(getArguments().getString("name", ""));
//        address = getArguments().getString("address", "");
//        rename.requestFocus();
//        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//    }


    public void setArguments(Bundle args) {
        super.setArguments(args);
        if(args != null){
            this.address = args.getString("address");
            this.name = args.getString("name");
        }
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getActivity() == null) return super.onCreateDialog(savedInstanceState);

        rename = new EditText(this.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        rename.setLayoutParams(lp);
        rename.setText(name);

        AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.Dialog));
        adb.setTitle(R.string.mi_rename);
        adb.setMessage("Allowed Characters: A-Z a-z 0-9 [Space] .");
        adb.setView(rename);
        adb.setPositiveButton("Confirm",  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RenameFragment.this.rename();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }

        });

        return adb.create();
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.dr_confirm:{
                rename();
                break;
            }
            case R.id.dr_cancel:{
                cancel();
                break;
            }
        }
    }

    public void rename(){
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).rename(address, rename.getText().toString());
        }
        dismiss();
    }

    public void cancel(){
        dismiss();
    }
}
