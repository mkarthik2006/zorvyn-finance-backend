package com.zorvyn.finance.repository;

import com.zorvyn.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    default Optional<User> findByEmailAndDeletedFalse(String email) {
        return findByEmail(email);
    }

    default Optional<User> findByIdAndDeletedFalse(Long id) {
        return findById(id);
    }

    default List<User> findAllByDeletedFalse() {
        return findAll();
    }

    default boolean existsByEmailAndDeletedFalse(String email) {
        return existsByEmail(email);
    }
}