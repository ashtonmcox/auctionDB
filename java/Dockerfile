/## 
# ITCS 3160-0002, Spring 2024
# Ashton Cox, ashtonmcox@outlook.com
# Andy Pham, apham21@uncc.edu
# Connor Schwab, cschwab3@uncc.edu
# David Saldivar, dsaldiva@uncc.edu
# Jamison Heinrich, jheinri2@uncc.edu
# University of North Carolina at Charlotte
 #/

FROM maven:3-jdk-11-slim

COPY 	mvn-spring-boot /mvn-spring-boot
WORKDIR /mvn-spring-boot


RUN mvn clean package


CMD ["sh", "/mvn-spring-boot/docker.sh"]

# CMD ["/mvn-spring-boot/docker.sh"]
