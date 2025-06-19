package com.springbootmicroservices.orderservice.model.common.mapper;

import java.util.List;
import java.util.Collection;
import java.util.stream.Collectors;

public interface BaseMapper<S, T> {

    T map(S source);

    default List<T> map(Collection<S> sources) {
        if (sources == null) {
            return null; // Or Collections.emptyList();
        }
        return sources.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}