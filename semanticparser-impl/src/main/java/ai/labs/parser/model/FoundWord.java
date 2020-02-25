package ai.labs.parser.model;

import ai.labs.expressions.Expressions;
import ai.labs.parser.extensions.dictionaries.IDictionary;

import java.util.Objects;

/**
 * @author ginccc
 */
public class FoundWord extends FoundDictionaryEntry implements IDictionary.IFoundWord {
    private final IDictionary.IWord word;

    public FoundWord(IDictionary.IWord word, boolean corrected, double matchingAccuracy) {
        super(word, corrected, matchingAccuracy);
        this.word = word;
        isWord = true;
    }

    public FoundWord(String unknownValue, Expressions unknownExp) {
        super(unknownValue, unknownExp, false, 0.0);
        word = null;
        isWord = true;
    }

    @Override
    public IDictionary.IWord getFoundWord() {
        return word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FoundWord foundWord = (FoundWord) o;

        return Objects.equals(value, foundWord.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isWord() {
        return word.isWord();
    }

    @Override
    public boolean isPhrase() {
        return word.isPhrase();
    }
}
