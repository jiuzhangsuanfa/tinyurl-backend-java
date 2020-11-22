package com.jiuzhang.url.db.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "LongToSequenceId", description = "")
@Entity
@Table(name = "LONG_TO_SEQUENCE_ID")
public class LongToSequenceId implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sequenceId;

    private String longUrl;
}
