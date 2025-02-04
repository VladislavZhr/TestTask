import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    public void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    public void testSave_NewDocument_ShouldGenerateIdAndSave() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Title")
                .content("Test Content")
                .author(new DocumentManager.Author(UUID.randomUUID().toString(), "Test Author"))
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertTrue(documentManager.findById(savedDocument.getId()).isPresent());
    }

    @Test
    public void testSave_DocumentWithExistingId_ShouldUpdateDocument() {
        String id = UUID.randomUUID().toString();
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .id(id)
                .title("Initial Title")
                .content("Initial Content")
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .id(id)
                .title("Updated Title")
                .content("Updated Content")
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        Optional<DocumentManager.Document> result = documentManager.findById(id);

        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Content", result.get().getContent());
    }

    @Test
    public void testFindById_NonExistingId_ShouldReturnEmpty() {
        Optional<DocumentManager.Document> result = documentManager.findById("non-existing-id");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearch_ByTitlePrefix_ShouldReturnMatchingDocuments() {
        documentManager.save(createDocument("1", "Title One", "Some content"));
        documentManager.save(createDocument("2", "Title Two", "Some content"));
        documentManager.save(createDocument("3", "Different Title", "Some content"));

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Title"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(doc -> doc.getTitle().equals("Title One")));
        assertTrue(results.stream().anyMatch(doc -> doc.getTitle().equals("Title Two")));
    }

    @Test
    public void testSearch_ByContent_ShouldReturnMatchingDocuments() {
        documentManager.save(createDocument("1", "Title One", "Important content"));
        documentManager.save(createDocument("2", "Title Two", "Another important thing"));
        documentManager.save(createDocument("3", "Different Title", "Random content"));

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("important"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(doc -> doc.getContent().equals("Important content")));
        assertTrue(results.stream().anyMatch(doc -> doc.getContent().equals("Another important thing")));
    }

    @Test
    public void testSearch_ByAuthor_ShouldReturnMatchingDocuments() {
        DocumentManager.Author author1 = new DocumentManager.Author("author1", "Author One");
        DocumentManager.Author author2 = new DocumentManager.Author("author2", "Author Two");

        documentManager.save(createDocument("1", "Title One", "Content", author1));
        documentManager.save(createDocument("2", "Title Two", "Content", author2));
        documentManager.save(createDocument("3", "Different Title", "Content", author1));

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("author1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(doc -> doc.getAuthor().getId().equals("author1")));
    }

    @Test
    public void testSearch_ByCreatedTime_ShouldReturnMatchingDocuments() {
        Instant now = Instant.now();
        Instant yesterday = now.minusSeconds(86400);
        Instant tomorrow = now.plusSeconds(86400);

        documentManager.save(createDocument("1", "Title One", "Content", yesterday));
        documentManager.save(createDocument("2", "Title Two", "Content", now));
        documentManager.save(createDocument("3", "Title Three", "Content", tomorrow));

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(yesterday)
                .createdTo(now)
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(doc -> doc.getTitle().equals("Title One")));
        assertTrue(results.stream().anyMatch(doc -> doc.getTitle().equals("Title Two")));
    }

    @Test
    public void testSearch_WithNoCriteria_ShouldReturnAllDocuments() {
        documentManager.save(createDocument("1", "Title One", "Some content"));
        documentManager.save(createDocument("2", "Title Two", "Some content"));
        documentManager.save(createDocument("3", "Title Three", "Some content"));

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder().build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(3, results.size());
    }

    // Utility method to create documents for testing
    private DocumentManager.Document createDocument(String id, String title, String content) {
        return DocumentManager.Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .created(Instant.now())
                .build();
    }

    // Overloaded utility method with author and creation time
    private DocumentManager.Document createDocument(String id, String title, String content, Instant created) {
        return DocumentManager.Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .created(created)
                .build();
    }

    private DocumentManager.Document createDocument(String id, String title, String content, DocumentManager.Author author) {
        return DocumentManager.Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .author(author)
                .created(Instant.now())
                .build();
    }
}
