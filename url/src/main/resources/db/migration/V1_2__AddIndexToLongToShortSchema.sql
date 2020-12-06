-- ----------------------------
-- Add index to Table LONG_TO_SHORT
-- ----------------------------
ALTER TABLE LONG_TO_SHORT ADD UNIQUE INDEX long_url_index (`longUrl`);
