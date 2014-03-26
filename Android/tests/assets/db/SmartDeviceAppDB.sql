﻿CREATE TABLE Printer (
  prn_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  prn_ip_address VARCHAR(20) NULL,
  prn_name VARCHAR(255) NULL,
  prn_port_setting INTEGER UNSIGNED NOT NULL DEFAULT 0,
  prn_enabled_lpr BOOL NOT NULL DEFAULT true,
  prn_enabled_raw BOOL NOT NULL DEFAULT true,
  prn_enabled_pagination BOOL NOT NULL DEFAULT true,
  prn_enabled_duplex BOOL NOT NULL DEFAULT true,
  prn_enabled_booklet_binding BOOL NOT NULL DEFAULT true,
  prn_enabled_staple BOOL NOT NULL DEFAULT true,
  prn_enabled_bind BOOL NOT NULL DEFAULT true
);

CREATE TABLE PrintSetting (
  prn_id INTEGER NOT NULL PRIMARY KEY,
  pst_copies INTEGER UNSIGNED NOT NULL DEFAULT 1,
  pst_color_mode INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_zoom BOOL NOT NULL DEFAULT false,
  pst_zoom_rate INTEGER UNSIGNED NOT NULL DEFAULT 100,
  pst_paper_type INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_paper_size INTEGER UNSIGNED NOT NULL DEFAULT 1,
  pst_duplex BOOL NOT NULL DEFAULT false,
  pst_pagination INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_image_quality INTEGER UNSIGNED NOT NULL DEFAULT 1,
  pst_sort INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_booklet_binding INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_booklet_tray INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_bind INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_staple INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_punch INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_catch_tray INTEGER UNSIGNED NOT NULL DEFAULT 0,
  FOREIGN KEY(prn_id) REFERENCES Printer(prn_id) ON DELETE CASCADE
);

CREATE TABLE DefaultPrinter (
  prn_id INTEGER UNSIGNED NOT NULL PRIMARY KEY,
  FOREIGN KEY(prn_id) REFERENCES Printer(prn_id) ON DELETE CASCADE
);

CREATE TABLE PrintJob (
  pjb_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  prn_id INTEGER UNSIGNED NOT NULL,
  pjb_name VARCHAR NULL,
  pjb_date DATETIME NULL,
  pjb_result INTEGER UNSIGNED NULL,
  FOREIGN KEY(prn_id) REFERENCES Printer(prn_id) ON DELETE CASCADE
);