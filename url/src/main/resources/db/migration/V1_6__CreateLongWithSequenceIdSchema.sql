-- ----------------------------
-- Table structure for LONG_SEQUENCE_ID
-- ----------------------------
DROP TABLE IF EXISTS LONG_TO_SEQUENCE_ID;

CREATE TABLE LONG_TO_SEQUENCE_ID(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sequenceId BIGINT NOT NULL,
  longUrl VARCHAR(256) NOT NULL,
  UNIQUE sequenceId_index (sequenceId),
  UNIQUE long_url_index (longUrl)
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;