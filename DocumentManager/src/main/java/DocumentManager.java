import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documents = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        documents.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation of this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list of matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            return new ArrayList<>(documents.values());
        }

        return documents.values().stream()
                .filter(document -> matchesTitle(document, request.getTitlePrefixes()))
                .filter(document -> matchesContent(document, request.getContainsContents()))
                .filter(document -> matchesAuthor(document, request.getAuthorIds()))
                .filter(document -> matchesCreatedTime(document, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    private boolean matchesTitle(Document document, List<String> titlePrefixes) {
        if (isNullOrEmpty(titlePrefixes)) {
            return true;
        }
        return titlePrefixes.stream().anyMatch(prefix ->
                document.getTitle() != null && document.getTitle().toLowerCase().startsWith(prefix.toLowerCase())
        );
    }

    private boolean matchesContent(Document document, List<String> containsContents) {
        if (isNullOrEmpty(containsContents)) {
            return true;
        }
        return containsContents.stream().anyMatch(content ->
                document.getContent() != null &&
                        document.getContent().toLowerCase().matches(".*\\b" + content.toLowerCase() + "\\b.*")
        );
    }

    private boolean matchesAuthor(Document document, List<String> authorIds) {
        if (isNullOrEmpty(authorIds)) {
            return true;
        }
        return document.getAuthor() != null && authorIds.contains(document.getAuthor().getId());
    }

    private boolean matchesCreatedTime(Document document, Instant createdFrom, Instant createdTo) {
        Instant created = document.getCreated();
        if (created == null) {
            return false;
        }

        boolean isAfterFrom = (createdFrom == null || !created.isBefore(createdFrom));
        boolean isBeforeTo = (createdTo == null || !created.isAfter(createdTo));

        return isAfterFrom && isBeforeTo;
    }

    private boolean isNullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}