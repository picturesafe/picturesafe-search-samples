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

package de.picturesafe.search.samples.pagination;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.elasticsearch.model.SearchResultItem;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
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
public class Pagination {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pagination.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Pagination.class)) {
            final Pagination pagination = ctx.getBean(Pagination.class);
            pagination.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();
            createTestRecords();

            final Expression expression = new FulltextExpression("test title");
            final int pageSize = 10;
            int pageIndex = 1;

            // Search results and retrieve first page
            SearchResult searchResult = singleIndexElasticsearchService.search(expression, createSearchParameter(pageSize, pageIndex));
            LOGGER.info("Found {} hits of {} total hits", searchResult.getResultCount(), searchResult.getTotalHitCount());
            showSearchResult(searchResult);

            while (pageIndex++ < searchResult.getPageCount()) {
                // retrieve results of next page
                searchResult = singleIndexElasticsearchService.search(expression, createSearchParameter(pageSize, pageIndex));
                showSearchResult(searchResult);
            }
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private void createTestRecords() {
        // Insert 105 test records
        LongStream.range(1, 106)
                .forEach(id -> singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING,
                        DocumentBuilder.id(id).put("title", "This is a test title " + id).build()));
    }

    private SearchParameter createSearchParameter(int pageSize, int pageIndex) {
        return SearchParameter.builder().pageSize(pageSize).pageIndex(pageIndex).sortOptions(SortOption.asc("id")).build();
    }

    private void showSearchResult(SearchResult searchResult) {
        LOGGER.info("Displaying results of page {}:", searchResult.getPageIndex());
        for (SearchResultItem searchResultItem : searchResult.getSearchResultItems()) {
            LOGGER.info("Id = {}, title = {}", searchResultItem.getId(), searchResultItem.getAttribute("title"));
        }
    }
}