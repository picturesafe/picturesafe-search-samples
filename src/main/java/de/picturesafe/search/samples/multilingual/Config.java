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

package de.picturesafe.search.samples.multilingual;

import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.IndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardFieldConfiguration;
import de.picturesafe.search.elasticsearch.impl.StaticFieldConfigurationProvider;
import de.picturesafe.search.spring.configuration.DefaultElasticConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = {"de.picturesafe.search.elasticsearch"})
@Import({DefaultElasticConfiguration.class})
public class Config {

    // By default, the FieldConfigurationProvider defined in DefaultElasticConfiguration/DefaultIndexConfiguration supports German and English.
    // If other languages are required, the FieldConfigurationProvider bean must be overwritten:
    @Bean
    @Primary
    FieldConfigurationProvider fieldConfigurationProvider(IndexPresetConfiguration indexPresetConfiguration,
                                                          List<FieldConfiguration> fieldConfigurations) {
        final Map<String, List<FieldConfiguration>> fieldConfigurationMap = new HashMap<>();
        fieldConfigurationMap.put(indexPresetConfiguration.getIndexAlias(), fieldConfigurations);
        final StaticFieldConfigurationProvider fieldConfigurationProvider = new StaticFieldConfigurationProvider(fieldConfigurationMap);
        fieldConfigurationProvider.setSupportedLocales(Arrays.asList(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH));
        return fieldConfigurationProvider;
    }

    @Bean
    List<FieldConfiguration> fieldConfigurations() {
        return Collections.singletonList(
                StandardFieldConfiguration.builder("title", ElasticsearchType.TEXT).multilingual(true).build()
        );
    }
}
