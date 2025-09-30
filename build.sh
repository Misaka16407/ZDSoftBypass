#!/bin/bash

set -e  # 遇到错误立即退出

echo "=== 开始构建 ZDSoftBypass ==="

# 清理旧文件
rm -rf build
mkdir -p build

echo "1. 编译Java代码..."
# 查找android.jar
ANDROID_JAR=$(find /usr -name "android.jar" 2>/dev/null | head -1)
if [ -z "$ANDROID_JAR" ]; then
    echo "错误: 找不到android.jar"
    exit 1
fi
echo "使用 Android JAR: $ANDROID_JAR"

# 编译Java源代码
javac -cp "libs/api-82.jar:$ANDROID_JAR" -d build src/com/example/zdsoftbypass/*.java

echo "2. 创建DEX文件..."
# 使用 d8 工具替代 dx（新版本Android SDK）
if command -v d8 >/dev/null 2>&1; then
    echo "使用 d8 工具创建DEX..."
    d8 build/com/example/zdsoftbypass/*.class --lib "$ANDROID_JAR" --output build/
    mv build/classes.dex build/classes.dex 2>/dev/null || true
elif command -v dx >/dev/null 2>&1; then
    echo "使用 dx 工具创建DEX..."
    dx --dex --output=build/classes.dex build/
else
    echo "错误: 找不到 d8 或 dx 工具"
    exit 1
fi

echo "3. 准备资源文件..."
mkdir -p build/res/values
mkdir -p build/assets

cat > build/res/values/strings.xml << 'EOF2'
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">ZDSoftBypass</string>
</resources>
EOF2

echo "com.example.zdsoftbypass.ZDSoftBypass" > build/assets/xposed_init.txt

echo "4. 创建APK..."
aapt package -f -M AndroidManifest.xml -S res -I "$ANDROID_JAR" -F build/ZDSoftBypass-unsigned.apk

echo "5. 添加文件到APK..."
cd build
aapt add ZDSoftBypass-unsigned.apk classes.dex >/dev/null 2>&1 || echo "添加classes.dex"
aapt add ZDSoftBypass-unsigned.apk assets/xposed_init.txt >/dev/null 2>&1 || echo "添加xposed_init.txt"
cd ..

echo "6. 生成签名密钥..."
if [ ! -f my-release-key.keystore ]; then
    keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000 -storepass password -keypass password -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, S=Unknown, C=Unknown" >/dev/null 2>&1
fi

echo "7. 签名APK..."
# 使用 jarsigner 作为主要签名工具
jarsigner -keystore my-release-key.keystore -storepass password -keypass password -digestalg SHA1 -sigalg SHA1withRSA build/ZDSoftBypass-unsigned.apk alias_name

# 重命名已签名的APK
mv build/ZDSoftBypass-unsigned.apk build/ZDSoftBypass-signed.apk

echo "=== 构建完成！ ==="
echo "APK文件位于: build/ZDSoftBypass-signed.apk"
ls -la build/ZDSoftBypass-signed.apk
