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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * DefinitionType generated by hbm2java
 */
@Entity
@Table(name = "definition_type", uniqueConstraints = @UniqueConstraint(columnNames = "definition_type"))
public class DefinitionTypeClient implements java.io.Serializable {

	/**
     * 
     */
	private static final long serialVersionUID = 388567626321223817L;
	private Integer definitionTypeId;
	private String definitionType;
	private Set<DefinitionClient> definitions = new HashSet<DefinitionClient>(0);

	public DefinitionTypeClient() {
	}

	public DefinitionTypeClient(String definitionType) {
		this.definitionType = definitionType;
	}

	public DefinitionTypeClient(String definitionType,
			Set<DefinitionClient> definitions) {
		this.definitionType = definitionType;
		this.definitions = definitions;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "definition_type_id", unique = true, nullable = false)
	public Integer getDefinitionTypeId() {
		return this.definitionTypeId;
	}

	public void setDefinitionTypeId(Integer definitionTypeId) {
		this.definitionTypeId = definitionTypeId;
	}

	@Column(name = "definition_type", unique = true, nullable = false, length = 45)
	public String getDefinitionType() {
		return this.definitionType;
	}

	public void setDefinitionType(String definitionType) {
		this.definitionType = definitionType;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "definitionType")
	public Set<DefinitionClient> getDefinitions() {
		return this.definitions;
	}

	public void setDefinitions(Set<DefinitionClient> definitions) {
		this.definitions = definitions;
	}

	@Override
	public String toString() {
		return definitionTypeId + ", " + definitionType;
	}
}
