package org.swyp.linkit.domain.main.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.global.common.dto.ApiResponseDto;

@RestController
@RequiredArgsConstructor
@Tag(name = "Default", description = "기본 API")
public class MainController {

    @GetMapping("/")
    public ResponseEntity<ApiResponseDto<String>> home1() {
        String responseData = "Hello World from /";
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDto.success("성공", responseData));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponseDto<String>> home2() {
        String responseData = "Hello World from /home";
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponseDto.success("성공", responseData));
    }
}