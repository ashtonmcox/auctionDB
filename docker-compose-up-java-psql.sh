# !/bin/bash
# ITCS 3160-0002, Spring 2024
# Ashton Cox, ashtonmcox@outlook.com
# Andy Pham, apham21@uncc.edu
# Connor Schwab, cschwab3@uncc.edu
# David Saldivar, dsaldiva@uncc.edu
# Jamison Heinrich, jheinri2@uncc.edu
# University of North Carolina at Charlotte

#
# ATTENTION: This will stop and delete all the running containers
# Use it only if you are not using docker for other ativities
#
# docker rm $(docker stop $(docker ps -a -q))



# add  -d  to the command below if you want the containers running in background without logs
docker-compose  -f docker-compose-java-psql.yml     up      --build
