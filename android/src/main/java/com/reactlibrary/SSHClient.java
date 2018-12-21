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

  public void connect(final String host, final Integer port, final String username, final String password, final Promise promise) {
    new Thread(new Runnable() {
      public void  run() {
        try {
          Properties properties = new Properties();
          properties.setProperty("StrictHostKeyChecking", "no");

          // Open session
          session = jsch.getSession(username, host, port);
          session.setPassword(password);
          session.setConfig(properties);
          session.connect();

        } catch (JSchException error) {
          Log.e(LOGTAG, "Connection failed: " + error.getMessage());
          promise.reject(error);
        } catch (Exception error) {
          Log.e(LOGTAG, "Connection failed: " + error.getMessage());
          promise.reject(error);
        }
      }
    }).start();
  }
}