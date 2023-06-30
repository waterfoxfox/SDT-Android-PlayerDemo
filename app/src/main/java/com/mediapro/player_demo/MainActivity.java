package com.mediapro.player_demo;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.content.SharedPreferences;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final int REQUEST_PERMISSIONS = 1;
	
    private EditText mEditServerIp;
    private EditText mEditRoomId;
    private Button serverIpClearBtn;
    private Button roomIdClearBtn;
    private Button loginButton;
    private Spinner buffTimeSelector;
    private Spinner decoderTypeSelector;
    private Spinner renderTypeSelector;
    private SharedPreferences sharedPreferences;

    private int BUFF_TIME_0_MS = 0;
    private int BUFF_TIME_150_MS = 150;
    private int BUFF_TIME_200_MS = 200;
    private int BUFF_TIME_300_MS = 300;
    private int BUFF_TIME_400_MS = 400;
    private int mBuffTimeMs = BUFF_TIME_200_MS;
    private int mBuffTimeSelectIndex = 2;

    private int DECODER_TYPE_SW = 0;
    private int DECODER_TYPE_HW = 1;
    private int DECODER_TYPE_HW_FIRST = 2;
    private int mDecoderType = DECODER_TYPE_HW_FIRST;

    private int RENDER_TYPE_KEEP_RATIO = 0;
    private int RENDER_TYPE_FULL = 1;
    private int mRenderType = RENDER_TYPE_KEEP_RATIO;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        {//* Init UI
            mEditServerIp = (EditText) findViewById(R.id.login_server_ip);
            mEditRoomId = (EditText)findViewById(R.id.login_room_id);

            serverIpClearBtn = (Button)findViewById(R.id.ip_clear_btn);
            serverIpClearBtn.setOnClickListener(this);
            roomIdClearBtn = (Button)findViewById(R.id.room_clear_btn);
            roomIdClearBtn.setOnClickListener(this);
            loginButton = (Button)findViewById(R.id.btn_watch_live);
            loginButton.setOnClickListener(this);

            buffTimeSelector = (Spinner) findViewById(R.id.buff_time_selctor);
            final String[] buffTimeValues = new String[]{"0", "150", "200", "300", "400"};
            ArrayAdapter<String> adapterBuffTime = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, buffTimeValues);
            adapterBuffTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            buffTimeSelector.setAdapter(adapterBuffTime);
            buffTimeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    SwitchBuffTimeValues(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });


            decoderTypeSelector = (Spinner) findViewById(R.id.decoder_type_selctor);
            final String[] decoderTypeValues = new String[]{"软解码", "硬解码", "自动"};
            ArrayAdapter<String> adapterVideoDecodeType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, decoderTypeValues);
            adapterVideoDecodeType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            decoderTypeSelector.setAdapter(adapterVideoDecodeType);
            decoderTypeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    SwitchDecoderTypeValues(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            renderTypeSelector = (Spinner) findViewById(R.id.render_type_selctor);
            final String[] renderTypeValues = new String[]{"保持比率", "覆盖全屏"};
            ArrayAdapter<String> adapterVideoRenderType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderTypeValues);
            adapterVideoRenderType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            renderTypeSelector.setAdapter(adapterVideoRenderType);
            renderTypeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    SwitchRenderTypeValues(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            sharedPreferences = getSharedPreferences("UserInfo", 0);
            String serverIp = sharedPreferences.getString("serverIp", "");
            long roomId = sharedPreferences.getLong("roomId", 1);
            mBuffTimeSelectIndex = sharedPreferences.getInt("buffTime", 2);
            buffTimeSelector.setSelection(mBuffTimeSelectIndex);
            mDecoderType = sharedPreferences.getInt("decoderType", DECODER_TYPE_HW_FIRST);
            decoderTypeSelector.setSelection(mDecoderType);
            mRenderType = sharedPreferences.getInt("renderType", RENDER_TYPE_KEEP_RATIO);


            if (serverIp.length() != 0)
            {
                mEditServerIp.setText(serverIp);
                mEditRoomId.setText(Long.toString(roomId));
            }
        }
    }

    public static boolean Isipv4(String ipv4)
    {
        if(ipv4==null || ipv4.length()==0){
            return false;//字符串为空或者空串
        }
        String[] parts=ipv4.split("\\.");//因为java doc里已经说明, split的参数是reg, 即正则表达式, 如果用"|"分割, 则需使用"\\|"
        if(parts.length!=4){
            return false;//分割开的数组根本就不是4个数字
        }
        for(int i=0;i<parts.length;i++){
            try{
                int n=Integer.parseInt(parts[i]);
                if(n<0 || n>255){
                    return false;//数字不在正确范围内
                }
            }catch (NumberFormatException e) {
                return false;//转换数字不正确
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            int granted = PackageManager.PERMISSION_GRANTED;
            for (int r : grantResults) {
                granted |= r;
            }
            if (granted == PackageManager.PERMISSION_GRANTED) {
                //获得了权限
            } else {
                //toast(getString(R.string.no_permission));
            }
        }
    }

    @TargetApi(M)
    private void requestPermissions() {
        String[] permissions = new String[]{WRITE_EXTERNAL_STORAGE};
        boolean showRationale = false;
        for (String perm : permissions) {
            showRationale |= shouldShowRequestPermissionRationale(perm);
        }
        if (!showRationale) {
            requestPermissions(permissions, REQUEST_PERMISSIONS);
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.using_your_sdcard_record_log))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        requestPermissions(permissions, REQUEST_PERMISSIONS))
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    //检查应用权限
    private boolean hasPermissions() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        int granted =  pm.checkPermission(WRITE_EXTERNAL_STORAGE, packageName);
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch(view.getId()){
            case R.id.ip_clear_btn:
                mEditServerIp.setText("");
                break;
            case R.id.room_clear_btn:
                mEditRoomId.setText("");
                break;
            case R.id.btn_watch_live:
                if (!hasPermissions())
                {
                    if (Build.VERSION.SDK_INT >= M)
                    {
                        //符合动态申请权限时
                        requestPermissions();
                        return;
                    }
                }

                //服务IP地址
                String serverIp = mEditServerIp.getEditableText().toString();
                if (Isipv4(serverIp) == false)
                {
                    Toast.makeText(MainActivity.this, "IP地址非法", Toast.LENGTH_SHORT).show();
                    return;
                }

                //房间ID
                String strRoomId = mEditRoomId.getEditableText().toString().trim();
                if(strRoomId.length() == 0)
                {
                    Toast.makeText(this, "请输入房间ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                long roomId = 1;
                try
                {
                    roomId = Long.parseLong(strRoomId);
                } catch (Exception e) {
                    Toast.makeText(this, "请输入合法的房间ID", Toast.LENGTH_SHORT).show();
                    return;
                }

                buffTimeSelector.getSelectedItemPosition();
                sharedPreferences = getSharedPreferences("UserInfo", 0);
                sharedPreferences.edit()
                        .putString("serverIp", serverIp)
                        .putLong("roomId", roomId)
                        .putInt("buffTime", mBuffTimeSelectIndex)
                        .putInt("decoderType", mDecoderType)
                        .putInt("renderType", mRenderType)
                        .commit();
                SwitchBuffTimeValues(mBuffTimeSelectIndex);

                Intent it = new Intent(this, PlayerActivity.class);
                Bundle bd = new Bundle();
                bd.putString("server_ip", serverIp);
                bd.putLong("room_id", roomId);
                bd.putInt("buff_time", mBuffTimeMs);
                bd.putInt("decoder_type", mDecoderType);
                bd.putInt("render_type", mRenderType);
                it.putExtras(bd);
                startActivity(it);

                break;
        }
    }

    void SwitchBuffTimeValues(int position) {
        mBuffTimeSelectIndex = position;
        switch (position) {
            case 0:
                mBuffTimeMs = BUFF_TIME_0_MS;
                break;
            case 1:
                mBuffTimeMs = BUFF_TIME_150_MS;
                break;
            case 2:
                mBuffTimeMs = BUFF_TIME_200_MS;
                break;
            case 3:
                mBuffTimeMs = BUFF_TIME_300_MS;
                break;
            case 4:
                mBuffTimeMs = BUFF_TIME_400_MS;
                break;
            default:
                mBuffTimeMs = BUFF_TIME_200_MS;
        }
    }

    void SwitchDecoderTypeValues(int position) {
        switch (position) {
            case 0:
                mDecoderType = DECODER_TYPE_SW;
                break;
            case 1:
                mDecoderType = DECODER_TYPE_HW;
                break;
            case 2:
                mDecoderType = DECODER_TYPE_HW_FIRST;
                break;
            default:
                mDecoderType = DECODER_TYPE_HW_FIRST;
        }
    }

    void SwitchRenderTypeValues(int position) {
        switch (position) {
            case 0:
                mRenderType = RENDER_TYPE_KEEP_RATIO;
                break;
            case 1:
                mRenderType = RENDER_TYPE_FULL;
                break;
            default:
                mRenderType = RENDER_TYPE_KEEP_RATIO;
        }
    }
}
