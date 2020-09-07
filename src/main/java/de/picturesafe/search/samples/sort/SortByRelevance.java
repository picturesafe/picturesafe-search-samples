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

@Component
@ComponentScan
public class SortByRelevance implements PicturesafeSearchSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(SortByRelevance.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SortByRelevance.class)) {
            final SortByRelevance sortByRelevance = ctx.getBean(SortByRelevance.class);
            sortByRelevance.run();
        }
    }

    @Override
    public void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1).put("title", "Black horse")
                            .put("caption", "I was riding on a black horse long time ago.").build(),
                    DocumentBuilder.id(2).put("title", "White horse")
                            .put("caption", "I was riding on a white horse followed by a black dog.").build(),
                    DocumentBuilder.id(3).put("title", "Black and white")
                            .put("caption", "Michael Jackson was singing about black and white, not about a black horse.").build()
            ));

            final Expression expression = new FulltextExpression("black");
            final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.relevance()).build();
            final SearchResult searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }
}
