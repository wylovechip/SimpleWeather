package cn.edu.pku.wzc.util;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
//实现getNetworkState方法
//和服务器的交互
/*ConnectivityManager提供了很多接口用于获取系统当前的网络连接信息*/
public class NetUtil {
    public static final int NETWORN_NONE = 0;
    public static final int NETWORN_WIFI = 1;
    public static final int NETWORN_MOBILE = 2;

    public static int getNetworkState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();   //获取当前激活的网络连接信息
        if (networkInfo == null) {
            return NETWORN_NONE;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            return NETWORN_MOBILE;
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            return NETWORN_WIFI;
        }
        return NETWORN_NONE;
    }
}