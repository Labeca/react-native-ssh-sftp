package com.reactlibrary;

import android.os.Environment;
import android.util.Log;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;


public class RNSshClientModule extends ReactContextBaseJavaModule {
  private SSHClient sshClient;
  private final ReactApplicationContext reactContext;
  private static final String LOGTAG = "RNSSHClient";
  private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath();


  public RNSshClientModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.sshClient = new SSHClient();
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNSSHClient";
  }
  
  @ReactMethod
  public void connect(final String host, final Integer port, final String username, final String password, final Promise promise) {
    this.sshClient.connect(host, port, username, password, promise);

    if(this.sshClient.getSession().isConnected()){
      promise.resolve(true);
    }
  }

  @ReactMethod
  public void connectSFTP(final Promise promise) {
    new Thread(new Runnable() {
      public void run() {
        try {
          SSHClient client = sshClient;
          ChannelSftp channelSftp = (ChannelSftp) client.getSession().openChannel("sftp");
          client.setSftpSession(channelSftp);
          client.getSftpSession().connect();

          promise.resolve(true);
        } catch (JSchException error) {
          Log.e(LOGTAG, "Error connecting SFTP:" + error.getMessage());
          promise.reject("ERROR", error.getMessage());
        }
      }
    }).start();
  }

  @ReactMethod
  public void disconnectSFTP() {
    SSHClient client = this.sshClient;
    if (client.getSftpSession() != null) {
      client.getSftpSession().disconnect();
    }
  }


  @ReactMethod
  public void sftpUpload(final String filePath, final String path, final Promise promise) {
    new Thread(new Runnable() {
      public void run() {
        try {
          SSHClient client = sshClient;
          ChannelSftp channelSftp = client.getSftpSession();
          channelSftp.put(filePath, path + '/' + (new File(filePath)).getName(), null, ChannelSftp.OVERWRITE);
          promise.resolve(true);
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to upload " + filePath);
          promise.reject(error);
        }
      }
    }).start();
  }

  @ReactMethod
  public void disconnect() {
    SSHClient client = this.sshClient;
    client.getSftpSession().disconnect();
    client.getSession().disconnect();
  }

}
