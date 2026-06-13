package backEnd.dao;

import backEnd.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberDao {
    Member save(Member member);
    Member update(Member member);
    Optional<Member> findById(Long id);
    Optional<Member> findByUsername(String username);
    List<Member> findAll();
    void delete(Long id);
    boolean existsByUsername(String username);
}
