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

package de.picturesafe.search.samples.sort;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
import de.picturesafe.search.samples.PicturesafeSearchSample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static de.picturesafe.search.parameter.SortOption.ArrayMode.AVG;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MAX;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MEDIAN;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.MIN;
import static de.picturesafe.search.parameter.SortOption.ArrayMode.SUM;

@Component
@ComponentScan
public class SortByArrayMode implements PicturesafeSearchSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SortByArrayMode.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SortByArrayMode.class)) {
            final SortByArrayMode sortByArrayMode = ctx.getBean(SortByArrayMode.class);
            sortByArrayMode.run();
        }
    }

    @Override
    public void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1).put("title", "Product A").put("quantities", Arrays.asList(50, 100, 150, 400)).build(),
                    DocumentBuilder.id(2).put("title", "Product B").put("quantities", Arrays.asList(40, 260)).build(),
                    DocumentBuilder.id(3).put("title", "Product C").put("quantities", Arrays.asList(0, 50, 200)).build(),
                    DocumentBuilder.id(4).put("title", "Product D").put("quantities", Arrays.asList(10, 190)).build()
            ));

            final Expression expression = new FulltextExpression("Product");

            // Pick the lowest value.
            // Product C (0) -> Product D (10) -> Product B (40) -> Product A (50)
            SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.asc("quantities").arrayMode(MIN)).build();
            SearchResult searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());

            // Pick the highest value.
            // Product A (400) -> Product B (260) -> Product C (200) -> Product D (190)
            searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("quantities").arrayMode(MAX)).build();
            searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());

            // Use the sum of all values as sort value. Only applicable for number based array fields.
            // Product A (700) -> Product B (300) -> Product C (250) -> Product D (200)
            searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("quantities").arrayMode(SUM)).build();
            searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());

            // Use the average of all values as sort value. Only applicable for number based array fields.
            // Product A (175) -> Product B (150) -> Product D (100) -> Product C (83)
            searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("quantities").arrayMode(AVG)).build();
            searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());

            // Use the median of all values as sort value. Only applicable for number based array fields.
            // Product B (150) -> Product A (125) -> Product D (100) -> Product C (50)
            searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("quantities").arrayMode(MEDIAN)).build();
            searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }
}
