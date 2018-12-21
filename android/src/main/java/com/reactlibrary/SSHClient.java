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

public class SSHClient {

  private Jsch jsch;

  private Session session;

  private ChannelSftp sftpSession;

  public SSHClient () {
    this.jsch = new Jsch();
  }

  public Session getSession() {
    return session;
  }

  public void setSftpSession(ChannelSftp sftpSession) {
    this.sftpSession = sftpSession;
  }

  public ChannelSftp getSftpSession() {
    return sftpSession;
  }

  public void connect(String host, Integer port, String username, String password, ReadableMap keyPairs) {
    Properties properties = new Properties();
    properties.setProperty("StrictHostKeyChecking", "no");

    try {
      if (password == null) {
        byte[] privateKey = keyPairs.getString("privateKey").getBytes();
        byte[] publicKey = keyPairs.hasKey("publicKey") ? keyPairs.getString("publicKey").getBytes() : null;
        byte[] passphrase = keyPairs.hasKey("passphrase") ? keyPairs.getString("passphrase").getBytes() : null;
        jsch.addIdentity("default", privateKey, publicKey, passphrase);
      } else {
        session.setPassword(password)
      }

      // Open session
      this.session = this.jsch.getSession(username, host, port);
      session.setConfig(properties);
      session.connect();

    } catch (JSchException error) {
      Log.e(LOGTAG, "Connection failed: " + error.getMessage());
      callback.invoke(error.getMessage());
    } catch (Exception error) {
      Log.e(LOGTAG, "Connection failed: " + error.getMessage());
      callback.invoke(error.getMessage());
    }
  }
}