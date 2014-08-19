﻿CREATE TABLE Printer (
  prn_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  pst_id INTEGER UNSIGNED NULL,
  prn_ip_address VARCHAR(20) NULL,
  prn_name VARCHAR(255) NULL,
  prn_port_setting INTEGER UNSIGNED NOT NULL DEFAULT 0,
  prn_enabled_lpr BOOL NOT NULL DEFAULT 1,
  prn_enabled_raw BOOL NOT NULL DEFAULT 1,
  prn_enabled_paper_lw BOOL NOT NULL DEFAULT 1,
  prn_enabled_feed_tray1 BOOL NOT NULL DEFAULT 1,
  prn_enabled_feed_tray2 BOOL NOT NULL DEFAULT 1,
  prn_enabled_feed_tray3 BOOL NOT NULL DEFAULT 1,
  prn_enabled_booklet_finishing BOOL NOT NULL DEFAULT 1,
  prn_enabled_stapler BOOL NOT NULL DEFAULT 1,
  prn_enabled_punch3 BOOL NOT NULL DEFAULT 0,
  prn_enabled_punch4 BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_facedown BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_top BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_stack BOOL NOT NULL DEFAULT 1,
  FOREIGN KEY(pst_id) REFERENCES PrintSetting(pst_id) 
);