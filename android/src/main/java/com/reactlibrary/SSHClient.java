package com.reactlibrary;

import android.util.Log;

import com.facebook.react.bridge.Promise;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


import java.util.Properties;


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

  public void connect(String host, Integer port, String username, String password, Promise promise) {
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
      promise.reject(error);
    } catch (Exception error) {
      Log.e(LOGTAG, "Connection failed: " + error.getMessage());
      promise.reject(error);
    }
  }
}