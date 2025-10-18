package com.ttp.evaluation.classification.repository;

import com.ttp.evaluation.classification.domain.User;
import com.ttp.evaluation.classification.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по email
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверить существование пользователя по email
     */
    boolean existsByEmail(String email);

    /**
     * Найти всех активных пользователей
     */
    List<User> findByActiveTrue();

    /**
     * Найти пользователей по роли
     */
    List<User> findByRole(UserRole role);

    /**
     * Найти активных пользователей по роли
     */
    List<User> findByRoleAndActiveTrue(UserRole role);

    /**
     * Найти пользователей по компании
     */
    @Query("SELECT u FROM User u WHERE u.company = :company AND u.active = true")
    List<User> findActiveUsersByCompany(@Param("company") String company);
}
