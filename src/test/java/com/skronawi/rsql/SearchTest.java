package com.skronawi.rsql;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.skronawi.rsql.logic.SearchLogic;
import com.skronawi.rsql.persistence.*;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.util.*;
import java.util.stream.Collectors;

import static com.skronawi.rsql.SearchTest.TEST_DATA;

/*
https://springtestdbunit.github.io/spring-test-dbunit/
 */
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(Parameterized.class)
//use this for parameters and the @ClassRule and @Rule instead of the SpringJUnit4ClassRunner
@DatabaseSetup(TEST_DATA)
@ContextConfiguration(classes = {SearchTest.Config.class, PersistenceConfig.class})
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = {TEST_DATA})
public class SearchTest {

    public static final String TEST_DATA = "classpath:movies-test-data.xml";

    public static class Config {

        @Bean
        public SearchLogic searchLogic() {
            return new SearchLogic();
        }

        @Bean
        public SearchService searchService() {
//            return new CriteriaQuerySearchService();
            return new PredicateSearchService();
        }

    }

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private SearchLogic searchLogic;

    @Autowired
    private MovieRepository movieRepository;

    private final String query;
    private final List<String> expectedIds;

    public SearchTest(String query, List<String> expectedIds) {
        this.query = query;
        this.expectedIds = expectedIds;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"name==Terminator", Collections.singletonList("3")}, //single string
                {"name==terminator", Collections.singletonList("3")}, //case-insensitive search
                {"name==\"Terminator 2\"", Collections.singletonList("4")}, //string with blank
                {"name==Te*tor", Collections.singletonList("3")}, //wildcard
                {"name==Te*tor*", Arrays.asList("3", "4")}, //multiple wildcards
                {"name==*tor*", Arrays.asList("3", "4")}, //beginning wildcards
                {"name!=Aliens", Arrays.asList("2", "3", "4", "5", "6")}, //not equals
                {"name==Aliens or name==Terminator", Arrays.asList("1", "3")}, //or
                {"name==Aliens and name==Terminator", Collections.emptyList()}, //and
                {"name=in=(Aliens,Terminator)", Arrays.asList("1", "3")}, //in
                {"name=out=(Aliens,Terminator)", Arrays.asList("2", "4", "5", "6")}, //out
                {"year>=2000", Arrays.asList("1", "3", "4", "6")}, //>= on an int
                {"year>=2000 and year<2016", Arrays.asList("3", "4", "6")}, //< on an int
                {"year>=2000 and (name==Po* or name==\"Die Schö*\")", Collections.singletonList("6")}, //precedence of brackets
        });
    }

    @Test
    public void testSearchByName() throws Exception {

        Page<Movie> movies = searchLogic.search(query, 0, 20);

        Assert.assertEquals(expectedIds.size(), movies.getNumberOfElements());
        if (expectedIds.isEmpty()) {
            return;
        }

        Set<String> foundIds = movies.getContent().stream().map(Movie::getId).collect(Collectors.toSet());
        expectedIds.forEach(expectedId -> Assert.assertTrue(foundIds.contains(expectedId)));
    }
}
