package com.comment.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
}
