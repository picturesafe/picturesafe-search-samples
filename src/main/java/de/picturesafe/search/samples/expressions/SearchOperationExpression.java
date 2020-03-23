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

package de.picturesafe.search.samples.expressions;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;
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
import java.util.Date;

@Component
@ComponentScan
public class SearchOperationExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchOperationExpression.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchOperationExpression.class)) {
            final SearchOperationExpression searchOperationExpression = ctx.getBean(SearchOperationExpression.class);
            searchOperationExpression.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1)
                            .put("keyword", "red")
                            .put("quantity", 10).build(),
                    DocumentBuilder.id(2)
                            .put("keyword", "red")
                            .put("quantity", 75).build(),
                    DocumentBuilder.id(3)
                            .put("keyword", "green")
                            .put("quantity", 100).build(),
                    DocumentBuilder.id(4)
                            .put("keyword", "yellow")
                            .put("quantity", 200).build()
            ));

            final Expression expression = OperationExpression.and(
                    new ValueExpression("quantity", ValueExpression.Comparison.GT, 50),
                    OperationExpression.or(
                            new ValueExpression("keyword", "yellow"),
                            new ValueExpression("keyword", "red")
                    )
            );

            final SearchResult searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
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
