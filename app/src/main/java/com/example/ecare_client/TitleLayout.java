/* Code developed by Team Morgaint
 * for Subject IT Project COMP30022
 * Team member:
 * Chengyao Xu
 * Jin Wei Loh
 * Philip Cervenjak
 * Qianqian Zheng
 * Sicong Hu
 */
package com.example.ecare_client;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TitleLayout extends LinearLayout implements View.OnClickListener{

    private TextView titletext;

    public TitleLayout(Context context, AttributeSet attrs){
        super(context,attrs);
        LayoutInflater.from(context).inflate(R.layout.title, this);

        Button titleBack = (Button) findViewById(R.id.title_back);
        Button titleEdit = (Button) findViewById(R.id.title_edit);
        titletext = (TextView) findViewById(R.id.title_text);


        titleBack.setOnClickListener(this);
        titleEdit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.title_back:
                ((Activity) getContext()).finish();
                break;
            case R.id.title_edit:
                Toast.makeText(getContext(), "You Clicked Edit button", Toast.LENGTH_SHORT).show();

        }

    }

    public void setTitleText(String text){
        this.titletext.setText(text);
    }
}