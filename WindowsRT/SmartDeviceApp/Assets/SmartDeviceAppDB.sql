CREATE TABLE Printer (
  prn_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  pst_id INTEGER UNSIGNED NULL,
  prn_ip_address VARCHAR(20) NULL,
  prn_name VARCHAR(255) NULL,
  prn_port_setting INTEGER UNSIGNED NOT NULL DEFAULT 0,
  prn_enabled_lpr BOOL NOT NULL DEFAULT 1,
  prn_enabled_raw BOOL NOT NULL DEFAULT 1,
  prn_enabled_booklet BOOL NOT NULL DEFAULT 1,
  prn_enabled_stapler BOOL NOT NULL DEFAULT 1,
  prn_enabled_punch3 BOOL NOT NULL DEFAULT 0,
  prn_enabled_punch4 BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_facedown BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_top BOOL NOT NULL DEFAULT 1,
  prn_enabled_tray_stack BOOL NOT NULL DEFAULT 1,
  FOREIGN KEY(pst_id) REFERENCES PrintSetting(pst_id) 
);

CREATE TABLE PrintSetting (
  pst_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  prn_id INTEGER NOT NULL,
  pst_color_mode INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_orientation INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_copies INTEGER UNSIGNED NOT NULL DEFAULT 1,
  pst_duplex INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_paper_size INTEGER UNSIGNED NOT NULL DEFAULT 2,
  pst_scale_to_fit BOOL NOT NULL DEFAULT 0,
  pst_paper_type INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_input_tray INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_imposition INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_imposition_order INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_sort INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_booklet BOOL NOT NULL DEFAULT 0,
  pst_booklet_finish INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_booklet_layout INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_finishing_side INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_staple INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_punch INTEGER UNSIGNED NOT NULL DEFAULT 0,
  pst_output_tray INTEGER UNSIGNED NOT NULL DEFAULT 0,
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