package com.skronawi.rsql;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.skronawi.rsql.logic.SearchLogic;
import com.skronawi.rsql.persistence.Movie;
import com.skronawi.rsql.persistence.MovieRepository;
import com.skronawi.rsql.persistence.PersistenceConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

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
@RunWith(SpringJUnit4ClassRunner.class)
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
    }

    @Autowired
    private SearchLogic searchLogic;

    @Autowired
    private MovieRepository movieRepository;

    /*
    - name=="Kill Bill";year=gt=2003
    - name=="Kill Bill" and year>2003
    - genres=in=(sci-fi,action);(director=='Christopher Nolan',actor==*Bale);year=ge=2000
    - genres=in=(sci-fi,action) and (director=='Christopher Nolan' or actor==*Bale) and year>=2000
    - director.lastName==Nolan;year=ge=2000;year=lt=2010
    - director.lastName==Nolan and year>=2000 and year<2010
    - genres=in=(sci-fi,action);genres=out=(romance,animated,horror),director==Que*Tarantino
    - genres=in=(sci-fi,action) and genres=out=(romance,animated,horror) or director==Que*Tarantino
     */

    @Test
    public void testSearchByName() throws Exception {
        Page<Movie> movies = searchLogic.search("name==Terminator", 0, 20);
        Assert.assertEquals(1, movies.getNumberOfElements());
        movies = searchLogic.search("name==\"Terminator 2\"", 0, 20);
        Assert.assertEquals(1, movies.getNumberOfElements());
    }
}
