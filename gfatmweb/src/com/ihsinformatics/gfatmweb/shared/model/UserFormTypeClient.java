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
 * UserFormType generated by hbm2java
 */
@Entity
@Table(name = "user_form_type", uniqueConstraints = @UniqueConstraint(columnNames = "uuid"))
public class UserFormTypeClient implements java.io.Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = -5408814658346814627L;
	private Integer userFormTypeId;
	private UsersClient usersByCreatedBy;
	private LocationClient locationByCreatedAt;
	private LocationClient locationByChangedAt;
	private UsersClient usersByChangedBy;
	private String userFormType;
	private String description;
	private Date dateCreated;
	private Date dateChanged;
	private String uuid;

	public UserFormTypeClient() {
	}

	public UserFormTypeClient(String encounterType) {
		this.userFormType = encounterType;
	}

	public UserFormTypeClient(String userFormType, String description,
			UsersClient createdBy, LocationClient createdAt, Date dateCreated,
			String uuid) {
		this.usersByCreatedBy = createdBy;
		this.locationByCreatedAt = createdAt;
		this.userFormType = userFormType;
		this.description = description;
		this.dateCreated = dateCreated;
		this.uuid = uuid;
	}

	public UserFormTypeClient(UsersClient usersByCreatedBy,
			LocationClient locationByCreatedAt,
			LocationClient locationByChangedAt, UsersClient usersByChangedBy,
			String userFormType, String description, Date dateCreated,
			Date dateChanged, String uuid) {
		this.usersByCreatedBy = usersByCreatedBy;
		this.locationByCreatedAt = locationByCreatedAt;
		this.locationByChangedAt = locationByChangedAt;
		this.usersByChangedBy = usersByChangedBy;
		this.userFormType = userFormType;
		this.description = description;
		this.dateCreated = dateCreated;
		this.dateChanged = dateChanged;
		this.uuid = uuid;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_form_type_id", unique = true, nullable = false)
	public Integer getUserFormTypeId() {
		return this.userFormTypeId;
	}

	public void setUserFormTypeId(Integer userFormTypeId) {
		this.userFormTypeId = userFormTypeId;
	}

	@Column(name = "user_form_type", unique = true, nullable = false, length = 45)
	public String getUserFormType() {
		return this.userFormType;
	}

	public void setUserFormType(String userFormType) {
		this.userFormType = userFormType;
	}

	@Column(name = "description", length = 255)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
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
		return userFormTypeId + ", " + userFormType + ", " + description + ", "
				+ dateCreated + ", " + dateChanged + ", " + uuid;
	}
}
