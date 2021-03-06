package com.moon.sql;

import com.moon.beans.BeanInfoUtil;
import com.moon.beans.FieldDescriptor;
import com.moon.lang.ThrowUtil;
import com.moon.lang.ref.ReferenceUtil;
import com.moon.lang.reflect.ConstructorUtil;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.moon.lang.ThrowUtil.noInstanceError;

/**
 * @author benshaoye
 */
public final class ResultSetUtil {
    private static Map<Object, Object> CACHE = ReferenceUtil.manageMap();

    private ResultSetUtil() {
        noInstanceError();
    }

    public static Map<String, Object> oneToMap(ResultSet set, String... columnsLabel) {
        try {
            String column;
            Map<String, Object> ret = new HashMap<>();
            for (int i = 0; i < columnsLabel.length; i++) {
                column = columnsLabel[i];
                ret.put(column, set.getObject(column));
            }
            return ret;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    /**
     * 将当前行映射成一个 Map 对象
     *
     * @param set
     * @return
     */
    public static Map<String, Object> oneToMap(ResultSet set) {
        return oneToMap(set, getColumnsLabel(set));
    }

    /**
     * 将剩余行每行映射成一个 Map 对象，所有数据用一个 List 返回
     *
     * @param set
     * @return
     */
    public static List<Map<String, Object>> restToMap(ResultSet set) {
        try {
            String[] columns = getColumnsLabel(set);
            List<Map<String, Object>> ret = new ArrayList<>();
            while (set.next()) {
                ret.add(oneToMap(set, columns));
            }
            return ret;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    /**
     * 将当前行用一个数组包装返回
     *
     * @param set
     * @return
     */
    public static Object[] oneToArray(ResultSet set) {
        try {
            int count = getColumnsCount(set);
            Object[] ret = new Object[count];
            for (int i = 0; i < count; i++) {
                ret[i] = set.getObject(i + 1);
            }
            return ret;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    /**
     * 将所有剩余行都用数组包装，放进 List 返回
     *
     * @param set
     * @return
     */
    public static List<Object[]> restToArray(ResultSet set) {
        try {
            int count = getColumnsCount(set);
            List<Object[]> ret = new ArrayList<>();
            while (set.next()) {
                Object[] row = new Object[count];
                for (int i = 0; i < count; i++) {
                    row[i] = set.getObject(i + 1);
                }
                ret.add(row);
            }
            return ret;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    /**
     * 将当前行按列名和字段名对应放进一个实体里面
     *
     * @param set
     * @param bean
     * @param <T>
     * @return
     */
    public static <T> T oneToBean(ResultSet set, T bean) {
        BeanInfoUtil.getFieldDescriptorsMap(bean.getClass()).forEach((name, descriptor) ->
            descriptor.ifSetterPresent(getConsumer(set, bean)));
        return bean;
    }

    /**
     * 将当前行按列名和字段名对应放进一个类的实例里面
     *
     * @param set
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T oneToInstance(ResultSet set, Class<T> type) {
        T bean = ConstructorUtil.newInstance(type);
        Consumer<FieldDescriptor> consumer = getConsumer(set, bean);
        BeanInfoUtil.getFieldDescriptorsMap(type).forEach((name, descriptor) ->
            descriptor.ifSetterPresent(consumer));
        return bean;
    }

    /**
     * 将所有行按列名和字段名对应放进一个类的实例
     *
     * @param set
     * @param type
     * @param <T>
     * @return
     */
    public static <T> List<T> restToInstance(ResultSet set, Class<T> type) {
        try {
            Map<String, FieldDescriptor> descMap = BeanInfoUtil.getFieldDescriptorsMap(type);
            List<T> ret = new ArrayList<>();
            while (set.next()) {
                T bean = ConstructorUtil.newInstance(type);
                Consumer<FieldDescriptor> consumer = getConsumer(set, bean);
                descMap.forEach((name, desc) -> desc.ifSetterPresent(consumer));
                ret.add(bean);
            }
            return ret;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }


    /**
     * 获取当前 ResultSet 的列名数组
     *
     * @param set
     * @return
     */
    public static String[] getColumnsLabel(ResultSet set) {
        try {
            String[] arr = (String[]) CACHE.get(set);
            if (arr == null) {
                ResultSetMetaData resultSetMetaData = set.getMetaData();
                int length = resultSetMetaData.getColumnCount();
                arr = new String[length];
                for (int i = 0; i < length; i++) {
                    arr[i] = resultSetMetaData.getColumnLabel(i + 1);
                }
            }
            return arr;
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    /**
     * 获取当前 ResultSet 的列数目
     *
     * @param set
     * @return
     */
    public static int getColumnsCount(ResultSet set) {
        try {
            return set.getMetaData().getColumnCount();
        } catch (Exception e) {
            return ThrowUtil.wrapAndThrow(e);
        }
    }

    private static <T> Consumer<FieldDescriptor> getConsumer(ResultSet set, T bean) {
        return desc -> {
            try {
                desc.setValue(bean, set.getObject(desc.getName()));
            } catch (SQLException e) {
                ThrowUtil.wrapAndThrow(e);
            }
        };
    }
}
