<%--
    Renders a player rating on the 0-10 scale the design uses.

    The backend stores ratings as 0-100 ints (currentForm and the six ability
    scores), so 87 shows as 8.7 and 90 as 9.0.

    Deliberately renders via EL rather than fmt:formatNumber. fmt would follow
    the en_ZA locale and print "8,7"; EL division yields a Double, and an int
    over 10 always stringifies to exactly one decimal with a period. So ratings
    keep the "." while money keeps the "," — see the money tag.

    Usage:  <t:rating value="${player.currentForm}" />
--%>
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:choose>
    <c:when test="${empty value}">&mdash;</c:when>
    <c:otherwise>${value / 10}</c:otherwise>
</c:choose>
