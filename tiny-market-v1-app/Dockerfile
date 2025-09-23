# 基础镜像
FROM eclipse-temurin:21-jre-alpine

# 作者
MAINTAINER gzc

# 配置
ENV PARAMS=""

# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/tiny-market-v1-app.jar /tiny-market-v1-app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /tiny-market-v1-app.jar $PARAMS"]