package de.picturesafe.search.samples.facet;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.AggregationField;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.stream.LongStream;

@Component
@ComponentScan
public class Facet {

    private static final Logger LOGGER = LoggerFactory.getLogger(Facet.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Facet.class)) {
            final Facet facet = ctx.getBean(Facet.class);
            facet.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();
            createTestRecords();

            final Expression expression = new FulltextExpression("test title");

            // Search results and retrieve first page
            final SearchResult searchResult = singleIndexElasticsearchService.search(expression, createSearchParameter());
            LOGGER.info("Found {} hits with {} facet(s)", searchResult.getResultCount(), searchResult.getFacets().size());
            showFacets(searchResult);
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private void createTestRecords() {
        // Insert 20 test records with city 'Hamburg'
        LongStream.range(1, 21)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "Hamburg").build()));

        // Insert 10 test records with city 'London'
        LongStream.range(21, 31)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "London").build()));

        // Insert 5 test records with city 'Paris'
        LongStream.range(31, 36)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "Paris").build()));
    }

    private SearchParameter createSearchParameter() {
        return SearchParameter.builder().pageSize(10).pageIndex(1)
                .sortOptions(SortOption.asc("id"))
                .aggregationFields(new AggregationField("city", 10)) // Deliver up to 10 facet items for field 'city'
                .build();
    }

    private void showFacets(SearchResult searchResult) {
        for (ResultFacet resultFacet : searchResult.getFacets()) {
            LOGGER.info("Search result contains facet '{}' with {} item(s):", resultFacet.getName(), resultFacet.getCount());
            for (ResultFacetItem resultFacetItem : resultFacet.getFacetItems()) {
                LOGGER.info("{} [{}]", resultFacetItem.getValue(), resultFacetItem.getCount());
            }
        }
    }
}