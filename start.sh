#!/bin/bash
APP_NAME="h-job-admin-1.0.0.jar"
PID_FILE="app.pid"

# JVM参数
JVM_OPTS="-Xms1024m -Xmx1024m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError"



# 检查是否已运行
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat $PID_FILE)
    if ps -p $OLD_PID > /dev/null 2>&1; then
        echo "服务已在运行中 PID: $OLD_PID"
        exit 1
    fi
fi

# 启动应用
echo "启动服务..."
nohup java $JVM_OPTS -jar $APP_NAME > logs/startup.log 2>&1 &
echo $! > $PID_FILE

echo "服务启动成功 PID: $!"
echo "日志: logs/startup.log"
