package com.juleswhite.module1;

import java.util.List;
import java.util.Map;

public interface AgentLanguage {

    LLM.Prompt constructPrompt(
            List<Tool> tools,
            Environment environment,
            List<Goal> goals,
            Memory memory
    );

    Map<String, Object> parseResponse(String response) throws Exception;



}
