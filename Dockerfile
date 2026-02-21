# 使用支持 M1 的 Temurin JDK 17 镜像 (arm64)
FROM eclipse-temurin:17-jdk AS builder

# 设置工作目录
WORKDIR /app

# 先复制 Gradle 相关文件（利用 Docker 缓存）
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# 给 gradlew 执行权限
RUN chmod +x gradlew

# 下载依赖（不复制源码，利用缓存）
RUN ./gradlew dependencies --no-daemon

# 复制源码
COPY src ./src

# 打包应用（跳过测试以加快构建）
RUN ./gradlew bootJar --no-daemon -x test

# 第二阶段：运行环境
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
