#!/bin/bash

# 清理旧文件
rm -rf build
mkdir -p build

# 编译Java源代码
javac -cp "libs/api-82.jar" -d build src/com/example/zdsoftbypass/*.java

# 创建DEX文件
dx --dex --output=build/classes.dex build/

# 创建资源ARSC文件
aapt package -f -m -J build -M AndroidManifest.xml -S res -I $PREFIX/share/java/android.jar

# 创建未签名的APK
aapt package -f -M AndroidManifest.xml -S res -I $PREFIX/share/java/android.jar -F build/ZDSoftBypass-unsigned.apk

# 添加DEX文件到APK
cd build
aapt add ZDSoftBypass-unsigned.apk classes.dex
aapt add ZDSoftBypass-unsigned.apk assets/xposed_init.txt
cd ..

# 生成密钥（如果不存在）
if [ ! -f my-release-key.keystore ]; then
    keytool -genkey -v -keystore my-release-key.keystore -alias alias_name -keyalg RSA -keysize 2048 -validity 10000 -storepass password -keypass password -dname "CN=Unknown, OU=Unknown, O=Unknown, L=Unknown, S=Unknown, C=Unknown"
fi

# 签名APK
apksigner sign --ks my-release-key.keystore --ks-pass pass:password --key-pass pass:password build/ZDSoftBypass-unsigned.apk

# 重命名已签名的APK
mv build/ZDSoftBypass-unsigned.apk build/ZDSoftBypass-signed.apk

echo "构建完成！APK文件位于: build/ZDSoftBypass-signed.apk"
