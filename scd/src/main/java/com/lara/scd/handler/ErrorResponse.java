package com.lara.scd.handler;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp,
                            String message,
                            String path) {
}