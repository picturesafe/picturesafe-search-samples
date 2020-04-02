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

package de.picturesafe.search.samples.multiindex;

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.IndexPresetConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.impl.StaticFieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.impl.StaticIndexPresetConfigurationProvider;
import de.picturesafe.search.spring.configuration.DefaultClientConfiguration;
import de.picturesafe.search.spring.configuration.DefaultQueryConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {"de.picturesafe.search.elasticsearch"})
@Import({DefaultClientConfiguration.class, DefaultQueryConfiguration.class})
public class Config {

    public static final String FIRST_INDEX_ALIAS = "myfirstindex";
    public static final String SECOND_INDEX_ALIAS = "mysecondindex";

    @Value("${elasticsearch.index.my_first_index.alias:" + FIRST_INDEX_ALIAS + "}")
    private String myFirstIndexAlias;

    @Value("${elasticsearch.index.my_second_index.alias:" + SECOND_INDEX_ALIAS + "}")
    private String mySecondIndexAlias;

    @Value("${elasticsearch.index.my_first_index.name_prefix:#{null}}")
    private String myFirstIndexNamePrefix;

    @Value("${elasticsearch.index.my_second_index.name_prefix:#{null}}")
    private String mySecondIndexNamePrefix;

    @Value("${elasticsearch.index.name_date_format:yyyyMMdd-HHmmss-SSS}")
    private String indexNameDateFormat;

    @Value("${elasticsearch.index.number_of_shards:1}")
    private int numberOfShards;

    @Value("${elasticsearch.index.number_of_replicas:0}")
    private int numberOfReplicas;

    @Value("${elasticsearch.index.fields_limit:1000}")
    private int fieldsLimit;

    @Value("${elasticsearch.index.max_result_window:10000}")
    private int maxResultWindow;

    @Bean
    IndexPresetConfigurationProvider indexPresetConfigurationProvider() {
        return new StaticIndexPresetConfigurationProvider(Arrays.asList(
                getIndexPresetConfiguration(myFirstIndexAlias, myFirstIndexNamePrefix),
                getIndexPresetConfiguration(mySecondIndexAlias, mySecondIndexNamePrefix)
        ));
    }

    @Bean
    FieldConfigurationProvider fieldConfigurationProvider() {
        final Map<String, List<FieldConfiguration>> fieldConfigurationMap = new HashMap<>();
        fieldConfigurationMap.put(myFirstIndexAlias, firstIndexfieldConfigurations());
        fieldConfigurationMap.put(mySecondIndexAlias, secondIndexfieldConfigurations());
        return new StaticFieldConfigurationProvider(fieldConfigurationMap);
    }

    private List<FieldConfiguration> firstIndexfieldConfigurations() {
        return Arrays.asList(
                FieldConfiguration.ID_FIELD,
                FieldConfiguration.FULLTEXT_FIELD,
                StandardFieldConfiguration.builder("title", ElasticsearchType.TEXT).copyToFulltext(true).sortable(true).build()
        );
    }

    private List<FieldConfiguration> secondIndexfieldConfigurations() {
        return Arrays.asList(
                FieldConfiguration.ID_FIELD,
                StandardFieldConfiguration.builder("firstname", ElasticsearchType.TEXT).sortable(true).build(),
                StandardFieldConfiguration.builder("lastname", ElasticsearchType.TEXT).sortable(true).build()
        );
    }

    private IndexPresetConfiguration getIndexPresetConfiguration(String indexAlias, String indexNamePrefix) {
        final StandardIndexPresetConfiguration cfg = new StandardIndexPresetConfiguration(indexAlias, indexNamePrefix,
                indexNameDateFormat, numberOfShards, numberOfReplicas, maxResultWindow);
        cfg.setFieldsLimit(fieldsLimit);
        cfg.setCharMappings(defaultCharMapping());
        return cfg;
    }

    private Map<String, String> defaultCharMapping() {
        final Map<String, String> charMapping = new HashMap<>();
        charMapping.put("ä", "ae");
        charMapping.put("ö", "oe");
        charMapping.put("ü", "ue");
        charMapping.put("ß", "ss");
        charMapping.put("Ä", "Ae");
        charMapping.put("Ö", "Oe");
        charMapping.put("Ü", "Ue");
        return charMapping;
    }
}
