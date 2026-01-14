package org.swyp.linkit.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private int status;
    private T data;

    // Success Response
    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .message("The request was successful")
                .status(200)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .message("The request was successful")
                .status(200)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(200)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(status)
                .data(data)
                .build();
    }

    // Error Response
    public static ApiResponse<Void> error() {
        return ApiResponse.<Void>builder()
                .message("Internal Server Error: An unexpected error occurred.")
                .status(500)
                .build();
    }

    public static ApiResponse<Void> error(int status) {
        String message = switch (status) {
            case 400 -> "Bad Request: The request is invalid.";
            case 401 -> "Unauthorized: Authentication is required.";
            case 403 -> "Forbidden: Access to the resource is denied.";
            case 404 -> "Not Found: The requested resource could not be found.";
            case 405 -> "Method Not Allowed: The HTTP method is not supported.";
            case 409 -> "Conflict: The request conflicts with the current resource state.";
            case 415 -> "Unsupported Media Type: The media format is not supported.";
            case 422 -> "Unprocessable Entity: The request could not be processed.";
            case 429 -> "Too Many Requests: Rate limit exceeded. Please try again later.";
            case 500 -> "Internal Server Error: An unexpected error occurred.";
            case 502 -> "Bad Gateway: The server received an invalid response.";
            case 503 -> "Service Unavailable: The server is temporarily unavailable. Please try again later.";
            case 504 -> "Gateway Timeout: The server did not respond in time. Please try again later.";
            default -> "Unexpected Error: An unknown error occurred. Please contact support.";
        };

        return ApiResponse.<Void>builder()
                .message(message)
                .status(status)
                .build();
    }

    public static ApiResponse<Void> error(int status, String message) {
        return ApiResponse.<Void>builder()
                .message(message)
                .status(status)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .status(status)
                .data(data)
                .build();
    }
}