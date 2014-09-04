package com.github.wpm;

import java.util.List;

/**
 * Break text into tokens
 */
public interface Tokenizer {
    List<String> tokenize(String text);
}
