package com.example.auth.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.example.auth.entity.UserEntity;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    //buat login, ngecek email dan passwordnya bener apa engga
    Optional<UserEntity> findByEmail(String email);

    //ngecek email udah ada apa belum
    boolean existsByEmail(String email);

    //nampilin daftar user yang aktif
    List<UserEntity> findByIsActiveTrue();

}
