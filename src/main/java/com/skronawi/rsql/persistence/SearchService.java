package com.skronawi.rsql.persistence;

import cz.jirutka.rsql.parser.ast.Node;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

public interface SearchService {

    Page<Movie> search(Node node, Sort.Order order, int page, int size);
}
