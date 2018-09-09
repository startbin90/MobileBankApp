\i accountSchema.ddl 
insert into person values ('123456789098765432', 'jiajun', 'chen');
insert into person values ('234567890987654321', 'davy', 'chen');

insert into accounts values ('12345671', '123456789098765432',100,'000000');
insert into accounts values ('12345672', '123456789098765432',100,'000000');
insert into accounts values ('12345673', '123456789098765432',100,'000000');
insert into accounts values ('12345674', '123456789098765432',100,'000000');
insert into accounts values ('12345675', '123456789098765432',100,'000000');
insert into accounts values ('12345676', '234567890987654321',100,'000000');
insert into accounts values ('12345677', '234567890987654321',100,'000000');
insert into accounts values ('12345678', '234567890987654321',100,'000000');
insert into accounts values ('12345679', '234567890987654321',100,'000000');
insert into accounts values ('12345680', '234567890987654321',100,'000000');

insert into linkedaccounts values ('12345671', '123456789098765432');
insert into linkedaccounts values ('12345672', '123456789098765432');
insert into linkedaccounts values ('12345683', '123456789098765432');
insert into linkedaccounts values ('12345684', '123456789098765432');
insert into linkedaccounts values ('12345685', '123456789098765432');
insert into linkedaccounts values ('12345686', '123456789098765432');
insert into linkedaccounts values ('12345687', '123456789098765432');

--select to_char(trans_id, '00000000') from transaction;
insert into transaction values (default, '1999-01-08 04:05:06','12345678','12345679','chen','+',50,50,'1','haha' );
insert into transaction values (default, '1999-01-08 04:05:10','12345678','12345679','chen','-',20,30,'1','haha' );
insert into transaction values (default, '2018-07-25 04:05:06','12345678','12345679','chen','+',20,50,'1','haha' );
insert into transaction values (default, '2018-07-25 04:05:10','12345678','12345679','chen','-',20,30,'1','haha' );
insert into transaction values (default, '2018-05-25 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-22 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );

insert into transaction values (default, '2018-07-23 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-23 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );
insert into transaction values (default, '2018-07-24 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-24 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );
insert into transaction values (default, '2018-07-25 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-25 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );
insert into transaction values (default, '2018-07-26 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-26 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );
insert into transaction values (default, '2018-07-27 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-27 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );
insert into transaction values (default, '2018-07-28 04:05:06','12345678','12345679','chen','-',10,20,'1','haha' );
insert into transaction values (default, '2018-07-28 04:05:10','12345678','12345679','chen','+',10,30,'1','haha' );



insert into mobilereg values('1@2.1', 'jiajun', 'chen', 'Male', '123456789098765432', '13775225800', 'bay st', 'cjj92501', '123', '1999-01-08 04:05:06', 'default');
insert into mobilereg values('1@2.2', 'jiajun', 'chen', 'Male', 'testnin', '13775225800', 'bay st', 'cjj92501', '123', '1999-01-08 04:05:06+08', 'default');

insert into recipients values('1@2.1', '12345686','jj', 'chen');
insert into recipients values('1@2.1', '12345687','jj', 'chen');
