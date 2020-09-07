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
import de.picturesafe.search.elasticsearch.model.ResultRangeFacetItem;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.parameter.aggregation.DateRangeAggregation;
import de.picturesafe.search.samples.PicturesafeSearchSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Component
@ComponentScan
public class SearchWithDateRangeAggregation implements PicturesafeSearchSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWithDateRangeAggregation.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchWithDateRangeAggregation.class)) {
            final SearchWithDateRangeAggregation facet = ctx.getBean(SearchWithDateRangeAggregation.class);
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
        final Instant now = Instant.now();
        final Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        final Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);

        // Insert 20 test records with create date 'now'
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BACKGROUND, LongStream.range(1, 21).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("created", Date.from(now)).build())
                .collect(Collectors.toList()));

        // Insert 10 test records with create date 'yesterday'
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BACKGROUND, LongStream.range(21, 31).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("created", Date.from(yesterday)).build())
                .collect(Collectors.toList()));

        // Insert 5 test records with create date 'seven days ago'
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, LongStream.range(31, 36).boxed()
                .map(id -> DocumentBuilder.id(id).put("title", "This is a test title " + id).put("created", Date.from(sevenDaysAgo)).build())
                .collect(Collectors.toList()));
    }

    private List<DateRangeAggregation.Range> getAggregationRanges() {
        return Arrays.asList(
                DateRangeAggregation.Range.from("now/d").to("now/d+1d").key("today"),
                DateRangeAggregation.Range.from("now/d-1d").to("now/d").key("yesterday"),
                DateRangeAggregation.Range.from("now/w").to("now/w+1w").key("week"),
                DateRangeAggregation.Range.from("now/w-1w").to("now/w").key("last week")
        );
    }

    private SearchParameter createSearchParameter() {
        return SearchParameter.builder().pageSize(10).pageIndex(1)
                .sortOptions(SortOption.asc("id"))
                .aggregations(DateRangeAggregation.field("created").format("dd.MM.yyyy").ranges(getAggregationRanges()))
                .build();
    }

    private void showFacets(SearchResult searchResult) {
        for (ResultFacet resultFacet : searchResult.getFacets()) {
            LOGGER.info("Search result contains facet '{}' with {} item(s):", resultFacet.getName(), resultFacet.getCount());
            for (ResultFacetItem resultFacetItem : resultFacet.getFacetItems()) {
                if (resultFacetItem instanceof ResultRangeFacetItem) {
                    final ResultRangeFacetItem resultRangeFacetItem = (ResultRangeFacetItem) resultFacetItem;
                    LOGGER.info("{} (from: {} to: {}) [{}]", resultRangeFacetItem.getValue(), resultRangeFacetItem.getFrom(),
                            resultRangeFacetItem.getTo(), resultRangeFacetItem.getCount());
                }
            }
        }
    }
}