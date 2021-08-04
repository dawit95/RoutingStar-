package com.curation.backend.user.web;

import com.curation.backend.global.dto.ExceptionResponseDto;
import com.curation.backend.global.dto.SuccessResponseDto;
import com.curation.backend.global.service.ResponseGenerateService;
import com.curation.backend.route.exception.NoRouteException;
import com.curation.backend.user.dto.FFResponseDto;
import com.curation.backend.user.exception.NoUserException;
import com.curation.backend.user.service.ReactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/reaction")
@RestController
public class ReactionController {
    Logger logger = LoggerFactory.getLogger(ReactionController.class);

    private final ReactionService reactionService;
    private final ResponseGenerateService responseGenerateService;

    @GetMapping("/like/{userId}/{routeId}")
    public ResponseEntity<SuccessResponseDto> setLikeReaction(@PathVariable Long userId, @PathVariable Long routeId) throws NoUserException, NoRouteException {

        String message = reactionService.setLike(userId, routeId);

        SuccessResponseDto successResponseDto = responseGenerateService.generateSuccessResponse(message);

        return new ResponseEntity<SuccessResponseDto>(successResponseDto, HttpStatus.OK);
    }

    @GetMapping("/store/{userId}/{routeId}")
    public ResponseEntity<SuccessResponseDto> setStoreReaction(@PathVariable Long userId, @PathVariable Long routeId) throws NoUserException, NoRouteException {

        String message = reactionService.setStore(userId, routeId);

        SuccessResponseDto successResponseDto = responseGenerateService.generateSuccessResponse(message);

        return new ResponseEntity<SuccessResponseDto>(successResponseDto, HttpStatus.OK);
    }

    @GetMapping("/follow/{userId}")
    public ResponseEntity<SuccessResponseDto> countOfFollow(@PathVariable Long userId) throws NoUserException, NoRouteException {

        FFResponseDto ffResponseDto =  reactionService.countOfFollow(userId);

        SuccessResponseDto successResponseDto = responseGenerateService.generateSuccessResponse(ffResponseDto);

        return new ResponseEntity<SuccessResponseDto>(successResponseDto, HttpStatus.OK);
    }

    @ExceptionHandler(NoUserException.class)
    public ResponseEntity<ExceptionResponseDto> noUserHandler() {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        String message = "없는 사용자입니다.";

        ExceptionResponseDto exceptionResponseDto = responseGenerateService.generateExceptionResponse(httpStatus, message);
        return new ResponseEntity<ExceptionResponseDto>(exceptionResponseDto, httpStatus);
    }

    @ExceptionHandler(NoRouteException.class)
    public ResponseEntity<ExceptionResponseDto> noRouteHandler() {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        String message = "없는 루트입니다.";

        ExceptionResponseDto exceptionResponseDto = responseGenerateService.generateExceptionResponse(httpStatus, message);
        return new ResponseEntity<ExceptionResponseDto>(exceptionResponseDto, httpStatus);
    }

}