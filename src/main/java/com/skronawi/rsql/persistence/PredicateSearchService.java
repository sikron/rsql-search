package com.skronawi.rsql.persistence;

import com.github.tennaito.rsql.jpa.JpaPredicateVisitor;
import cz.jirutka.rsql.parser.ast.Node;
import cz.jirutka.rsql.parser.ast.RSQLVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;

@Service
public class PredicateSearchService implements SearchService {

    @Autowired
    private EntityManager entityManager;

    //see https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
    @Autowired
    private MovieRepository movieRepository;

    @Override
    public Page<Movie> search(Node node, Sort.Order order, int page, int size) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery criteria = builder.createQuery(Movie.class);
        From root = criteria.from(Movie.class);

        RSQLVisitor<Predicate, EntityManager> visitor = new JpaPredicateVisitor<>().defineRoot(root);

        Predicate predicate = node.accept(visitor, entityManager);

//        MovieSpec specification = new MovieSpec(predicate);
//        return (Page<Movie>) movieRepository.findAll(specification,
//                new PageRequest(page, size, new Sort(order.getDirection(), order.getProperty())));

        CriteriaQuery criteriaQuery = criteria.where(predicate).orderBy(new Order() {
            @Override
            public Order reverse() {
                return null;
            }

            @Override
            public boolean isAscending() {
                return order.isAscending();
            }

            @Override
            public Expression<?> getExpression() {
                return root.get(order.getProperty());
            }
        });

        List<Movie> total = entityManager.createQuery(criteriaQuery).getResultList();
        List<Movie> resultList = entityManager.createQuery(criteriaQuery)
                .setFirstResult(page * size).setMaxResults(size).getResultList();

        return new PageImpl<>(resultList,
                new PageRequest(page, size, new Sort(order.getDirection(), order.getProperty())), total.size());
    }

    private class MovieSpec implements Specification<Movie>{

        private final Predicate predicate;

        public MovieSpec(Predicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
            return predicate;
        }
    }
}
