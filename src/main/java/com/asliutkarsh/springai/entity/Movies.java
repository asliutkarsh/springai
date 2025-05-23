package com.asliutkarsh.springai.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"title", "director", "genre", "releaseYear"})
@Data
public class Movies {
    private String title;
    private String director;
    private List<String> genre;
    private String releaseYear;    
}
