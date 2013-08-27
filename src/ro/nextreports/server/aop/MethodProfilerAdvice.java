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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Decebal Suiu
 */
@Aspect
public class MethodProfilerAdvice {

	private static final Logger LOG = LoggerFactory.getLogger(MethodProfilerAdvice.class);
	
    @Pointcut("@annotation(profile)")
    public void isProfileAnnotation(Profile profile) {
    }

	/**
	 * Intercepts methods that declare Profile annotation and prints out the time it takes to complete/
	 * 
	 * @param joinPoint proceeding join point
	 * @return the intercepted method returned object
	 * @throws Throwable in case something goes wrong in the actual method call
	 */
	@Around("isProfileAnnotation(profile)")
	public Object profileMethod(ProceedingJoinPoint joinPoint, Profile profile)	throws Throwable {
		String logPrefix = null;

		boolean debug = LOG.isDebugEnabled();
		long time = System.currentTimeMillis();

		// parse out the first arg
		String arg1 = "";
		Object[] args = joinPoint.getArgs();
		if ((args != null) && (args.length > 0) && (args[0] != null)) {
			arg1 = args[0].toString();
		}
		if (debug) {
			logPrefix = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + " " + arg1;
			LOG.debug(logPrefix + " START");
		}
		Object returnValue = joinPoint.proceed();
		time = System.currentTimeMillis() - time;
		if (debug) {
			LOG.debug(logPrefix + " EXECUTED in " + time + " ms");
		}
		return returnValue;

	}

}
