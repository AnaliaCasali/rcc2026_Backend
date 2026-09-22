package edu.ies63.rcc2026_backend.enums;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
  USER, ADMIN, ORGANIZADOR, GRUPO;

  @Override
  public @Nullable String getAuthority() {
    return "";
  }
}
