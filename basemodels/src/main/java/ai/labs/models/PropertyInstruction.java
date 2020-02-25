package ai.labs.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyInstruction extends Property {
    private String fromObjectPath = "";
    private Boolean override = true;
    private HttpCodeValidator httpCodeValidator;
}
