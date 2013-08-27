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
package ro.nextreports.server.domain;

import org.jcrom.annotations.JcrProperty;

import ro.nextreports.server.distribution.DestinationType;


public class SmtpAlertDestination extends SmtpDestination {

	@JcrProperty
	private String operator;

	@JcrProperty
	private String rightOperand;

	@JcrProperty
	private String rightOperand2;

	public SmtpAlertDestination() {
		super();
		setName(DestinationType.ALERT.toString());
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getRightOperand() {
		return rightOperand;
	}

	public void setRightOperand(String rightOperand) {
		this.rightOperand = rightOperand;
	}

	public String getRightOperand2() {
		return rightOperand2;
	}

	public void setRightOperand2(String rightOperand2) {
		this.rightOperand2 = rightOperand2;
	}

	public String getType() {
		return DestinationType.ALERT.toString();
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SmtpAlertDestination[");
		buffer.append("name = ").append(name);
		buffer.append(", path = ").append(path);
		buffer.append(", attachFile = ").append(isAttachFile());
		buffer.append(" groupRecipients = ").append(getGroupRecipients());
		buffer.append(" mailBody = ").append(getMailBody());
		buffer.append(" mailRecipients = ").append(getMailRecipients());
		buffer.append(" mailSubject = ").append(getMailSubject());
		buffer.append(" userRecipients = ").append(getUserRecipients());
		buffer.append(" operator = ").append(operator);
		buffer.append(" rightOperand = ").append(rightOperand);
		buffer.append(" rightOperand2 = ").append(rightOperand2);
		buffer.append("]");

		return buffer.toString();
	}

}
