# 
# ITCS 3160-0002, Spring 2024
# Ashton Cox, ashtonmcox@outlook.com
# University of North Carolina at Charlotte

FROM maven:3-jdk-11-slim

COPY 	mvn-spring-boot /mvn-spring-boot
WORKDIR /mvn-spring-boot


RUN mvn clean package


CMD ["sh", "/mvn-spring-boot/docker.sh"]

# CMD ["/mvn-spring-boot/docker.sh"]
