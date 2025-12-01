package com.ctxh.volunteer.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private int page;
    private int size;
    private int numberOfElements;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private List<T> contents;

    public static <T,R> PaginatedResponse<T> of(Page<R> page, Function<R,T> mapper) {
        List<T> data = page.getContent().stream()
                .map(mapper)
                .toList();
        return PaginatedResponse.<T>builder()
                .contents(data)
                .page(page.getNumber())
                .size(page.getSize())
                .numberOfElements(page.getNumberOfElements())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }



}
