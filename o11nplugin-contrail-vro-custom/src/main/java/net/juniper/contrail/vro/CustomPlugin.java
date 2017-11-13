/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType;
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin;

import java.util.List;
import java.util.ListIterator;

public class CustomPlugin extends Plugin {

    @Override
    public List<ManagedType> getTypes() {
        // Generator adds default managed types, which we want wrapped in our extended class.
        // Method is used to add to and view the list of ManagedTypes
        // It gives us more info while generating the wrappers.
        List<ManagedType> list = super.getTypes();
        maybeWrap(list);
        return list;
    }

    private void maybeWrap(List<ManagedType> types) {
        for (ListIterator<ManagedType> iterator = types.listIterator(); iterator.hasNext(); ) {
            ManagedType type = iterator.next();
            if( !(type instanceof CustomManagedType) ) {
                iterator.set(CustomManagedType.wrap(type));
            }
        }
    }
}
