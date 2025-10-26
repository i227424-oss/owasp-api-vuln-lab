package edu.nu.owaspapivulnlab.web.dto;

import edu.nu.owaspapivulnlab.model.Account;

/**
 * FIX-4: DTO for Account to prevent excessive data exposure
 */
public record AccountDto(Long id, String iban, Double balance) {
    public static AccountDto from(Account account) {
        return new AccountDto(account.getId(), account.getIban(), account.getBalance());
    }
}
