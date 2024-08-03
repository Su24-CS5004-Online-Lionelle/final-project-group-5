package group5.controller;

public enum ErrorMessage {

    ERROR("Error"),
    DELETE_WATCHLIST("Failed to delete watchlist"),
    CREATE_WATCHLIST("Failed to create new watchlist"),
    IMPORT_WATCHLIST("Failed to import watchlist from '%s'");

    private final String errorMessage;

    ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage(String filepath) {
        return String.format(errorMessage, filepath);
    }

}

