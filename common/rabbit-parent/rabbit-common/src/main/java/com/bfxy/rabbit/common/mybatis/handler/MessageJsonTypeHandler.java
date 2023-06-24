package com.bfxy.rabbit.common.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.bfxy.rabbit.api.Message;
import com.bfxy.rabbit.common.util.FastJsonConvertUtil;



/**
 * 必须继承BaseTypeHandler<Message>
 * 这是mybatis规定的，传过来什么对象，才可以在落库的时候自动将对象序列化后再存储
 *
 * @author BuFanxueyuan
 * @since 2018年5月15日 下午2:35:54
 */
public class MessageJsonTypeHandler extends BaseTypeHandler<Message> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Message parameter,
            JdbcType jdbcType) throws SQLException {
        
        ps.setString(i, FastJsonConvertUtil.convertObjectToJSON(parameter));
    }

    @Override
    public Message getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
    	String value = rs.getString(columnName);
    	if(null != value && !StringUtils.isBlank(value)) {
    		return FastJsonConvertUtil.convertJSONToObject(rs.getString(columnName), Message.class);
    	}
    	return null;  
    }

    @Override
    public Message getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    	String value = rs.getString(columnIndex);
    	if(null != value && !StringUtils.isBlank(value)) {
    		return FastJsonConvertUtil.convertJSONToObject(rs.getString(columnIndex), Message.class);
    	}
    	return null;         
    }

    @Override
    public Message getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    	String value = cs.getString(columnIndex);
    	if(null != value && !StringUtils.isBlank(value)) {
    		return FastJsonConvertUtil.convertJSONToObject(cs.getString(columnIndex), Message.class);
    	}
    	return null;   
    }

}