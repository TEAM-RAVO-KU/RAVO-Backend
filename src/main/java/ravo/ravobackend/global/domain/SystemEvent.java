package ravo.ravobackend.global.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemEvent {

    private EventType eventType;
    private Severity severity;

    private String title;
    private String description;
    private String timestamp;
    private String details;
}
