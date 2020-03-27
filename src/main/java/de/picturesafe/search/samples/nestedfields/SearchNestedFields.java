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

package de.picturesafe.search.samples.nestedfields;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.SearchParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@Component
@ComponentScan
public class SearchNestedFields {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchNestedFields.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchNestedFields.class)) {
            final SearchNestedFields searchNestedFields = ctx.getBean(SearchNestedFields.class);
            searchNestedFields.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1)
                            .put("article", Collections.singletonList(DocumentBuilder.withoutId()
                                    .put("title", "This is a test title")
                                    .put("caption", "This is a test caption")
                                    .put("author", "John Doe")
                                    .put("date", getDate("10.03.2020")).build()
                            )).build(),
                    DocumentBuilder.id(2)
                            .put("article", Collections.singletonList(DocumentBuilder.withoutId()
                                    .put("title", "This is another test title")
                                    .put("caption", "This is another test caption")
                                    .put("author", "Jane Doe")
                                    .put("date", getDate("12.03.2020")).build()
                            )).build(),
                    DocumentBuilder.id(3)
                            .put("article", Collections.singletonList(DocumentBuilder.withoutId()
                                    .put("title", "This is one more test title")
                                    .put("caption", "This is one more test caption")
                                    .put("author", "Jane Doe")
                                    .put("date", getDate("12.03.2020")).build()
                            )).build()
            ));

            Expression expression = new FulltextExpression("Doe");
            SearchResult searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            expression = new ValueExpression("article.author", "Jane Doe");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            expression = new ValueExpression("article.date", getDate("12.03.2020"));
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private Date getDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Parsing date '" + date + "' failed!", e);
        }
    }
}
