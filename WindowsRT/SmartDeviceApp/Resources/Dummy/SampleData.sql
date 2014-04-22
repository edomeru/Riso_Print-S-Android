INSERT INTO Printer VALUES(1, 1, "192.168.0.1", "RISO_Printer1", 1111, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(2, 2, "192.168.0.22", "RISO_Printer2", 2222, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(3, 3, "192.168.0.3", "RISO_Printer3", 3333, 0, 1, 0, 1, 0, 1, 0, 1, 0);
INSERT INTO Printer VALUES(4, 4, "192.168.0.4", "RISO_Printer4", 4444, 1, 0, 1, 0, 1, 0, 1, 0 , 1);
INSERT INTO Printer VALUES(5, 5, "192.168.0.5", "RISO_Printer5", 5555, 0, 0, 0, 0, 0, 0, 0, 0, 0);
INSERT INTO Printer VALUES(6, 6, "192.168.0.6", "RISO_Printer6", 6666, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(7, 7, "192.168.0.7", "RISO_Printer7", 7777, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(8, 8, "192.168.0.8", "RISO_Printer8", 8888, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(9, 9, "192.168.0.9", "RISO_Printer9", 9999, 0, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(10, 10, "192.168.0.10", "RISO_Printer10_with_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_very_long_name", 1010, 1, 1, 1, 1, 1, 1, 1, 1, 1);
INSERT INTO Printer VALUES(11, 11, "192.168.0.11", "RISO Printer11 with very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very long name", 1111, 0, 0, 0, 0, 0, 0, 0, 0, 0);
INSERT INTO Printer VALUES(12, 12, "192.168.0.12", "RISO Printer12 with no matching print settings", 1212, 1, 1, 1, 1, 1, 1, 1, 1, 1);

INSERT INTO PrintSetting VALUES (1, 2, 0, 1, 2, 0, 1, 0, 1, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0);
INSERT INTO PrintSetting VALUES (2, 1, 1, 0, 1, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
INSERT INTO PrintSetting VALUES (3, 3, 1, 0, 9999, 0, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0);
INSERT INTO PrintSetting VALUES (4, 4, 1, 0, 1, 0, 2, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0);
INSERT INTO PrintSetting VALUES (5, 5, 1, 1, 1, 0, 2, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 2, 0, 0);
INSERT INTO PrintSetting VALUES (6, 6, 1, 0, 2, 0, 2, 1, 0, 0, 2, 3, 0, 0, 0, 0, 0, 0, 2, 0);
INSERT INTO PrintSetting VALUES (7, 7, 1, 1, 2, 0, 2, 1, 0, 0, 2, 5, 0, 0, 0, 0, 0, 0, 0, 0);
INSERT INTO PrintSetting VALUES (8, 8, 1, 0, 1, 0, 2, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0);
INSERT INTO PrintSetting VALUES (9, 9, 1, 0, 1, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 0, 1, 0, 0, 0);
INSERT INTO PrintSetting VALUES (10, 10, 1, 0, 3, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 1, 2, 0, 0, 0);
INSERT INTO PrintSetting VALUES (11, 11, 1, 0, 3, 0, 2, 1, 0, 0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 0);

INSERT INTO DefaultPrinter VALUES(2);

INSERT INTO PrintJob VALUES (1, 7, "Job6", "2013-11-02 07:16:19.000", 1);
INSERT INTO PrintJob VALUES (2, 7, "Job7", "2013-11-03 08:15:20.000", 1);
INSERT INTO PrintJob VALUES (3, 8, "Job1", "2013-11-04 08:14:21.000", 0);
INSERT INTO PrintJob VALUES (4, 6, "Job5", "2013-10-26 09:13:22.000", 1);
INSERT INTO PrintJob VALUES (5, 4, "Job3", "2013-10-15 10:12:23.000", 0);
INSERT INTO PrintJob VALUES (6, 7, "Job4", "2013-10-31 11:11:24.000", 1);
INSERT INTO PrintJob VALUES (7, 7, "Job5", "2013-11-01 12:10:25.000", 1);
INSERT INTO PrintJob VALUES (8, 9, "Job4", "2013-11-15 13:09:26.000", 1);
INSERT INTO PrintJob VALUES (9, 2, "Job2", "2013-10-09 14:08:27.000", 0);
INSERT INTO PrintJob VALUES (10, 3, "Job1", "2013-10-10 15:07:28.000", 0);
INSERT INTO PrintJob VALUES (11, 7, "Job2", "2013-10-29 16:06:29.000", 0);
INSERT INTO PrintJob VALUES (12, 7, "Job3", "2013-10-30 17:05:30.000", 0);
INSERT INTO PrintJob VALUES (13, 10, "Job2", "2013-11-22 18:04:31.000", 0);
INSERT INTO PrintJob VALUES (14, 3, "Job2", "2013-10-11 19:03:32.000", 0);
INSERT INTO PrintJob VALUES (15, 9, "Job8", "2013-11-19 20:02:33.000", 1);
INSERT INTO PrintJob VALUES (16, 9, "Job9", "2013-11-20 21:02:34.000", 1);
INSERT INTO PrintJob VALUES (17, 10, "Job1", "2013-11-21 22:01:35.000", 0);
INSERT INTO PrintJob VALUES (18, 8, "Job7", "2013-11-10 23:00:36.000", 1);
INSERT INTO PrintJob VALUES (19, 8, "Job8", "2013-11-11 00:59:37.000", 1);
INSERT INTO PrintJob VALUES (20, 9, "Job1", "2013-11-12 01:58:38.000", 0);
INSERT INTO PrintJob VALUES (21, 9, "Job2", "2013-11-13 02:57:39.000", 0);
INSERT INTO PrintJob VALUES (22, 9, "Job3", "2013-11-14 03:56:40.000", 0);
INSERT INTO PrintJob VALUES (23, 8, "Job4", "2013-11-07 04:55:41.000", 1);
INSERT INTO PrintJob VALUES (24, 6, "Job6", "2013-10-27 05:54:42.000", 1);
INSERT INTO PrintJob VALUES (25, 7, "Job1", "2013-10-28 06:53:43.000", 0);
INSERT INTO PrintJob VALUES (26, 9, "Job5", "2013-11-16 07:52:44.000", 1);
INSERT INTO PrintJob VALUES (27, 9, "Job6", "2013-11-17 08:51:45.000", 1);
INSERT INTO PrintJob VALUES (28, 9, "Job7", "2013-11-18 09:50:46.000", 1);
INSERT INTO PrintJob VALUES (29, 10, "Job9", "2013-11-29 10:49:47.000", 1);
INSERT INTO PrintJob VALUES (30, 10, "Job10", "2013-11-30 11:48:48.000", 1);
INSERT INTO PrintJob VALUES (31, 8, "Job2", "2013-11-05 12:47:49.000", 0);
INSERT INTO PrintJob VALUES (32, 8, "Job3", "2013-11-06 13:46:50.000", 0);
INSERT INTO PrintJob VALUES (33, 3, "Job3", "2013-10-12 14:45:51.000", 0);
INSERT INTO PrintJob VALUES (34, 4, "Job1", "2013-10-13 15:44:52.000", 0);
INSERT INTO PrintJob VALUES (35, 4, "Job2", "2013-10-14 16:43:53.000", 0);
INSERT INTO PrintJob VALUES (36, 8, "Job5", "2013-11-08 17:42:54.000", 1);
INSERT INTO PrintJob VALUES (37, 8, "Job6", "2013-11-09 18:41:55.000", 1);
INSERT INTO PrintJob VALUES (38, 10, "Job3", "2013-11-23 19:40:56.000", 0);
INSERT INTO PrintJob VALUES (39, 10, "Job4", "2013-11-24 20:39:57.000", 1);
INSERT INTO PrintJob VALUES (40, 10, "Job5", "2013-11-25 21:38:58.000", 1);
INSERT INTO PrintJob VALUES (41, 10, "Job6", "2013-11-26 22:37:59.000", 1);
INSERT INTO PrintJob VALUES (42, 6, "Job2", "2013-10-23 23:36:00.000", 0);
INSERT INTO PrintJob VALUES (43, 6, "Job3", "2013-10-24 00:35:01.000", 0);
INSERT INTO PrintJob VALUES (44, 6, "Job4", "2013-10-25 01:34:02.000", 1);
INSERT INTO PrintJob VALUES (45, 10, "Job7", "2013-11-27 02:33:03.000", 1);
INSERT INTO PrintJob VALUES (46, 10, "Job8", "2013-11-28 03:32:04.000", 1);
INSERT INTO PrintJob VALUES (47, 1, "Job1", "2013-10-07 04:31:05.000", 0);
INSERT INTO PrintJob VALUES (48, 4, "Job4", "2013-10-16 05:30:06.000", 1);
INSERT INTO PrintJob VALUES (49, 5, "Job1", "2013-10-17 06:29:07.000", 0);
INSERT INTO PrintJob VALUES (50, 5, "Job2", "2013-10-18 07:28:08.000", 0);
INSERT INTO PrintJob VALUES (51, 5, "Job3", "2013-10-19 08:27:09.000", 0);
INSERT INTO PrintJob VALUES (52, 5, "Job4", "2013-10-20 09:26:10.000", 1);
INSERT INTO PrintJob VALUES (53, 5, "Job5", "2013-10-21 10:25:11.000", 1);
INSERT INTO PrintJob VALUES (54, 6, "Job1", "2013-10-22 11:24:12.000", 0);
INSERT INTO PrintJob VALUES (55, 2, "Job1", "2013-10-08 12:23:13.000", 0);
