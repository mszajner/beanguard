package io.beanguard.server.controllers;

import io.beanguard.server.exceptions.DemoLicenceAlreadyExistsException;
import io.beanguard.server.exceptions.InvalidAuthorizationHeader;
import io.beanguard.server.exceptions.InvalidLicenceTokenException;
import io.beanguard.server.exceptions.LastAdminDeletionException;
import io.beanguard.server.exceptions.LicenceNotFoundException;
import io.beanguard.server.exceptions.LicenceTransferNotFoundException;
import io.beanguard.server.exceptions.LicenceTransferExpiredException;
import io.beanguard.server.exceptions.LicenceTransferInitException;
import io.beanguard.server.exceptions.OpenOrdersExistException;
import io.beanguard.server.exceptions.OrderAlreadyProcessedException;
import io.beanguard.server.exceptions.OrderNotFoundException;
import io.beanguard.server.exceptions.ProductNotFoundException;
import io.beanguard.server.exceptions.ProFormaNotFoundException;
import io.beanguard.server.exceptions.UserNotFoundException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class, LicenceNotFoundException.class,
            OrderNotFoundException.class, ProductNotFoundException.class,
            ProFormaNotFoundException.class})
    public ProblemDetail handleNotFoundExceptions(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({InvalidAuthorizationHeader.class, MalformedJwtException.class})
    public ProblemDetail handleAuthorizationExceptions(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidLicenceTokenException.class)
    public ProblemDetail handleInvalidLicenceToken(InvalidLicenceTokenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(LicenceTransferNotFoundException.class)
    public ProblemDetail handleTransferNotFound(LicenceTransferNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LicenceTransferExpiredException.class)
    public ProblemDetail handleTransferExpired(LicenceTransferExpiredException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
    }

    @ExceptionHandler(LicenceTransferInitException.class)
    public ProblemDetail handleTransferInit(LicenceTransferInitException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(OpenOrdersExistException.class)
    public ProblemDetail handleOpenOrders(OpenOrdersExistException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setProperty("openOrderIds", ex.getOpenOrderIds());
        return pd;
    }

    @ExceptionHandler({OrderAlreadyProcessedException.class, DemoLicenceAlreadyExistsException.class,
            LastAdminDeletionException.class})
    public ProblemDetail handleConflictExceptions(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}
