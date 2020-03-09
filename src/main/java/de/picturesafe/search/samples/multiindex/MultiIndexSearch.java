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

package de.picturesafe.search.samples.multiindex;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.config.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import static de.picturesafe.search.samples.multiindex.Config.FIRST_INDEX_ALIAS;
import static de.picturesafe.search.samples.multiindex.Config.SECOND_INDEX_ALIAS;

@Component
@ComponentScan
public class MultiIndexSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiIndexSearch.class);

    @Autowired
    private ElasticsearchService elasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultiIndexSearch.class)) {
            final MultiIndexSearch multiIndexSearch = ctx.getBean(MultiIndexSearch.class);
            multiIndexSearch.run();
        }
    }

    private void run() {
        try {
            elasticsearchService.createIndexWithAlias(FIRST_INDEX_ALIAS);
            elasticsearchService.createIndexWithAlias(SECOND_INDEX_ALIAS);
            createTestRecords();

            final SearchResult firstIndexSearchResult = elasticsearchService.search(FIRST_INDEX_ALIAS, new FulltextExpression("test"), SearchParameter.DEFAULT);
            LOGGER.info(firstIndexSearchResult.toString());

            final SearchResult secondIndexSearchResult
                    = elasticsearchService.search(SECOND_INDEX_ALIAS, new ValueExpression("lastname", "Doe"), SearchParameter.DEFAULT);
            LOGGER.info(secondIndexSearchResult.toString());

        } finally {
            elasticsearchService.deleteIndexWithAlias(FIRST_INDEX_ALIAS);
            elasticsearchService.deleteIndexWithAlias(SECOND_INDEX_ALIAS);
        }
    }

    private void createTestRecords() {
        elasticsearchService.addToIndex(FIRST_INDEX_ALIAS, DataChangeProcessingMode.BLOCKING,
                DocumentBuilder.id(1).put("title", "This is a test title").build());
        elasticsearchService.addToIndex(FIRST_INDEX_ALIAS, DataChangeProcessingMode.BLOCKING,
                DocumentBuilder.id(2).put("title", "This is another test title").build());

        elasticsearchService.addToIndex(SECOND_INDEX_ALIAS, DataChangeProcessingMode.BLOCKING,
                DocumentBuilder.id(1).put("firstname", "John").put("lastname", "Doe").build());
        elasticsearchService.addToIndex(SECOND_INDEX_ALIAS, DataChangeProcessingMode.BLOCKING,
                DocumentBuilder.id(2).put("firstname", "Jane").put("lastname", "Doe").build());
    }
}
