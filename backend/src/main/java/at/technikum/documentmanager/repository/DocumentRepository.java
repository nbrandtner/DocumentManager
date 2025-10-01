package at.technikum.documentmanager.repository;

import at.technikum.documentmanager.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {}
