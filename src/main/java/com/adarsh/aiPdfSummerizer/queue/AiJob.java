package com.adarsh.aiPdfSummerizer.queue;


public class AiJob {

    private final String prompt;
    private final java.util.function.Consumer<String> onComplete;
    private final java.util.function.Consumer<Throwable> onError;

    public AiJob(String prompt,
                 java.util.function.Consumer<String> onComplete,
                 java.util.function.Consumer<Throwable> onError) {
        this.prompt = prompt;
        this.onComplete = onComplete;
        this.onError = onError;
    }

    public String getPrompt() {
        return prompt;
    }

    public void complete(String result) {
        onComplete.accept(result);
    }

    public void fail(Throwable error) {
        onError.accept(error);
    }
}
