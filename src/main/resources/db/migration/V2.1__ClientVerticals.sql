INSERT INTO client (created_at, is_vertical, internal_id, name, description) VALUES 
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL01', 'client_financial', "Generic financial client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL02', 'client_healthcare', "Generic healthcare client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL03', 'client_retail', "Generic retail client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL04', 'client_technology', "Generic technology client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL05', 'client_telco', "Generic telecommunications client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL06', 'client_travel', "Generic travel client entry."),
(UNIX_TIMESTAMP(now())*1000, 1, 'VERTICAL07', 'client_utilities', "Generic utilities client entry.");

