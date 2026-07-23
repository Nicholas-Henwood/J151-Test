<%--
    Renders a rand amount consistently across the app.

    Default is the "millions" form: amounts are stored as e.g. 12.5 meaning
    R12,5m. Player values, team totals and budgets are all on this scale, so
    they all use the default.

    plain="true" renders a whole-rand amount as R50 000 000 instead. Nothing
    uses it today — it is here for any future amount genuinely denominated in
    rands rather than millions.

    The locale is pinned to en_ZA so the decimal separator does not follow each
    visitor's browser: money reads with a comma everywhere. Ratings use the
    rating tag, which deliberately keeps a period.

    Usage:  <t:money value="${player.value}" />
            <t:money value="${someWholeRandAmount}" plain="true" />
--%>
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="value" required="true" type="java.lang.Object" %>
<%@ attribute name="plain" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_ZA" />
<c:choose>
    <c:when test="${empty value}">&mdash;</c:when>
    <c:when test="${plain}">R<fmt:formatNumber value="${value}" maxFractionDigits="0" groupingUsed="true" /></c:when>
    <c:otherwise>R<fmt:formatNumber value="${value}" minFractionDigits="1" maxFractionDigits="1" />m</c:otherwise>
</c:choose>
