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

package de.picturesafe.search.samples.suggest;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.SuggestExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@ComponentScan
public class Suggest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Suggest.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Suggest.class)) {
            final Suggest suggest = ctx.getBean(Suggest.class);
            suggest.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();
            createTestRecords();

            final SuggestResult suggestResult = singleIndexElasticsearchService.suggest(new SuggestExpression("Ha", 10));
            showSuggestions(suggestResult);
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private void createTestRecords() {
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                DocumentBuilder.id(1).put("city", "Hamburg").build(),
                DocumentBuilder.id(2).put("city", "Berlin").build(),
                DocumentBuilder.id(3).put("city", "Hannover").build(),
                DocumentBuilder.id(4).put("city", "Freiburg").build(),
                DocumentBuilder.id(5).put("city", "Hamburg").build()
        ));
    }

    private void showSuggestions(SuggestResult suggestResult) {
        for (String suggestion : suggestResult.getSuggestions(FieldConfiguration.FIELD_NAME_SUGGEST)) {
            LOGGER.info(suggestion);
        }
    }
}