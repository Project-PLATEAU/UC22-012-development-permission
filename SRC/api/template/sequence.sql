-- ###############################################
-- ### シーケンス生成用SQL 
-- ###############################################
-- 申請IDシーケンス
DROP SEQUENCE IF EXISTS seq_application;
CREATE SEQUENCE seq_application
    INCREMENT 1
    START 1;

-- 申請者情報IDシーケンス
DROP SEQUENCE IF EXISTS seq_applicant;
CREATE SEQUENCE seq_applicant
    INCREMENT 1
    START 1;

-- 照合IDシーケンス 
DROP SEQUENCE IF EXISTS seq_collation;
CREATE SEQUENCE seq_collation
    INCREMENT 1
    START 1;

-- 回答IDシーケンス 
DROP SEQUENCE IF EXISTS seq_answer;
CREATE SEQUENCE seq_answer
    INCREMENT 1
    START 1;

-- 回答ファイルIDシーケンス 
DROP SEQUENCE IF EXISTS seq_answer_file;
CREATE SEQUENCE seq_answer_file
    INCREMENT 1
    START 1;

-- ファイルIDシーケンス 
DROP SEQUENCE IF EXISTS seq_file;
CREATE SEQUENCE seq_file
    INCREMENT 1
    START 1;
-- 概況診断結果IDシーケンス
DROP SEQUENCE IF EXISTS seq_general_condition_diagnosis;
CREATE SEQUENCE seq_general_condition_diagnosis
    INCREMENT 1
    START 1;