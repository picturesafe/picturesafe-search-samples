package de.picturesafe.search.samples.analyzer;

import de.picturesafe.search.elasticsearch.config.IndexSettingsObject;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.spring.configuration.DefaultIndexConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.Collections;

public class CustomIndexConfiguration extends DefaultIndexConfiguration {

    public static final String CUSTOM_ANALYZER_NAME = "file_name";

    @Bean
    @Override
    public StandardIndexPresetConfiguration indexPresetConfiguration() {

        final StandardIndexPresetConfiguration cfg = super.indexPresetConfiguration();
        try {
            final IndexSettingsObject fileNameTokenizer = new IndexSettingsObject("file_name_tokenizer");
            fileNameTokenizer.content().startObject()
                    .field("type", "char_group")
                    .array("tokenize_on_chars", "whitespace", ".", "-", "_", "\n")
                    .endObject();
            final IndexSettingsObject fileNameAnalyzer = new IndexSettingsObject(CUSTOM_ANALYZER_NAME);
            fileNameAnalyzer.content().startObject()
                    .field("type", "custom")
                    .field("tokenizer", "file_name_tokenizer")
                    .array("filter", "lowercase")
                    .endObject();
            cfg.setCustomTokenizers(Collections.singletonList(fileNameTokenizer));
            cfg.setCustomAnalyzers(Collections.singletonList(fileNameAnalyzer));
        } catch (IOException e) {
            throw new RuntimeException("Failed to set custom analyzer!", e);
        }
        return cfg;
    }
}
