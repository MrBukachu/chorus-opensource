/*
 * C O P Y R I G H T   N O T I C E
 * -----------------------------------------------------------------------
 * Copyright (c) 2011-2012 InfoClinika, Inc. 5901 152nd Ave SE, Bellevue, WA 98006,
 * United States of America.  (425) 442-8058.  http://www.infoclinika.com.
 * All Rights Reserved.  Reproduction, adaptation, or translation without prior written permission of InfoClinika, Inc. is prohibited.
 * Unpublished--rights reserved under the copyright laws of the United States.  RESTRICTED RIGHTS LEGEND Use, duplication or disclosure by the
 */
package com.infoclinika.mssharing.platform.entity;


import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * Instrument vendor (Thermo, Waters, Bruker, ABI, Agilent, Shimadzu, etc.)
 *
 * @author Stanislav Kurilin
 */
@Entity
@AttributeOverride(name = "name", column = @Column(unique = true))
public class Vendor extends Dictionary {

    public Vendor() {
    }

    public Vendor(String name) {
        super(name);
    }

}
