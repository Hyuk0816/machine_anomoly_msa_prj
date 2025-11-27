package dev.study.portal.controller.sse;

import dev.study.portal.service.sse.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class SseController {
    private final SseEmitterService sseEmitterService;

         /**
       * SSE 연결 엔드포인트
       * 클라이언트는 이 엔드포인트로 연결하여 실시간 알림 수신
       */
      @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
      public SseEmitter subscribe(
              @RequestParam(required = false) String clientId
      ) {
          // clientId가 없으면 UUID 생성
          String id = (clientId != null) ? clientId : UUID.randomUUID().toString();
          return sseEmitterService.subscribe(id);
      }
}
