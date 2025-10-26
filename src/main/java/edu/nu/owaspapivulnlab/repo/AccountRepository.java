package edu.nu.owaspapivulnlab.repo;

import edu.nu.owaspapivulnlab.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * FIX-3: Repository methods restricted by owner (userId)
 * Prevents accessing or deleting other users’ data.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Get all accounts owned by the current user
    List<Account> findAllByOwnerUserId(Long ownerUserId);

    // Get a specific account only if it belongs to that user
    Optional<Account> findByIdAndOwnerUserId(Long id, Long ownerUserId);

    // Delete only if the account belongs to that user
    void deleteByIdAndOwnerUserId(Long id, Long ownerUserId);
}
