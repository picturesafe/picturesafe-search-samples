/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.samples.aggregations;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.ResultFacet;
import de.picturesafe.search.elasticsearch.model.ResultFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.parameter.aggregation.DateHistogramAggregation;
import de.picturesafe.search.parameter.aggregation.DefaultAggregation;
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.stream.LongStream;

import static de.picturesafe.search.parameter.aggregation.DateHistogramAggregation.IntervalType.CALENDAR;

@Component
@ComponentScan
public class SearchWithAggregations {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWithAggregations.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchWithAggregations.class)) {
            final SearchWithAggregations facet = ctx.getBean(SearchWithAggregations.class);
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
        final Instant now = Instant.now();
        final Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        final Instant oneYearAgo = now.minus(365, ChronoUnit.DAYS);

        // Insert 20 test records with city 'Hamburg', 'Tag A' and create date 'now'
        LongStream.range(1, 21)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id)
                                .put("title", "This is a test title " + id)
                                .put("city", "Hamburg")
                                .put("tag", "Tag A")
                                .put("created", Date.from(now)).build()));

        // Insert 10 test records with city 'London', 'Tag B' and create date 'yesterday'
        LongStream.range(21, 31)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id)
                                .put("city", "London")
                                .put("tag", "Tag B")
                                .put("created", Date.from(yesterday)).build()));

        // Insert 5 test records with city 'Paris', 'Tag A' and create date 'one year ago'
        LongStream.range(31, 36)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id)
                                .put("city", "Paris").put("created", Date.from(oneYearAgo))
                                .put("tag", "Tag A")
                                .build()));
    }

    private SearchParameter createSearchParameter() {
        return SearchParameter.builder().pageSize(10).pageIndex(1)
                .sortOptions(SortOption.asc("id"))
                .aggregations(
                        DefaultAggregation.field("tag"),
                        TermsAggregation.field("city").maxCount(10),
                        DateHistogramAggregation.field("created").interval(CALENDAR, "1y").format("yyyy").name("years"))
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