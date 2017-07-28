
create or replace type DBAX_HTBUF_ARR as table of varchar2(256) ;
/

CREATE GLOBAL TEMPORARY TABLE temp_cgi_env (  
  name  varchar2(100),
  value varchar2(256)  
)
ON COMMIT DELETE ROWS;
/

create or replace package util_ as
    empty_vc_arr owa_util.vc_arr;
end util_;
/

CREATE OR REPLACE PROCEDURE hello (name_array    IN owa_util.vc_arr DEFAULT util_.empty_vc_arr
                                  , value_array   IN owa_util.vc_arr DEFAULT util_.empty_vc_arr )
AS
l_instance_name varchar2(100);
BEGIN
   htp.init;
--   owa_util.mime_header ('text/html', FALSE);
 --  owa_util.status_line (nstatus => 201, creason => NULL, bclose_header => FALSE);
  -- owa_cookie.send (name => 'EASY_COOKIE', value => 'ejemplo', expires => sysdate + 365);
   --owa_cookie.send (name => 'EASY_COOKIE2', value => 'ejemplo2', expires => sysdate + 1);

   --htp.p ('Server: PHP/5.2.6-2ubuntu4.2');
   --htp.p('X-Forwarded-For: 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789');
   owa_util.http_header_close;

    SELECT sys_context('USERENV','DB_NAME') into l_instance_name
    FROM dual;

   owa_cache.set_cache('ETAGLaLALA', 'USER');

   htp.p ('Hola desde HTP.P de Oracle<br>: ' || l_instance_name);
   htp.p ('Este dato viene del primer parametro: '|| value_array(1) || '<br>');
   htp.p('<hr>');
   
   

--   FOR i IN 1 .. owa.num_cgi_vars
--   LOOP
--      INSERT INTO temp_cgi_env
--        VALUES   (owa.cgi_var_name (i), owa.cgi_var_val (i));
--   END LOOP;
--
--   FOR c1 IN (  SELECT   *
--                  FROM   temp_cgi_env
--              ORDER BY   nlssort(name,'NLS_SORT=BINARY')
--               )
--   LOOP
--      htp.p (c1.name || '=' || c1.value || '<br>');
--   END LOOP;

END;
/






