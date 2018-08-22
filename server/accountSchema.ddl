
-- This file descripts a simulated banking service for moible users. The clients of the bank(people who 
-- own the bank accounts of the bank) can download the Android APP on which clients is able to check bank
-- accounts balance, make transactions and etc. 

-- Clients should provide NIN (National Identity Number in China, it can also be passport number or 
-- social security number in other countries as long as it can identify a person), one of their 
-- bank accounts and correct withdrawal password for that account to validate its identity. Then the 
-- client should provide some personal info to register. The registration info is stored in table mobilereg.
-- The account the client provided in registration is automatically linked to this mobile banking registration
-- by inserting the account to table linkedaccounts. The client can choose to link more accounts to mobile banking.

-- Clients can log in mobile banking service using the registered email address or either one of account number
-- which has been linked.

drop schema if exists accountschema cascade;
create schema accountschema;
set search_path to accountschema;

create type sex_type as ENUM('Male', 'Female');

-- This table documents basic information of the client
create table person(
	id char(18) primary key,
	firstname varchar(10) not null,
	lastname varchar(10) not null
);

-- This table actually should exist in another database system. This table can only be read but not modified by 
-- Server in charge of mobile banking services.
--     'id': id is the number which identifies everyone. It is also called National Identity Number
--         in China. Everyone has his/her own unique NIN. the user should be responsible to provide their 
--         real NIN.
-- 		'withdrawpwd': The password used to do transaction in ATM or in bank counters. Also used to link 
--         accounts to mobile banking service.
create table accounts(
	account char(8) primary key,
	id char(18) references person(id) on update cascade,
	balance real not null,
	withdrawpwd char(6) not null
); 

-- mobilereg table documents the registration of mobile banking service.
-- It is the main table of mobile banking background database.
-- Every existing client of the bank has to register in this table to become a mobile banking service user, 
-- and provide infomation as listed below. Meanwhile, he has to set up mobile bank access Password and 
-- Transaction Password for doing transaction on mobile devices.
--     'email': the user has to provide an emial to register and is able to use email to log in
--     'name': the user's nickname. The name the user would like to be called in the Mobile Banking APP
--     'id': id is the number which identifies everyone. It is also called National Identity Number
--         in China. Everyone has his/her own unique NIN. the user should be responsible to provide their 
--         real NIN.
--     'cell': user's cellphone. Should be unique in the table.
--     'address': user's address
--     'pwd': Password using to login mobile banking service.
--     'transpwd': Transaction password used when the user want to finish a transaction on Mobile banking APP.
--     'opendate': the date when this user registers this service.
--     'status': the status of this registration, default as true. If the user desires to close mobile banking 
--          service. This will be set to false;
create table mobilereg(
	-- account
	email varchar(50) unique not null,
    name varchar(10) not null,
	sex sex_type not null,
	id char(18) primary key references person(id) on update cascade,
	cell varchar(15) unique not null,
	address varchar(100) not null,
	-- password
	pwd varchar(20) not null,
	transpwd varchar(10) not null,
	opendate timestamp not null,
	status boolean default true
	
);

-- accounts linked to mobile banking will be documented in this table.
-- accounts can only be linked to mobile banking registration with the same id
-- and lastname and firstname.
-- One account should be linked to one and only one mobile banking registration.
-- One id or one mobile banking registration can have several or zero accounts linked to itself.
-- the person represented by id must has already opened mobile banking service
create table linkedaccounts(
	account char(8) primary key references accounts(account) on update cascade, 
	-- id is unnecessary in this case but it provides a good foreign key constraint 
	-- which allows only id in mobilereg table to be added in this table.
	id char(18) references mobilereg(id) on update cascade
);

-- This table stores the saved payee info for each mobile bank registration.
-- email should be in mobilereg(email) but account is allowed to be any number even though
-- it is not in table accounts.
create table recipients(
	email varchar(50) references mobilereg(email) ON update cascade, 
	account char(8) not null,
	firstname varchar(10) not null,
	lastname varchar(10) not null,
	primary key(email, account)
);

-- transaction table documents all the transaction details.
-- One transaction will generate two rows in this table.
-- trans_from is not allowed to be the same as trans_to.
-- trans_value should be larger than 0.
create table transaction(
	--serial is only Integer large, id can be bigger than Int
	trans_id SERIAL PRIMARY KEY,
	trans_date timestamp not null,
	-- on update cascade should never occurred since the account number will not be modified
	trans_from char(8) references accounts(account) on update cascade not null,
	trans_to char(8) references accounts(account)  on update cascade not null,
	-- last name of trans_to
	trans_to_lastname varchar(10) not null,
	-- transaction direction, '-' stands for borrow, '+' stands' for loan
	trans_dir char(1) not null,
	trans_value real not null CONSTRAINT positive_price check (trans_value > 0),
	-- balance of trans_from after the transaction
	trans_post_balance real not null,
	--transaction channel, 1 stands for mobile Client, 2 stands for Web, 3 stands for bank counter
	-- 交易渠道1:手机2.网银3:柜台
	trans_channel char(1) not null,
	trans_memo varchar(100),
	CHECK (trans_from != trans_to)

);


