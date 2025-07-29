package gigabank.accountmanagement.persistence;

import java.util.Arrays;
import java.util.List;

public record SqlQuery(String sql, List<Object> parameters) {
    public SqlQuery(String sql) {
        this(sql, List.of());
    }

    public SqlQuery(String sql, Object... params) {
        this(sql, Arrays.asList(params));
    }
}