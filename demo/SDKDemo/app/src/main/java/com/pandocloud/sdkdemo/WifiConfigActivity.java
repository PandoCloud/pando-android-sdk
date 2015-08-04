package com.pandocloud.sdkdemo;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pandocloud.android.config.wifi.WifiConfigManager;
import com.pandocloud.android.config.wifi.WifiConfigMessageHandler;
import com.pandocloud.android.utils.WifiConnectUtil;

public class WifiConfigActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void start(View view) {
        String SSIDString ;
        String PasswordString;
        RadioButton SmartLink;
        String ConfigModeString;

        final TextView ConfigMessage = (TextView)findViewById(R.id.message);

        final String devicekey = "devicekey: ";
        final String failed = "failed: ";
        WifiConfigMessageHandle MyWifiConfigHandle = new WifiConfigMessageHandle(new Handler() {
            public void handleMessage(Message var1)
            {
                Log.d("wifi config", "config finished...");
                switch(var1.what)
                {
                    case WifiConfigManager.CONFIG_SUCCESS:
                        ConfigMessage.setText(devicekey + var1.obj.toString());
                        break;
                    case WifiConfigManager.CONFIG_FAILED:
                        ConfigMessage.setText(failed + var1.obj.toString());
                        break;

                    default:
                        ConfigMessage.setText(failed + var1.what);
                        break;
                }

            }
        });

        SSIDString = ((EditText)findViewById(R.id.editSsid)).getText().toString();
        PasswordString = ((EditText)findViewById(R.id.editPassword)).getText().toString();
        SmartLink = (RadioButton)findViewById(R.id.smartlink);

        if(SmartLink.isChecked())
        {
            ConfigModeString = "smartlink";
        }
        else
        {
            ConfigModeString = "hotspot";
        }

        WifiConfigManager.setMsgHandler(MyWifiConfigHandle);

        Log.d("ConfigModeString", ConfigModeString);
        Log.d("SSIDString", SSIDString);
        Log.d("PasswordString", PasswordString);

        WifiConfigManager.startConfig(this, ConfigModeString, SSIDString, PasswordString);
        ConfigMessage.setText("config start...");
    }

    public void stop(View view)
    {
        WifiConfigManager.stopConfig();
    }

    public class WifiConfigMessageHandle extends WifiConfigMessageHandler {
        public WifiConfigMessageHandle(Handler handler)
        {
            super(handler);
        }

        @Override
        public void handleMessage(Message var1)
        {
            getHandler().handleMessage(var1);

        }
    }
}
