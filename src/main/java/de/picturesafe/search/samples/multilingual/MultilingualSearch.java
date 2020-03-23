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

package de.picturesafe.search.samples.multilingual;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@ComponentScan
public class MultilingualSearch {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultilingualSearch.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MultilingualSearch.class)) {
            final MultilingualSearch multilingualSearch = ctx.getBean(MultilingualSearch.class);
            multilingualSearch.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1)
                            .put("title.en", "This is an english test title")
                            .put("title.de", "Dies ist ein deutscher Test-Titel").build(),
                    DocumentBuilder.id(2)
                            .put("title.en", "This is another english test title")
                            .put("title.de", "Dies ist ein anderer deutscher Test-Titel").build()
            ));

            final SearchParameter searchParameter = SearchParameter.builder().language("en").build();
            final Expression expression = new ValueExpression("title", ValueExpression.Comparison.LIKE, "english");
            final SearchResult searchResult = singleIndexElasticsearchService.search(expression, searchParameter);

            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }
}
