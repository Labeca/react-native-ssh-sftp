package com.reactlibrary;

import android.os.Environment;
import android.util.Log;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
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

import okhttp3.internal.Util;

public class RNSshClientModule extends ReactContextBaseJavaModule {
  private SSHClient sshClient;
  private final ReactApplicationContext reactContext;
  private static final String LOGTAG = "RNSSHClient";
  private static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath();

  public Map<String, SSHClient> clientPool = new HashMap<>();

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
  private void connect(String host, Integer port, String username, String password, ReadableMap keyPairs, Callback callback) {
    sshClient.connect(host, port, username, password, keyPairs, callback);

    if(sshClient.getSession().isConnected()){
      callback.invoke(null, sshClient;
    }
  }

  @ReactMethod
  public void connectSFTP(final String key, final Callback callback) {
    try {
      SSHClient client = this.sshClient;
      ChannelSftp channelSftp = (ChannelSftp) client.getSession().openChannel("sftp");
      sshClient.setSftpSession(channelSftp);
      sshClient.getSftpSession().connect();

      callback.invoke(null, sshClient.getSftpSession());
    } catch (JSchException error) {
      Log.e(LOGTAG, "Error connecting SFTP:" + error.getMessage());
      callback.invoke(error.getMessage());
    }
  }

  @ReactMethod
  public void disconnectSFTP(final String key) {
    SSHClient client = this.sshClient;
    if (client.getSftpSession() != null) {
      client.getSftpSession().disconnect();
    }
  }

  @ReactMethod
  public void sftpLs(final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          ChannelSftp channelSftp = client._sftpSession;

          Vector<LsEntry> files = channelSftp.ls(path);
          WritableArray response = new WritableNativeArray();

          for (LsEntry file: files) {
            int isDir = 0;
            String filename = file.getFilename();
            if (filename.trim().equals(".") || filename.trim().equals(".."))
              continue;

            if (file.getAttrs().isDir()) {
              isDir = 1;
              filename += '/';
            }
            String str = String.format(Locale.getDefault(),
              "{\"filename\":\"%s\"," +
              "\"isDirectory\":%d," +
              "\"modificationDate\":\"%s\"," +
              "\"lastAccess\":\"%s\"," +
              "\"fileSize\":%d," +
              "\"ownerUserID\":%d," +
              "\"ownerGroupID\":%d," +
              "\"permissions\":\"%s\"," +
              "\"flags\":%d}",
              filename,
              isDir,
              file.getAttrs().getMTime(),
              file.getAttrs().getATime(),
              file.getAttrs().getSize(),
              file.getAttrs().getUId(),
              file.getAttrs().getGId(),
              file.getAttrs().getPermissions(),
              file.getAttrs().getFlags()
            );
            response.pushString(str);
          }
          callback.invoke(null, response);
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to list path " + path);
          callback.invoke("Failed to list path " + path);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpRename(final String oldPath, final String newPath, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.rename(oldPath, newPath);
          callback.invoke();
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to rename path " + oldPath);
          callback.invoke("Failed to rename path " + oldPath);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpMkdir(final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.mkdir(path);
          callback.invoke();
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to create directory " + path);
          callback.invoke("Failed to create directory " + path);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpRm(final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.rm(path);
          callback.invoke();
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to remove " + path);
          callback.invoke("Failed to remove " + path);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpRmdir(final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.rmdir(path);
          callback.invoke();
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to remove " + path);
          callback.invoke("Failed to remove " + path);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpDownload(final String filePath, final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          client._downloadContinue = true;
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.get(filePath, path, new progressMonitor(key, "DownloadProgress"));
          callback.invoke(null, path + '/' + (new File(filePath)).getName());
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to download " + filePath);
          callback.invoke("Failed to download " + filePath);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpUpload(final String filePath, final String path, final String key, final Callback callback) {
    new Thread(new Runnable()  {
      public void run() {
        try {
          SSHClient client = clientPool.get(key);
          client._uploadContinue = true;
          ChannelSftp channelSftp = client._sftpSession;
          channelSftp.put(filePath, path + '/' + (new File(filePath)).getName(), new progressMonitor(key, "UploadProgress"), ChannelSftp.OVERWRITE);
          callback.invoke();
        } catch (SftpException error) {
          Log.e(LOGTAG, "Failed to upload " + filePath);
          callback.invoke("Failed to upload " + filePath);
        }
      }
    }).start();
  }

  @ReactMethod
  public void sftpCancelDownload(final String key) {
    SSHClient client = clientPool.get(key);
    client._downloadContinue = false;
  }

  @ReactMethod
  public void sftpCancelUpload(final String key) {
    SSHClient client = clientPool.get(key);
    client._uploadContinue = false;
  }

  @ReactMethod
  public void disconnect(final String key) {
    this.closeShell(key);
    this.disconnectSFTP(key);

    SSHClient client = clientPool.get(key);
    client._session.disconnect();
  }

  private class progressMonitor implements SftpProgressMonitor {
    private long max = 0;
    private long count = 0;
    private long downloadedPerc = 0;
    private String key;
    private String name;

    public progressMonitor(String key, String name) {
      this.key = key;
      this.name = name;
    }

    public void init(int arg0, String arg1, String arg2, long arg3) {
        this.max = arg3;
    }

    public boolean count(long arg0) {
      SSHClient client = clientPool.get(this.key);
      this.count += arg0;
      long newPerc = this.count * 100 / max;
      if(newPerc % 5 == 0 && newPerc > this.downloadedPerc) {
        this.downloadedPerc = newPerc;
        WritableMap map = Arguments.createMap();
        map.putString("name", this.name);
        map.putString("key", this.key);
        map.putString("value", Long.toString(this.downloadedPerc));
        sendEvent(reactContext, this.name, map);
      }
      boolean con;
      if (this.name.equals("DownloadProgress")) {
        con = client._downloadContinue;
      } else {
        con = client._uploadContinue;
      }
      return con;
    }

    public void end() {
    }
  }
}
