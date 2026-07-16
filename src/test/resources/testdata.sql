delete from m101_company where company_cd in ('C001','C002');
INSERT INTO m101_company (company_cd, company_nm_kana, company_nm_kanji, company_abbreviation, postal_cd,
                                area_cd, address, phone_no, fax_no, version, deleted_flag, created_user_cd)
VALUES ('C001', 'テストカブシキガイシャ', 'テスト株式会社', 'テスト', '1000001', '001', '東京都千代田区1-1-1', '03-1111-1111', '03-1111-1112', 0,
        '0', 'mock_user')
    ON CONFLICT (company_cd) DO NOTHING;

INSERT INTO m101_company (company_cd, company_nm_kana, company_nm_kanji, company_abbreviation, postal_cd,
                                area_cd, address, phone_no, fax_no, version, deleted_flag, created_user_cd)
VALUES ('C002', 'サンプルカブシキガイシャ', 'サンプル株式会社', 'サンプル', '1000002', '002', '東京都港区2-2-2', '03-2222-2221', '03-2222-2222',
        0, '0', 'mock_user')
    ON CONFLICT (company_cd) DO NOTHING;