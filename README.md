# jjtransfer - RESTful API for money transfer

API can be invoked by multiple service and systems on behalf of end users.
 
 [![Build Status](https://travis-ci.org/samfrown/jjtransfer.svg?branch=master)](https://travis-ci.org/samfrown/jjtransfer)
## Design
Implemented use cases
![Use cases](design/usecases.png)

Class diagram and data model
![Class diagram](design/classes.png)

### API
#### Accounts
    POST /accounts
    GET /accounts/(id) 
    GET /accounts
 
#### Transfers

    POST /accounts/(id)/transfers
    GET /accounts/(id)/transfers
    PUT /accounts/(id)/transfers/(tid)
    POST /accounts/(id)/transfers/(tid)
    GET /transfers

## Running
Start server on 8080 port:

    java -jar <app.jar

Start server on another port:
    
    java -jar <app.jar> -Dhttp.port=8888
    
Start server by maven:
    
    mvn exec:java

## Build 
    
    mvn clean package
