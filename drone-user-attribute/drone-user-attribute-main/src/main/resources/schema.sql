/* ユーザ管理 Begin */
-- 事業者属性
DROP TABLE IF EXISTS operator_attribute CASCADE;

CREATE TABLE IF NOT EXISTS operator_attribute
(
    operator_id varchar(40) NOT NULL,
    mail_address varchar(255) NOT NULL,
    operator_name varchar(255) NOT NULL,
    role varchar NOT NULL,
    dips_account_id varchar(40),
    dips_account_name varchar(40),
    phone_number varchar(20),
    swim_operator_id varchar(3) NOT NULL,
    creation_id varchar(40) NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_id varchar(40) NOT NULL,
    update_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_flag boolean DEFAULT false NOT NULL,
    PRIMARY KEY (operator_id)
);

COMMENT ON COLUMN operator_attribute.operator_id IS '所属事業者ID(UUID)';
COMMENT ON COLUMN operator_attribute.mail_address IS 'メールアドレス';
COMMENT ON COLUMN operator_attribute.operator_name IS '事業者名';
COMMENT ON COLUMN operator_attribute.role IS 'ロール: 対応文字列は設定値参照';
COMMENT ON COLUMN operator_attribute.dips_account_id IS 'DIPSアカウントID';
COMMENT ON COLUMN operator_attribute.dips_account_name IS 'DIPSアカウント名';
COMMENT ON COLUMN operator_attribute.phone_number IS '電話番号';
COMMENT ON COLUMN operator_attribute.swim_operator_id IS 'SWIM連携用事業者ID';
COMMENT ON COLUMN operator_attribute.creation_id IS '作成者ID';
COMMENT ON COLUMN operator_attribute.creation_datetime IS '作成日時';
COMMENT ON COLUMN operator_attribute.update_id IS '更新者ID';
COMMENT ON COLUMN operator_attribute.update_datetime IS '更新日時';
COMMENT ON COLUMN operator_attribute.deleted_flag IS '事業者削除フラグ';

-- ユーザ属性
DROP TABLE IF EXISTS user_attribute;

CREATE TABLE IF NOT EXISTS user_attribute
(
    user_id varchar(40) NOT NULL,
    mail_address varchar(255) NOT NULL,
    user_name varchar(255) NOT NULL,
    role varchar NOT NULL,
    operator_id varchar(40) NOT NULL,
    creation_id varchar(40) NOT NULL,
    creation_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_id varchar(40) NOT NULL,
    update_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_flag boolean DEFAULT false NOT NULL,
    PRIMARY KEY (user_id),
    FOREIGN KEY (operator_id) REFERENCES operator_attribute(operator_id)
);

COMMENT ON COLUMN user_attribute.user_id IS 'ユーザID(UUID)';
COMMENT ON COLUMN user_attribute.mail_address IS 'メールアドレス';
COMMENT ON COLUMN user_attribute.user_name IS 'ユーザ名';
COMMENT ON COLUMN user_attribute.role IS 'ロール: 対応文字列は設定値参照';
COMMENT ON COLUMN user_attribute.operator_id IS '所属事業者ID(UUID)';
COMMENT ON COLUMN user_attribute.creation_id IS '作成者ID';
COMMENT ON COLUMN user_attribute.creation_datetime IS '作成日時';
COMMENT ON COLUMN user_attribute.update_id IS '更新者ID';
COMMENT ON COLUMN user_attribute.update_datetime IS '更新日時';
COMMENT ON COLUMN user_attribute.deleted_flag IS 'ユーザ削除フラグ';
/* ユーザ管理 End */