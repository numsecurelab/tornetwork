package org.torproject.android.service.vpn;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashMap;

public class Tun2Socks
{
    static{
        System.loadLibrary("tun2socks");
    }

    public static interface IProtectSocket
    {
        boolean doVpnProtect(Socket socket);
        boolean doVpnProtect(DatagramSocket socket);
    };

    private static final String TAG = Tun2Socks.class.getSimpleName();
    private static final boolean LOGD = true;

    private static Thread mThread;
    private static ParcelFileDescriptor mVpnInterfaceFileDescriptor;
    private static int mVpnInterfaceMTU;
    private static String mVpnIpAddress;
    private static String mVpnNetMask;
    private static String mSocksServerAddress;
    private static String mUdpgwServerAddress;
    private static boolean mUdpgwTransparentDNS;

    private static HashMap<Integer,String> mAppUidBlacklist = new HashMap<>();
    private static Context mContext;

    // Note: this class isn't a singleton, but you can't run more
    // than one instance due to the use of global state (the lwip
    // module, etc.) in the native code.
    public static void init () {}

    public static void Start(
            Context context,
            ParcelFileDescriptor vpnInterfaceFileDescriptor,
            int vpnInterfaceMTU,
            String vpnIpAddress,
            String vpnNetMask,
            String socksServerAddress,
            String udpgwServerAddress,
            boolean udpgwTransparentDNS)
    {
        mContext = context;

        mVpnInterfaceFileDescriptor = vpnInterfaceFileDescriptor;
        mVpnInterfaceMTU = vpnInterfaceMTU;
        mVpnIpAddress = vpnIpAddress;
        mVpnNetMask = vpnNetMask;
        mSocksServerAddress = socksServerAddress;
        mUdpgwServerAddress = udpgwServerAddress;
        mUdpgwTransparentDNS = udpgwTransparentDNS;

        if (mVpnInterfaceFileDescriptor != null)
            runTun2Socks(
                    mVpnInterfaceFileDescriptor.detachFd(),
                    mVpnInterfaceMTU,
                    mVpnIpAddress,
                    mVpnNetMask,
                    mSocksServerAddress,
                    mUdpgwServerAddress,
                    mUdpgwTransparentDNS ? 1 : 0);
    }

    public static void Stop()
    {
        terminateTun2Socks();
    }

    public static void logTun2Socks(
            String level,
            String channel,
            String msg)
    {
        String logMsg = level + "(" + channel + "): " + msg;
        if (0 == level.compareTo("ERROR"))
        {
            Log.e(TAG, logMsg);
        }
        else
        {
            if (LOGD) Log.d(TAG, logMsg);
        }
    }

    private native static int runTun2Socks(
            int vpnInterfaceFileDescriptor,
            int vpnInterfaceMTU,
            String vpnIpAddress,
            String vpnNetMask,
            String socksServerAddress,
            String udpgwServerAddress,
            int udpgwTransparentDNS);

    private native static void terminateTun2Socks();


    public static void setBlacklist(HashMap<Integer,String> appUidBlacklist)
    {
        mAppUidBlacklist = appUidBlacklist;
    }

    public static void clearBlacklist()
    {
        mAppUidBlacklist.clear();
    }

    public static void addToBlacklist (int uid, String pkgId)
    {
        mAppUidBlacklist.put(uid,pkgId);
    }

    public static void removeFromBlacklist (int uid)
    {
        mAppUidBlacklist.remove(uid);
    }

}