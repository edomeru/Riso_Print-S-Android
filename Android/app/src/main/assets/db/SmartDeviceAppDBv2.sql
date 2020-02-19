ALTER TABLE Printer ADD COLUMN prn_enabled_external_feeder BOOL NOT NULL DEFAULT 0;
ALTER TABLE Printer ADD COLUMN prn_enabled_punch0 BOOL NOT NULL DEFAULT 0;