package com.skronawi.rsql.logic;

import com.skronawi.rsql.persistence.Movie;
import com.skronawi.rsql.persistence.SearchService;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SearchLogic {

    @Autowired
    private SearchService searchService;

    public Page<Movie> search(String query, int page, int size) {

        //https://github.com/jirutka/rsql-parser
        //see also https://github.com/jirutka/rsql-hibernate or https://github.com/vineey/archelix-rsql
        Node node = new RSQLParser().parse(query);

        return searchService.search(node, new Sort.Order(Sort.Direction.ASC, "name"), page, size);
    }
}
