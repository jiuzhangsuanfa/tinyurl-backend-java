-- ----------------------------
-- Add index to Table LONG_TO_SHORT
-- ----------------------------
ALTER TABLE LONG_TO_SHORT ADD UNIQUE long_url_index (`longUrl`);
ALTER TABLE LONG_TO_SHORT ADD UNIQUE short_url_index (`shortUrl`);

