/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.dto.CustomDataKeyOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * CustomDataKeySerializer: Converts a CustomDataKeyOverview object for
 * representation as an XMLRPC struct.
 *
 *
 * @apidoc.doc
 *      #struct_begin("custom info")
 *          #prop("int", "id")
 *          #prop("string", "label")
 *          #prop("string", "description")
 *          #prop("int", "system_count")
 *          #prop("$date", "last_modified")
 *      #struct_end()
 */
public class CustomDataKeySerializer extends ApiResponseSerializer<CustomDataKeyOverview> {

    @Override
    public Class<CustomDataKeyOverview> getSupportedClass() {
        return CustomDataKeyOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(CustomDataKeyOverview src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("description", src.getDescription())
                .add("system_count", src.getServerCount().intValue())
                .add("last_modified", src.getLastModified())
                .build();
    }
}
