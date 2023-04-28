package ai.labs.eddi.modules.nlp.extensions.dictionaries.providers;


import ai.labs.eddi.modules.nlp.expressions.utilities.IExpressionProvider;
import ai.labs.eddi.modules.nlp.extensions.dictionaries.DecimalDictionary;
import ai.labs.eddi.modules.nlp.extensions.dictionaries.IDictionary;
import io.quarkus.runtime.Startup;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Map;


/**
 * @author ginccc
 */
@Startup(1000)
@ApplicationScoped
public class DecimalDictionaryProvider implements IDictionaryProvider {
    public static final String ID = "ai.labs.parser.dictionaries.decimal";

    private final IExpressionProvider expressionProvider;

    @Inject
    public DecimalDictionaryProvider(IExpressionProvider expressionProvider) {
        this.expressionProvider = expressionProvider;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Decimal Dictionary";
    }

    @Override
    public IDictionary provide(Map<String, Object> config) {
        return new DecimalDictionary(expressionProvider);
    }
}
