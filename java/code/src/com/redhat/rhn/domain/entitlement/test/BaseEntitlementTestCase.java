/**
 * Copyright (c) 2009 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.entitlement.test;

import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.testing.RhnBaseTestCase;

/**
 * BaseEntitlementTestCase
 * @version $Rev$
 */
public abstract class BaseEntitlementTestCase extends RhnBaseTestCase {

    protected Entitlement ent;

    public void setUp() {
        createEntitlement();
    }

    public void testLabel() {
        assertEquals(getLabel(), ent.getLabel());
    }
    
    protected abstract void createEntitlement();
    protected abstract String getLabel();
}
