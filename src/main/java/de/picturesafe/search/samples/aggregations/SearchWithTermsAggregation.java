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
import de.picturesafe.search.parameter.aggregation.TermsAggregation;
import de.picturesafe.search.samples.PicturesafeSearchSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Component
@ComponentScan
public class SearchWithTermsAggregation implements PicturesafeSearchSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWithTermsAggregation.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchWithTermsAggregation.class)) {
            final SearchWithTermsAggregation facet = ctx.getBean(SearchWithTermsAggregation.class);
            facet.run();
        }
    }

    @Override
    public void run() {
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
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BACKGROUND, LongStream.range(1, 21).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "Hamburg").build())
                .collect(Collectors.toList()));

        // Insert 10 test records with city 'London'
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BACKGROUND, LongStream.range(21, 31).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "London").build())
                .collect(Collectors.toList()));

        // Insert 5 test records with city 'Paris'
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, LongStream.range(31, 36).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("city", "Paris").build())
                .collect(Collectors.toList()));
    }

    private SearchParameter createSearchParameter() {
        return SearchParameter.builder().pageSize(10).pageIndex(1)
                .sortOptions(SortOption.asc("id"))
                .aggregations(TermsAggregation.field("city").maxCount(10)) // Deliver up to 10 facet items for field 'city' via TermsAggregation
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