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

package de.picturesafe.search.samples.collapse;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.model.DocumentBuilder;
import de.picturesafe.search.elasticsearch.model.SearchResult;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.parameter.CollapseOption;
import de.picturesafe.search.parameter.InnerHitsOption;
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
public class Collapse implements PicturesafeSearchSample {

    private static final Logger LOGGER = LoggerFactory.getLogger(Collapse.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Collapse.class)) {
            final Collapse collapse = ctx.getBean(Collapse.class);
            collapse.run();
        }
    }

    @Override
    public void run() {
        try {
            singleIndexElasticsearchService.createIndexWithAlias();

            singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, Arrays.asList(
                    DocumentBuilder.id(1).put("user", "Jeanne d’Arc").put("tweet", "Tweet A").put("likes", 10).build(),
                    DocumentBuilder.id(2).put("user", "Jeanne d’Arc").put("tweet", "Tweet B").put("likes", 101).build(),
                    DocumentBuilder.id(3).put("user", "John Doe").put("tweet", "Tweet C").put("likes", 8).build(),
                    DocumentBuilder.id(4).put("user", "John Doe").put("tweet", "Tweet D").put("likes", 99).build(),
                    DocumentBuilder.id(5).put("user", "Jeanne d’Arc").put("tweet", "Tweet E").put("likes", 5).build(),
                    DocumentBuilder.id(6).put("user", "Jeanne d’Arc").put("tweet", "Tweet F").put("likes", 16).build()
            ));

            final Expression expression = new ValueExpression("tweet", ValueExpression.Comparison.LIKE, "Tweet");

            findTweetsSortByLikes(expression);
            findMostLikedTweetOfEachUserSortByLikes(expression);
            findMostLikedTweetsOfEachUserSortByLikes(expression, 2);
            findMostLikedTweetsOfEachUserSortByLikes(expression, 3);
        } finally {
            singleIndexElasticsearchService.deleteIndexWithAlias();
        }
    }

    private void findTweetsSortByLikes(Expression expression) {
        final SearchParameter searchParameter = SearchParameter.builder().sortOptions(SortOption.desc("likes")).build();
        search(expression, searchParameter);
    }

    private void findMostLikedTweetOfEachUserSortByLikes(Expression expression) {
        // collapse search results based on field value
        final SearchParameter searchParameter = SearchParameter.builder()
                .sortOptions(SortOption.desc("likes"))
                .collapseOption(CollapseOption.field("user")).build();

        search(expression, searchParameter);
    }

    private void findMostLikedTweetsOfEachUserSortByLikes(Expression expression, int innerHitsSize) {
        // expand each collapsed top hits with the InnerHitsOption
        final SearchParameter searchParameter = SearchParameter.builder()
                .sortOptions(SortOption.desc("likes"))
                .collapseOption(CollapseOption.field("user")
                        .innerHits(InnerHitsOption.name("mostLiked").size(innerHitsSize).sortOptions(SortOption.desc("likes")))).build();

        search(expression, searchParameter);
    }

    private void search(Expression expression, SearchParameter searchParameter) {
        final SearchResult searchResult = singleIndexElasticsearchService.search(expression, searchParameter);
        LOGGER.info(searchResult.toString());
    }
}
