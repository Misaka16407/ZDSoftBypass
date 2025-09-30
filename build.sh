#!/bin/bash

set -e  # 遇到错误立即退出

echo "=== 开始构建 ZDSoftBypass ==="

# 清理旧文件
rm -rf build
mkdir -p build

echo "1. 编译Java代码..."
# 编译Java源代码（不需要android.jar，Xposed模块不依赖Android框架）
javac -cp "libs/api-82.jar" -d build src/com/example/zdsoftbypass/*.java

echo "2. 创建DEX文件..."
# 使用 d8 工具创建DEX
if command -v d8 >/dev/null 2>&1; then
    echo "使用 d8 工具创建DEX..."
    d8 build/com/example/zdsoftbypass/*.class --lib "libs/api-82.jar" --output build/
elif command -v dx >/dev/null 2>&1; then
    echo "使用 dx 工具创建DEX..."
    dx --dex --output=build/classes.dex build/
else
    echo "警告: 无法创建DEX文件，将直接使用class文件"
fi

echo "3. 创建简单的APK..."
# 创建基本的APK结构
mkdir -p build/apk
cd build/apk

# 创建必要的目录结构
mkdir -p assets
mkdir -p META-INF

# 创建manifest文件
cat > AndroidManifest.xml << 'EOF2'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.zdsoftbypass">
    
    <application android:label="ZDSoftBypass">
        <meta-data android:name="xposedmodule" android:value="true" />
        <meta-data android:name="xposeddescription" android:value="Bypass for ZDSoft Newsquirrel" />
        <meta-data android:name="xposedminversion" android:value="82" />
    </application>
</manifest>
EOF2

# 创建xposed_init.txt
echo "com.example.zdsoftbypass.ZDSoftBypass" > assets/xposed_init.txt

# 添加DEX文件（如果存在）
if [ -f ../classes.dex ]; then
    cp ../classes.dex classes.dex
fi

# 创建APK文件
zip -r ../../ZDSoftBypass.apk .

cd ../..

echo "=== 构建完成！ ==="
echo "APK文件位于: ZDSoftBypass.apk"
ls -la ZDSoftBypass.apk
