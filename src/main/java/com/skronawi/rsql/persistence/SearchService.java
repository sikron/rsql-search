package com.skronawi.rsql.persistence;

import com.github.tennaito.rsql.jpa.JpaCriteriaQueryVisitor;
import cz.jirutka.rsql.parser.ast.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.List;

/*
repository can not be used here as the library produces a CriteriaQuery. for this the entityManager has to be used

TODO how to get around this? maybe https://spring.io/blog/2011/04/26/advanced-spring-data-jpa-specifications-and-querydsl/
 */
@Service
public class SearchService {

    @Autowired
    private EntityManager entityManager;

    public Page<Movie> search(Node node, Sort.Order order, int page, int size) {

        //https://github.com/tennaito/rsql-jpa
        JpaCriteriaQueryVisitor<Movie> visitor = new JpaCriteriaQueryVisitor<>();
        CriteriaQuery<Movie> criteriaQuery = node.accept(visitor, entityManager);

        criteriaQuery.orderBy(new Order() {
            public Order reverse() {
                return null;
            }

            public boolean isAscending() {
                return order.isAscending();
            }

            public Expression<?> getExpression() {
                Root<?> next = criteriaQuery.getRoots().iterator().next();
                return next.get(order.getProperty());
            }
        });

        List<Movie> total = entityManager.createQuery(criteriaQuery).getResultList();
        List<Movie> resultList = entityManager.createQuery(criteriaQuery)
                .setFirstResult(page * size).setMaxResults(size).getResultList();

        return new PageImpl<>(resultList,
                new PageRequest(page, size, new Sort(order.getDirection(), order.getProperty())), total.size());
    }
}
