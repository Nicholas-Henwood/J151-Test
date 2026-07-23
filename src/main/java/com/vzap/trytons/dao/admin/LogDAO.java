package com.vzap.trytons.dao.admin;


import com.vzap.trytons.model.admin.Log;
import com.vzap.trytons.model.admin.LogActionCount;

import java.util.List;

public interface LogDAO {
    List<Log> findRecentLogs(int limit);
    List<LogActionCount> countByActionType();
}
