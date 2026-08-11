package com.adarsh.aiPdfSummerizer.common.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue){
        super(String.format("%s not found %s : '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String message){
        super(message);
    }
}
