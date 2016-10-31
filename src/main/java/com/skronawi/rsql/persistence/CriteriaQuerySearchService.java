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
public class CriteriaQuerySearchService implements SearchService {

    @Autowired
    private EntityManager entityManager;

    @Override
    public Page<Movie> search(Node node, Sort.Order order, int page, int size) {

        //https://github.com/tennaito/rsql-jpa
        JpaCriteriaQueryVisitor<Movie> visitor = new JpaCriteriaQueryVisitor<>();

        visitor.getBuilderTools().setPropertiesMapper((s, aClass) -> {
            if (s.equalsIgnoreCase("isratedm")) {
                return "isRatedM";
            }
            if (s.equalsIgnoreCase("cost")) {
                return "costInMillionDollars";
            }
            return s;
        });

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

        /*
        analyzing the hibernate queries for the RSQL query "regisseur.firstName=in=(John,Max)" a little bit.
        with the rsql-jpa visitor, per criteriaQuery 3 queries are performed
        - find the movies
        - find the person John
        - find the person Max
        this can be optimized using fetch joins.

        ok, for a "normal" query with e.g. 3 such "in" attributes, the impact will not be too big. but nevertheless
        using joins, only 1 query will be performed!


        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Movie> movieQuery = cb.createQuery(Movie.class);
        Root<Movie> movie = movieQuery.from(Movie.class);
        //http://stackoverflow.com/questions/4511368/jpa-2-criteria-fetch-path-navigation
        movie.fetch("regisseur"); //this will result in an inner join, so that the regisseurs are NOT fetched singularly for every found movie (ala N+1)
        movieQuery = movieQuery.select(movie).where(movie.get("regisseur").get("firstName").in("John", "Max"));

        List<Movie> total = entityManager.createQuery(movieQuery).getResultList();
        List<Movie> resultList = entityManager.createQuery(movieQuery)
                .setFirstResult(page * size).setMaxResults(size).getResultList();
        return new PageImpl<>(resultList,
                new PageRequest(page, size, new Sort(order.getDirection(), order.getProperty())), total.size());
        */
    }
}
