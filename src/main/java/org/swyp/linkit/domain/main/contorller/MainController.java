package org.swyp.linkit.domain.main.contorller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swyp.linkit.global.dto.ApiResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Default", description = "기본 API")
public class MainController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> home1() {
        String responseData = "Hello World from /";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<?>> home2() {
        String responseData = "Hello World from /home";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseData));
    }
}