
## ITCS 3160-0002, Spring 2024
## Ashton Cox, ashtonmcox@outlook.com
## Andy Pham, apham21@uncc.edu
## Connor Schwab, cschwab3@uncc.edu
## David Saldivar, dsaldiva@uncc.edu
## Jamison Heinrich, jheinri2@uncc.edu
## University of North Carolina at Charlotte


## Overview of the Contents

- [####`PostgreSQL`####](postgresql) - Database ready to run in a `docker` container with or without the help of the `docker-compose` tool;
- [####`Java`####](java) - Source code of web application template in java/spark with `docker` container configured. Ready to run in `docker-compose` with PostgreSQL or in your favorite IDE.
- [####`postman`####](postman) - A collection of requests exported of postman tool;


## Auction Management System Manual

## 1. Introduction
Welcome to the Auction Management System manual! This system allows users to create, manage, and participate in auctions. It provides RESTful APIs for various functionalities, including user management, auction creation, bidding, messaging, and more.

## 2. Prerequisites
To execute this project it is required to have installed:

- `docker`
- `docker-compose`
- `maven`

## 3. Deployment
Follow these steps to deploy the Auction Management System:
Navigate to the project directory
Run docker script: 
For Mac: ./docker-compose-up-java-psql.sh
For Windows: ./docker-compose-up-java-psql.bat

## Web browser access: http://localhost:8080

  
## 4. Running the Application
After running the up script, the application will start, and the RESTful APIs will be accessible via HTTP (http://localhost:8080). In order to post or put, use Postman to send the payloads. Postman can also be used for the get methods rather than the HTTP method.

## 5. API Endpoints
The system offers the following API endpoints for various functionalities:

https://github.com/ashtonmcox/auctionDB/blob/main/API_Endpoints

## 6. Example Usage
Here are some example use cases for the Auction Management System:
  Creating a User Account: Use the createUser endpoint to create a new user account.
  Logging In: Obtain an access token by logging in using the loginUser endpoint.
  Creating an Auction: Use the create auction endpoint to create a new auction.
  Searching for Auctions: Utilize the search auction endpoint to find relevant auctions.
  Placing a Bid: Bid on an auction using the create bid endpoint.
  Editing an Auction: Edit the details of an auction you created using the edit auction endpoint.
  Viewing Application Statistics: Retrieve statistics about the application to gain insights into user activity.
  Viewing Completed Auctions: Retrieve information about completed auctions to see the results.
  Banning a User: If necessary, ban a user from the system using the ban user endpoint.

## 7. Conclusion
Congratulations! You have successfully deployed and run the Auction Management System. Refer to this manual for guidance on using the system's functionalities. If you encounter any issues or have questions, please refer to the project documentation or contact the system administrator for assistance.




