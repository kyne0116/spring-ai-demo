package com.example.springai;

import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Date;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final OpenAiChatModel chatModel;
    private final StreamingChatModel streamingChatModel;

    public ChatController(OpenAiChatModel chatModel, StreamingChatModel streamingChatModel) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
    }

    @PostMapping
    public ResponseEntity<ChatResponse<String>> chat(@RequestBody ChatRequest request) {
        try {
            String reply = chatModel.call(request.getMessage());
            return ResponseEntity.ok(ChatResponse.<String>builder()
                    .errcode(ChatResponse.SUCCESS_CODE)
                    .timestamp(new Date())
                    .status(ChatResponse.SUCCESS_STATUS)
                    .message("OK")
                    .data(reply)
                    .build());
        } catch (Exception e) {
            String baseUrl = System.getenv("AI_BASE_URL");
            String model = System.getenv("AI_MODEL");
            String fallback = "当前AI服务不可用，请检查配置。\n" +
                    "AI_BASE_URL=" + (baseUrl != null ? baseUrl : "(未设置)") + ", 模型=" + (model != null ? model : "(未设置)") + "。\n" +
                    "临时回显：" + request.getMessage();
            return ResponseEntity.ok(ChatResponse.<String>builder()
                    .errcode(ChatResponse.ERROR_CODE)
                    .timestamp(new Date())
                    .status(ChatResponse.ERROR_STATUS)
                    .error("Service Unavailable")
                    .message("Fallback")
                    .data(fallback)
                    .build());
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("message") String message) {
        SseEmitter emitter = new SseEmitter(0L);
        streamingChatModel.stream(message)
                .doOnNext(chunk -> {
                    try {
                        if (chunk != null && !chunk.isEmpty()) {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        }
                    } catch (Exception ignored) {}
                })
                .doOnError(e -> {
                    try {
                        String baseUrl = System.getenv("AI_BASE_URL");
                        String model = System.getenv("AI_MODEL");
                        String fb = "当前AI服务不可用，请检查配置。 AI_BASE_URL=" +
                                (baseUrl != null ? baseUrl : "(未设置)") + ", 模型=" +
                                (model != null ? model : "(未设置)") + "。 ";
                        emitter.send(SseEmitter.event().name("chunk").data(fb));
                        emitter.send(SseEmitter.event().name("chunk").data("临时回显：" + message));
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    } catch (Exception ignored) {}
                    emitter.complete();
                })
                .doOnComplete(() -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    } catch (Exception ignored) {}
                    emitter.complete();
                })
                .subscribe();
        return emitter;
    }
}
