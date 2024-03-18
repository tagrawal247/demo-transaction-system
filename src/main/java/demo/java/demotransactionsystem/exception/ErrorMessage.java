package demo.java.demotransactionsystem.exception;

public class ErrorMessage {
    public static final String USER_ALREADY_EXISTS = "User already exists with this email Id";
    public static final String USER_UNAUTHORIZED_TO_CREATE_ACCOUNT = "User can create account only for their own email id";
    public static final String USER_UNAUTHORIZED_TO_RETRIEVE_ACCOUNT_IBAN_MISMATCH = "Users can retrieve only their account data: IBAN mismatch";
    public static final String USER_UNAUTHORIZED_TO_TRANSFER_AMOUNT_IBAN_MISMATCH = "Sender IBAN does not belong to the requesting User";
    public static final String VALIDATION_TRANSFER_AMOUNT_NOT_ENOUGH_MONEY = "Sender IBAN does not belong to the requesting User";
    public static final String VALIDATION_TRANSFER_AMOUNT_INVALID_RECEIVER_IBAN = "Receiver's IBAN does not exists";
    public static final String VALIDATION_TRANSFER_AMOUNT_INCORRECT = "Transfer amount must be greater than 0";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Something went wrong, check with administrator";
    public static final String LOGIN_ERROR ="Either email or password is incorrect";
}
