package ai.labs.resources.rest.config.parser.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author ginccc
 */
@Setter
@Getter
public class ParserConfiguration {
    private Map<String, Object> extensions;
    private Map<String, Object> config;
}
