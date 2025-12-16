create sequence seq_users INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_categories INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_products INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_payments INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_favorite_products INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_reviews INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;
create sequence seq_accounts INCREMENT BY 1 START WITH 1 MINVALUE 1 MAXVALUE 9999 NOCYCLE NOCACHE NOORDER;

CREATE TABLE users (
	id varchar2(20) primary key,
    pw varchar2(20) not null,
	nickname varchar2(50) not NULL,
    profile_image varchar2(250) NULL,
    email varchar2(50) not NULL,
	address	varchar2(100) NULL,
    phone varchar2(20) null,
    birth DATE null,
    role varchar2(20)
);

CREATE TABLE categories (
	category_id	NUMBER primary key,
	category varchar2(50) not null
);

CREATE TABLE products (
	product_id NUMBER primary key,
	seller_id varchar(20) NOT NULL,
	category_id	NUMBER NOT NULL,
	price NUMBER NOT NULL,
	title varchar2(100) NOT NULL,
	description	varchar2(1000) NOT NULL,
    product_image varchar2(250) NOT NULL,
	view_cnt NUMBER DEFAULT 0,
	created_at date NOT NULL,
    product_state NUMBER(1) DEFAULT 0,
    foreign key (seller_id) 
    references users (id) ON DELETE CASCADE,
    foreign key (category_id) 
    references categories (category_id) ON DELETE CASCADE
);

create table payments (
    imp_uid varchar2(50) primary key,
    merchant_uid varchar2(50) not null,
    task_id NUMBER not null,
    amount NUMBER not null
);

CREATE TABLE favorite_products (
	fp_id NUMBER primary key,
	user_id	varchar(20) NOT NULL,
	product_id NUMBER NOT NULL,
    foreign key (user_id) 
    references users (id) ON DELETE CASCADE,
    foreign key (product_id) 
    references products (product_id) ON DELETE CASCADE
);

create table reviews (
    review_id number primary key,
    seller_id varchar2(20) not null,
    reviewer_id varchar2(20) not null,
    score number not null,
    content varchar2(500) NULL,
    foreign key (reviewer_id)
    references users (id) ON DELETE CASCADE,
    foreign key (seller_id)
    references users (id) ON DELETE CASCADE
);
create table accounts(
    account_id number primary key,
    seller_id varchar2(20) not null,
    bank_code number not null,
    account_number varchar(100),
    foreign key (seller_id) 
    references users (id) ON DELETE CASCADE
);