package com.asliutkarsh.springai.entity;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"actor", "movies"})
@Getter
@Setter
public class ActorsFilms {
    private String actor;
    private List<Movies> movies;
}