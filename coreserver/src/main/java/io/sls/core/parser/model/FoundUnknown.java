package io.sls.core.parser.model;

/**
 * @author ginccc
 */
public class FoundUnknown extends FoundWord {
    public FoundUnknown(Unknown unknown) {
        super(unknown, false, 0.0);
    }
}
