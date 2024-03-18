# demo-transaction-system

This microservice serves as a practical demonstration of a transaction system. It provides simple APis for account management and transactions. It's designed to illustrate essential banking functionalities in a clear and concise manner.

This system provides following key functionalities:

- **Sign Up:** New users can easily create an account by providing basic details such as name, email, and password.
- **Login:** Existing users can log in using their email and password credentials to receive JWT which needed for account management and transfer.
- **Create Account:** Users can open multiple accounts by specifying their name, email, and opening balance.
- **Retrieve Account Details:** Users can retrieve details of all their accounts or a specific account using unique identifiers such as IBAN.

- **Transfer Amount:** Facilitates transfer of funds between accounts
## Base URL

[http://localhost:8080/api/v1](http://localhost:8080/api/v1)

## Swagger URL

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Prerequisites:

* java 17
* docker
* maven

## To run follow the sequence from command prompt from root project directory :

* mvn clean install
* docker build -t demotransactionsystem .
* docker-compose up


# Demo Transaction System Flow Description

## Sign Up

1. User enters their name, email address, and password.
2. System validates the input:
    - Checks if all required values are provided.
    - Verifies if the email is not already registered in the system.
3. If validation passes, the password is encrypted and stored in the users table.
4. User is notified whether the request was successful or failed.

## Login

1. User provides their email and password.
2. System verifies the provided credentials:
    - Checks if the email and password match an existing user record.
3. If credentials are valid, a JWT token is generated and returned.
4. If credentials are invalid, an error response indicating invalid credentials is returned.

## Create Account

1. User submits a request to create a new account with their name, email, and opening balance.
2. System validates the request:
    - Verifies if the email in the request matches the email associated with the JWT token.
    - Validates the presence of all required values.
    - Opening balance should be zero or more. 
3. If validation passes, a new account is created with the provided details and stored in the Accounts table.
4. The system returns the generated IBAN number along with other account details.

## Retrieve All Accounts

1. User sends a request to retrieve all accounts.
2. User email is taken from JWT for further processing.
3. The system retrieves all account details associated with the user's email and returns them in an array.

## Retrieve Single Account

1. User sends a request to retrieve details of a specific account identified by its accountId.
2. System validates the request:
    - Verifies if the accountId belongs to the user associated with the JWT token.
3. If validation passes, the system retrieves the account details and returns them.
4. If the accountId does not belong to the user or the account is not found, an error response is returned.

## Transfer Amount

1. User initiates a request to transfer an amount from one account to another.
2. System validates the request:
    - Verifies if the sender's IBAN belongs to the user associated with the JWT token.
    - Checks if the sender has sufficient balance for the transfer.
    - Validates if the receiver's IBAN exists in the Accounts table.
    - Verify transfer amount > 0
3. If validation passes, the system transfers the specified amount from the sender to the receiver.
4. The system updates the account balances for both the sender and receiver accordingly.


# Future Enhancements :
1. Improve Security: We need tighter implementation of JWT. Currently, JWT is used just for demonstration purpose used only to validate user email. In the future, we can improve to make sure only few APIs can be called without JWT.
2. Transaction Audit Record: Current system implements a very basic transaction mechanism. We would like to improve it to keep audit records for each transaction It can be used for both provide statement to user and keep status of transactions. 
3. Improve Observability: Logging and metrics needs to be added to improve debugging and system maintenance. 
4. Add UUID to track each request end to end.
4. Add extra checks and facility to verify opening account balance while creating account.
5. Add account type and descriptions for different purposes