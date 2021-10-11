/*
Copyright(C) 2016 Interactive Health Solutions, Pvt. Ltd.
This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 3 of the License (GPLv3), or any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with this program; if not, write to the Interactive Health Solutions, info@ihsinformatics.com
You can also access the license on the internet at the address: http://www.gnu.org/licenses/gpl-3.0.html

Interactive Health Solutions, hereby disclaims all copyright interest in this program written by the contributors.
 */

/**
 * @author owais.hussain@ihsinformatics.com
 */

package com.ihsinformatics.gfatmweb.shared.model;

// Generated Dec 9, 2015 9:08:22 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/**
 * Element generated by hbm2java
 */
@Entity
@Table(name = "element", uniqueConstraints = @UniqueConstraint(columnNames = "uuid"))
public class ElementClient implements java.io.Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 7509221548506154943L;
	private Integer elementId;
	private UsersClient usersByCreatedBy;
	private LocationClient locationByCreatedAt;
	private LocationClient locationByChangedAt;
	private UsersClient usersByChangedBy;
	private String elementName;
	private String validationRegex;
	private String dataType;
	private String description;
	private Date dateCreated;
	private Date dateChanged;
	private String uuid;

	public ElementClient() {
	}

	public ElementClient(String elementName) {
		this.elementName = elementName;
	}

	public ElementClient(String elementName, Date dateCreated, String uuid) {
		this.elementName = elementName;
		this.dateCreated = dateCreated;
		this.uuid = uuid;
	}

	public ElementClient(String elementName, String validationRegex,
			String dataType, String description, UsersClient createdBy,
			LocationClient createdAt, Date dateCreated, String uuid) {
		this.usersByCreatedBy = createdBy;
		this.locationByCreatedAt = createdAt;
		this.elementName = elementName;
		this.validationRegex = validationRegex;
		this.dataType = dataType;
		this.description = description;
		this.dateCreated = dateCreated;
		this.uuid = uuid;
	}

	public ElementClient(UsersClient usersByCreatedBy,
			LocationClient locationByCreatedAt,
			LocationClient locationByChangedAt, UsersClient usersByChangedBy,
			String elementName, String validationRegex, String dataType,
			String description, Date dateCreated, Date dateChanged, String uuid) {
		this.usersByCreatedBy = usersByCreatedBy;
		this.locationByCreatedAt = locationByCreatedAt;
		this.locationByChangedAt = locationByChangedAt;
		this.usersByChangedBy = usersByChangedBy;
		this.elementName = elementName;
		this.validationRegex = validationRegex;
		this.dataType = dataType;
		this.description = description;
		this.dateCreated = dateCreated;
		this.dateChanged = dateChanged;
		this.uuid = uuid;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "element_id", unique = true, nullable = false)
	public Integer getElementId() {
		return this.elementId;
	}

	public void setElementId(Integer elementId) {
		this.elementId = elementId;
	}

	@Column(name = "element_name", nullable = false, unique = true, length = 45)
	public String getElementName() {
		return this.elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	public UsersClient getUsersByCreatedBy() {
		return this.usersByCreatedBy;
	}

	public void setUsersByCreatedBy(UsersClient usersByCreatedBy) {
		this.usersByCreatedBy = usersByCreatedBy;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_at")
	public LocationClient getLocationByCreatedAt() {
		return this.locationByCreatedAt;
	}

	public void setLocationByCreatedAt(LocationClient locationByCreatedAt) {
		this.locationByCreatedAt = locationByCreatedAt;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_at")
	public LocationClient getLocationByChangedAt() {
		return this.locationByChangedAt;
	}

	public void setLocationByChangedAt(LocationClient locationByChangedAt) {
		this.locationByChangedAt = locationByChangedAt;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "changed_by")
	public UsersClient getUsersByChangedBy() {
		return this.usersByChangedBy;
	}

	public void setUsersByChangedBy(UsersClient usersByChangedBy) {
		this.usersByChangedBy = usersByChangedBy;
	}

	@Column(name = "validation_regex")
	public String getValidationRegex() {
		return this.validationRegex;
	}

	public void setValidationRegex(String validationRegex) {
		this.validationRegex = validationRegex;
	}

	@Column(name = "data_type", length = 10)
	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_created", nullable = false, length = 19)
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_changed", length = 19)
	public Date getDateChanged() {
		return this.dateChanged;
	}

	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	@Column(name = "uuid", unique = true, nullable = false, length = 38)
	public String getUuid() {
		return this.uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		return elementId + ", " + elementName + ", " + validationRegex + ", "
				+ dataType + ", " + description + ", " + dateCreated + ", "
				+ dateChanged + ", " + uuid;
	}
}
