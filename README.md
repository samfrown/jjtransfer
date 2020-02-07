# jjtransfer - RESTful API for money transfer

API can be invoked by multiple service and systems on behalf of end users.
 
## Design
Implemented use cases
![Use cases](jjtransfer/design/usecases.png)

Class diagram and data model
![Class diagram](jjtransfer/design/classes.png)

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
    DELETE /accounts/(id)/transfers/(tid)
    GET /transfers


Client - AccountRequest - ACC
Client - TransferRequest - ACC 
Client - (post transfer) - ACC update (lock)  - TRDB - TransferCommand - TRS (queue) 

(queue) - TransferCommand - TRS - ACC update (lock) 


##Running
Start server by:

    java -jar app.jar

##Build 
    
    mvn clean package

##Tests
    
    man clean test
