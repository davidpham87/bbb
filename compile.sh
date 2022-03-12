#!/bin/sh
mkdir -p META-INF/native-image
echo "[{
    "name" : "org.apache.commons.logging.impl.Jdk14Logger",
    "allDeclaredConstructors" : true,
    "allPublicConstructors" : true,
    "allDeclaredMethods" : true,
    "allPublicMethods" : true
  }, {
  "name": ...
  ]" | tee META-INF/native-image/logging.json

native-image -cp app.jar -jar app.jar \
             -H:Name=app \
             -H:+ReportExceptionStackTraces \
             -J-Dclojure.spec.skip.macros=true \
             -J-Dclojure.compiler.direct-linking=true \
             -J-Xmx3G \
             --enable-http \
             --enable-https \
             --verbose \
             --no-fallback \
             --report-unsupported-elements-at-runtime \
             --native-image-info \
             -H:+StaticExecutableWithDynamicLibC \
             -H:CCompilerOption=-pipe \
             --allow-incomplete-classpath \
             --enable-url-protocols=http,https \
             -H:ReflectionConfigurationFiles=reflection-config.json
             # --initialize-at-run-time=org.httpkit.client.ClientSslEngineFactory$SSLHolder \
             # --initialize-at-build-time=com.fasterxml.jackson,javax.xml.datatype,jdk.xml.internal.SecuritySupport


chmod +x app

echo "Size of generated native-image `ls -sh app`"
