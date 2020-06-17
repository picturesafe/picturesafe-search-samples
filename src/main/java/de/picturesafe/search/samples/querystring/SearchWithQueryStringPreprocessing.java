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

package de.picturesafe.search.samples.querystring;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.FulltextExpression;
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
public class SearchWithQueryStringPreprocessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWithQueryStringPreprocessing.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(SearchWithQueryStringPreprocessing.class)) {
            final SearchWithQueryStringPreprocessing searchWithQueryStringPreprocessing = ctx.getBean(SearchWithQueryStringPreprocessing.class);
            searchWithQueryStringPreprocessing.run();
        }
    }

    private void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1).put("title", "Berlin is the capital and largest city of Germany").build(),
                    DocumentBuilder.id(2).put("title", "London is the capital and largest city of England and the United Kingdom").build(),
                    DocumentBuilder.id(3).put("title", "Washington, D.C. is the capital city of the United States of America").build(),
                    DocumentBuilder.id(4).put("title", "Paris is the capital and most populous city of France").build()
            ));

            // The FulltextExpression supports the compact Lucene query string syntax, which allows to
            // specify AND|OR|NOT conditions and multi-field search within a single query string.
            // In addition, picturesafe-search offers a preprocessor that allows a less strict,
            // more intuitive query string, especially regarding logical operators and parentheses.
            //
            // Default settings:
            //    adding brackets automatically
            //    adding missing operators automatically
            //    Synonyms for the operator AND: and und & +
            //    Synonyms for the operator OR: or oder | ,
            //    Synonyms for the operator NOT: not nicht -
            //    Token delimiters: , "(){}[]:=\\/^~
            //
            // The query string preprocessor is enabled by default. Settings can be changed in elasticsearch.properties.

            Expression expression = new FulltextExpression("+Berlin +Germany");
            SearchResult searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin AND Germany");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin and Germany");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin & Germany");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin && Germany");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin Germany");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            expression = new FulltextExpression("Berlin OR London or Washington | Paris");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin,London,Washington,Paris");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            expression = new FulltextExpression("(Berlin AND Germany) OR (Paris AND France) OR Washington");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("Berlin AND Germany OR Paris AND France OR Washington");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            expression = new FulltextExpression("capital -Berlin");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());

            // same result as above:
            expression = new FulltextExpression("capital NOT Berlin");
            searchResult = singleIndexElasticsearchService.search(expression, SearchParameter.DEFAULT);
            LOGGER.info(searchResult.toString());
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }
}
