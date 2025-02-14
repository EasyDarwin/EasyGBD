package com.easygbs.easygbd.logger;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.easygbs.easygbd.logger.LogLevel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.LinkedList;

public class Logger {

    private static Logger instance;
    private TextView logTextView;
    private ScrollView logScrollView; // 用于滚动的 ScrollView
    private Queue<String> logQueue; // 用于存储日志
    private Handler uiHandler; // 更新 UI 的 Handler
    private Handler loggingHandler; // 处理日志的后台线程 Handler

    // 日志等级，默认是 DEBUG
    private LogLevel currentLogLevel = LogLevel.DEBUG;

    // 最大日志条数（例如，最多显示 500 行日志）
    private static final int MAX_LOG_COUNT = 500;
    // 最大字符长度（例如，最多 10000 个字符）
    private static final int MAX_TEXT_LENGTH = 10000;

    // 是否允许日志记录的标志
    private boolean isLogging = true;

    // 单例模式获取 Logger 实例
    public static synchronized Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    private Logger() {
        this.logQueue = new LinkedList<>();
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    // 设置 logTextView 和 logScrollView，供 Activity 或 Fragment 调用
    public void setLogTextView(TextView logTextView, ScrollView logScrollView) {
        this.logTextView = logTextView;
        this.logScrollView = logScrollView;
    }

    // 设置日志等级
    public void setLogLevel(LogLevel logLevel) {
        this.currentLogLevel = logLevel;
    }

    // 记录 DEBUG 日志
    public void debug(String message) {
        if (isLogging && currentLogLevel.ordinal() <= LogLevel.DEBUG.ordinal()) {
            log(message, LogLevel.DEBUG);
        }
    }

    // 记录 INFO 日志
    public void info(String message) {
        if (isLogging && currentLogLevel.ordinal() <= LogLevel.INFO.ordinal()) {
            log(message, LogLevel.INFO);
        }
    }

    // 记录 WARN 日志
    public void warn(String message) {
        if (isLogging && currentLogLevel.ordinal() <= LogLevel.WARN.ordinal()) {
            log(message, LogLevel.WARN);
        }
    }

    // 记录 ERROR 日志
    public void error(String message) {
        if (isLogging && currentLogLevel.ordinal() <= LogLevel.ERROR.ordinal()) {
            log(message, LogLevel.ERROR);
        }
    }

    // 处理日志输出
    private void log(String message, LogLevel logLevel) {
        // 只在当前日志等级大于或等于设定等级时才记录日志
        if (logLevel.ordinal() < currentLogLevel.ordinal()) {
            return; // 如果日志等级低于当前设定等级，跳过此日志
        }

        String formattedMessage = formatLogMessage(message, logLevel);

        // 如果日志队列已满，则删除最早的日志
        if (logQueue.size() >= MAX_LOG_COUNT) {
            logQueue.poll(); // 移除队列中的第一个元素
        }

        // 将符合条件的日志消息添加到队列中
        logQueue.offer(formattedMessage);

        // 在后台线程中处理日志
        processLogs();
    }

    // 格式化日志消息，包含日志等级和时间戳
    private String formatLogMessage(String message, LogLevel logLevel) {
        // 获取当前线程的 ID
        int threadId = android.os.Process.myPid();

        // 获取当前时间戳
        long timestampMillis = System.currentTimeMillis();

        // 使用 SimpleDateFormat 格式化时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedTimestamp = sdf.format(new Date(timestampMillis));

        // 格式化日志消息，加入日志等级信息
        return String.format("[%s] [%d] %s: %s", formattedTimestamp, threadId, logLevel, message);
    }

    // 逐行更新日志
    private void processLogs() {
        if (!isLogging) return; // 如果日志已停止，则不再处理日志

        // 每次更新一条日志，延迟 200 毫秒逐条更新
        uiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!logQueue.isEmpty()) {
                    String logMessage = logQueue.poll();
                    LogLevel logLevel = getLogLevelFromMessage(logMessage); // 获取日志等级
                    updateLogOnScreen(logMessage, logLevel);
                    // 延迟继续更新，直到队列为空
                    processLogs();
                }
            }
        }, 200); // 每次更新的间隔时间为 200 毫秒
    }

    // 假设你有方法可以从日志消息中解析出日志等级
    private LogLevel getLogLevelFromMessage(String logMessage) {
        if (logMessage.contains("DEBUG")) {
            return LogLevel.DEBUG;
        } else if (logMessage.contains("INFO")) {
            return LogLevel.INFO;
        } else if (logMessage.contains("WARN")) {
            return LogLevel.WARN;
        } else if (logMessage.contains("ERROR")) {
            return LogLevel.ERROR;
        }
        return LogLevel.DEBUG; // 默认等级
    }

    // 更新 logTextView 中的内容，并滚动到最后一行
    private void updateLogOnScreen(String logMessage, LogLevel logLevel) {
        // 在更新 UI 之前，先进行日志等级的检查
        if (logLevel.ordinal() < currentLogLevel.ordinal()) {
            return; // 如果日志等级低于当前设定的日志等级，跳过此日志
        }

        if (logTextView != null) {
            // 使用 append() 来追加内容而不是替换
            logTextView.append("\n" + logMessage);

            // 确保 TextView 的内容不会超过最大长度
            if (logTextView.getText().length() > MAX_TEXT_LENGTH) {
                // 清除部分旧日志，确保不超过最大字符数
                String currentText = logTextView.getText().toString();
                int excessLength = currentText.length() - MAX_TEXT_LENGTH;
                logTextView.setText(currentText.substring(excessLength)); // 删除最早的日志
            }

            // 滚动到最后一行
            if (logScrollView != null) {
                logScrollView.post(() -> logScrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        }
    }

    // 清空日志内容
    public void clearLogText() {
        if (logTextView != null) logTextView.setText("");
    }

    // 启动日志记录
    public void startLogging() {
        isLogging = true;
    }

    // 停止日志记录
    public void stopLogging() {
        isLogging = false;
        logQueue.clear();
        if (logTextView != null) {
            logTextView.setText("");
        }
    }
}
