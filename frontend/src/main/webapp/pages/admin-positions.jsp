<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Position Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-positions.css">
</head>
<body class="admin-positions">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminPositions">

    <h1>Position Management</h1>

    <c:if test="${not empty error}">
        <p class="error-message" role="alert"><c:out value="${error}" /></p>
    </c:if>
    <c:if test="${not empty message}">
        <p class="success-message" role="status"><c:out value="${message}" /></p>
    </c:if>

    <section id="positionListSection">
        <h2>Existing Positions</h2>
        <c:choose>
            <c:when test="${empty positions}">
                <p id="positionsEmptyState">No positions have been created yet.</p>
            </c:when>
            <c:otherwise>
                <table id="positionsTable">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Category</th>
                        <th>Min Required</th>
                        <th>Max Allowed</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="pos" items="${positions}">
                        <tr>
                            <td><c:out value="${pos.positionName}" /></td>
                            <td><c:out value="${pos.positionCategory}" /></td>
                            <td><c:out value="${pos.minRequired}" /></td>
                            <td><c:out value="${pos.maxAllowed}" /></td>
                            <td>
                                <a href="${pageContext.request.contextPath}/admin/positions?submit=position&amp;positionId=${pos.positionId}">Edit</a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

    <section id="positionFormSection">
        <c:choose>
            <c:when test="${not empty position}">
                <h2>Edit Position</h2>
            </c:when>
            <c:otherwise>
                <h2>Create Position</h2>
            </c:otherwise>
        </c:choose>

        <form method="post" action="${pageContext.request.contextPath}/admin/positions" id="positionForm">
            <c:choose>
                <c:when test="${not empty position}">
                    <input type="hidden" name="submit" value="position/update" />
                    <input type="hidden" name="positionId" value="${position.positionId}" />
                </c:when>
                <c:otherwise>
                    <input type="hidden" name="submit" value="position/create" />
                </c:otherwise>
            </c:choose>

            <div>
                <label for="positionName">Name</label>
                <input type="text" id="positionName" name="positionName"
                        value="${not empty position ? fn:escapeXml(position.positionName) : ''}" required>
            </div>

            <div>
                <label for="positionCategory">Category</label>
                <input type="text" id="positionCategory" name="positionCategory"
                        value="${not empty position ? fn:escapeXml(position.positionCategory) : ''}" required>
            </div>

            <div>
                <label for="minRequired">Min Required</label>
                <input type="number" id="minRequired" name="minRequired" min="0"
                        value="${not empty position ? position.minRequired : ''}" required>
            </div>

            <div>
                <label for="maxAllowed">Max Allowed</label>
                <input type="number" id="maxAllowed" name="maxAllowed" min="0"
                        value="${not empty position ? position.maxAllowed : ''}" required>
            </div>

            <button type="submit">
                <c:choose>
                    <c:when test="${not empty position}">Update Position</c:when>
                    <c:otherwise>Create Position</c:otherwise>
                </c:choose>
            </button>

            <c:if test="${not empty position}">
                <a href="${pageContext.request.contextPath}/admin/positions?submit=positions">Cancel edit</a>
            </c:if>
        </form>
    </section>

</main>
</body>
</html>
