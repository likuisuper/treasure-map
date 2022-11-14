package com.cxylk.apm.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


/**
 * @author likui
 * @date 2022/11/11 下午2:25
 **/
@Configuration
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    /**
     * 默认的converter不支持long到localdatime的转换，从elasticsearch读取的时候会报错，所以在这里添加一个。
     */
//    @Bean
//    @Override
//    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
//        List<Converter> converters= new ArrayList<>();
//        converters.add(LongToLocalDateTimeConverter.INSTANCE);
//        return new ElasticsearchCustomConversions(converters);
//    }

//    @ReadingConverter
//    static enum LongToLocalDateTimeConverter implements Converter<Long, LocalDateTime> {
//        INSTANCE;
//
//        private LongToLocalDateTimeConverter() {
//        }
//
//        @Override
//        public LocalDateTime convert(Long source) {
//            return Instant.ofEpochMilli(source).atZone(ZoneId.systemDefault()).toLocalDateTime();
//        }
//    }
}

