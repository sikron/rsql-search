package com.skronawi.rsql.persistence;

import cz.jirutka.rsql.parser.ast.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/*
a own approach for building a query using predicates. as the maven-version of the "rsql-jpa" does
not support dates.

see
- https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
- http://www.baeldung.com/rest-api-search-language-rsql-fiql
 */
@Service
public class PredicateSearchService implements SearchService {

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Page<Movie> search(Node node, Sort.Order order, int page, int size) {

        MovieVisitor movieVisitor = new MovieVisitor();
        Specification<Movie> specification = node.accept(movieVisitor);

        return movieRepository.findAll(specification,
                new PageRequest(page, size, new Sort(order.getDirection(), order.getProperty())));
    }

    private class MovieVisitor implements RSQLVisitor<Specification<Movie>, EntityManager> {

        @Override
        public Specification<Movie> visit(AndNode andNode, EntityManager entityManager) {
            return null;
        }

        @Override
        public Specification<Movie> visit(OrNode orNode, EntityManager entityManager) {

            List<Node> children = orNode.getChildren();

            List<Specification<Movie>> specs = new ArrayList<>();
            for (Node child : children){
                specs.add(visitInternal(child, entityManager));
            }

            Specifications<Movie> where = Specifications.where(specs.get(0));
            specs.remove(0);
            for (Specification<Movie> spec : specs){
                where = Specifications.where(where).or(spec);
            }
            return where;
        }

        @Override
        public Specification<Movie> visit(ComparisonNode comparisonNode, EntityManager entityManager) {

            List<String> arguments = comparisonNode.getArguments();
            ComparisonOperator operator = comparisonNode.getOperator();
            String selector = comparisonNode.getSelector();

            return new MovieSpecification(arguments, operator, selector);
        }

        private Specification<Movie> visitInternal(Node node, EntityManager entityManager){
            if (node instanceof AndNode){
                return visit((AndNode)node, entityManager);
            } else if (node instanceof OrNode) {
                return visit((OrNode) node, entityManager);
            } else if (node instanceof ComparisonNode) {
                return visit((ComparisonNode) node, entityManager);
            }
            throw new IllegalStateException("node is not an AndNode, nor a OrNode nor a ComparisonNode: " + node);
        }
    }

    private class MovieSpecification implements Specification<Movie> {

        private final List<String> arguments;
        private final ComparisonOperator operator;
        private final String selector;

        public MovieSpecification(List<String> arguments, ComparisonOperator operator, String selector) {
            this.arguments = arguments;
            this.operator = operator;
            this.selector = selector;
        }

        @Override
        public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

            if (operator.getSymbol().equals(RSQLOperators.EQUAL.getSymbol())){
                Path<Object> path = root.get(selector);
                return criteriaBuilder.equal(path, arguments.get(0));
            }
            //...
            throw new IllegalArgumentException("to do");
        }
    }
}
