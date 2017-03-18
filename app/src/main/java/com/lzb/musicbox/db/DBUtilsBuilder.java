package com.lzb.musicbox.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lzb.musicbox.app.App;
import com.lzb.musicbox.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/19.
 */
public class DBUtilsBuilder{

    private static Map<Class,DBUtils> map= new HashMap<Class,DBUtils>();

    /**
     * 单例获取DBUtils对象
     * @param type          实体类的class
     * @param <T>
     * @return
     */
    public static <T> DBUtils<T> getInstance(Class<T> type){
        DBUtils<T> dbUtils = map.get(type);
        if (dbUtils == null){
            synchronized (DBUtilsBuilder.class){
                if ((dbUtils = map.get(type)) == null){     // 重新获取并判断，如果上一次为该对象设置了值，则值不为空
                    dbUtils = new DBUtils<T>(type);
                    map.put(type, dbUtils);
                }
            }
        }
        return dbUtils;
    }
    public static class DBUtils<T>{

        private SQLiteDatabase db = null;
        private Class<T> cls = null;

        private String table = null;
        private String[] columns = null;
        private String selection = null;
        private String[] selectionArgs = null;
        private String groupBy = null;
        private String having = null;
        private String orderBy = null;
        private String limit = null;

        private DBUtils(Class<T> cls){
            this.cls = cls;
            DBHelper dbHelper = new DBHelper(App.getContext());
            db = dbHelper.getWritableDatabase();
        }

        /**
         * 设置表名
         * @param table
         * @return
         */
        public DBUtils<T> table(String table){
            this.table = table;
            return this;
        }

        /**
         * 设置列
         * @param columns
         * @return
         */
        public DBUtils<T> column(String[] columns){
            this.columns = columns;
            return this;
        }

        /**
         * 设置条件
         * @param selection
         * @param selectionArgs
         * @return
         */
        public DBUtils<T> where(String selection,String[] selectionArgs){
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            return this;
        }

        /**
         * 设置分组               -- 暂时不可用
         * @param groupBy
         * @return
         */
        private DBUtils<T> group(String groupBy){
            this.groupBy = groupBy;
            return this;
        }

        /**
         * 设置分组后的聚合条件   -- 暂时不可用
         * @param having
         * @return
         */
        private DBUtils<T> having(String having){
            this.having = having;
            return this;
        }

        /**
         * 设置排序 column desc
         * @param orderBy
         * @return
         */
        public DBUtils<T> order(String orderBy){
            this.orderBy = orderBy;
            return this;
        }

        /**
         * 设置限制行数
         * @param limit
         * @return
         */
        public DBUtils<T> limit(String limit){
            this.limit = limit;
            return this;
        }

        /**
         * 查询，返回列表
         * @return
         */
        public List<T> select(){
            Cursor cursor = db.query(table,columns,selection,selectionArgs,groupBy,having,orderBy,limit);
            List<T> list = null;
            try {
                list = parseCursorToList(cursor);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            setToNull();
            return list;
        }

        /**
         * 找到一条数据
         * @return
         */
        public T find(){
            Cursor cursor = db.query(table,columns,selection,selectionArgs,groupBy,having,orderBy,limit);
            List<T> list = null;
            try {
                list = parseCursorToList(cursor);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            setToNull();
            if (list == null || list.size() == 0){
                return null;
            }
            return list.get(0);
        }

        public boolean isExist(){
            return find() == null ? false : true;
        }

        /**
         * 插入一条数据
         * @param obj       插入的实体
         * @return
         */
        public int insert(T obj){
            int result = (int)db.insert(table,null,getNotIdContentValues(obj));
            setToNull();
            return result;
        }

        /**
         * 删除
         * @return
         */
        public int delete(){
            int result = db.delete(table,selection,selectionArgs);
            setToNull();
            return result;
        }

        /**
         * 更新
         * @param obj
         * @return
         */
        public int update(T obj){
            int result = db.update(table,getContentValues(obj,columns,null),selection,selectionArgs);
            setToNull();
            return result;
        }

        /**
         * 清空所有值
         */
        private void setToNull(){
            table = null;
            columns = null;
            selection = null;
            selectionArgs = null;
            groupBy = null;
            having = null;
            orderBy = null;
            limit = null;
        }

        /**
         * 获取ContentValues
         */
        private ContentValues getAllContentValues(Object obj){
            return getContentValues(obj,null,null);
        }

        private ContentValues getNotIdContentValues(Object obj){
            return getContentValues(obj,null,new String[]{"id"});
        }

        /**
         * 根据对象属性获取需要的ContentValues
         * @param obj               实体类对象
         * @param needColumns       需要的列名数组
         * @param notNeedColumns    当需要的列名为空时（即所有列），不需要的列名数组生效
         * @return
         */
        private ContentValues getContentValues(Object obj,String[] needColumns,String[] notNeedColumns){
            ContentValues values = new ContentValues();
            Class<?> cls = obj.getClass();
            Field[] fields = cls.getDeclaredFields();
            try {
                if (needColumns == null){
                    // 所有列
                    if (notNeedColumns == null){
                        // 不排除列
                        for (Field field : fields){
                            Object value = getter(obj,field.getName());
                            put(values, "`" + field.getName() + "`", value);
                        }
                    }else{
                        // 排除指定列
                        for (Field field : fields){
                            if (!isColumnExist(notNeedColumns,field.getName())){
                                Object value = getter(obj,field.getName());
                                put(values, "`" + field.getName() + "`", value);
                            }
                        }
                    }
                }else{
                    // 指定列，排除列无效
                    for (Field field : fields){
                        if (isColumnExist(needColumns,field.getName())){
                            Object value = getter(obj,field.getName());
                            put(values, "`" + field.getName() + "`", value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return values;
        }

        /**
         * 解析Cursor对象到列表中
         * @param cursor        游标对象
         * @return
         * @throws IllegalAccessException
         * @throws InstantiationException
         * @throws NoSuchMethodException
         * @throws InvocationTargetException
         */
        private List<T> parseCursorToList(Cursor cursor) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            List<T> list = new ArrayList<T>();
            Field[] fields = cls.getDeclaredFields();
            if (cursor != null && cursor.moveToFirst()){
                do{
                    T obj = cls.newInstance();
                /*
                field.getType().getName();
                boolean、byte、double、float、int、long、short
                java.lang.Boolean、java.lang.Byte、java.lang.Double、java.lang.Float
                java.lang.Integer、java.lang.Long、java.lang.Short、java.lang.String
                cursor getXXX double,float,int,long,short,String
                */
                    for (Field field : fields){
                        if (!isColumnExist(columns,field.getName())){           // 没有在列中找到
                            if (!isColumnLikeAS(columns,field.getName())){      // 没有在列中匹配到别名
                                continue;       // 则该列无效
                            }
                        }
                        String type = field.getType().getName();
                        if (type.equals("double")){
                            Object value = cursor.getDouble(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),double.class,value);
                        }else if(type.equals("java.lang.Double")){
                            Object value = cursor.getDouble(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),Double.class,value);
                        }else if(type.equals("float")){
                            Object value = cursor.getFloat(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),float.class,value);
                        }else if(type.equals("java.lang.Float")){
                            Object value = cursor.getFloat(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),Float.class,value);
                        }else if(type.equals("int")){
                            Object value = cursor.getInt(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),int.class,value);
                        }else if(type.equals("java.lang.Integer")){
                            Object value = cursor.getInt(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),Integer.class,value);
                        }else if(type.equals("long")){
                            Object value = cursor.getLong(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),long.class,value);
                        }else if(type.equals("java.lang.Long")){
                            Object value = cursor.getLong(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),Long.class,value);
                        }else if(type.equals("short")){
                            Object value = cursor.getShort(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),short.class,value);
                        }else if(type.equals("java.lang.Short")){
                            Object value = cursor.getShort(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),Short.class,value);
                        }else if(type.equals("java.lang.String")){
                            Object value = cursor.getString(cursor.getColumnIndex(field.getName()));
                            setter(obj,field.getName(),String.class,value);
                        }
                    }
                    list.add(obj);
                }while(cursor.moveToNext());
            }
            return list;
        }

        /**
         * 判断值是否在数组中
         * @param columns       columns为空代表所有列
         * @param column
         * @return
         */
        private boolean isColumnExist(String[] columns,String column){
            if (columns == null){
                return true;
            }
            for (String col : columns){
                if (col.equals(column)){
                    return true;
                }
            }
            return false;
        }

        /**
         * 判断值是否在数组中匹配到别名
         * @param columns
         * @param column
         * @return
         */
        private boolean isColumnLikeAS(String[] columns,String column){
            if (columns == null){
                return true;
            }
            for (String col : columns){
                if (col.trim().endsWith("as " + column) || col.trim().endsWith("AS " + column)){
                    return true;
                }
            }
            return false;
        }
        /**
         * 反射调用set
         * @param obj           设置的对象
         * @param fieldName     属性名
         * @param type          属性类型
         * @param value         属性值
         * @throws NoSuchMethodException
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        private void setter(Object obj,String fieldName,Class<?> type,Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            String methodName = "set" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            Method method = obj.getClass().getMethod(methodName,type);
            method.invoke(obj,value);
        }

        /**
         * 反射调用get
         * @param obj           设置的对象
         * @param fieldName     属性名
         * @return
         * @throws NoSuchMethodException
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         */
        private Object getter(Object obj,String fieldName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            String methodName = "get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1);
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        }

        /**
         * 判断值的类型并put
         * @param values
         * @param key
         * @param value
         * @throws RuntimeException
         */
        private void put(ContentValues values,String key,Object value) throws RuntimeException{
            if (value instanceof Boolean){
                values.put(key,(Boolean)value);
            }else if(value instanceof  Byte){
                values.put(key,(Byte)value);
            }else if(value instanceof Double){
                values.put(key,(Double)value);
            }else if(value instanceof Float){
                values.put(key,(Float)value);
            }else if(value instanceof Integer){
                values.put(key,(Integer)value);
            }else if(value instanceof Long){
                values.put(key,(Long)value);
            }else if(value instanceof Short){
                values.put(key,(Short)value);
            }else if(value instanceof String){
                values.put(key,(String)value);
            }else{
                throw new RuntimeException("类型不支持");
            }
        }
    }
}
