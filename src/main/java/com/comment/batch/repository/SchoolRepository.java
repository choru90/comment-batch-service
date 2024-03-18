package com.comment.batch.repository;

import com.comment.batch.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolRepository extends JpaRepository<School, Long> {

    List<School> findAllByNameIn(List<String> names);
}
