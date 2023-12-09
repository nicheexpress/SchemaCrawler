SELECT
  NULL AS TABLE_CATALOG,
  TABLES.OWNER AS TABLE_SCHEMA,
  TABLES.TABLE_NAME,
  DBMS_METADATA.GET_DDL('TABLE', TABLES.TABLE_NAME, TABLES.OWNER)
    AS TABLE_DEFINITION
FROM
  ${catalogscope}_TABLES TABLES
  LEFT OUTER JOIN ${catalogscope}_MVIEWS MVIEWS
    ON TABLES.OWNER = MVIEWS.OWNER
      AND TABLES.TABLE_NAME = MVIEWS.MVIEW_NAME
  INNER JOIN ${catalogscope}_USERS USERS
    ON TABLES.OWNER = USERS.USERNAME
      AND USERS.ORACLE_MAINTAINED = 'N'
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')  
WHERE
  REGEXP_LIKE(TABLES.OWNER, '${schema-inclusion-rule}')
  AND TABLES.TABLE_NAME NOT LIKE 'BIN$%'
  AND NOT REGEXP_LIKE(TABLES.TABLE_NAME, '^(SYS_IOT|MDOS|MDRS|MDRT|MDOT|MDXT)_.*$')
  AND TABLES.NESTED = 'NO'
  AND (TABLES.IOT_TYPE IS NULL OR TABLES.IOT_TYPE = 'IOT')
  AND MVIEWS.MVIEW_NAME IS NULL
ORDER BY
  TABLE_SCHEMA,
  TABLE_NAME
