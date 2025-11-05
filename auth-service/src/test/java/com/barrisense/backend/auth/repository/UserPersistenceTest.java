package com.barrisense.backend.auth.repository;

import com.barrisense.backend.auth.domain.Role;
import com.barrisense.backend.auth.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class UserPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em; // handy for raw DB verification

    @Test
    void whenSavingUser_thenUserRolesEntryIsCreated() {
        // given
    UserEntity user = UserEntity.builder()
        .username("alice")
        .password("secret")
        .build();

    // when
    userRepository.save(user);
    em.flush(); // force Hibernate to write to DB

    // then
    // verify the main entity exists
    var found = userRepository.findByUsername("alice").orElseThrow();
    assertThat(found.getRoles()).containsExactly(Role.ROLE_USER);

        // verify the user_roles table got a record
        var count = em.getEntityManager()
                .createNativeQuery("SELECT COUNT(*) FROM user_roles WHERE user_id = :id")
                .setParameter("id", found.getId())
                .getSingleResult();

        assertThat(((Number) count).intValue()).isEqualTo(1);
    }
}
