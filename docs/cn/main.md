### 目录
1. [sdk介绍](#sdk介绍)
2. [准备工作](#准备工作)
2. [wifi设备配置](#wifi设备配置)
3. [获取设备devicekey](#获取设备devicekey)
4. [工具方法](#工具方法)

### sdk介绍
pando手机sdk是pandocloud物联网云平台针对手机终端提供的物联网开发工具。拥有以下功能：

* **设备配置**：配置设备上网，目前该功能主要针对wifi设备
* **设备发现**：发现周围的设备并获取其信息
* **设备代理**：将连接在手机上的设备（如蓝牙设备）通过sdk和连接上互联网，和其他设备或用户进行交互

### 准备工作
sdk使用步骤如下：
###### 1. 导入包
下载最新的sdk的jar包，并将其加入到工程的libs文件夹中
###### 2. 配置AndroidManifest.xml
sdk依赖若干系统权限，将如下权限设置加到<manifest>标签下：

``` xml
    <!-- sdk必要权限 -->
    <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
添加sdk的service到<application>标签下：

``` xml
    <!-- wifi config service -->
    <service
        android:name="com.pandocloud.android.config.WifiConfigService"
        android:enabled="true"
        android:exported="false" >
    </service>
```


### wifi设备配置
在wifi环境下，app可以利用sdk将当前wifi的ssid和密码发送给wifi设备，如果ssid和密码正确，wifi设备就能成功联网。目前根据设备类型不同，支持两种配置模式：

* **热点模式**(hotspot)：该方法基本原理是设备启动配置模式后，会开启一个wifi热点，用户将手机连接上该wifi（无密码）后，将ssid和密码发送给设备。
* **智能模式**(smart):该方法基本原理是app直接将ssid名和密码广播到当前局域网，wifi设备通过抓取探测路由器的包长变化解码出密码信息。该方法不需要用户有额外的操作，体验较好，如果设备支持的情况下，推荐采用该模式。

##### 接口说明：
wifi设备配置由类*com.pandocloud.android.config.wifi.WifiConfigManager*提供接口，相关接口：
###### 1. 设置message handler接收配置结果
app需要先实现一个处理wifi配置结果的handler,该handler必须实现*com.pandocloud.android.config.wifi.WifiConfigMessageHandler*接口：

``` java
public interface WifiConfigMessageHandler {
    public void handleMessage(Message msg);
}
```

然后将该handler设置给WifiConfigManager，后者会将wifi配置的结果通知app。

``` java
    public static void setGateWayMsgHandler(GateWayMsgHandler msgHandler)
```
###### 2. 开始配置
app调用WifiConfigManager的startConfig方法启动配置：

``` java
    /**
	 * 启动设备wifi配置
	 * @param context 传入当前activity的context
	 * @param mode 配置模式,根据设备类型可传入"hotspot"或"smart"
	 * @param ssid 需要连接的wifi名
	 * @param pwd 对应的wifi密码
	 */
public static void startConfig(Context context, String mode, String ssid, String pwd)
```
> 注意：如果是hotspot模式，app需保证用户已经连接上设备发出的wifi热点，app可以提示用户连接或者采用sdk中提供的[工具方法](#工具方法)自动连接上设备的wifi热点。

###### 3.获取设备信息 
devicekey是一串字符串，是设备身份的凭据，app再拿到devicekey后，就可以绑定该设备成为该设备的所有者。在收到设备wifi配置成功的message后，app可以调用getConfigDeviceKey获取该设备key：

``` java
    /**
	 * 启动设备wifi配置
	 * @param context 传入当前activity的context
	 * @param mode 配置模式,根据设备类型可传入"hotspot"或"smart"
	 * @param ssid 需要连接的wifi名
	 * @param pwd 对应的wifi密码
	 */
public static String getConfigDeviceKey()
```

###### 3. 结束配置
当用户主动取消配置或者配置出错时，app可以调用stopConfig方法结束配置：

``` java
public static void stopConfig()
```
> 注意：如果是hotspot模式，wifi配置成功后app如果需要获取设备信息（devicekey），则需要继续保持手机连接在设备ap上，等获取设备信息成功后，再调用stopConfig结束配置并断开和热点的连接

