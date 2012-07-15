package com.googlecode.gtalksms.cmd;

import android.util.Log;

import com.googlecode.gtalksms.MainService;
import com.googlecode.gtalksms.tools.Logs;
import com.googlecode.gtalksms.tools.Tools;

public class LogsCmd extends CommandHandlerBase {
    
    class LogsThread implements Runnable {
        Logs mLogs;
        boolean mStop;
        int mLength;
        
        public LogsThread() {
            this(50000);
        }
        
        public LogsThread(int length) {
            mLogs = new Logs(false);
            mStop = false;
            mLength = length;
        }
        
        public void stop() {
            mLogs.stop();
            mStop = true;
        }
        
        public void run() {
            try { 
                send("Building Logs...");
                String logs = mLogs.getLogs(sContext, mLength);
                int index = 0;
                while ((index = logs.indexOf(Logs.LINE_SEPARATOR, 1000)) != -1 && !mStop) {
                    send(logs.substring(0, index));
                    logs = logs.substring(index);
                }
                if (logs.length() > 0 && logs != "\n") {
                    send(logs);
                }
            } catch (Exception e) {
                send(e.getMessage());
                Log.w(Tools.LOG_TAG, "Failed to send logs", e);
            }
            
            mLogsThread = null;
            mThread = null;
        }
    }
     
    // Execution thread
    Thread mThread;
    LogsThread mLogsThread;
    
    public LogsCmd(MainService mainService) {
        super(mainService, CommandHandlerBase.TYPE_INTERNAL, new Cmd("logs", "log"));
    }

    protected void execute(String cmd, String args) {
        String[] argsArray = splitArgs(args);
        if (isMatchingCmd("logs", cmd)) {
            if (mThread != null && mThread.isAlive()) {
                mLogsThread.stop();
            }
            if (argsArray.length == 0) {
                mLogsThread = new LogsThread();
            } else {
                mLogsThread = new LogsThread(Tools.parseInt(argsArray[0], 100));
            }
            mThread = new Thread(mLogsThread);
            mThread.start();
        }
    }
    
    @Override
    protected void initializeSubCommands() {
    }  
}
