<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Clubs - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/sidebar.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/clubs.css">
</head>
<body class="catalog-page clubs-page">

<c:set var="activeNav" value="clubs" scope="request" />
<%@ include file="/WEB-INF/jspf/sidebar.jspf" %>

<main class="catalog-main" data-catalog data-catalog-noun="club">
    <%-- Photo layer sits under everything, behind a heavy dark-green wash. --%>
    <div class="catalog-backdrop" aria-hidden="true"></div>

    <div class="catalog-content">

        <header class="catalog-header">
            <div>
                <p class="catalog-eyebrow">Fantasy TryTons League</p>
                <h1 class="brand-font">Clubs</h1>
            </div>
            <%-- catalog-filter.js keeps this in step with the filtered list. --%>
            <p class="catalog-count" data-catalog-count>${fn:length(clubs)} clubs</p>
        </header>

        <c:if test="${not empty error}">
            <p class="catalog-error" role="alert">${fn:escapeXml(error)}</p>
        </c:if>

        <div class="catalog-toolbar">
            <label class="search-wrap" for="clubSearchInput">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>
                <input type="search" id="clubSearchInput" name="search"
                       placeholder="Search club name" autocomplete="off"
                       aria-label="Search club name"
                       data-catalog-search
                       value="${fn:escapeXml(searchTerm)}">
            </label>

            <%-- Distinct locations, taken off the clubs list the servlet already
                 supplies. A location is emitted only when no earlier club shares it,
                 so the options follow the list's own order (clubs come back ordered
                 by club name). Six clubs, so the inner scan costs nothing. --%>
            <select class="sel sel-filter" id="locationFilter" aria-label="Filter by location" data-catalog-filter="location">
                <option value="">All locations</option>
                <c:forEach var="club" items="${clubs}" varStatus="status">
                    <c:set var="isFirstOfLocation" value="${true}" />
                    <c:forEach var="earlier" items="${clubs}" varStatus="earlierStatus">
                        <c:if test="${earlierStatus.index < status.index and earlier.location == club.location}">
                            <c:set var="isFirstOfLocation" value="${false}" />
                        </c:if>
                    </c:forEach>
                    <c:if test="${isFirstOfLocation and not empty club.location}">
                        <option value="${fn:escapeXml(club.location)}">${fn:escapeXml(club.location)}</option>
                    </c:if>
                </c:forEach>
            </select>

            <div class="sort-wrap">
                <label class="sort-label" for="clubSort">Sort by</label>
                <select class="sel" id="clubSort" data-catalog-sort>
                    <option value="name" data-dir="asc">Name</option>
                    <option value="location" data-dir="asc">Location</option>
                    <option value="venue" data-dir="asc">Home venue</option>
                </select>
            </div>
        </div>

        <div class="ctable" id="clubsTable" data-catalog-table>
            <div class="crow chead">
                <span>Club</span>
                <span>Location</span>
                <span>Home venue</span>
                <span>Status</span>
            </div>
            <div class="cbody" id="clubsBody" data-catalog-body>
                <c:forEach var="club" items="${clubs}">
                    <c:set var="clubName" value="${empty club.clubName ? '' : club.clubName}" />
                    <c:set var="nameParts" value="${fn:split(clubName, ' ')}" />

                    <a class="crow crow-link"
                       href="${pageContext.request.contextPath}/club?clubId=${club.clubId}"
                       data-name="${fn:escapeXml(fn:toLowerCase(clubName))}"
                       data-location="${fn:escapeXml(club.location)}"
                       data-venue="${fn:escapeXml(club.homeVenue)}">

                        <span class="c-name">
                            <span class="c-avatar" aria-hidden="true"><c:if test="${fn:length(nameParts) > 0}">${fn:toUpperCase(fn:substring(nameParts[0], 0, 1))}<c:if test="${fn:length(nameParts) > 1}">${fn:toUpperCase(fn:substring(nameParts[fn:length(nameParts) - 1], 0, 1))}</c:if></c:if></span>
                            <span class="c-name-text" title="${fn:escapeXml(clubName)}">${fn:escapeXml(clubName)}</span>
                        </span>

                        <span class="c-text" title="${fn:escapeXml(club.location)}">${fn:escapeXml(club.location)}</span>

                        <span class="c-text" title="${fn:escapeXml(club.homeVenue)}">${fn:escapeXml(club.homeVenue)}</span>

                        <span>
                            <c:choose>
                                <c:when test="${club.active}">
                                    <span class="avail avail-ok"><span class="avail-label">Active</span></span>
                                </c:when>
                                <c:otherwise>
                                    <span class="avail avail-out"><span class="avail-label">Inactive</span></span>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </a>
                </c:forEach>
            </div>
        </div>

        <p class="catalog-empty" id="clubsEmptyState" data-catalog-empty hidden>No clubs match your search.</p>

    </div>
</main>

<script src="${pageContext.request.contextPath}/assets/js/catalog-filter.js"></script>
</body>
</html>
