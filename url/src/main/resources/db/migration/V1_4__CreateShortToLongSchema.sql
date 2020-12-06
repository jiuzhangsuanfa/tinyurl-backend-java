-- ----------------------------
-- Table structure for SHORT_TO_LONG
-- ----------------------------
DROP TABLE IF EXISTS SHORT_TO_LONG;

CREATE TABLE SHORT_TO_LONG(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  shortUrl VARCHAR(20) NOT NULL,
  longUrl VARCHAR(256) NOT NULL,
  UNIQUE INDEX short_url_index (shortUrl)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;