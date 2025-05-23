package com.asliutkarsh.springai.entity;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilmRequest {
    private String mood;
    private String goal;
    private List<String> actors;
    private String message;
}
