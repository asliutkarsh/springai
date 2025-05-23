package com.asliutkarsh.springai.controller;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asliutkarsh.springai.entity.ActorsFilms;
import com.asliutkarsh.springai.entity.ErrorResponse;
import com.asliutkarsh.springai.entity.FilmRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/api/azure/")
public class AzureAiController {

    private final AzureOpenAiChatModel chatModel;
    private final ChatClient chatClient;

    public AzureAiController(AzureOpenAiChatModel chatModel) {
        this.chatModel = chatModel;
        this.chatClient = ChatClient.create(chatModel);
    }


    @GetMapping("/generate/actorFilm")
    public ResponseEntity<?> generate(@RequestBody FilmRequest request) {

        String actorList = String.join(", ", request.getActors());

        UserMessage userMessage = new UserMessage(request.getMessage());

        
        var outputParser = new BeanOutputConverter<>(ActorsFilms.class);
        var errorOutputParser = new BeanOutputConverter<>(ErrorResponse.class);

        List<String> outputFormat = List.of(outputParser.getFormat());
        String errorFormat = errorOutputParser.getFormat();

        String systemPromptText = """
                You are a film recommendation expert for HOLLYWOOD movies ONLY.

                Your job is to generate a list of 5 Hollywood movies featuring a given list of {actors}. The movies should match a desired {mood} and {goal}.

                Before generating any response, you MUST check if ALL actors listed are from Hollywood.

                STRICT RULE:
                - If EVEN ONE actor is NOT from Hollywood (e.g., Bollywood, Korean, European, or any non-Hollywood industry), DO NOT generate any movie recommendations.
                - Instead, return ONLY the following structured error JSON output (and nothing else):
                {errorFormat}

                Examples of NON-HOLLYWOOD actors include Amitabh Bachchan, Shah Rukh Khan, Song Kang-ho, Marion Cotillard.

                Hollywood actors include Tom Hanks, Scarlett Johansson, Leonardo DiCaprio, and Zendaya.

                Do not output anything else. Do not try to guess or make assumptions.

                If all actors are from Hollywood, return the response in exactly the following format:
                {outputFormat}
            """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("mood", request.getMood(), "actors", actorList, "goal", request.getGoal(), "outputFormat", outputFormat, "errorFormat", errorFormat));

        Prompt prompt = new Prompt(List.of(userMessage,systemMessage));

        // List<ActorsFilms> actorsFilms = chatClient.prompt(prompt)
        // .call()
        // .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});

        String response = chatClient.prompt(prompt)
            .call().content();


        try {
            // Try parsing as a list of movies first
            if(response != null && response.contains("error")){
                ErrorResponse error = errorOutputParser.convert(response);
                return ResponseEntity.badRequest().body(error);
            }
            List<ActorsFilms> actorsFilms = Collections.singletonList(outputParser.convert(response));
            return ResponseEntity.ok(actorsFilms);
        } catch (Exception e) {
            // Unrecognized format
            ResponseEntity<ErrorResponse> errorResponse = new ResponseEntity<>(new ErrorResponse("Invalid response format","",""), HttpStatus.INTERNAL_SERVER_ERROR);   
            return errorResponse; 
        }
    }

}

