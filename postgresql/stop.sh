# !/bin/bash
# ITCS 3160-0002, Spring 2024
# Marco Vieira, marco.vieira@charlotte.edu
# University of North Carolina at Charlotte

image="db-proj"
container="db"



docker stop $container
docker rm $container
