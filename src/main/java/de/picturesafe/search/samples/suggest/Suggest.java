package de.picturesafe.search.samples.suggest;

import de.picturesafe.search.elasticsearch.DataChangeProcessingMode;
import de.picturesafe.search.elasticsearch.SingleIndexElasticsearchService;
import de.picturesafe.search.elasticsearch.config.DocumentBuilder;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.model.SuggestResult;
import de.picturesafe.search.expression.SuggestExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan
public class Suggest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Suggest.class);

    @Autowired
    private SingleIndexElasticsearchService singleIndexElasticsearchService;

    public static void main(String[] args) {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Suggest.class);
        final Suggest suggest = ctx.getBean(Suggest.class);
        suggest.run();
        ctx.close();
    }

    private void run() {
        singleIndexElasticsearchService.createIndexWithAlias();
        createTestRecords();

        final SuggestResult suggestResult = singleIndexElasticsearchService.suggest(new SuggestExpression("Ha", 10));
        showSuggestions(suggestResult);

        singleIndexElasticsearchService.deleteIndexWithAlias();
    }

    private void createTestRecords() {
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, DocumentBuilder.id(1).put("city", "Hamburg").build());
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, DocumentBuilder.id(2).put("city", "Berlin").build());
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, DocumentBuilder.id(3).put("city", "Hannover").build());
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, DocumentBuilder.id(4).put("city", "Freiburg").build());
        singleIndexElasticsearchService.addToIndex(DataChangeProcessingMode.BLOCKING, DocumentBuilder.id(5).put("city", "Hamburg").build());
    }

    private void showSuggestions(SuggestResult suggestResult) {
        for (String suggestion : suggestResult.getSuggestions(FieldConfiguration.FIELD_NAME_SUGGEST)) {
            LOGGER.info(suggestion);
        }
    }
}