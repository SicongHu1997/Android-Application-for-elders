package com.example.ecare_client.alarmclock.clock;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ecare_client.R;

public class SimpleDialog extends Dialog {
    public TextView dialog_title;
    public EditText dialog_message;
    public Button bt_cancel, bt_confirm;
    private LinearLayout ll_button;
    protected Context mContext;
    private View.OnClickListener listener;
    private View customView;
    //	@Bind(R.id.icon)
    ImageView icon;


    public SimpleDialog(Context context, int style) {
        super(context, R.style.FullScreenDialog);
        mContext = context;
        customView = LayoutInflater.from(context).inflate(R.layout.dialog_simple, null);

        icon = (ImageView) customView.findViewById(R.id.icon);

        ll_button = (LinearLayout) customView.findViewById(R.id.ll_button);
        dialog_title = (TextView) customView.findViewById(R.id.dialog_title);
        setTitle("Message");
        dialog_message = (EditText) customView.findViewById(R.id.dialog_message);
        dialog_message.clearFocus();
        bt_confirm = (Button) customView.findViewById(R.id.dialog_confirm);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(customView);
        //ButterKnife  view绑定
        //ButterKnife.bind(this,customView);
    }

    public SimpleDialog setClickListener(View.OnClickListener listener) {
        this.listener = listener;
        bt_confirm.setOnClickListener(listener);
        return this;
    }

    public SimpleDialog setMessage(String message) {
        dialog_message.setText(message);
        return this;
    }

    public SimpleDialog setTitle(String title) {
        dialog_title.setText(title);
        return this;
    }

    public SimpleDialog setIcon(int iconResId) {
        dialog_title.setVisibility(View.GONE);
        icon.setVisibility(View.VISIBLE);
        icon.setBackgroundResource(iconResId);

        return this;
    }

}
