#!/bin/bash
PID_FILE="app.pid"

# 检查PID文件
if [ ! -f "$PID_FILE" ]; then
    echo "服务未运行"
    exit 0
fi

PID=$(cat $PID_FILE)

# 停止服务
echo "停止服务 PID: $PID"
kill $PID

# 等待结束
sleep 3

# 检查是否还在运行
if ps -p $PID > /dev/null 2>&1; then
    echo "强制停止..."
    kill -9 $PID
fi

rm -f $PID_FILE
echo "服务已停止"
