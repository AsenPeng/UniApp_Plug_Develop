package com.pys.wifilibrary;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Asen
 * User: Administrator
 * Date: 2019/9/29 0029
 * Time: 下午 5:19
 * Description:
 */
public class OneModel extends WXModule {
    WifiManager mWifiManager;
    List<WifiConfiguration> mWifiConfiguration;
    Socket mSocket = null;
    OutputStream mOuts = null;
    Handler handler = new Handler();

    /**
     * 显示弹框
     */
    @JSMethod(uiThread = true)
    public void showDialog(final JSCallback jsCallback) {
        final Dialog dialog = new Dialog(mWXSDKInstance.getContext());
        LinearLayout rootLin = new LinearLayout(mWXSDKInstance.getContext());
        ViewGroup.LayoutParams rootParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootLin.setLayoutParams(rootParams);
        rootLin.setOrientation(LinearLayout.VERTICAL);
        rootLin.setBackgroundColor(Color.WHITE);

        TextView txt = new TextView(mWXSDKInstance.getContext());
        ViewGroup.LayoutParams txtParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(mWXSDKInstance.getContext(), 40f));
        txt.setTextColor(Color.BLACK);
        txt.setText("选择WIFI");
        txt.getPaint().setTextSize(sp2px(mWXSDKInstance.getContext(), 18f));
        txt.setLayoutParams(txtParams);
        txt.setGravity(Gravity.CENTER);
        rootLin.addView(txt);

        View view = new View(mWXSDKInstance.getContext());
        ViewGroup.LayoutParams viewParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(mWXSDKInstance.getContext(), 0.5f));
        view.setLayoutParams(viewParams);
        view.setBackgroundColor(Color.parseColor("#e0e0e0"));
        rootLin.addView(view);

        ScrollView scroll = new ScrollView(mWXSDKInstance.getContext());
        ViewGroup.LayoutParams scrollParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scroll.setLayoutParams(scrollParams);
        rootLin.addView(scroll);

        LinearLayout layout = new LinearLayout(mWXSDKInstance.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(layout);

        List<ScanResult> wifiList = getWifiList();
        if (wifiList != null && wifiList.size() > 0) {
            for (int i = 0; i < wifiList.size(); i++) {
                final ScanResult scanResult = wifiList.get(i);
                final TextView txtFlag = new TextView(mWXSDKInstance.getContext());
                ViewGroup.LayoutParams txtParamsFlag = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(mWXSDKInstance.getContext(), 40f));
                txtFlag.setTextColor(Color.parseColor("#666666"));
                if (scanResult != null && scanResult.SSID != null) {
                    txtFlag.setText(scanResult.SSID);
                }
                txtFlag.setLayoutParams(txtParamsFlag);
                txtFlag.getPaint().setTextSize(sp2px(mWXSDKInstance.getContext(), 16f));
                txtFlag.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                txtFlag.setPadding(dip2px(mWXSDKInstance.getContext(), 10f), 0, dip2px(mWXSDKInstance.getContext(), 10f), 0);
                layout.addView(txtFlag);
                txtFlag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = "";
                        if (scanResult != null && scanResult.SSID != null) {
                            str = scanResult.SSID;
                        }
                        jsCallback.invoke(str);
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                });

                View viewFlag = new View(mWXSDKInstance.getContext());
                ViewGroup.LayoutParams viewParamsFlag = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip2px(mWXSDKInstance.getContext(), 0.5f));
                viewFlag.setLayoutParams(viewParamsFlag);
                viewFlag.setBackgroundColor(Color.parseColor("#e0e0e0"));
                layout.addView(viewFlag);
            }
        }
        dialog.setContentView(rootLin);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.width = getScreenWith(mWXSDKInstance.getContext()) - dip2px(mWXSDKInstance.getContext(), 24f);
        windowParams.height = getScreenHeight(mWXSDKInstance.getContext()) / 2;
        window.setAttributes(windowParams);
        dialog.show();
    }

    /**
     * 连接无线网
     *
     * @param wifiName
     * @param wifiPassow
     */
    @JSMethod(uiThread = true)
    public void connectWifiWay(final String wifiName, final String wifiPassow, final JSCallback jsCallback) {
        String strs[] = {};
        new AsyncTask<String, Object, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                int result;
                try {
                    AutoWifi(wifiName, wifiPassow, 3);
                    result = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 0;
                }
                return result;
            }

            @Override
            protected void onPostExecute(Integer objs) {
                super.onPostExecute(objs);
                if (jsCallback != null) {
                    jsCallback.invoke(objs);
                }
            }
        }.execute(strs);
//        List<ScanResult> wifiList = getWifiList();
//        int wifiId;
//        if ((wifiId = isConfiguration("\"" + wifiName + "\"")) == -1) {
//            //加入了密码
//            wifiId = addWifiConfig(wifiList, wifiName, wifiPassow);
//        }
//        int result;
//        if (wifiId != -1) {
//            if (connectWifi(wifiId)) {
//                result = 1;//连接成功
//            } else {
//                result = 0;//连接失败
//            }
//        } else {
//            result = 0;//连接失败
//        }
//        jsCallback.invoke(result);
    }

    /* 根据传递过来的三个无线网络参数连接wifi网络； */
    private void AutoWifi(String ssid, String passwd, Integer type) {
        /*
         * 创建对象，打开wifi功能，等到wifi启动完成后将传递来的wifi网络添加进Network，
         * 然后等待连接诶成功后，传递设备名称，设备IP，设备端口号给connectedSocketServer方法，
         * 用来连接远程Socket服务器；Integer.valueOf(str[5])是将字符串转换为整型；
         */
        /**
         * 定义AutoWifiConfig对象，通过该对象对wifi进行操作； WifiConfig myWifi = new
         * WifiConfig(this); 不能用作全局，不然会出现刷nfc连接wifi，连接到socket，再刷nfc时程序卡死的情况；
         */
        WifiConfig myWifi = new WifiConfig(mWXSDKInstance.getContext());
        myWifi.stopAutoConnect();
        Boolean b;
        if (!isWifiEnabled()) {
            myWifi.openWifi();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                b = isWifiEnabled();
            } while (!b);
        }
        while (!isWifiConnect() || !(myWifi.getSSID().replaceAll("\"", "")).equals(ssid)) {
            myWifi.addNetwork(myWifi.setWifiParamsPassword(ssid, passwd));
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                b = isWifiConnect();
            } while (!b);
        }
    }

    /* 检查wifi是否连接成功；成功则返回true； */
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) mWXSDKInstance.getContext().getSystemService(mWXSDKInstance.getContext().CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    /* 检查wifi是否可用；是则返回true； */
    public boolean isWifiEnabled() {
        ConnectivityManager connManager = (ConnectivityManager) mWXSDKInstance.getContext().getSystemService(mWXSDKInstance.getContext().CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isAvailable();
    }

    /**
     * 连接Socket
     *
     * @param host
     * @param port
     * @param jsCallback
     */
    @JSMethod(uiThread = true)
    public void connectSocket(final String host, final int port, final JSCallback jsCallback) {
        String strs[] = {};
        new AsyncTask<String, Object, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                int result = 0;
                try {
                    closeSocket();
                    if (mSocket == null) {
                        mSocket = new Socket(host, port);
                        mOuts = mSocket.getOutputStream();
                        result = 1;
                    }
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = 0;
                }
                return result;
            }

            @Override
            protected void onPostExecute(Integer objs) {
                super.onPostExecute(objs);
                if (jsCallback != null) {
                    jsCallback.invoke(objs);
                }
            }
        }.execute(strs);
    }

    /**
     * 传输数据
     */
    @JSMethod(uiThread = true)
    public void transferInfo(final String msg, final JSCallback jsCallback) {
        String strs[] = {msg};
        new AsyncTask<String, Object, Integer>() {
            @Override
            protected Integer doInBackground(String... strings) {
                int result = 0;
                if (strings != null && strings.length > 0 && strings[0] != null) {
                    try {
                        if (mOuts != null) {
                            mOuts.write(strings[0].getBytes());//默认"utf-8"
                            result = 1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(Integer objs) {
                super.onPostExecute(objs);
                if (jsCallback != null) {
                    jsCallback.invoke(objs);
                }
            }
        }.execute(strs);
    }

    /**
     * 关闭Socket
     */
    public void closeSocket() {
        try {
            if (mOuts != null) {
                mOuts.close();
            }
            if (mSocket != null) {
                mSocket.close();//关闭
                mSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前无线网信息
     */
    @JSMethod(uiThread = true)
    public void getCurentWifi(final JSCallback jsCallback) {
        String str = "";
        try {
            mWifiManager = (WifiManager) mWXSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
            str = connectionInfo.getSSID();
        } catch (Exception e) {
            e.printStackTrace();
        }
        jsCallback.invoke(str);
    }

    /**
     * 判断是否需要添加配置
     */
    private int isConfiguration(String SSID) {
        if (mWifiConfiguration != null) {
            for (int i = 0; i < mWifiConfiguration.size(); i++) {
                if (mWifiConfiguration.get(i).SSID.equals(SSID)) {
                    return mWifiConfiguration.get(i).networkId;
                }
            }
        }
        return -1;
    }

    /**
     * 添加配置
     */
    private int addWifiConfig(List<ScanResult> scanResult, String ssid, String pass) {
        int wifiId = -1;
        if (scanResult != null) {
            for (int i = 0; i < scanResult.size(); i++) {
                ScanResult wifi = scanResult.get(i);
                if (wifi.SSID.equals(ssid)) {
                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    wifiConfig.SSID = "\"" + wifi.SSID + "\"";  //需要用双引号括起来

                    //加入了密码
                    wifiConfig.preSharedKey = "\"" + pass + "\""; //需要用双引号括起来
                    wifiConfig.hiddenSSID = false;
                    wifiConfig.status = WifiConfiguration.Status.ENABLED;
                    wifiId = mWifiManager.addNetwork(wifiConfig);
                    if (wifiId != -1) {
                        return wifiId;
                    }
                }
            }
        }
        return wifiId;
    }

    /**
     * 连接指定wifi
     */
    private boolean connectWifi(int wifiId) {
        boolean flag;
        flag = mWifiManager.enableNetwork(wifiId, true);
        return flag;

    }

    public List<ScanResult> getWifiList() {
        mWifiManager = (WifiManager) mWXSDKInstance.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        mWifiManager.startScan();
        List<ScanResult> scanWifiList = mWifiManager.getScanResults();
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();    //得到配置好的网络列表
        List<ScanResult> wifiList = new ArrayList<>();
        if (scanWifiList != null && scanWifiList.size() > 0) {
            HashMap<String, Integer> signalStrength = new HashMap<String, Integer>();
            for (int i = 0; i < scanWifiList.size(); i++) {
                ScanResult scanResult = scanWifiList.get(i);
                if (!scanResult.SSID.isEmpty()) {
                    String key = scanResult.SSID + " " + scanResult.capabilities;
                    if (!signalStrength.containsKey(key)) {
                        signalStrength.put(key, i);
                        wifiList.add(scanResult);
                    }
                }
            }
        }
        return wifiList;
    }

    /**
     * 实际绘制时，需要使用像素进行绘制，此处提供sp 转 px的方法
     *
     * @param context
     * @param spValue
     * @return
     */
    public float sp2px(Context context, float spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * scale;
    }

    /**
     * @Title: dip2px @Description: TODO(dp转px) @param @param
     * context @param @param dpValue @param @return 设定文件 @return int
     * 返回类型 @throws
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public int getScreenWith(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }
}
