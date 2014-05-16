INSERT INTO Printer (prn_id, prn_name, prn_ip_address) VALUES (1, "Full Capabilities", "192.168.2.1"); 
INSERT INTO Printer (prn_id, prn_name, prn_ip_address, prn_enabled_booklet, prn_enabled_stapler, prn_enabled_tray_facedown, prn_enabled_tray_top, prn_enabled_tray_stack, prn_enabled_punch3, prn_enabled_punch4) VALUES (2,"All Disabled", "192.168.2.2", 0, 0, 0, 0, 0, 0, 0);
INSERT INTO Printer (prn_id, prn_name, prn_ip_address, prn_enabled_punch3, prn_enabled_punch4) VALUES (3, "Japanese Printer", "192.168.2.3", 1, 0); 
INSERT INTO Printer (prn_id, prn_ip_address) VALUES (4, "192.168.2.4"); 
INSERT INTO Printer (prn_id, prn_name, prn_ip_address) VALUES (5, "Printer with a very long name specified for testing the ellipsis", "192.168.2.5"); 
INSERT INTO Printer (prn_id, prn_name, prn_ip_address) VALUES (6, "RISO Printer IPv6", "udp6:[2001::4:225:5cff:fe34:7c27%en0]"); 

INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (1,1,"myfile1.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (2,1,"file1.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (3,1,"test file.pdf","2014-03-19 16:55:38",1);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (4,1,"filename.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (5,1,"this is a long filename.pdf","2014-03-19 16:55:38",1);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (6,2,"ALLCAPS.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (7,2,"qwerty.pdf","2014-03-19 16:55:38",1);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (8,2,"!@#$%^^&*.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (9,3,"123456789 0987654321.pdf","2014-03-19 16:55:38",1);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (10,3,"this is a long file name without a new line.pdf","2014-03-19 16:55:38",0);
INSERT INTO PrintJob (pjb_id, prn_id, pjb_name, pjb_date, pjb_result) VALUES (11,4,"this is a long file name with a newline.pdf","2014-03-19 16:55:38",1);
