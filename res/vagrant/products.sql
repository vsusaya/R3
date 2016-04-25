--
-- PostgreSQL database dump
--


--after setup, run the following on local machine (assuming 'python parser.py' was already run to create the files referenced below)
--password is 'vagrant'
--scp -P 2222 products.txt vagrant@localhost:/home/vagrant
--scp -P 2222 reviews.txt vagrant@localhost:/home/vagrant

--psql -U vlad -h localhost store
--password is 'vlad'

--\copy product (productid, price, features, name, imgurl) FROM products.txt (DELIMITER(E'\t'));
--\copy review (reviewid, productid, title, author, overall, rdate, content) FROM reviews.txt (DELIMITER(E'\t'));

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;


DROP TABLE IF EXISTS product;

CREATE TABLE product (

    productid text,
    price text,
    features text,
    name text,
    imgurl text

);


--\.
--'stdin' data
--\.



DROP TABLE IF EXISTS review;

CREATE TABLE review (

    reviewid text,
    productid text,
    title text,
    author text,
    overall real,
    content text,
    rdate text

);


--
-- PostgreSQL database dump complete
--

