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


public class SSHClient {

  private JSch jsch;

  private Session session;

  private ChannelSftp sftpSession;

  private static final String LOGTAG = "SSHClient";

  public SSHClient () {
    this.jsch = new JSch();
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

  public void connect(String host, Integer port, String username, String password) {
    Properties properties = new Properties();
    properties.setProperty("StrictHostKeyChecking", "no");

    try {
      // Open session
      this.session = this.jsch.getSession(username, host, port);
      this.session.setPassword(password);
      this.session.setConfig(properties);
      this.session.connect();

    } catch (JSchException error) {
      Log.e(LOGTAG, "Connection failed: " + error.getMessage());
//      callback.invoke(error.getMessage());
    } catch (Exception error) {
      Log.e(LOGTAG, "Connection failed: " + error.getMessage());
//      callback.invoke(error.getMessage());
    }
  }
}