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
import de.picturesafe.search.parameter.ScriptDefinition;
import de.picturesafe.search.parameter.ScriptSortOption;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;
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
public class ScriptSort {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptSort.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScriptSort.class)) {
            final ScriptSort scriptSort = ctx.getBean(ScriptSort.class);
            scriptSort.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1)
                            .put("title", "Title (A)")
                            .put("created", getDate("13.01.2020"))
                            .build(),
                    DocumentBuilder.id(2)
                            .put("title", "Title B")
                            .put("created", getDate("13.01.2020"))
                            .put("updated", getDate("22.01.2020")).build(),
                    DocumentBuilder.id(3)
                            .put("title", "Title C")
                            .put("created", getDate("13.01.2020"))
                            .put("updated", getDate("24.01.2020")).build(),
                    DocumentBuilder.id(4)
                            .put("title", "Title (D)")
                            .put("created", getDate("13.01.2020"))
                            .build(),
                    DocumentBuilder.id(5)
                            .put("title", "Title E")
                            .put("created", getDate("13.01.2020"))
                            .put("updated", getDate("23.01.2020")).build()
            ));

            final Expression expression = new FulltextExpression("title");

            // Without script: Title C, E, B, (A), (D)
            withoutScriptSort(expression);

            // With script: Title (A), (D), C, E, B
            withScriptSort(expression);
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private void withoutScriptSort(Expression expression) {
        final SearchParameter searchParameter = SearchParameter.builder()
                .sortOptions(
                        SortOption.desc("created"),
                        SortOption.desc("updated"))
                .build();

        search(expression, searchParameter);
    }

    private void withScriptSort(Expression expression) {
        final SearchParameter searchParameter = SearchParameter.builder()
                .sortOptions(
                        SortOption.desc("created"),
                        ScriptSortOption.asc(ScriptDefinition.inline("doc['updated'].empty ? 0 : 1")),
                        SortOption.desc("updated"))
                .build();

        search(expression, searchParameter);
    }

    private void search(Expression expression, SearchParameter searchParameter) {
        final SearchResult searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
        LOGGER.info(searchResult.toString());
    }

    private Date getDate(String date) {
        try {
            return new SimpleDateFormat("dd.MM.yyyy").parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Parsing date '" + date + "' failed!", e);
        }
    }
}
