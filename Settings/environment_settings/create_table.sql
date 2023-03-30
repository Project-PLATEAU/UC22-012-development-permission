-- 開発許可申請管理システムテーブル作成DDL
-- 順次実行しテーブルを作成してください。

-- M_行政ユーザ
drop table if exists m_government_user cascade;

create table m_government_user (
  user_id character varying(10) not null
  , login_id character varying(50) not null
  , password character varying(1024)
  , role_code character(1)
  , department_id character varying(10) not null
  , constraint m_government_user_PKC primary key (user_id)
) ;

-- M_申請情報検索結果
drop table if exists m_application_search_result cascade;

create table m_application_search_result (
  application_search_result_id character varying(10) not null
  , reference_type character(1)
  , display_column_name character varying(100)
  , display_order integer
  , table_name character varying(100)
  , table_column_name character varying(100)
  , response_key character varying(100)
  , table_width real
  , constraint m_application_search_result_PKC primary key (application_search_result_id)
) ;

-- M_地番検索結果定義
drop table if exists m_lot_number_search_result_definition cascade;

create table m_lot_number_search_result_definition (
  lot_number_search_definition_id character varying(10) not null
  , display_order integer
  , table_type character(1)
  , display_column_name character varying(50)
  , table_column_name character varying(100)
  , table_width real
  , response_key character varying(100)
) ;

-- O_申請区分
drop table if exists o_application_category cascade;

create table o_application_category (
  application_id integer not null
  , view_id character varying(10) not null
  , category_id character varying(10) not null
  , constraint o_application_category_PKC primary key (application_id,view_id,category_id)
) ;

create unique index idx_o_application_category
  on o_application_category(application_id,view_id,category_id);

-- O_回答ファイル
drop table if exists o_answer_file cascade;

create table o_answer_file (
  answer_file_id integer not null
  , answer_id integer not null
  , answer_file_name text
  , file_path text
  , notified_file_path text
  , delete_unnotified_flag character(1)
  , constraint o_answer_file_PKC primary key (answer_file_id)
) ;

alter table o_answer_file add constraint idx_o_answer_file
  unique (answer_id,answer_file_id) ;

-- O_申請ファイル
drop table if exists o_application_file cascade;

create table o_application_file (
  file_id integer not null
  , application_id integer not null
  , application_file_id character varying(10) not null
  , upload_file_name text
  , file_path text
  , constraint o_application_file_PKC primary key (file_id)
) ;

create unique index idx_o_application_file
  on o_application_file(application_id,application_file_id,file_id);

-- O_申請地番
drop table if exists o_application_lot_number cascade;

create table o_application_lot_number (
  lot_number_id integer not null
  , application_id integer not null
  , regeister_datetime timestamp
  , constraint o_application_lot_number_PKC primary key (lot_number_id,application_id)
) ;

create unique index idx_o_application_lot_number
  on o_application_lot_number(application_id,lot_number_id);

-- O_回答
drop table if exists o_answer cascade;

create table o_answer (
  answer_id integer not null
  , application_id integer not null
  , judgement_id character varying(10) not null
  , judgement_result text
  , answer_content text
  , notified_text text
  , register_datetime timestamp
  , update_datetime timestamp
  , complete_flag character(1)
  , notified_flag character(1)
  , constraint o_answer_PKC primary key (answer_id)
) ;

alter table o_answer add constraint idx_o_answer
  unique (application_id,judgement_id,answer_id) ;

-- M_レイヤ
drop table if exists m_layer cascade;

create table m_layer (
  layer_id character varying(10) not null
  , layer_type character(1)
  , layer_name character varying(256)
  , table_name character varying(30)
  , layer_code character varying(256)
  , layer_query character varying(256)
  , query_require_flag character(1)
  , constraint m_layer_PKC primary key (layer_id)
) ;

create unique index idx_m_layer
  on m_layer(layer_id);

-- M_申請者情報項目
drop table if exists m_applicant_information_item cascade;

create table m_applicant_information_item (
  applicant_information_item_id character varying(10) not null
  , display_order integer
  , display_flag CHAR(1)
  , require_flag CHAR(1)
  , item_name character varying(256)
  , regex character varying(256)
  , mail_address CHAR(1)
  , search_condition_flag CHAR(1)
  , constraint m_applicant_information_item_PKC primary key (applicant_information_item_id)
) ;

alter table m_applicant_information_item add constraint idx_m_applicant_information_item
  unique (applicant_information_item_id) ;

-- M_ラベル
drop table if exists m_label cascade;

create table m_label (
  view_code character varying(10) not null
  , label_id character varying(10) not null
  , label_key character varying(50)
  , label_type char(1)
  , label_text text
  , constraint m_label_PKC primary key (view_code,label_id)
) ;

create unique index idx_m_label
  on m_label(view_code,label_id);

-- O_申請者情報
drop table if exists o_applicant_information cascade;

create table o_applicant_information (
  application_id integer not null
  , applicant_id integer not null
  , item_1 text
  , item_2 text
  , item_3 text
  , item_4 text
  , item_5 text
  , item_6 text
  , item_7 text
  , item_8 text
  , item_9 text
  , item_10 text
  , mail_address character varying(100)
  , collation_id character varying(20)
  , password character varying(1024)
  , constraint o_applicant_information_PKC primary key (applicant_id)
) ;

alter table o_applicant_information add constraint idx_o_applicant_information
  unique (applicant_id) ;

-- M_部署
drop table if exists m_department cascade;

create table m_department (
  department_id character varying(10) not null
  , department_name text
  , mail_address text
  , answer_authority_flag char(1)
  , constraint m_department_PKC primary key (department_id)
) ;

create unique index idx_m_department
  on m_department(department_id);

-- O_申請
drop table if exists o_application cascade;

create table o_application (
  application_id integer not null
  , applicant_id integer
  , status character(1)
  , register_status character(1)
  , collation_text character varying(100)
  , register_datetime timestamp
  , update_datetime timestamp
  , constraint o_application_PKC primary key (application_id)
) ;

alter table o_application add constraint idx_o_application
  unique (application_id) ;

-- M_申請ファイル
drop table if exists m_application_file cascade;

create table m_application_file (
  application_file_id character varying(10) not null
  , judgement_item_id character varying(10) not null
  , require_flag char(1)
  , upload_file_name text
  , constraint m_application_file_PKC primary key (application_file_id,judgement_item_id)
) ;

create unique index idx_m_application_file
  on m_application_file(judgement_item_id,application_file_id);

-- M_区分判定
drop table if exists m_category_judgement cascade;

create table m_category_judgement (
  judgement_item_id character varying(10) not null
  , department_id character varying(10)
  , category_1 text
  , category_2 text
  , category_3 text
  , category_4 text
  , category_5 text
  , category_6 text
  , category_7 text
  , category_8 text
  , category_9 text
  , category_10 text
  , gis_judgement char(1)
  , buffer double precision
  , display_attribute_flag char(1)
  , judgement_layer character varying(100)
  , title text
  , applicable_summary text
  , applicable_description text
  , non_applicable_display_flag char(1)
  , non_applicable_summary text
  , non_applicable_description text
  , table_name text
  , field_name text
  , non_applicable_layer_display_flag char(1)
  , simultaneous_display_layer text
  , simultaneous_display_layer_flag char(1)
  , disp_order double precision
  , answer_require_flag char(1)
  , default_answer text
  , answer_editable_flag char(1)
  , constraint m_category_judgement_PKC primary key (judgement_item_id)
) ;

create unique index idx_m_category_judgement
  on m_category_judgement(judgement_item_id);

-- M_申請区分
drop table if exists m_application_category cascade;

create table m_application_category (
  category_id character varying(10) not null
  , view_id character varying(10) not null
  , "order" integer
  , label_name character varying(256)
  , constraint m_application_category_PKC primary key (category_id,view_id)
) ;

create unique index idx_m_application_category
  on m_application_category(view_id,category_id);

-- M_申請区分選択画面
drop table if exists m_application_category_selection_view cascade;

create table m_application_category_selection_view (
  view_id character varying(10) not null
  , view_flag character(1)
  , multiple_flag character(1)
  , require_flag character(1)
  , title text
  , description text
  , constraint m_application_category_selection_view_PKC primary key (view_id)
) ;

create unique index idx_m_application_category_selection_view
  on m_application_category_selection_view(view_id);

comment on table m_government_user is 'M_行政ユーザ';
comment on column m_government_user.user_id is 'ユーザID';
comment on column m_government_user.login_id is 'ログインID';
comment on column m_government_user.password is 'パスワード';
comment on column m_government_user.role_code is 'ロールコード:1: 事業者 2: 行政';
comment on column m_government_user.department_id is '部署ID';

comment on table m_application_search_result is 'M_申請情報検索結果';
comment on column m_application_search_result.application_search_result_id is '申請情報検索結果ID';
comment on column m_application_search_result.reference_type is '参照タイプ:0:申請区分 1:申請者情報';
comment on column m_application_search_result.display_column_name is '表示カラム名:画面表示するカラム名';
comment on column m_application_search_result.display_order is '表示順';
comment on column m_application_search_result.table_name is 'テーブル名:参照テーブル名';
comment on column m_application_search_result.table_column_name is 'テーブルカラム名:参照カラム名';
comment on column m_application_search_result.response_key is 'レスポンスキー:レスポンスJSONのキー';
comment on column m_application_search_result.table_width is 'テーブル幅';

comment on table m_lot_number_search_result_definition is 'M_地番検索結果定義';
comment on column m_lot_number_search_result_definition.lot_number_search_definition_id is '地番検索結果定義ID';
comment on column m_lot_number_search_result_definition.display_order is '表示順';
comment on column m_lot_number_search_result_definition.table_type is 'テーブル種別:1:F_地番テーブル 0:F_大字テーブル';
comment on column m_lot_number_search_result_definition.display_column_name is '表示カラム名:画面表示するカラム名';
comment on column m_lot_number_search_result_definition.table_column_name is 'テーブルカラム名:地番検索結果表示カラム名を指定';
comment on column m_lot_number_search_result_definition.table_width is 'テーブル幅:%指定.データカラムの幅の合計で100%となるように設定.';
comment on column m_lot_number_search_result_definition.response_key is 'レスポンスキー:レスポンスJSONキー';

comment on table o_application_category is 'O_申請区分';
comment on column o_application_category.application_id is '申請ID';
comment on column o_application_category.view_id is '画面ID';
comment on column o_application_category.category_id is '申請区分ID';

comment on table o_answer_file is 'O_回答ファイル';
comment on column o_answer_file.answer_file_id is '回答ファイルID:ファイルごとに付与される一意のID';
comment on column o_answer_file.answer_id is '回答ID';
comment on column o_answer_file.answer_file_name is '回答ファイル名';
comment on column o_answer_file.file_path is 'ファイルパス';
comment on column o_answer_file.notified_file_path is '通知済みファイルパス:通知済みの回答ファイルパス';
comment on column o_answer_file.delete_unnotified_flag is '削除未通知フラグ:1:削除済み・未通知';

comment on table o_application_file is 'O_申請ファイル';
comment on column o_application_file.file_id is 'ファイルID:ファイルごとに付与される一意のID';
comment on column o_application_file.application_id is '申請ID';
comment on column o_application_file.application_file_id is '申請ファイルID';
comment on column o_application_file.upload_file_name is 'アップロードファイル名';
comment on column o_application_file.file_path is 'ファイルパス';

comment on table o_application_lot_number is 'O_申請地番';
comment on column o_application_lot_number.lot_number_id is '地番ID';
comment on column o_application_lot_number.application_id is '申請ID';
comment on column o_application_lot_number.regeister_datetime is '登録日時';

comment on table o_answer is 'O_回答';
comment on column o_answer.answer_id is '回答ID';
comment on column o_answer.application_id is '申請ID';
comment on column o_answer.judgement_id is '判定項目ID';
comment on column o_answer.judgement_result is '判定結果';
comment on column o_answer.answer_content is '回答内容:最新の回答内容';
comment on column o_answer.notified_text is '通知テキスト:通知済みの回答内容';
comment on column o_answer.register_datetime is '登録日時';
comment on column o_answer.update_datetime is '更新日時';
comment on column o_answer.complete_flag is '完了フラグ:1: 完了 0: 未完了';
comment on column o_answer.notified_flag is '通知フラグ:1:通知済み 0:未通知';

comment on table m_layer is 'M_レイヤ';
comment on column m_layer.layer_id is 'レイヤID';
comment on column m_layer.layer_type is 'レイヤ種別:1: 判定対象レイヤ 0: 関連レイヤ';
comment on column m_layer.layer_name is 'レイヤ名:画面に表示するレイヤ名';
comment on column m_layer.table_name is 'テーブル名:フィーチャテーブル格納テーブル名';
comment on column m_layer.layer_code is 'レイヤコード:GeoServer上のレイヤID';
comment on column m_layer.layer_query is 'レイヤクエリ:GeoServerにリクエストを投げる際のクエリ文字列';
comment on column m_layer.query_require_flag is 'クエリ必須フラグ:1: 必須 0:不要';

comment on table m_applicant_information_item is 'M_申請者情報項目';
comment on column m_applicant_information_item.applicant_information_item_id is '申請者情報項目ID';
comment on column m_applicant_information_item.display_order is '昇順';
comment on column m_applicant_information_item.display_flag is '表示有無';
comment on column m_applicant_information_item.require_flag is '必須有無';
comment on column m_applicant_information_item.item_name is '項目名';
comment on column m_applicant_information_item.regex is '正規表現:空の場合正規表現チェックはしない';
comment on column m_applicant_information_item.mail_address is 'メールアドレス:1=メールアドレス';
comment on column m_applicant_information_item.search_condition_flag is '検索条件表示有無';

comment on table m_label is 'M_ラベル';
comment on column m_label.view_code is '画面コード';
comment on column m_label.label_id is 'ラベルID';
comment on column m_label.label_key is 'ラベルキー';
comment on column m_label.label_type is '種別:0:事業者行政ともに使用 1:事業者のみ使用 2:行政のみ使用';
comment on column m_label.label_text is 'テキスト';

comment on table o_applicant_information is 'O_申請者情報';
comment on column o_applicant_information.application_id is '申請ID';
comment on column o_applicant_information.applicant_id is '申請者情報ID';
comment on column o_applicant_information.item_1 is '項目1:申請者情報項目ID=1001の登録値を格納';
comment on column o_applicant_information.item_2 is '項目2:申請者情報項目ID=1002の登録値を格納';
comment on column o_applicant_information.item_3 is '項目3:申請者情報項目ID=1003の登録値を格納';
comment on column o_applicant_information.item_4 is '項目4:申請者情報項目ID=1004の登録値を格納';
comment on column o_applicant_information.item_5 is '項目5:申請者情報項目ID=1005の登録値を格納';
comment on column o_applicant_information.item_6 is '項目6:申請者情報項目ID=1006の登録値を格納';
comment on column o_applicant_information.item_7 is '項目7:申請者情報項目ID=1007の登録値を格納';
comment on column o_applicant_information.item_8 is '項目8:申請者情報項目ID=1008の登録値を格納';
comment on column o_applicant_information.item_9 is '項目9:申請者情報項目ID=1009の登録値を格納';
comment on column o_applicant_information.item_10 is '項目10:申請者情報項目ID=1010の登録値を格納';
comment on column o_applicant_information.mail_address is 'メールアドレス:通知に使用するメールアドレス';
comment on column o_applicant_information.collation_id is '照合ID';
comment on column o_applicant_information.password is 'パスワード:ハッシュ化して格納';

comment on table m_department is 'M_部署';
comment on column m_department.department_id is '部署ID';
comment on column m_department.department_name is '部署名';
comment on column m_department.mail_address is 'メールアドレス:通知先のメールアドレスをカンマ区切りで保持';
comment on column m_department.answer_authority_flag is '回答権限フラグ';

comment on table o_application is 'O_申請';
comment on column o_application.application_id is '申請ID';
comment on column o_application.applicant_id is '申請者情報ID';
comment on column o_application.status is 'ステータス:0:申請中 1:回答中（未回答課あり）2:回答完了 3:通知済み';
comment on column o_application.register_status is '登録ステータス:0: 仮申請中 1: 申請済み';
comment on column o_application.collation_text is '照合文字列:本登録時の照合文字列';
comment on column o_application.register_datetime is '登録日時';
comment on column o_application.update_datetime is '更新日時';

comment on table m_application_file is 'M_申請ファイル';
comment on column m_application_file.application_file_id is '申請ファイルID';
comment on column m_application_file.judgement_item_id is '判定項目ID';
comment on column m_application_file.require_flag is '必須有無:1:必須 0:任意';
comment on column m_application_file.upload_file_name is 'アップロードファイル名';

comment on table m_category_judgement is 'M_区分判定';
comment on column m_category_judgement.judgement_item_id is '判定項目ID';
comment on column m_category_judgement.department_id is '担当部署ID';
comment on column m_category_judgement.category_1 is '区分1:画面ID1001と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_2 is '区分2:画面ID1002と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_3 is '区分3:画面ID1003と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_4 is '区分4:画面ID1004と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_5 is '区分5:画面ID1005と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_6 is '区分6:画面ID1006と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_7 is '区分7:画面ID1007と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_8 is '区分8:画面ID1008と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_9 is '区分9:画面ID1009と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.category_10 is '区分10:画面ID1010と対応.
対象の申請区分IDをカンマ区切りで格納.
判定に用いない場合0を格納.';
comment on column m_category_judgement.gis_judgement is 'GIS判定:0: GIS判定なし 1: 重なり判定 2: 重ならない判定 3: バッファ判定';
comment on column m_category_judgement.buffer is 'バッファ:バッファ判定時のバッファ半径 (m)';
comment on column m_category_judgement.display_attribute_flag is '重なり属性表示フラグ:0: 属性表示なし 1: 区切り文字で区切って表示 2:改行表示';
comment on column m_category_judgement.judgement_layer is '判定対象レイヤ:GIS判定で使用するレイヤのレイヤID（カンマ区切り）';
comment on column m_category_judgement.title is 'タイトル';
comment on column m_category_judgement.applicable_summary is '該当表示概要';
comment on column m_category_judgement.applicable_description is '該当表示文言';
comment on column m_category_judgement.non_applicable_display_flag is '非該当表示有無:1:表示 0:非表示';
comment on column m_category_judgement.non_applicable_summary is '非該当表示概要';
comment on column m_category_judgement.non_applicable_description is '非該当表示文言';
comment on column m_category_judgement.table_name is 'テーブル名:文言のラベルとして使用するテーブル名';
comment on column m_category_judgement.field_name is 'フィールド名:文言のラベルとして使用するフィールド名';
comment on column m_category_judgement.non_applicable_layer_display_flag is '判定レイヤ非該当時表示有無:1:表示 0:非表示';
comment on column m_category_judgement.simultaneous_display_layer is '同時表示レイヤ:該当診断結果画面で同時表示する関連レイヤのレイヤID（カンマ区切り）';
comment on column m_category_judgement.simultaneous_display_layer_flag is '同時表示レイヤ表示有無:1:表示 0: 非表示';
comment on column m_category_judgement.disp_order is '表示順';
comment on column m_category_judgement.answer_require_flag is '回答必須フラグ:1:回答必須 0:回答任意';
comment on column m_category_judgement.default_answer is 'デフォルト回答:回答任意の項目に登録する初期回答文言';
comment on column m_category_judgement.answer_editable_flag is '編集可能フラグ :1:編集可能 0:編集不可';

comment on table m_application_category is 'M_申請区分';
comment on column m_application_category.category_id is '申請区分ID';
comment on column m_application_category.view_id is '画面ID';
comment on column m_application_category.order is '昇順';
comment on column m_application_category.label_name is '選択肢名';

comment on table m_application_category_selection_view is 'M_申請区分選択画面';
comment on column m_application_category_selection_view.view_id is '画面ID';
comment on column m_application_category_selection_view.view_flag is '表示有無:1=表示, 0=非表示';
comment on column m_application_category_selection_view.multiple_flag is '複数選択有無:1=複数選択なし 0=複数選択あり';
comment on column m_application_category_selection_view.require_flag is '必須有無:1=選択必須 0=選択任意';
comment on column m_application_category_selection_view.title is 'タイトル';
comment on column m_application_category_selection_view.description is '説明文';


