package com.tfs.learningsystems.util;

public class ErrorCode {

    private ErrorCode() {
        throw new IllegalStateException("Utility class");
    }

    public static final String INVALID_MODULES = "invalid_modules";

    public static final String INVALID_INTENT_LENGTH = "invalid_intent_lenght";

    public static final String CONFLICTING_DATASET = "conflicting_dataset";

    public static final String INVALID_GITHUB_INFO_DEPLOY2 =  "invalid_github_info_deploy2";

    public static final String NON_EXISTING_MODULES = "non_existing_modules";

    public static final String PROJECT_MODELS_EMPTY = "project_models_empty";
}
