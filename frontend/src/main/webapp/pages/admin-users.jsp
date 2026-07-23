<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User Management - Fantasy TryTons</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-users.css">
</head>
<body class="admin-users">
<%@ include file="/WEB-INF/jspf/navigation.jspf" %>

<main id="adminUsers">

    <h1>User Management</h1>

    <c:if test="${not empty success}">
        <p class="success-message" role="status">
            <c:out value="${success}"/>
        </p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="error-message" role="alert">
            <c:out value="${error}"/>
        </p>
    </c:if>

    <section id="userSearch">
        <h2>Search Users</h2>
        <form action="${pageContext.request.contextPath}/admin/users" method="get" id="userSearchForm">
            <div class="field">
                <label for="searchTerm">Search (email, username or role)</label>
                <input type="text" id="searchTerm" name="searchTerm" value="${searchTerm}">
            </div>
            <button type="submit">Search</button>
        </form>
    </section>

    <section id="userList">
        <h2>Users</h2>
        <c:choose>
            <c:when test="${empty users}">
                <p id="usersEmptyState">No users found.</p>
            </c:when>
            <c:otherwise>
                <table id="usersTable">
                    <thead>
                        <tr>
                            <th>Email</th>
                            <th>Username</th>
                            <th>Role</th>
                            <th>Status</th>
                            <th></th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="user" items="${users}">
                            <tr>
                                <td><c:out value="${user.email}"/></td>
                                <td><c:out value="${user.username}"/></td>
                                <td><c:out value="${user.role}"/></td>
                                <td>${user.active ? 'Active' : 'Inactive'}</td>
                                <td>
                                    <form action="${pageContext.request.contextPath}/admin/users" method="post">
                                        <input type="hidden" name="searchTerm" value="${searchTerm}">
                                        <input type="hidden" name="userId" value="${user.userId}">
                                        <c:choose>
                                            <c:when test="${user.active}">
                                                <input type="hidden" name="isActive" value="false">
                                                <button type="submit">Deactivate</button>
                                            </c:when>
                                            <c:otherwise>
                                                <input type="hidden" name="isActive" value="true">
                                                <button type="submit">Activate</button>
                                            </c:otherwise>
                                        </c:choose>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </section>

</main>
</body>
</html>
