package com.asliutkarsh.springai.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;

@JsonPropertyOrder({"error"})
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String reason;
    private String solution;
}
