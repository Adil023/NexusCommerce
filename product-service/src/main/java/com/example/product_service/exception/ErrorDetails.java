package com.example.product_service.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    String errorCode;
    String localizedMessage;
    String requestUri;
    int value;
    String objectName;

}