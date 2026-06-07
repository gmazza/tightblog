package org.tightblog.dto;

import org.tightblog.domain.WeblogRole;

public record UserHasWeblogDto(
    String userId,
    String weblogId,
    String weblogName,
    String weblogHandle,
    String weblogAbsoluteUrl,
    String weblogAbout,
    int weblogHitsToday,
    int weblogUnapprovedComments,
    WeblogRole role,
    boolean emailComments) {
}
