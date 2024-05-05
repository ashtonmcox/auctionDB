# 
# ITCS 3160-0002, Spring 2024
# Marco Vieira, marco.vieira@charlotte.edu
# University of North Carolina at Charlotte

FROM library/postgres


ENV POSTGRES_USER scott
ENV POSTGRES_PASSWORD tiger
ENV POSTGRES_DB dbproj

COPY dbproj.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
