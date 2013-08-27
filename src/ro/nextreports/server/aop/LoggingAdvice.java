/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.nextreports.server.aop;

import java.lang.reflect.Method;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

/**
 * @author Decebal Suiu
 */
public class LoggingAdvice implements MethodBeforeAdvice,
		AfterReturningAdvice, ThrowsAdvice {

	public void before(Method method, Object[] arguments, Object target)
			throws Throwable {
		/*
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("LoggingAdvice.before()");
		System.out.println("method = " + getMethodSignature(method));
		for (Object argument : arguments) {
			System.out.println("argument = " + argument);
		}
		System.out.println("target = " + target);
		System.out.println("username = " + ServerUtil.getUsername());
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		*/
	}

	public void afterReturning(Object returnValue, Method method, Object[] arguments,
			Object target) throws Throwable {
		/*
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println("LoggingAdvice.afterReturning()");
		System.out.println("method = " + getMethodSignature(method));
		for (Object argument : arguments) {
			System.out.println("argument = " + argument);
		}
		System.out.println("returnValue = " + returnValue);
		System.out.println("target = " + target);
		System.out.println("username = " + ServerUtil.getUsername());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		*/
	}

	public void afterThrowing(Method method, Object[] arguments, Object target,
			Throwable exception) {
		/*
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("LoggingAdvice.afterThrowing()");
		System.out.println("method = " + getMethodSignature(method));
		for (Object argument : arguments) {
			System.out.println("argument = " + argument);
		}
		System.out.println("exception = " + exception.getMessage());
		System.out.println("target = " + target);
		System.out.println("username = " + ServerUtil.getUsername());
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		*/
	}

	/*
	private String getMethodSignature(Method method) {
		StringBuilder sb = new StringBuilder(150);

		sb.append(method.getDeclaringClass().getCanonicalName());
		sb.append('.');
		sb.append(method.getName());
		sb.append("()");

		return sb.toString();
	}
	*/

}
