# ITCS 3160-0002, Spring 2024
# Ashton Cox, ashtonmcox@outlook.com
# Andy Pham, apham21@uncc.edu
# Connor Schwab, cschwab3@uncc.edu
# David Saldivar, dsaldiva@uncc.edu
# Jamison Heinrich, jheinri2@uncc.edu
# University of North Carolina at Charlotte

FROM library/postgres


ENV POSTGRES_USER scott
ENV POSTGRES_PASSWORD tiger
ENV POSTGRES_DB dbproj

COPY dbproj.sql /docker-entrypoint-initdb.d/

EXPOSE 5432
