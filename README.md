# Searching via RSQL

This is a little project for playing around with some libraries for searching with RSQL.
RSQL is a super-set of the Feed Item Query Language (FIQL). The libraries provide a generic
way to search on JPA entities.

## Some examples:

    name=="Kill Bill";year=gt=2003
    name=="Kill Bill" and year>2003
    
    genres=in=(sci-fi,action);(director=='Christopher Nolan',actor==*Bale);year=ge=2000
    genres=in=(sci-fi,action) and (director=='Christopher Nolan' or actor==*Bale) and year>=2000
    
    director.lastName==Nolan;year=ge=2000;year=lt=2010
    director.lastName==Nolan and year>=2000 and year<2010
    
    genres=in=(sci-fi,action);genres=out=(romance,animated,horror),director==Que*Tarantino
    genres=in=(sci-fi,action) and genres=out=(romance,animated,horror) or director==Que*Tarantino

## Some links:

* http://www.baeldung.com/rest-api-search-language-rsql-fiql  
good example on baeldung, also with the following library
* https://github.com/jirutka/rsql-parser  
the parser used to transform the query into a "Node"
* https://github.com/tennaito/rsql-jpa  
transforms the "Node" into a JPA CriteriaQuery
* https://github.com/vineey/archelix-rsql  
For using QueryDsl with RSQL

## Issues i see

* what if the searched-for entity is related to other entities, e.g. via `@ManyToOne`? How can the "rsql-jpa"
transformer be used for this? Is e.g. such a path expression supported: `director.firstName==John`?
* the EntityManager is needed for the "rsql-jpa". How can this be used together with Spring and especially the
`@Repository` instances?
* the "rsql-jpa" cannot compare Dates - see the baeldung example and create a custom `Specification`, which supports this
