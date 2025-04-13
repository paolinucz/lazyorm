package it.paolinucz.lazy.orm.pagination;

import lombok.*;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class LazyPagination<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalCount;

}