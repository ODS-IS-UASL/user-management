/* ユーザ管理 Begin */
-- 事業者属性
insert into  operator_attribute (operator_id, mail_address, operator_name, role, dips_account_id, dips_account_name, phone_number, creation_id, update_id, deleted_flag) 
values ('650e8400-e29b-41d4-a716-446655440000','zzz@co.jp','operator00','1','dips_account_id_00','dips_account_name_00','090-0000-0000','650e8400-e29b-41d4-a716-446655440000','650e8400-e29b-41d4-a716-446655440000',false);
insert into  operator_attribute (operator_id, mail_address, operator_name, role, dips_account_id, dips_account_name, phone_number, creation_id, update_id, deleted_flag) 
values ('650e8400-e29b-41d4-a716-446655440001','yyy@co.jp','operator01','2','dips_account_id_01','dips_account_name_01','090-1111-1111','650e8400-e29b-41d4-a716-446655440001','650e8400-e29b-41d4-a716-446655440001',false);
insert into  operator_attribute (operator_id, mail_address, operator_name, role, dips_account_id, dips_account_name, phone_number, creation_id, update_id, deleted_flag) 
values ('650e8400-e29b-41d4-a716-446655440002','www@co.jp','operator02','2','dips_account_id_02','dips_account_name_02','090-2222-2222','650e8400-e29b-41d4-a716-446655440002','650e8400-e29b-41d4-a716-446655440002',true);
insert into  operator_attribute (operator_id, mail_address, operator_name, role, dips_account_id, dips_account_name, phone_number, creation_id, update_id, deleted_flag) 
values ('650e8400-e29b-41d4-a716-446655440003','xxx@co.jp','operator03','3','dips_account_id_03','dips_account_name_03','090-3333-3333','650e8400-e29b-41d4-a716-446655440003','650e8400-e29b-41d4-a716-446655440003',false);

-- ユーザ属性
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440000','aaa@co.jp','user00','10','650e8400-e29b-41d4-a716-446655440000','550e8400-e29b-41d4-a716-446655440000','550e8400-e29b-41d4-a716-446655440000',false);
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440001','bbb@co.jp','user01','11','650e8400-e29b-41d4-a716-446655440000','550e8400-e29b-41d4-a716-446655440000','550e8400-e29b-41d4-a716-446655440000',false);
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440002','ccc@co.jp','user02','20','650e8400-e29b-41d4-a716-446655440001','550e8400-e29b-41d4-a716-446655440001','550e8400-e29b-41d4-a716-446655440001',false);
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440003','ddd@co.jp','user03','21','650e8400-e29b-41d4-a716-446655440001','550e8400-e29b-41d4-a716-446655440002','550e8400-e29b-41d4-a716-446655440002',false);
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440004','eee@co.jp','user04','20','650e8400-e29b-41d4-a716-446655440002','550e8400-e29b-41d4-a716-446655440000','550e8400-e29b-41d4-a716-446655440000',false);
insert into  user_attribute (user_id, mail_address, user_name, role, operator_id, creation_id, update_id, deleted_flag) 
values ('550e8400-e29b-41d4-a716-446655440005','fff@co.jp','user05','21','650e8400-e29b-41d4-a716-446655440002','550e8400-e29b-41d4-a716-446655440004','550e8400-e29b-41d4-a716-446655440004',true);


/* ユーザ管理 End */