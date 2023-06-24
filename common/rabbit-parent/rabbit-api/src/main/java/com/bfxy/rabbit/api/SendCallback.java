package com.bfxy.rabbit.api;

/**
 * 	$SendCallback 回调函数处理
 *  接口由来：在编写MessageProducer的方法时，除了send和批量send，还可以添加回调方法，用于接受回调的信息，顾产生了此接口
 * @author Alienware
 *
 */
public interface SendCallback {

	void onSuccess();
	
	void onFailure();
	
}
