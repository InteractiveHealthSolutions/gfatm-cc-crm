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
 * Encounter generated by hbm2java
 */
@Entity
@Table(name = "encounter", uniqueConstraints = @UniqueConstraint(columnNames = "uuid"))
public class EncounterClient implements java.io.Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = -4779245169672809816L;
	private Integer encounterId;
	private UsersClient usersByCreatedBy;
	private PersonClient person;
	private UsersClient usersByUserId;
	private EncounterTypeClient encounterType;
	private LocationClient locationByCreatedAt;
	private LocationClient locationByChangedAt;
	private UsersClient usersByChangedBy;
	private Integer durationSeconds;
	private Date dateEntered;
	private Date dateCreated;
	private Date dateChanged;
	private String uuid;

	public EncounterClient() {
	}

	public EncounterClient(EncounterTypeClient encounterType,
			PersonClient person, UsersClient user) {
		this.encounterType = encounterType;
		this.person = person;
		this.usersByUserId = user;
	}

	public EncounterClient(EncounterTypeClient encounterType,
			PersonClient person, UsersClient user, Date dateEntered,
			UsersClient createdBy, LocationClient createdAt, Date dateCreated,
			String uuid) {
		this.encounterType = encounterType;
		this.person = person;
		this.usersByUserId = user;
		this.dateCreated = dateCreated;
		this.uuid = uuid;
	}

	public EncounterClient(UsersClient usersByCreatedBy, PersonClient person,
			UsersClient usersByUserId, EncounterTypeClient encounterType,
			LocationClient locationByCreatedAt,
			LocationClient locationByChangedAt, UsersClient usersByChangedBy,
			Integer durationSeconds, Date dateEntered, Date dateCreated,
			Date dateChanged, String uuid) {
		this.usersByCreatedBy = usersByCreatedBy;
		this.person = person;
		this.usersByUserId = usersByUserId;
		this.encounterType = encounterType;
		this.locationByCreatedAt = locationByCreatedAt;
		this.locationByChangedAt = locationByChangedAt;
		this.usersByChangedBy = usersByChangedBy;
		this.durationSeconds = durationSeconds;
		this.dateEntered = dateEntered;
		this.dateCreated = dateCreated;
		this.dateChanged = dateChanged;
		this.uuid = uuid;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "encounter_id", unique = true, nullable = false)
	public Integer getEncounterId() {
		return this.encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	public UsersClient getUsersByCreatedBy() {
		return this.usersByCreatedBy;
	}

	public void setUsersByCreatedBy(UsersClient usersByCreatedBy) {
		this.usersByCreatedBy = usersByCreatedBy;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "person_id")
	public PersonClient getPerson() {
		return this.person;
	}

	public void setPerson(PersonClient person) {
		this.person = person;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id")
	public UsersClient getUsersByUserId() {
		return this.usersByUserId;
	}

	public void setUsersByUserId(UsersClient usersByUserId) {
		this.usersByUserId = usersByUserId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "encounter_type_id")
	public EncounterTypeClient getEncounterType() {
		return this.encounterType;
	}

	public void setEncounterType(EncounterTypeClient encounterType) {
		this.encounterType = encounterType;
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

	@Column(name = "duration_seconds")
	public Integer getDurationSeconds() {
		return this.durationSeconds;
	}

	public void setDurationSeconds(Integer durationSeconds) {
		this.durationSeconds = durationSeconds;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_entered", length = 19)
	public Date getDateEntered() {
		return this.dateEntered;
	}

	public void setDateEntered(Date dateEntered) {
		this.dateEntered = dateEntered;
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
		return encounterId + ", " + person.getPersonId() + ", "
				+ durationSeconds + ", " + dateEntered + ", " + dateCreated
				+ ", " + dateChanged + ", " + uuid;
	}
}
