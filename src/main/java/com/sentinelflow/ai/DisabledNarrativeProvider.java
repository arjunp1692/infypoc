package com.sentinelflow.ai;

import java.util.Optional;

public class DisabledNarrativeProvider implements NarrativeProvider {

    @Override
    public Optional<String> complete(String prompt) {
        return Optional.empty();
    }
}
