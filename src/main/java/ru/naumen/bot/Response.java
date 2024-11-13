package ru.naumen.bot;

import ru.naumen.model.State;

public record Response(String message, State botState) {
}
