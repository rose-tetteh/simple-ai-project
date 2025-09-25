package com.example.simple_ai_project.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseHandler {
	private ResponseHandler() {
	}

    public static ResponseEntity<Object> success(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("success", true);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<Object> error(Object data, String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("message", message);
        response.put("success", false);
        return new ResponseEntity<>(response, status);
    }
}
