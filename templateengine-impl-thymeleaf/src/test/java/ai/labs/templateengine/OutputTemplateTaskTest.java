package ai.labs.templateengine;

import ai.labs.memory.IConversationMemory;
import ai.labs.memory.IData;
import ai.labs.memory.IDataFactory;
import ai.labs.memory.IMemoryItemConverter;
import ai.labs.memory.model.Data;
import ai.labs.models.Context;
import ai.labs.output.model.QuickReply;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static ai.labs.templateengine.ITemplatingEngine.TemplateMode.TEXT;
import static org.mockito.Mockito.*;

/**
 * @author ginccc
 */
public class OutputTemplateTaskTest {
    private static final String KEY_QUICK_REPLY_SOME_ACTION = "quickReplies:someAction";
    private static final String KEY_QUICK_REPLY_SOME_ACTION_PRE_TEMPLATED = "quickReplies:someAction:preTemplated";
    private static final String KEY_QUICK_REPLY_SOME_ACTION_POST_TEMPLATED = "quickReplies:someAction:postTemplated";
    private static final String KEY_OUTPUT_TEXT_SOME_ACTION_PRE_TEMPLATED = "output:text:someAction:preTemplated";
    private static final String KEY_OUTPUT_TEXT_SOME_ACTION_POST_TEMPLATED = "output:text:someAction:postTemplated";
    private IDataFactory dataFactory;
    private IConversationMemory conversationMemory;
    private IConversationMemory.IWritableConversationStep currentStep;
    private String templateString = "This is some output with context such as [[${context}]]";
    private OutputTemplateTask outputTemplateTask;
    private final String expectedOutputString = "This is some output with context such as someContextValue";
    private ITemplatingEngine templatingEngine;

    @Before
    public void setUp() {
        templatingEngine = mock(ITemplatingEngine.class);
        dataFactory = mock(IDataFactory.class);
        conversationMemory = mock(IConversationMemory.class);
        currentStep = mock(IConversationMemory.IWritableConversationStep.class);
        when(conversationMemory.getCurrentStep()).then(invocation -> currentStep);
        IMemoryItemConverter memoryTemplateConverter = mock(IMemoryItemConverter.class);
        when(memoryTemplateConverter.convert(any(IConversationMemory.class))).
                then(invocation -> new HashMap<>());
        outputTemplateTask = new OutputTemplateTask(templatingEngine, memoryTemplateConverter, dataFactory);
    }

    @Test
    public void executeTaskWithContextString() throws Exception {
        //setup
        when(currentStep.getAllData(eq("context"))).then(invocation -> {
            LinkedList<IData<Context>> ret = new LinkedList<>();
            ret.add(new MockData<>("context:someContext",
                    new Context(Context.ContextType.string, "someContextValue")));
            return ret;
        });
        List<QuickReply> expectedPostQuickReplies = setupTask();

        //test
        outputTemplateTask.executeTask(conversationMemory);

        //assert
        verifyTask(expectedPostQuickReplies);
    }

    @Test
    public void executeTaskWithContextObject() throws Exception {
        //setup
        final TestContextObject testContextObject = new TestContextObject("someContext", "someContextValue");
        when(currentStep.getAllData(eq("context"))).then(invocation -> {
            LinkedList<IData<Context>> ret = new LinkedList<>();
            ret.add(new MockData<>("context:someContext",
                    new Context(Context.ContextType.object, testContextObject)));
            return ret;
        });
        List<QuickReply> expectedPostQuickReplies = setupTask();

        //test
        outputTemplateTask.executeTask(conversationMemory);

        //assert
        verifyTask(expectedPostQuickReplies);
    }

    private List<QuickReply> setupTask() throws ITemplatingEngine.TemplateEngineException {
        when(currentStep.getAllData(eq("output"))).then(invocation -> {
            LinkedList<IData<String>> ret = new LinkedList<>();
            ret.add(new MockData<>("output:text:someAction", templateString));
            return ret;
        });
        List<QuickReply> expectedPreQuickReplies = new LinkedList<>();
        expectedPreQuickReplies.add(new QuickReply(
                "Quick Reply Value [[${context}]]",
                "quickReply(expression)", false));

        List<QuickReply> expectedPostQuickReplies = new LinkedList<>();
        expectedPostQuickReplies.add(new QuickReply(
                "Quick Reply Value someContextValue",
                "quickReply(expression)", false));

        when(currentStep.getAllData(eq("quickReplies"))).then(invocation -> {
            LinkedList<IData<List<QuickReply>>> ret = new LinkedList<>();
            ret.add(new MockData<>(KEY_QUICK_REPLY_SOME_ACTION, expectedPreQuickReplies));
            ret.add(new MockData<>(KEY_QUICK_REPLY_SOME_ACTION, expectedPostQuickReplies));
            return ret;
        });
        when(dataFactory.createData(eq(KEY_OUTPUT_TEXT_SOME_ACTION_PRE_TEMPLATED), eq(templateString)))
                .then(invocation -> new Data<>(KEY_OUTPUT_TEXT_SOME_ACTION_PRE_TEMPLATED, templateString));

        when(dataFactory.createData(eq(KEY_OUTPUT_TEXT_SOME_ACTION_POST_TEMPLATED), eq(expectedOutputString)))
                .then(invocation -> new Data<>(KEY_OUTPUT_TEXT_SOME_ACTION_POST_TEMPLATED, expectedOutputString));

        when(dataFactory.createData(eq(KEY_QUICK_REPLY_SOME_ACTION_PRE_TEMPLATED), anyList()))
                .then(invocation -> new Data<>(KEY_QUICK_REPLY_SOME_ACTION_PRE_TEMPLATED, expectedPreQuickReplies));

        when(dataFactory.createData(eq(KEY_QUICK_REPLY_SOME_ACTION_POST_TEMPLATED), anyList()))
                .then(invocation -> new Data<>(KEY_QUICK_REPLY_SOME_ACTION_POST_TEMPLATED, expectedPostQuickReplies));

        when(templatingEngine.processTemplate(eq(templateString), anyMap(), eq(TEXT))).
                then(invocation -> expectedOutputString);

        var expectedPreQuickReply = expectedPreQuickReplies.get(0);
        var expectedPostQuickReply = expectedPostQuickReplies.get(0);

        String expectedPreQuickReplyValue = expectedPreQuickReply.getValue();
        String expectedPostQuickReplyValue = expectedPostQuickReply.getValue();
        String expectedPostQuickReplyExpressions = expectedPostQuickReply.getExpressions();

        when(templatingEngine.processTemplate(eq(expectedPreQuickReplyValue), anyMap())).
                then(invocation -> expectedPostQuickReplyValue);
        when(templatingEngine.processTemplate(eq(expectedPreQuickReply.getExpressions()), anyMap())).
                then(invocation -> expectedPostQuickReplyExpressions);

        when(templatingEngine.processTemplate(eq(expectedPostQuickReplyValue), anyMap())).
                then(invocation -> expectedPostQuickReplyValue);
        when(templatingEngine.processTemplate(eq(expectedPostQuickReplyExpressions), anyMap())).
                then(invocation -> expectedPostQuickReplyExpressions);

        return expectedPostQuickReplies;
    }

    private void verifyTask(List<QuickReply> expectedPostQuickReplies) {
        verify(currentStep).getAllData("output");
        verify(currentStep).getAllData("quickReplies");
        verify(dataFactory).createData(eq(KEY_OUTPUT_TEXT_SOME_ACTION_PRE_TEMPLATED), eq(templateString));
        verify(dataFactory).createData(eq(KEY_OUTPUT_TEXT_SOME_ACTION_POST_TEMPLATED), eq(expectedOutputString));
        verify(dataFactory, times(2)).createData(eq(KEY_QUICK_REPLY_SOME_ACTION_PRE_TEMPLATED), any());
        verify(dataFactory, times(2)).createData(eq(KEY_QUICK_REPLY_SOME_ACTION_POST_TEMPLATED), eq(expectedPostQuickReplies));
        verify(currentStep, times(9)).storeData(any(IData.class));
    }


    @EqualsAndHashCode
    private static class MockData<T> implements IData<T> {
        private final String key;
        private T result;

        MockData(String key, T result) {
            this.key = key;
            this.result = result;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public List<T> getPossibleResults() {
            return null;
        }

        @Override
        public T getResult() {
            return result;
        }

        @Override
        public Date getTimestamp() {
            return null;
        }

        @Override
        public boolean isPublic() {
            return false;
        }

        @Override
        public void setPossibleResults(List<T> possibleResults) {

        }

        @Override
        public void setResult(T result) {
            this.result = result;
        }

        @Override
        public void setPublic(boolean isPublic) {

        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class TestContextObject {
        private String key;
        private String value;
    }
}

