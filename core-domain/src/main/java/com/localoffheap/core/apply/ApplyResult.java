package com.localoffheap.core.apply;

import com.localoffheap.core.model.StateChangeNotification;

import java.util.Optional;

public record ApplyResult(ApplyStatus status, Optional<StateChangeNotification> notification) {
}
