#!/bin/sh

# ファイル名
JAR_FILE=$(ls -1 /drn/bin/app/*.jar)

# Javaプロセス起動用
java -jar $JAR_FILE "$@"
