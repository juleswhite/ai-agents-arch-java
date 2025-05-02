I'll update the README with the requested changes.

# Building AI Agents in Java

[![Coursera Course](https://img.shields.io/badge/Coursera-Building%20AI%20Agents%20in%20Java-blue)](https://www.coursera.org/learn/ai-agents-java)

This repository contains the implementation code for the Coursera course [Building AI Agents in Java](https://www.coursera.org/learn/ai-agents-java). The course provides a comprehensive guide to building LLM-powered agents that can interact with systems and perform complex tasks.

## Course Overview

This hands-on course teaches you how to build AI agents that can reason about their environment and take actions to achieve goals. You'll learn how to implement the GAME architecture (Goals, Actions, Memory, Environment) from scratch and build increasingly powerful agents that can solve real-world problems.

## Repository Structure

The code is organized into four modules, each corresponding to a section of the course:

```
com.juleswhite.module1/ - Foundations of AI Agents
com.juleswhite.module2/ - Agent Tools and Function Calling
com.juleswhite.module3/ - Building a Reusable Agent Framework
com.juleswhite.module4/ - Advanced Agent Applications
```

## Key Features

### Module 1: Foundations of AI Agents
- Introduction to LLM-based agents
- Basic prompting techniques for agents
- Core agent loop implementation
- First working agent examples

### Module 2: Agent Tools and Function Calling
- Defining tools using JSON Schema
- Function calling with LLMs
- Structured input/output with agents
- Tool discovery and registration

### Module 3: Building a Reusable Agent Framework
- The GAME architecture (Goals, Actions, Memory, Environment)
- Creating a modular agent framework
- Implementation of ActionRegistry, Environment, and more
- Supporting both static and instance method tools

### Module 4: Advanced Agent Applications
- Extending the agent framework with annotations
- Implementing automatic tool discovery
- Building a secure file explorer agent
- Practical applications and real-world examples

## Getting Started

### Prerequisites
- Java 11 or newer
- Maven or Gradle for dependency management
- OpenAI API key

### Project Setup

#### IntelliJ IDEA
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ai-agents-java.git
   ```
2. **Open in IntelliJ**:
    - Select `File > Open` and navigate to the cloned repository directory
    - Wait for the IDE to index the files and download dependencies

3. **Set up OpenAI API key**:
    - Go to `Run > Edit Configurations`
    - In the "Environment variables" field, add: `OPENAI_API_KEY=your_api_key_here`
    - Alternatively, for a permanent solution:
        - Go to `Help > Edit Custom VM Options`
        - Add: `-DOPENAI_API_KEY=your_api_key_here`

#### Eclipse
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ai-agents-java.git
   ```
2. **Import project**:
    - Select `File > Import > Maven > Existing Maven Projects`
    - Navigate to the repository directory and select the pom.xml file

3. **Set up OpenAI API key**:
    - Right-click on your project and select `Run As > Run Configurations`
    - In the "Environment" tab, click "New" and add:
        - Name: `OPENAI_API_KEY`
        - Value: `your_api_key_here`

#### VS Code
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ai-agents-java.git
   ```
2. **Open in VS Code**:
   ```bash
   code ai-agents-java
   ```
3. **Install Extensions**:
    - Install the "Extension Pack for Java" from the marketplace

4. **Set up OpenAI API key**:
    - Create a `.vscode/launch.json` file if it doesn't exist
    - Add the following configuration:
   ```json
   {
     "version": "0.2.0",
     "configurations": [
       {
         "type": "java",
         "name": "Run Agent",
         "request": "launch",
         "mainClass": "com.juleswhite.module4.FileExplorerAgent",
         "env": {
           "OPENAI_API_KEY": "your_api_key_here"
         }
       }
     ]
   }
   ```

### Running Example Agents

Each module comes with example agents that you can run to see the concepts in action:

```bash
# Example: Running the File Explorer Agent from Module 4
java -cp target/classes com.juleswhite.module4.FileExplorerAgent
```

## Advanced Usage

### Creating Custom Agents

You can easily create your own agents using the utility methods in the `Agents` class:

```java
// Create an agent with tools discovered from a class's package
Agent agent = Agents.createAgent(YourToolsClass.class, goals);

// Or with instance methods
YourToolsClass tools = new YourToolsClass();
Agent agent = Agents.createInstanceAgent(tools, goals);
```

### Defining Custom Tools

Create tools by annotating methods with `@RegisterTool`:

```java
@RegisterTool(tags = {"category"})
public Map<String, Object> yourTool(String param1) {
    // Implementation
    Map<String, Object> result = new HashMap<>();
    result.put("key", value);
    return result;
}
```

## Lessons Learned

Throughout this course, you'll gain insights into:

- How to structure prompts for effective agent reasoning
- Best practices for tool design and implementation
- Strategies for handling unexpected user inputs and agent responses
- Techniques for debugging and improving agent performance
- Security considerations when building AI agents

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [Jules White](https://www.coursera.org/instructor/juleswhite) - Course Instructor
- [Coursera](https://www.coursera.org/learn/ai-agents-java) - Course Platform
- The developers of the LLM libraries used in this course

---

Happy agent building! 🤖✨