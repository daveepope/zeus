package org.zeus.controller;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.zeus.model.StatisticType;

@Component
public class StatisticTypeConverter implements Converter<String, StatisticType> {

    @Override
    public StatisticType convert(String source) {
        try {
            return StatisticType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid statistic type: " + source, e);
        }
    }
}
