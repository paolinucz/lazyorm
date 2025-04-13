package it.paolinucz.lazy.orm.pagination.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@SuperBuilder
public class LazyPaginationSearch {

    private int page;
    private int size;

}
