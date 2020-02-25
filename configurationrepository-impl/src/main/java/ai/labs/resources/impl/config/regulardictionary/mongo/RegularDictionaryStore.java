package ai.labs.resources.impl.config.regulardictionary.mongo;

import ai.labs.persistence.ResultManipulator;
import ai.labs.persistence.mongo.HistorizedResourceStore;
import ai.labs.persistence.mongo.MongoResourceStorage;
import ai.labs.resources.rest.config.regulardictionary.IRegularDictionaryStore;
import ai.labs.resources.rest.config.regulardictionary.model.RegularDictionaryConfiguration;
import ai.labs.serialization.IDocumentBuilder;
import ai.labs.utilities.RuntimeUtilities;
import com.mongodb.client.MongoDatabase;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ginccc
 */
public class RegularDictionaryStore implements IRegularDictionaryStore {
    private HistorizedResourceStore<RegularDictionaryConfiguration> regularDictionaryResourceStore;

    private static final WordComparator WORD_COMPARATOR = new WordComparator();
    private static final PhraseComparator PHRASE_COMPARATOR = new PhraseComparator();

    @Inject
    public RegularDictionaryStore(MongoDatabase database, IDocumentBuilder documentBuilder) {
        RuntimeUtilities.checkNotNull(database, "database");

        final String collectionName = "regulardictionaries";
        MongoResourceStorage<RegularDictionaryConfiguration> resourceStorage =
                new MongoResourceStorage<>(database, collectionName, documentBuilder, RegularDictionaryConfiguration.class);
        this.regularDictionaryResourceStore = new HistorizedResourceStore<>(resourceStorage);
    }

    @Override
    public RegularDictionaryConfiguration readIncludingDeleted(String id, Integer version) throws ResourceNotFoundException, ResourceStoreException {
        return regularDictionaryResourceStore.readIncludingDeleted(id, version);
    }

    @Override
    public IResourceId create(RegularDictionaryConfiguration regularDictionaryConfiguration) throws ResourceStoreException {
        RuntimeUtilities.checkCollectionNoNullElements(regularDictionaryConfiguration.getWords(), "words");
        RuntimeUtilities.checkCollectionNoNullElements(regularDictionaryConfiguration.getPhrases(), "phrases");
        return regularDictionaryResourceStore.create(regularDictionaryConfiguration);
    }

    @Override
    public RegularDictionaryConfiguration read(String id, Integer version) throws ResourceNotFoundException, ResourceStoreException {
        return regularDictionaryResourceStore.read(id, version);
    }

    @Override
    public RegularDictionaryConfiguration read(String id, Integer version, String filter, String order, Integer index, Integer limit) throws ResourceNotFoundException, ResourceStoreException {
        RuntimeUtilities.checkNotNull(filter, "filter");
        RuntimeUtilities.checkNotNull(order, "order");
        RuntimeUtilities.checkNotNull(index, "index");
        RuntimeUtilities.checkNotNull(limit, "limit");

        ResultManipulator<RegularDictionaryConfiguration.WordConfiguration> wordManipulator;
        ResultManipulator<RegularDictionaryConfiguration.PhraseConfiguration> phraseManipulator;

        RegularDictionaryConfiguration regularDictionary = regularDictionaryResourceStore.read(id, version);

        wordManipulator = new ResultManipulator<>(regularDictionary.getWords(),
                RegularDictionaryConfiguration.WordConfiguration.class);
        phraseManipulator = new ResultManipulator<>(regularDictionary.getPhrases(),
                RegularDictionaryConfiguration.PhraseConfiguration.class);

        try {
            wordManipulator.filterEntities(filter);
            phraseManipulator.filterEntities(filter);
        } catch (ResultManipulator.FilterEntriesException e) {
            throw new ResourceStoreException(e.getLocalizedMessage(), e);
        }

        wordManipulator.sortEntities(WORD_COMPARATOR, order);
        phraseManipulator.sortEntities(PHRASE_COMPARATOR, order);

        wordManipulator.limitEntities(index, limit);
        phraseManipulator.limitEntities(index, limit);

        return regularDictionary;
    }

    @Override
    public List<String> readExpressions(String id, Integer version, String filter, String order, Integer index, Integer limit) throws ResourceStoreException, ResourceNotFoundException {
        List<String> retExpressions = new LinkedList<>();
        for (int i = index; ; i++) {
            RegularDictionaryConfiguration regularDictionary = read(id, version, filter, order, i, limit);
            List<RegularDictionaryConfiguration.WordConfiguration> words = regularDictionary.getWords();
            if (words.size() == 0 || retExpressions.size() >= 20) {
                break;
            }
            for (RegularDictionaryConfiguration.WordConfiguration word : words) {
                String exp = word.getExpressions();
                if (!RuntimeUtilities.isNullOrEmpty(exp) && exp.contains(filter) && !retExpressions.contains(exp)) {
                    retExpressions.add(exp);
                    if (retExpressions.size() >= 20) {
                        break;
                    }
                }
            }

            List<RegularDictionaryConfiguration.PhraseConfiguration> phrases = regularDictionary.getPhrases();
            for (RegularDictionaryConfiguration.PhraseConfiguration phrase : phrases) {
                String exp = phrase.getExpressions();
                if (!RuntimeUtilities.isNullOrEmpty(exp) && exp.contains(filter) && !retExpressions.contains(exp)) {
                    retExpressions.add(exp);
                    if (retExpressions.size() >= 20) {
                        break;
                    }
                }
            }
        }

        return retExpressions;
    }

    @Override
    @ConfigurationUpdate
    public Integer update(String id, Integer version, RegularDictionaryConfiguration regularDictionaryConfiguration) throws ResourceStoreException, ResourceModifiedException, ResourceNotFoundException {
        RuntimeUtilities.checkCollectionNoNullElements(regularDictionaryConfiguration.getWords(), "words");
        RuntimeUtilities.checkCollectionNoNullElements(regularDictionaryConfiguration.getPhrases(), "phrases");

        return regularDictionaryResourceStore.update(id, version, regularDictionaryConfiguration);
    }

    @Override
    @ConfigurationUpdate
    public void delete(String id, Integer version) throws ResourceStoreException, ResourceModifiedException, ResourceNotFoundException {
        regularDictionaryResourceStore.delete(id, version);
    }

    @Override
    public void deleteAllPermanently(String id) {
        regularDictionaryResourceStore.deleteAllPermanently(id);
    }

    @Override
    public IResourceId getCurrentResourceId(String id) throws ResourceNotFoundException {
        return regularDictionaryResourceStore.getCurrentResourceId(id);
    }

    private static class WordComparator implements Comparator<RegularDictionaryConfiguration.WordConfiguration> {
        @Override
        public int compare(RegularDictionaryConfiguration.WordConfiguration word1, RegularDictionaryConfiguration.WordConfiguration word2) {
            return word1.getWord().compareTo(word2.getWord());
        }
    }

    private static class PhraseComparator implements Comparator<RegularDictionaryConfiguration.PhraseConfiguration> {
        @Override
        public int compare(RegularDictionaryConfiguration.PhraseConfiguration phrase1, RegularDictionaryConfiguration.PhraseConfiguration phrase2) {
            return phrase1.getPhrase().compareTo(phrase2.getPhrase());
        }
    }
}
