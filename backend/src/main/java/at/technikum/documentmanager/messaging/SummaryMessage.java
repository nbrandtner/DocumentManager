package at.technikum.documentmanager.messaging;

import lombok.Data;
import java.util.UUID;

@Data
public class SummaryMessage {
    private UUID documentId;
    private String summary;
}
