package ai.labs.behavior.impl.conditions;

import ai.labs.memory.IConversationMemory;
import ai.labs.memory.IMemoryItemConverter;
import ai.labs.memory.model.ConversationOutput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static ai.labs.behavior.impl.conditions.IBehaviorCondition.ExecutionState;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ginccc
 */
public class DynamicValueMatcherTest {
    private IConversationMemory conversationMemory;
    private IConversationMemory.IWritableConversationStep currentStep;
    private IMemoryItemConverter memoryItemConverter;

    @Before
    public void setUp() {
        memoryItemConverter = mock(IMemoryItemConverter.class);
        conversationMemory = mock(IConversationMemory.class);
        currentStep = mock(IConversationMemory.IWritableConversationStep.class);
        when(conversationMemory.getCurrentStep()).then(invocation -> currentStep);
    }

    private void initOutput() {
        initOutput("username", "John");
    }

    private void initOutput(String nameKey, String name) {
        when(memoryItemConverter.convert(any())).thenAnswer(invocation ->
                new LinkedHashMap<>(createConversationOutput(nameKey, name)));
        when(currentStep.getConversationOutput()).then(invocation -> createConversationOutput(nameKey, name));
    }

    @Test
    public void dynamicValue_NothingDefined() {
        //setup
        initOutput();
        DynamicValueMatcher dynamicValueMatcher = new DynamicValueMatcher(memoryItemConverter);
        dynamicValueMatcher.setConfigs(createValues(false, false, false));

        //test
        ExecutionState executionState = dynamicValueMatcher.execute(conversationMemory, new LinkedList<>());

        //assert
        Assert.assertEquals(ExecutionState.FAIL, executionState);
    }

    @Test
    public void dynamicValue_ValuePathOnly() {
        //setup
        initOutput();
        DynamicValueMatcher dynamicValueMatcher = new DynamicValueMatcher(memoryItemConverter);
        dynamicValueMatcher.setConfigs(createValues(true, false, false));

        //test
        ExecutionState executionState = dynamicValueMatcher.execute(conversationMemory, new LinkedList<>());

        //assert
        Assert.assertEquals(ExecutionState.SUCCESS, executionState);
    }

    @Test
    public void dynamicValue_NothingEquals() {
        //setup
        initOutput();
        DynamicValueMatcher dynamicValueMatcher = new DynamicValueMatcher(memoryItemConverter);
        dynamicValueMatcher.setConfigs(createValues(true, true, false));

        //test
        ExecutionState executionState = dynamicValueMatcher.execute(conversationMemory, new LinkedList<>());

        //assert
        Assert.assertEquals(ExecutionState.SUCCESS, executionState);
    }

    @Test
    public void dynamicValue_NothingContains() {
        //setup
        initOutput();
        DynamicValueMatcher dynamicValueMatcher = new DynamicValueMatcher(memoryItemConverter);
        dynamicValueMatcher.setConfigs(createValues(true, false, true));

        //test
        ExecutionState executionState = dynamicValueMatcher.execute(conversationMemory, new LinkedList<>());

        //assert
        Assert.assertEquals(ExecutionState.SUCCESS, executionState);
    }

    @Test
    public void dynamicValue_propertyIsNull() {
        //setup
        initOutput("name", "Tom");
        DynamicValueMatcher dynamicValueMatcher = new DynamicValueMatcher(memoryItemConverter);
        dynamicValueMatcher.setConfigs(createValues(true, false, false));

        //test
        ExecutionState executionState = dynamicValueMatcher.execute(conversationMemory, new LinkedList<>());

        //assert
        Assert.assertEquals(ExecutionState.FAIL, executionState);
    }

    private Map<String, String> createValues(boolean isValuePath, boolean isEquals, boolean isContains) {
        Map<String, String> ret = new HashMap<>();
        if (isValuePath) {
            ret.put("valuePath", "properties.username");
        }
        if (isEquals) {
            ret.put("equals", "John");
        }
        if (isContains) {
            ret.put("contains", "Jo");
        }
        return ret;
    }

    private ConversationOutput createConversationOutput(String nameKey, String name) {
        ConversationOutput conversationOutput = new ConversationOutput();
        var usernameMap = new HashMap<>();
        usernameMap.put(nameKey, name);
        conversationOutput.put("properties", usernameMap);
        return conversationOutput;
    }
}
