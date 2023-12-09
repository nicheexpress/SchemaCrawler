SELECT
  NULL AS ROUTINE_CATALOG,
  PROCEDURES.OWNER AS ROUTINE_SCHEMA,
  PROCEDURES.OBJECT_NAME AS ROUTINE_NAME,
  PROCEDURES.OBJECT_NAME AS SPECIFIC_NAME,
  'SQL' AS ROUTINE_BODY,
  DBMS_METADATA.GET_DDL(OBJECT_TYPE, PROCEDURES.OBJECT_NAME, PROCEDURES.OWNER)
    AS ROUTINE_DEFINITION
FROM
  ${catalogscope}_PROCEDURES PROCEDURES
  INNER JOIN ${catalogscope}_USERS USERS
    ON PROCEDURES.OWNER = USERS.USERNAME
      AND USERS.ORACLE_MAINTAINED = 'N'
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^APEX_[0-9]{6}$')
      AND NOT REGEXP_LIKE(USERS.USERNAME, '^FLOWS_[0-9]{5}$')
WHERE
  REGEXP_LIKE(PROCEDURES.OWNER, '${schema-inclusion-rule}')
ORDER BY
  ROUTINE_SCHEMA,
  ROUTINE_NAME
