package com.sentinelflow.ai;

import java.util.Optional;

public interface NarrativeProvider {

    Optional<String> complete(String prompt);
}
