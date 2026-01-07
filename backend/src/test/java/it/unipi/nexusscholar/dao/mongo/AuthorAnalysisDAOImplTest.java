package it.unipi.nexusscholar.dao.mongo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import it.unipi.nexusscholar.dao.exception.DAOException;
import it.unipi.nexusscholar.model.mongo.ProlificAuthor;
import java.util.Collections;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

@ExtendWith(MockitoExtension.class)
class AuthorAnalysisDAOImplTest {

    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private AuthorAnalysisDAOImpl authorAnalysisDAO;

    private ProlificAuthor testProlificAuthor;

    @BeforeEach
    void setUp() {
        testProlificAuthor = new ProlificAuthor();
        testProlificAuthor.setAuthorId("auth-1");
        testProlificAuthor.setAuthorName("Mario Rossi");
        // Se ProlificAuthor ha altri campi (es. count), settali qui
    }

    @Test
    void getProlificAuthors_ReturnsResults() {
        // 1. Setup Mock Response
        List<ProlificAuthor> expectedList = List.of(testProlificAuthor);

        // AggregationResults è la classe wrapper di Spring Data Mongo.
        // Dobbiamo crearne un'istanza fake da far ritornare al mock.
        AggregationResults<ProlificAuthor> fakeResults =
                new AggregationResults<>(expectedList, new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("authors"), eq(ProlificAuthor.class)))
                .thenReturn(fakeResults);

        // 2. Execute
        List<ProlificAuthor> result = authorAnalysisDAO.getProlificAuthors(5);

        // 3. Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Mario Rossi", result.get(0).getAuthorName());

        // Verifichiamo che aggregate sia stato chiamato con la collection "authors"
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("authors"), eq(ProlificAuthor.class));
    }

    @Test
    void getProlificAuthors_EmptyCollection() {
        // 1. Setup Empty Response
        AggregationResults<ProlificAuthor> emptyResults =
                new AggregationResults<>(Collections.emptyList(), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("authors"), eq(ProlificAuthor.class)))
                .thenReturn(emptyResults);

        // 2. Execute
        List<ProlificAuthor> result = authorAnalysisDAO.getProlificAuthors(10);

        // 3. Verify
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProlificAuthors_ThrowsDAOException() {
        // 1. Setup Exception
        // Simuliamo che MongoTemplate lanci una RuntimeException (es. DB giù)
        when(mongoTemplate.aggregate(any(Aggregation.class), anyString(), eq(ProlificAuthor.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        // 2. Execute & Verify
        DAOException exception = assertThrows(DAOException.class, () -> {
            authorAnalysisDAO.getProlificAuthors(5);
        });

        assertTrue(exception.getMessage().contains("Error executing prolific authors aggregation"));
    }
}