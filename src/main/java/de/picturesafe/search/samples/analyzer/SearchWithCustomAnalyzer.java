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

package de.picturesafe.search.samples.analyzer;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
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
public class SearchWithCustomAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWithCustomAnalyzer.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchWithCustomAnalyzer.class)) {
            final SearchWithCustomAnalyzer searchWithCustomAnalyzer = ctx.getBean(SearchWithCustomAnalyzer.class);
            searchWithCustomAnalyzer.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.withoutId().put("filename", "test.jpg").build(),
                    DocumentBuilder.withoutId().put("filename", "test.pdf").build(),
                    DocumentBuilder.withoutId().put("filename", "my_image.jpg").build()
            ));

            SearchResult searchResult = singleIndexElasticsearchService.search(new ValueExpression("filename", "jpg"), SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            searchResult = singleIndexElasticsearchService.search(new ValueExpression("filename", "test"), SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            searchResult = singleIndexElasticsearchService.search(new ValueExpression("filename", "my"), SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            searchResult = singleIndexElasticsearchService.search(new ValueExpression("filename", "image"), SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            searchResult = singleIndexElasticsearchService.search(new ValueExpression("filename", "my_image.jpg"), SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }
}
